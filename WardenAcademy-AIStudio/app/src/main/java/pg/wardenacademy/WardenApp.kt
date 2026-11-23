package pg.wardenacademy

import android.app.Application
import android.content.Context
import pg.wardenacademy.data.content.ContentRepository
import pg.wardenacademy.data.db.LocalStore

/** Simple manual dependency container. */
class WardenApp : Application() {
    val store: LocalStore by lazy { LocalStore(this) }
    val content: ContentRepository by lazy { ContentRepository(this) }
    val session: Session by lazy { Session(this) }
}

/** Remembers which trainee is signed in on this device. */
class Session(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    var currentTraineeId: Long?
        get() = prefs.getLong(KEY, -1L).takeIf { it > 0 }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY) else putLong(KEY, value)
            }.apply()
        }

    private companion object { const val KEY = "current_trainee_id" }
}

val PNG_PROVINCES = listOf(
    "Autonomous Region of Bougainville", "Central", "Chimbu (Simbu)", "East New Britain",
    "East Sepik", "Eastern Highlands", "Enga", "Gulf", "Hela", "Jiwaka", "Madang", "Manus",
    "Milne Bay", "Morobe", "National Capital District", "New Ireland", "Northern (Oro)",
    "Southern Highlands", "Western (Fly)", "Western Highlands", "West New Britain",
    "West Sepik (Sandaun)"
)
