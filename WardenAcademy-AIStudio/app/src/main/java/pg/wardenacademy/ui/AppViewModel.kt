package pg.wardenacademy.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pg.wardenacademy.WardenApp
import pg.wardenacademy.data.content.Course
import pg.wardenacademy.data.db.QuizAttempt
import pg.wardenacademy.data.db.Trainee
import pg.wardenacademy.domain.ModuleProgress
import pg.wardenacademy.domain.Progress
import pg.wardenacademy.domain.TraineeStats

/** Screens of the app. Navigation is a simple back stack held in the ViewModel. */
sealed interface Screen {
    data object Welcome : Screen
    data object Home : Screen
    data object Leaderboard : Screen
    data object Glossary : Screen
    data object Profile : Screen
    data class Module(val id: String) : Screen
    /** `session` makes every quiz visit a fresh attempt. */
    data class Quiz(val id: String, val session: Long = System.nanoTime()) : Screen
    data object Certificate : Screen
    data object About : Screen
}

val TAB_SCREENS: List<Screen> = listOf(Screen.Home, Screen.Leaderboard, Screen.Glossary, Screen.Profile)

class AppViewModel(private val app: WardenApp) : ViewModel() {

    val course: Course = app.content.course
    private val store = app.store

    // ---------- Navigation ----------
    var backStack by mutableStateOf(
        listOf(if (app.session.currentTraineeId == null) Screen.Welcome else Screen.Home)
    )
        private set
    val screen: Screen get() = backStack.last()
    val canGoBack: Boolean get() = backStack.size > 1

    fun navigate(to: Screen) { backStack = backStack + to }
    fun back() { if (canGoBack) backStack = backStack.dropLast(1) }
    fun openTab(tab: Screen) { backStack = if (tab == Screen.Home) listOf(Screen.Home) else listOf(Screen.Home, tab) }
    fun resetTo(to: Screen) { backStack = listOf(to) }

    // ---------- Data ----------
    private val _currentId = MutableStateFlow(app.session.currentTraineeId)
    val currentId: StateFlow<Long?> = _currentId

    val trainees: StateFlow<List<Trainee>> = store.trainees
    private val attempts: StateFlow<List<QuizAttempt>> = store.attempts

    val leaderboard: StateFlow<List<TraineeStats>> =
        combine(trainees, attempts) { t, a -> Progress.leaderboard(t, course.modules, a) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Progress.leaderboard(trainees.value, course.modules, attempts.value))

    val currentStats: StateFlow<TraineeStats?> =
        combine(_currentId, leaderboard) { id, board -> board.firstOrNull { it.trainee.id == id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val myAttempts: StateFlow<List<QuizAttempt>> =
        combine(_currentId, attempts) { id, a -> a.filter { it.traineeId == id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val moduleProgress: StateFlow<List<ModuleProgress>> =
        myAttempts.map { Progress.moduleProgress(course.modules, it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, Progress.moduleProgress(course.modules, emptyList()))

    fun register(name: String, staffId: String, province: String, onResult: (String?) -> Unit) {
        val cleanName = name.trim()
        val cleanId = staffId.trim().uppercase()
        if (cleanName.length < 2) return onResult("Please enter your full name.")
        if (cleanId.isEmpty()) return onResult("Please enter your staff or trainee ID.")
        if (province.isBlank()) return onResult("Please choose your province.")
        val id = store.insertTrainee(Trainee(name = cleanName, staffId = cleanId, province = province))
            ?: return onResult("ID $cleanId is already registered on this device. Select it from the list.")
        select(id)
        onResult(null)
    }

    fun select(id: Long) {
        app.session.currentTraineeId = id
        _currentId.value = id
    }

    fun signOut() {
        app.session.currentTraineeId = null
        _currentId.value = null
        resetTo(Screen.Welcome)
    }

    fun deleteTrainee(trainee: Trainee) {
        store.deleteTrainee(trainee)
        if (_currentId.value == trainee.id) {
            app.session.currentTraineeId = null
            _currentId.value = null
        }
    }
}
