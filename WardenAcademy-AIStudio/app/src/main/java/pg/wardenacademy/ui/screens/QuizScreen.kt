package pg.wardenacademy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pg.wardenacademy.WardenApp
import pg.wardenacademy.domain.Scoring
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.QuizUiState
import pg.wardenacademy.ui.QuizController
import pg.wardenacademy.ui.Screen
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold
import pg.wardenacademy.ui.components.formatSeconds
import pg.wardenacademy.ui.theme.Danger
import pg.wardenacademy.ui.theme.Success

@Composable
fun QuizScreen(
    app: WardenApp,
    vm: AppViewModel,
    quiz: Screen.Quiz,
    onExit: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val traineeId = vm.currentId.collectAsState().value ?: return
    val module = vm.course.modules.firstOrNull { it.id == quiz.id } ?: return
    // Scope ends when this screen leaves composition, which cancels the timer.
    val scope = rememberCoroutineScope()
    val quizVm = remember(quiz) { QuizController(app.store, scope, traineeId, module) }
    val state by quizVm.state.collectAsState()
    var confirmExit by remember { mutableStateOf(false) }
    var reviewing by remember { mutableStateOf(false) }

    BackHandler { if (state.finished) onExit() else confirmExit = true }

    WardenScaffold(
        title = if (state.finished) "Results • Module ${module.number}" else "Quiz • Module ${module.number}",
        onBack = { if (state.finished) onExit() else confirmExit = true }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.finished && reviewing -> ReviewContent(state) { reviewing = false }

                state.finished -> ResultContent(
                    state = state,
                    onReview = { reviewing = true },
                    onRetake = { reviewing = false; quizVm.start() },
                    onBackToModules = onExit,
                    onLeaderboard = onLeaderboard
                )

                else -> QuestionContent(
                    state = state,
                    onSelect = quizVm::select,
                    onCheck = quizVm::check,
                    onNext = quizVm::next
                )
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Leave the quiz?") },
            text = { Text("Your answers so far will be lost and this attempt won't be recorded.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onExit() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Keep going") } }
        )
    }
}

@Composable
private fun QuestionContent(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit
) {
    val item = state.current ?: return
    val isChecked = state.index in state.checked
    val chosen = state.selected[state.index]
    val lowTime = state.secondsLeft <= 30

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Question ${state.index + 1} of ${state.items.size}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.Timer, contentDescription = "Time left", tint = if (lowTime) Danger else MaterialTheme.colorScheme.onSurface)
            Text(
                " ${formatSeconds(state.secondsLeft)}",
                color = if (lowTime) Danger else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (state.index + 1).toFloat() / state.items.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text(item.question.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

        item.options.forEachIndexed { i, option ->
            val border = when {
                isChecked && i == item.correctIndex -> Success
                isChecked && i == chosen -> Danger
                i == chosen -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val container = when {
                isChecked && i == item.correctIndex -> Success.copy(alpha = 0.12f)
                isChecked && i == chosen -> Danger.copy(alpha = 0.12f)
                i == chosen -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            OutlinedCard(
                onClick = { onSelect(i) },
                enabled = !isChecked,
                border = BorderStroke(2.dp, border),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = container,
                    disabledContainerColor = container,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${'A' + i}.  $option", modifier = Modifier.padding(14.dp))
            }
        }

        if (isChecked) {
            val correct = chosen == item.correctIndex
            SectionCard(container = (if (correct) Success else Danger).copy(alpha = 0.10f)) {
                Text(
                    if (correct) "✅ Correct" else "❌ Not quite. The answer is ${'A' + item.correctIndex}.",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(item.question.explanation, style = MaterialTheme.typography.bodyMedium)
                item.question.reference?.let {
                    Text("Reference: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.index == state.items.lastIndex) "Finish quiz" else "Next question")
            }
        } else {
            Button(onClick = onCheck, enabled = chosen != null, modifier = Modifier.fillMaxWidth()) {
                Text("Check answer")
            }
        }
    }
}

@Composable
private fun ResultContent(
    state: QuizUiState,
    onReview: () -> Unit,
    onRetake: () -> Unit,
    onBackToModules: () -> Unit,
    onLeaderboard: () -> Unit
) {
    val r = state.result ?: return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (r.passed) "🎉" else "📘", style = MaterialTheme.typography.displayMedium)
        Text(
            if (r.passed) "Module passed!" else "Not yet. Keep studying.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (state.timedOut) Text("Time ran out. Unanswered questions were marked wrong.", color = Danger)
        Text(
            "${state.correctCount}/${state.items.size} correct • ${r.percent}%  (pass mark ${Scoring.PASS_PERCENT}%)",
            textAlign = TextAlign.Center
        )
        Text("Attempt #${state.attemptNumber}", style = MaterialTheme.typography.labelMedium)

        SectionCard {
            Text("Points earned", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            r.breakdown.forEach { (label, pts) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(label, modifier = Modifier.weight(1f))
                    Text("+$pts", fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("${r.totalPoints}", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Only your best attempt per module counts toward the leaderboard.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(onClick = onReview, modifier = Modifier.fillMaxWidth()) { Text("Review answers") }
        OutlinedButton(onClick = onRetake, modifier = Modifier.fillMaxWidth()) { Text("Retake quiz") }
        OutlinedButton(onClick = onLeaderboard, modifier = Modifier.fillMaxWidth()) { Text("View leaderboard") }
        TextButton(onClick = onBackToModules) { Text("Back to module") }
    }
}

@Composable
private fun ReviewContent(state: QuizUiState, onDone: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(state.items) { i, item ->
            val chosen = state.selected[i]
            val correct = chosen == item.correctIndex
            SectionCard(container = (if (correct) Success else Danger).copy(alpha = 0.10f)) {
                Text("Q${i + 1}. ${item.question.prompt}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Your answer: ${chosen?.let { item.options[it] } ?: "No answer"}",
                    color = if (correct) Success else Danger)
                if (!correct) Text("Correct answer: ${item.options[item.correctIndex]}", color = Success)
                Spacer(Modifier.height(4.dp))
                Text(item.question.explanation, style = MaterialTheme.typography.bodySmall)
            }
        }
        item { Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Back to results") } }
    }
}
