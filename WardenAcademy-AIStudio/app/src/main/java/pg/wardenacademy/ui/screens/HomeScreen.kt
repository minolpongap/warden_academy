package pg.wardenacademy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.ModuleProgress
import pg.wardenacademy.domain.Scoring
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.Pill
import pg.wardenacademy.ui.components.RankHeader
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold
import pg.wardenacademy.ui.theme.Success

@Composable
fun HomeScreen(vm: AppViewModel, onOpenModule: (String) -> Unit) {
    val stats by vm.currentStats.collectAsState()
    val progress by vm.moduleProgress.collectAsState()

    WardenScaffold(title = "Training Modules", tabScreen = true) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stats?.let { s -> item { RankHeader(s) } }
            item {
                Text(
                    "Pass each module quiz (${Scoring.PASS_PERCENT}%+) to unlock the next one.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            items(progress, key = { it.module.id }) { mp ->
                ModuleCard(mp) { if (mp.unlocked) onOpenModule(mp.module.id) }
            }
        }
    }
}

@Composable
private fun ModuleCard(mp: ModuleProgress, onClick: () -> Unit) {
    val m = mp.module
    SectionCard(
        Modifier
            .alpha(if (mp.unlocked) 1f else 0.55f)
            .clickable(enabled = mp.unlocked, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when {
                    !mp.unlocked -> Icons.Filled.Lock
                    mp.passed -> Icons.Filled.CheckCircle
                    else -> Icons.Filled.PlayCircle
                },
                contentDescription = null,
                tint = when {
                    mp.passed -> Success
                    mp.unlocked -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("MODULE ${m.number} • ${m.sections}", style = MaterialTheme.typography.labelSmall)
                Text(m.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(m.summary, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("${m.lessons.size} lessons", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
            Pill("${m.quiz.size} questions", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
        }
        if (mp.attempts > 0) {
            Spacer(Modifier.height(6.dp))
            Pill(
                "Best ${mp.bestPercent}% • ${mp.bestPoints} pts • ${mp.attempts} attempt(s)",
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
