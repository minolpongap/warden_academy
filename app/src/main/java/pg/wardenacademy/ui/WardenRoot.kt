package pg.wardenacademy.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pg.wardenacademy.WardenApp
import pg.wardenacademy.ui.screens.AboutScreen
import pg.wardenacademy.ui.screens.CertificateScreen
import pg.wardenacademy.ui.screens.GlossaryScreen
import pg.wardenacademy.ui.screens.HomeScreen
import pg.wardenacademy.ui.screens.LeaderboardScreen
import pg.wardenacademy.ui.screens.ModuleScreen
import pg.wardenacademy.ui.screens.ProfileScreen
import pg.wardenacademy.ui.screens.QuizScreen
import pg.wardenacademy.ui.screens.WelcomeScreen

private data class Tab(val screen: Screen, val label: String, val icon: ImageVector, val testTag: String)

private val tabs = listOf(
    Tab(Screen.Home, "Modules", Icons.Filled.School, "tab_modules"),
    Tab(Screen.Leaderboard, "Leaderboard", Icons.Filled.EmojiEvents, "tab_leaderboard"),
    Tab(Screen.Glossary, "Glossary", Icons.Filled.MenuBook, "tab_glossary"),
    Tab(Screen.Profile, "Profile", Icons.Filled.AccountCircle, "tab_profile"),
)

/** Single-activity root: bottom tabs plus a ViewModel-held back stack (no Navigation library). */
@Composable
fun WardenRoot(app: WardenApp) {
    val vm: AppViewModel = viewModel(factory = viewModelFactory { initializer { AppViewModel(app) } })
    val screen = vm.screen
    val showTabs = screen in TAB_SCREENS

    // The quiz screen handles its own back press (it asks before leaving).
    BackHandler(enabled = vm.canGoBack && screen !is Screen.Quiz) { vm.back() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showTabs) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = screen == tab.screen,
                            onClick = { vm.openTab(tab.screen) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (screen) {
                Screen.Welcome -> WelcomeScreen(vm) { vm.resetTo(Screen.Home) }
                Screen.Home -> HomeScreen(vm, onOpenModule = { vm.navigate(Screen.Module(it)) })
                Screen.Leaderboard -> LeaderboardScreen(vm)
                Screen.Glossary -> GlossaryScreen(vm)
                Screen.Profile -> ProfileScreen(
                    vm = vm,
                    onCertificate = { vm.navigate(Screen.Certificate) },
                    onAbout = { vm.navigate(Screen.About) },
                    onSwitchTrainee = { vm.signOut() }
                )
                is Screen.Module -> ModuleScreen(
                    vm = vm,
                    moduleId = screen.id,
                    onBack = { vm.back() },
                    onStartQuiz = { vm.navigate(Screen.Quiz(screen.id)) }
                )
                is Screen.Quiz -> QuizScreen(
                    app = app,
                    vm = vm,
                    quiz = screen,
                    onExit = { vm.back() },
                    onLeaderboard = { vm.openTab(Screen.Leaderboard) }
                )
                Screen.Certificate -> CertificateScreen(vm, onBack = { vm.back() })
                Screen.About -> AboutScreen(vm, onBack = { vm.back() })
            }
        }
    }
}
