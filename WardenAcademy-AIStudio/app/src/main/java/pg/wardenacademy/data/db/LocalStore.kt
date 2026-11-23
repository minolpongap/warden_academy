package pg.wardenacademy.data.db

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import pg.wardenacademy.data.content.objects
import java.io.File

/**
 * Offline storage for trainees and quiz attempts in a single JSON file in the app's
 * private storage. Changes update the in-memory StateFlows straight away, then the file
 * is written in the background. Each write goes to a temp file first, then is renamed,
 * so the data file is never left half-written.
 */
class LocalStore(context: Context) {

    private val file = File(context.filesDir, "warden_store.json")
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeLock = Mutex()
    private val lock = Any()

    private val _trainees = MutableStateFlow<List<Trainee>>(emptyList())
    private val _attempts = MutableStateFlow<List<QuizAttempt>>(emptyList())
    val trainees: StateFlow<List<Trainee>> = _trainees
    val attempts: StateFlow<List<QuizAttempt>> = _attempts

    private var nextId = 1L

    init { load() }

    // ---- Trainees ----
    fun findByStaffId(staffId: String): Trainee? =
        _trainees.value.firstOrNull { it.staffId.equals(staffId, ignoreCase = true) }

    /** Returns the new id, or null if the staff ID already exists. */
    fun insertTrainee(t: Trainee): Long? = synchronized(lock) {
        if (findByStaffId(t.staffId) != null) return null
        val saved = t.copy(id = nextId++)
        _trainees.update { (it + saved).sortedBy { tr -> tr.name.lowercase() } }
        persist()
        saved.id
    }

    fun deleteTrainee(t: Trainee) = synchronized(lock) {
        _trainees.update { list -> list.filterNot { it.id == t.id } }
        _attempts.update { list -> list.filterNot { it.traineeId == t.id } }
        persist()
    }

    // ---- Attempts ----
    fun countAttempts(traineeId: Long, moduleId: String): Int =
        _attempts.value.count { it.traineeId == traineeId && it.moduleId == moduleId }

    fun insertAttempt(a: QuizAttempt): Long = synchronized(lock) {
        val saved = a.copy(id = nextId++)
        _attempts.update { listOf(saved) + it } // newest first
        persist()
        saved.id
    }

    // ---- File I/O ----
    private fun load() {
        if (!file.exists()) return
        try {
            val root = JSONObject(file.readText())
            _trainees.value = root.optJSONArray("trainees").objects().map {
                Trainee(
                    id = it.getLong("id"), name = it.getString("name"),
                    staffId = it.getString("staffId"), province = it.getString("province"),
                    createdAt = it.getLong("createdAt")
                )
            }.sortedBy { it.name.lowercase() }
            _attempts.value = root.optJSONArray("attempts").objects().map {
                QuizAttempt(
                    id = it.getLong("id"), traineeId = it.getLong("traineeId"),
                    moduleId = it.getString("moduleId"), attemptNumber = it.getInt("attemptNumber"),
                    correct = it.getInt("correct"), total = it.getInt("total"),
                    secondsUsed = it.getInt("secondsUsed"), timeLimitSeconds = it.getInt("timeLimitSeconds"),
                    basePoints = it.getInt("basePoints"), bonusPoints = it.getInt("bonusPoints"),
                    totalPoints = it.getInt("totalPoints"), passed = it.getBoolean("passed"),
                    perfect = it.getBoolean("perfect"), completedAt = it.getLong("completedAt")
                )
            }.sortedByDescending { it.completedAt }
            nextId = root.optLong("nextId", 1L)
                .coerceAtLeast((_trainees.value.maxOfOrNull { it.id } ?: 0L) + 1)
                .coerceAtLeast((_attempts.value.maxOfOrNull { it.id } ?: 0L) + 1)
        } catch (e: Exception) {
            // Corrupt file: keep a copy for recovery and start fresh
            file.copyTo(File(file.parentFile, "warden_store.corrupt.json"), overwrite = true)
        }
    }

    private fun persist() {
        val snapshot = toJson()
        io.launch {
            writeLock.withLock {
                val tmp = File(file.parentFile, "warden_store.tmp")
                tmp.writeText(snapshot)
                if (!tmp.renameTo(file)) {
                    file.writeText(snapshot)
                    tmp.delete()
                }
            }
        }
    }

    private fun toJson(): String {
        val root = JSONObject()
        root.put("nextId", nextId)
        root.put("trainees", JSONArray().apply {
            _trainees.value.forEach {
                put(JSONObject().apply {
                    put("id", it.id); put("name", it.name); put("staffId", it.staffId)
                    put("province", it.province); put("createdAt", it.createdAt)
                })
            }
        })
        root.put("attempts", JSONArray().apply {
            _attempts.value.forEach {
                put(JSONObject().apply {
                    put("id", it.id); put("traineeId", it.traineeId); put("moduleId", it.moduleId)
                    put("attemptNumber", it.attemptNumber); put("correct", it.correct); put("total", it.total)
                    put("secondsUsed", it.secondsUsed); put("timeLimitSeconds", it.timeLimitSeconds)
                    put("basePoints", it.basePoints); put("bonusPoints", it.bonusPoints)
                    put("totalPoints", it.totalPoints); put("passed", it.passed)
                    put("perfect", it.perfect); put("completedAt", it.completedAt)
                })
            }
        })
        return root.toString()
    }
}
