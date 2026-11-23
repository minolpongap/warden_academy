package pg.wardenacademy.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pg.wardenacademy.data.content.TrainingModule
import pg.wardenacademy.data.db.QuizAttempt
import pg.wardenacademy.data.db.Trainee

class ScoringTest {

    @Test fun perfectFirstAttemptGetsAllBonuses() {
        val r = Scoring.score(correct = 6, total = 6, attemptNumber = 1, secondsUsed = 0, timeLimitSeconds = 270)
        assertTrue(r.passed); assertTrue(r.perfect)
        assertEquals(60 + 20 + 25 + 15, r.totalPoints)
        assertEquals(Scoring.maxPointsFor(6), r.totalPoints)
    }

    @Test fun failGetsNoBonus() {
        val r = Scoring.score(correct = 3, total = 6, attemptNumber = 1, secondsUsed = 10, timeLimitSeconds = 270)
        assertFalse(r.passed)
        assertEquals(30, r.totalPoints)
    }

    @Test fun retakePassHasNoFirstAttemptBonus() {
        val r = Scoring.score(correct = 5, total = 6, attemptNumber = 2, secondsUsed = 270, timeLimitSeconds = 270)
        assertTrue(r.passed) // 83%
        assertEquals(50, r.totalPoints)
    }

    @Test fun ranksByThreshold() {
        assertEquals(Rank.TRAINEE, Rank.forPoints(0))
        assertEquals(Rank.FIELD_OFFICER, Rank.forPoints(100))
        assertEquals(Rank.CHIEF_WARDEN, Rank.forPoints(5000))
    }

    @Test fun nextModuleUnlocksOnlyAfterPass() {
        val modules = listOf(
            TrainingModule(id = "m1", number = 1, title = "A", sections = "", summary = ""),
            TrainingModule(id = "m2", number = 2, title = "B", sections = "", summary = "")
        )
        val fail = attempt("m1", passed = false, points = 30)
        assertFalse(Progress.moduleProgress(modules, listOf(fail))[1].unlocked)
        val pass = attempt("m1", passed = true, points = 70)
        assertTrue(Progress.moduleProgress(modules, listOf(fail, pass))[1].unlocked)
    }

    @Test fun leaderboardUsesBestAttemptAndSharesTies() {
        val modules = listOf(TrainingModule(id = "m1", number = 1, title = "A", sections = "", summary = ""))
        val a = Trainee(id = 1, name = "Ana", staffId = "A1", province = "Morobe")
        val b = Trainee(id = 2, name = "Ben", staffId = "B1", province = "Enga")
        val attempts = listOf(
            attempt("m1", true, 70, traineeId = 1),
            attempt("m1", true, 100, traineeId = 1),
            attempt("m1", true, 100, traineeId = 2)
        )
        val board = Progress.leaderboard(listOf(a, b), modules, attempts)
        assertEquals(100, board[0].totalPoints)
        assertEquals(1, board[0].position)
        assertEquals(1, board[1].position)
    }

    private fun attempt(moduleId: String, passed: Boolean, points: Int, traineeId: Long = 1) = QuizAttempt(
        traineeId = traineeId, moduleId = moduleId, attemptNumber = 1, correct = if (passed) 5 else 3,
        total = 6, secondsUsed = 60, timeLimitSeconds = 270, basePoints = points, bonusPoints = 0,
        totalPoints = points, passed = passed, perfect = false
    )
}
