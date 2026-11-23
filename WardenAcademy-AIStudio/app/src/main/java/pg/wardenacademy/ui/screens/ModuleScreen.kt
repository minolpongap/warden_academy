package pg.wardenacademy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.Scoring
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.BulletList
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold
import pg.wardenacademy.ui.components.formatSeconds

@Composable
fun ModuleScreen(vm: AppViewModel, moduleId: String, onBack: () -> Unit, onStartQuiz: () -> Unit) {
    val module = vm.course.modules.firstOrNull { it.id == moduleId } ?: return
    val progress by vm.moduleProgress.collectAsState()
    val mp = progress.firstOrNull { it.module.id == moduleId }

    WardenScaffold(title = "Module ${module.number}", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(module.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Mining Act 1992 • ${module.sections}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                SectionCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("Learning objectives", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    BulletList(module.objectives)
                }
            }
            itemsIndexed(module.lessons) { i, lesson ->
                SectionCard {
                    Text("Lesson ${i + 1}", style = MaterialTheme.typography.labelSmall)
                    Text(lesson.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    lesson.reference?.let {
                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (lesson.body.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(lesson.body, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (lesson.bullets.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        BulletList(lesson.bullets)
                    }
                    lesson.note?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("💡 $it", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                    }
                }
            }
            item {
                SectionCard(container = MaterialTheme.colorScheme.primaryContainer) {
                    Text("Key points to remember", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    BulletList(module.keyPoints)
                }
            }
            module.tokPisin?.let { tp ->
                item {
                    SectionCard {
                        Text("Long Tok Pisin", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(tp, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    }
                }
            }
            item {
                SectionCard {
                    Text("Module quiz", fontWeight = FontWeight.Bold)
                    Text(
                        "${module.quiz.size} questions • ${formatSeconds(Scoring.timeLimitFor(module.quiz.size))} time limit • " +
                            "pass mark ${Scoring.PASS_PERCENT}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (mp != null && mp.attempts > 0) {
                        Text(
                            "Attempts: ${mp.attempts} • Best: ${mp.bestPercent}% (${mp.bestPoints} pts)" +
                                if (mp.passed) " • PASSED" else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
                        Text(if ((mp?.attempts ?: 0) > 0) "Retake quiz" else "Start quiz")
                    }
                }
            }
        }
    }
}
