package pg.wardenacademy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.Rank
import pg.wardenacademy.domain.Scoring
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.BulletList
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold

@Composable
fun AboutScreen(vm: AppViewModel, onBack: () -> Unit) {
    val maxPerModule = vm.course.modules.firstOrNull()?.let { Scoring.maxPointsFor(it.quiz.size) } ?: 0
    WardenScaffold(title = "About", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard {
                Text("How points work", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                BulletList(
                    listOf(
                        "${Scoring.POINTS_PER_CORRECT} points per correct answer",
                        "Pass mark: ${Scoring.PASS_PERCENT}%. Passing unlocks the next module.",
                        "+${Scoring.FIRST_ATTEMPT_PASS_BONUS} for passing on your first attempt",
                        "+${Scoring.PERFECT_BONUS} for a perfect score",
                        "Up to +${Scoring.MAX_SPEED_BONUS} speed bonus on a pass (more time left = more points)",
                        "${Scoring.SECONDS_PER_QUESTION} seconds per question. When time runs out the quiz auto-submits.",
                        "Only your BEST attempt per module counts toward the leaderboard (max $maxPerModule per module)",
                        "Leaderboard ties are broken by modules passed, then by who got there first"
                    )
                )
            }
            SectionCard {
                Text("Ranks", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                BulletList(Rank.entries.map { "${it.title}: ${it.minPoints}+ points" })
            }
            SectionCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                Text("Important", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(vm.course.disclaimer, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${vm.course.title} • content v${vm.course.version}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
