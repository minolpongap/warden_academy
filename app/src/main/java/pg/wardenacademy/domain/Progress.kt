package pg.wardenacademy.domain

import pg.wardenacademy.data.content.TrainingModule
import pg.wardenacademy.data.db.QuizAttempt
import pg.wardenacademy.data.db.Trainee

enum class Rank(val title: String, val minPoints: Int) {
    TRAINEE("Trainee", 0),
    FIELD_OFFICER("Field Officer", 100),
    ASSISTANT_WARDEN("Assistant Warden", 250),
    WARDEN("Warden", 450),
    SENIOR_WARDEN("Senior Warden", 650),
    CHIEF_WARDEN("Chief Warden", 850);

    val next: Rank? get() = Rank.entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): Rank = Rank.entries.last { points >= it.minPoints }
    }
}

enum class Badge(val title: String, val description: String, val emoji: String) {
    FIRST_STEPS("First Steps", "Pass your first module", "🥾"),
    HEARING_READY("Hearing Ready", "Pass Modules 1–4 (hearings and reports)", "🗣️"),
    COMPENSATION_EXPERT("Compensation Expert", "Pass Modules 5 and 6", "⚖️"),
    PERFECTIONIST("Perfectionist", "Score 100% on any quiz", "💯"),
    FLAWLESS_FIRST("Flawless First", "Score 100% on a first attempt", "🌟"),
    TRIPLE_PERFECT("Triple Perfect", "Score 100% on three different modules", "🏅"),
    FULLY_CERTIFIED("Fully Certified", "Pass every module", "🎓")
}

data class ModuleProgress(
    val module: TrainingModule,
    val attempts: Int,
    val bestPoints: Int,
    val bestPercent: Int,
    val passed: Boolean,
    val perfect: Boolean,
    val unlocked: Boolean
)

data class TraineeStats(
    val trainee: Trainee,
    val totalPoints: Int,
    val modulesPassed: Int,
    val totalModules: Int,
    val perfectModules: Int,
    val totalAttempts: Int,
    val averageBestPercent: Int,
    val rank: Rank,
    val badges: List<Badge>,
    val lastActivity: Long,
    val position: Int = 0
) {
    val allPassed: Boolean get() = totalModules > 0 && modulesPassed == totalModules
}

object Progress {

    /** Each module is unlocked once the module before it has been passed. */
    fun moduleProgress(modules: List<TrainingModule>, attempts: List<QuizAttempt>): List<ModuleProgress> {
        val byModule = attempts.groupBy { it.moduleId }
        var previousPassed = true
        return modules.sortedBy { it.number }.map { m ->
            val list = byModule[m.id].orEmpty()
            val best = list.maxWithOrNull(compareBy<QuizAttempt> { it.totalPoints }.thenBy { it.correct })
            val passed = list.any { it.passed }
            val mp = ModuleProgress(
                module = m,
                attempts = list.size,
                bestPoints = best?.totalPoints ?: 0,
                bestPercent = list.maxOfOrNull { Scoring.percent(it.correct, it.total) } ?: 0,
                passed = passed,
                perfect = list.any { it.perfect },
                unlocked = previousPassed
            )
            previousPassed = passed
            mp
        }
    }

    fun stats(trainee: Trainee, modules: List<TrainingModule>, attempts: List<QuizAttempt>): TraineeStats {
        val mine = attempts.filter { it.traineeId == trainee.id }
        val progress = moduleProgress(modules, mine)
        val total = progress.sumOf { it.bestPoints }
        val passedIds = progress.filter { it.passed }.map { it.module.number }.toSet()
        val perfectCount = progress.count { it.perfect }
        val attempted = progress.filter { it.attempts > 0 }

        val badges = buildList {
            if (passedIds.isNotEmpty()) add(Badge.FIRST_STEPS)
            if (passedIds.containsAll(listOf(1, 2, 3, 4))) add(Badge.HEARING_READY)
            if (passedIds.containsAll(listOf(5, 6))) add(Badge.COMPENSATION_EXPERT)
            if (perfectCount >= 1) add(Badge.PERFECTIONIST)
            if (mine.any { it.perfect && it.attemptNumber == 1 }) add(Badge.FLAWLESS_FIRST)
            if (perfectCount >= 3) add(Badge.TRIPLE_PERFECT)
            if (modules.isNotEmpty() && passedIds.size == modules.size) add(Badge.FULLY_CERTIFIED)
        }

        return TraineeStats(
            trainee = trainee,
            totalPoints = total,
            modulesPassed = passedIds.size,
            totalModules = modules.size,
            perfectModules = perfectCount,
            totalAttempts = mine.size,
            averageBestPercent = if (attempted.isEmpty()) 0 else attempted.sumOf { it.bestPercent } / attempted.size,
            rank = Rank.forPoints(total),
            badges = badges,
            lastActivity = mine.maxOfOrNull { it.completedAt } ?: trainee.createdAt
        )
    }

    /** Sort: points ↓, modules passed ↓, then whoever got there first. Ties share a position. */
    fun leaderboard(
        trainees: List<Trainee>,
        modules: List<TrainingModule>,
        attempts: List<QuizAttempt>
    ): List<TraineeStats> {
        val sorted = trainees.map { stats(it, modules, attempts) }
            .sortedWith(
                compareByDescending<TraineeStats> { it.totalPoints }
                    .thenByDescending { it.modulesPassed }
                    .thenBy { it.lastActivity }
            )
        var lastKey: Pair<Int, Int>? = null
        var lastPos = 0
        return sorted.mapIndexed { i, s ->
            val key = s.totalPoints to s.modulesPassed
            if (key != lastKey) { lastPos = i + 1; lastKey = key }
            s.copy(position = lastPos)
        }
    }
}
