package pg.wardenacademy.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pg.wardenacademy.data.content.Question
import pg.wardenacademy.data.content.TrainingModule
import pg.wardenacademy.data.db.LocalStore
import pg.wardenacademy.data.db.QuizAttempt
import pg.wardenacademy.domain.Scoring

/** A question with its options shuffled for this attempt. */
data class QuizItem(
    val question: Question,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizUiState(
    val items: List<QuizItem> = emptyList(),
    val index: Int = 0,
    val selected: Map<Int, Int> = emptyMap(),   // question index -> chosen option index
    val checked: Set<Int> = emptySet(),          // questions whose answer has been revealed
    val timeLimit: Int = 0,
    val secondsLeft: Int = 0,
    val result: Scoring.Result? = null,
    val attemptNumber: Int = 0,
    val timedOut: Boolean = false
) {
    val current: QuizItem? get() = items.getOrNull(index)
    val correctCount: Int get() = items.indices.count { selected[it] == items[it].correctIndex }
    val finished: Boolean get() = result != null
}

/**
 * Runs one quiz. It is tied to the quiz screen's composition scope, so leaving the
 * screen cancels the timer and nothing is recorded.
 */
class QuizController(
    private val store: LocalStore,
    private val scope: CoroutineScope,
    private val traineeId: Long,
    val module: TrainingModule
) {
    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state
    private var timerJob: Job? = null

    init { start() }

    fun start() {
        val items = module.quiz.shuffled().map { q ->
            val order = q.options.indices.shuffled()
            QuizItem(q, order.map { q.options[it] }, order.indexOf(q.answerIndex))
        }
        val limit = Scoring.timeLimitFor(items.size)
        _state.value = QuizUiState(items = items, timeLimit = limit, secondsLeft = limit)
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _state.value.secondsLeft > 0 && !_state.value.finished) {
                delay(1_000)
                _state.update { it.copy(secondsLeft = (it.secondsLeft - 1).coerceAtLeast(0)) }
            }
            if (!_state.value.finished) {
                _state.update { it.copy(timedOut = true) }
                finish()
            }
        }
    }

    fun select(option: Int) {
        val s = _state.value
        if (s.finished || s.index in s.checked) return
        _state.update { it.copy(selected = it.selected + (it.index to option)) }
    }

    fun check() {
        if (_state.value.index !in _state.value.selected) return
        _state.update { it.copy(checked = it.checked + it.index) }
    }

    fun next() {
        val s = _state.value
        if (s.index < s.items.lastIndex) _state.update { it.copy(index = it.index + 1) } else finish()
    }

    fun finish() {
        val s = _state.value
        if (s.finished) return
        timerJob?.cancel()
        val attemptNumber = store.countAttempts(traineeId, module.id) + 1
        val used = s.timeLimit - s.secondsLeft
        val result = Scoring.score(s.correctCount, s.items.size, attemptNumber, used, s.timeLimit)
        store.insertAttempt(
            QuizAttempt(
                traineeId = traineeId, moduleId = module.id, attemptNumber = attemptNumber,
                correct = s.correctCount, total = s.items.size, secondsUsed = used,
                timeLimitSeconds = s.timeLimit, basePoints = result.basePoints,
                bonusPoints = result.bonusPoints, totalPoints = result.totalPoints,
                passed = result.passed, perfect = result.perfect
            )
        )
        _state.update { it.copy(result = result, attemptNumber = attemptNumber) }
    }
}
