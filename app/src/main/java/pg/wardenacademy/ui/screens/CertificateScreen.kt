package pg.wardenacademy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.WardenScaffold
import pg.wardenacademy.ui.theme.PngGold
import pg.wardenacademy.ui.theme.PngRed
import pg.wardenacademy.util.CertificatePdf

@Composable
fun CertificateScreen(vm: AppViewModel, onBack: () -> Unit) {
    val stats by vm.currentStats.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val s = stats

    WardenScaffold(title = "Certificate", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (s == null || !s.allPassed) {
                Text("Pass every module to unlock your certificate.")
                return@Column
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(6.dp, PngRed))
                    .padding(6.dp)
                    .border(BorderStroke(2.dp, PngGold))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("WARDEN ACADEMY PNG", color = PngRed, fontWeight = FontWeight.Bold)
                Text("Certificate of Completion", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("This is to certify that", fontStyle = FontStyle.Italic)
                Text(s.trainee.name, style = MaterialTheme.typography.headlineMedium, color = PngRed, fontWeight = FontWeight.Bold)
                Text(
                    "has completed all ${s.totalModules} modules on the functions of the Mining Warden " +
                        "under the Mining Act 1992 (Papua New Guinea).",
                    textAlign = TextAlign.Center
                )
                Text("Rank: ${s.rank.title} • ${s.totalPoints} points", fontWeight = FontWeight.SemiBold)
            }
            Button(
                enabled = !busy,
                onClick = {
                    busy = true
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            CertificatePdf.create(
                                context,
                                CertificatePdf.Details(
                                    name = s.trainee.name,
                                    staffId = s.trainee.staffId,
                                    province = s.trainee.province,
                                    rank = s.rank.title,
                                    points = s.totalPoints,
                                    modules = s.totalModules,
                                    averagePercent = s.averageBestPercent
                                )
                            )
                        }
                        busy = false
                        CertificatePdf.share(context, file)
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("export_pdf_button")
            ) { Text(if (busy) "Preparing PDF…" else "Export PDF & share") }
        }
    }
}
