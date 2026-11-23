package pg.wardenacademy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.domain.Rank
import pg.wardenacademy.domain.TraineeStats

/**
 * Shared screen frame. Tab screens sit above the bottom navigation bar, so they
 * don't add the system navigation-bar inset again.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    tabScreen: Boolean = false,
    actions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        contentWindowInsets = if (tabScreen) WindowInsets(0) else WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("nav_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { line ->
            Row {
                Text("•  ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun Pill(text: String, container: Color, content: Color) {
    Box(
        Modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = content, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Avatar(name: String, size: Int = 40) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
    }
}

/** Rank title, points and progress to the next rank. */
@Composable
fun RankHeader(stats: TraineeStats) {
    val next = stats.rank.next
    SectionCard(container = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(stats.trainee.name, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stats.trainee.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${stats.rank.title} • ${stats.trainee.province}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${stats.totalPoints}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("points", style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (next != null) {
            val span = (next.minPoints - stats.rank.minPoints).coerceAtLeast(1)
            val into = (stats.totalPoints - stats.rank.minPoints).coerceIn(0, span)
            LinearProgressIndicator(
                progress = { into.toFloat() / span },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${next.minPoints - stats.totalPoints} pts to ${next.title}",
                style = MaterialTheme.typography.labelMedium
            )
        } else {
            Text("Top rank reached: ${Rank.CHIEF_WARDEN.title} 🏆", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Modules passed: ${stats.modulesPassed}/${stats.totalModules}",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

fun formatSeconds(total: Int): String = "%d:%02d".format(total / 60, total % 60)
