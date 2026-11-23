package pg.wardenacademy.data.db

data class Trainee(
    val id: Long = 0,
    val name: String,
    val staffId: String,
    val province: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class QuizAttempt(
    val id: Long = 0,
    val traineeId: Long,
    val moduleId: String,
    val attemptNumber: Int,
    val correct: Int,
    val total: Int,
    val secondsUsed: Int,
    val timeLimitSeconds: Int,
    val basePoints: Int,
    val bonusPoints: Int,
    val totalPoints: Int,
    val passed: Boolean,
    val perfect: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
