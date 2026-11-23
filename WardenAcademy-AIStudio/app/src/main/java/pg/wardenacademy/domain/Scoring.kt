package pg.wardenacademy.domain

/**
 * Points rules (shown to trainees on the About screen):
 *  - 10 points per correct answer
 *  - Pass mark: 70%
 *  - +20 for passing on the FIRST attempt
 *  - +25 for a perfect score
 *  - Up to +15 speed bonus on a pass, in proportion to time left
 * A module counts toward the leaderboard by its BEST attempt only, so retakes
 * can improve a score but cannot be farmed for extra points.
 */
object Scoring {
    const val POINTS_PER_CORRECT = 10
    const val PASS_PERCENT = 70
    const val FIRST_ATTEMPT_PASS_BONUS = 20
    const val PERFECT_BONUS = 25
    const val MAX_SPEED_BONUS = 15
    const val SECONDS_PER_QUESTION = 45

    data class Result(
        val basePoints: Int,
        val bonusPoints: Int,
        val totalPoints: Int,
        val percent: Int,
        val passed: Boolean,
        val perfect: Boolean,
        val breakdown: List<Pair<String, Int>>
    )

    fun timeLimitFor(questionCount: Int): Int = questionCount * SECONDS_PER_QUESTION

    fun percent(correct: Int, total: Int): Int = if (total <= 0) 0 else correct * 100 / total

    fun maxPointsFor(questionCount: Int): Int =
        questionCount * POINTS_PER_CORRECT + FIRST_ATTEMPT_PASS_BONUS + PERFECT_BONUS + MAX_SPEED_BONUS

    fun score(
        correct: Int,
        total: Int,
        attemptNumber: Int,
        secondsUsed: Int,
        timeLimitSeconds: Int
    ): Result {
        val base = correct * POINTS_PER_CORRECT
        val pct = percent(correct, total)
        val passed = pct >= PASS_PERCENT
        val perfect = total > 0 && correct == total
        val breakdown = mutableListOf("Correct answers ($correct × $POINTS_PER_CORRECT)" to base)
        var bonus = 0

        if (passed && attemptNumber == 1) {
            bonus += FIRST_ATTEMPT_PASS_BONUS
            breakdown += "First-attempt pass bonus" to FIRST_ATTEMPT_PASS_BONUS
        }
        if (perfect) {
            bonus += PERFECT_BONUS
            breakdown += "Perfect score bonus" to PERFECT_BONUS
        }
        if (passed && timeLimitSeconds > 0) {
            val remaining = (timeLimitSeconds - secondsUsed).coerceIn(0, timeLimitSeconds)
            val speed = MAX_SPEED_BONUS * remaining / timeLimitSeconds
            if (speed > 0) {
                bonus += speed
                breakdown += "Speed bonus" to speed
            }
        }
        return Result(base, bonus, base + bonus, pct, passed, perfect, breakdown)
    }
}
