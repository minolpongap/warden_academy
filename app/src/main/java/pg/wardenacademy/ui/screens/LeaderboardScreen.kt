package pg.wardenacademy.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.TraineeStats
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.Avatar
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(vm: AppViewModel) {
    val board by vm.leaderboard.collectAsState()
    val me by vm.currentId.collectAsState()
    var province by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val provinces = board.map { it.trainee.province }.distinct().sorted()
    val shown = if (province == null) board
    else board.filter { it.trainee.province == province }
        .mapIndexed { i, s -> s.copy(position = i + 1) }

    WardenScaffold(
        title = "Leaderboard",
        tabScreen = true,
        actions = {
            IconButton(
                onClick = {
                    val text = buildString {
                        appendLine("Warden Academy PNG: Leaderboard${province?.let { " ($it)" } ?: ""}")
                        shown.take(20).forEach {
                            appendLine("${it.position}. ${it.trainee.name}: ${it.totalPoints} pts, ${it.rank.title}, ${it.modulesPassed}/${it.totalModules} modules")
                        }
                    }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, "Share leaderboard"))
                },
                modifier = Modifier.testTag("share_leaderboard_button")
            ) { Icon(Icons.Filled.Share, contentDescription = "Share leaderboard") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (provinces.size > 1) {
                item {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = province == null,
                            onClick = { province = null },
                            label = { Text("All") },
                            modifier = Modifier.testTag("filter_all_provinces")
                        )
                        provinces.forEach { p ->
                            FilterChip(selected = province == p, onClick = { province = p }, label = { Text(p) })
                        }
                    }
                }
            }
            if (shown.isEmpty()) {
                item { Text("No trainees yet. Complete a quiz to appear here.") }
            }
            items(shown, key = { it.trainee.id }) { s -> LeaderRow(s, isMe = s.trainee.id == me) }
        }
    }
}

@Composable
private fun LeaderRow(s: TraineeStats, isMe: Boolean) {
    val medal = when (s.position) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#${s.position}" }
    SectionCard(
        container = if (isMe) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(medal, style = MaterialTheme.typography.titleLarge, modifier = Modifier.widthIn(min = 44.dp))
            Avatar(s.trainee.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.trainee.name + if (isMe) " (you)" else "", fontWeight = FontWeight.SemiBold)
                Text(
                    "${s.rank.title} • ${s.modulesPassed}/${s.totalModules} modules • ${s.badges.size} badges",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(s.trainee.province, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${s.totalPoints}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("pts", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
