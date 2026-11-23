package pg.wardenacademy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.Badge
import pg.wardenacademy.domain.Rank
import pg.wardenacademy.domain.Scoring
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.RankHeader
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold
import pg.wardenacademy.ui.theme.Danger
import pg.wardenacademy.ui.theme.Success
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    vm: AppViewModel,
    onCertificate: () -> Unit,
    onAbout: () -> Unit,
    onSwitchTrainee: () -> Unit
) {
    val stats by vm.currentStats.collectAsState()
    val attempts by vm.myAttempts.collectAsState()
    val s = stats
    val titles = vm.course.modules.associate { it.id to "Module ${it.number}" }
    val fmt = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

    WardenScaffold(title = "My Profile", tabScreen = true) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (s != null) {
                item { RankHeader(s) }
                item {
                    SectionCard {
                        Text("Summary", fontWeight = FontWeight.Bold)
                        Text("Leaderboard position: #${s.position}")
                        Text("Quiz attempts: ${s.totalAttempts}")
                        Text("Average best score: ${s.averageBestPercent}%")
                        Text("Perfect modules: ${s.perfectModules}")
                        Text("Staff ID: ${s.trainee.staffId}")
                    }
                }
                item {
                    SectionCard {
                        Text("Badges (${s.badges.size}/${Badge.entries.size})", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Badge.entries.forEach { b ->
                            val earned = b in s.badges
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp).alpha(if (earned) 1f else 0.4f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(b.emoji, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(b.title, fontWeight = FontWeight.SemiBold)
                                    Text(b.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                item {
                    SectionCard {
                        Text("Rank ladder", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Rank.entries.forEach { r ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    (if (r == s.rank) "▶ " else "    ") + r.title,
                                    fontWeight = if (r == s.rank) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("${r.minPoints}+ pts")
                            }
                        }
                    }
                }
                item {
                    SectionCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("Certificate of Completion", fontWeight = FontWeight.Bold)
                        Text(
                            if (s.allPassed) "You have passed every module. Your certificate is ready."
                            else "Pass all ${s.totalModules} modules to unlock your certificate.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onCertificate, enabled = s.allPassed, modifier = Modifier.fillMaxWidth()) {
                            Text("View certificate")
                        }
                    }
                }
            }

            item { Text("Attempt history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (attempts.isEmpty()) item { Text("No quiz attempts yet.") }
            items(attempts, key = { it.id }) { a ->
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${titles[a.moduleId] ?: a.moduleId} • attempt #${a.attemptNumber}", fontWeight = FontWeight.SemiBold)
                            Text(fmt.format(Date(a.completedAt)), style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${Scoring.percent(a.correct, a.total)}% ${if (a.passed) "PASS" else "FAIL"}",
                                color = if (a.passed) Success else Danger, fontWeight = FontWeight.Bold
                            )
                            Text("${a.totalPoints} pts", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("About & scoring rules") }
                TextButton(onClick = onSwitchTrainee, modifier = Modifier.fillMaxWidth()) { Text("Switch trainee") }
            }
        }
    }
}
