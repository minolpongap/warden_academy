package pg.wardenacademy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pg.wardenacademy.PNG_PROVINCES
import pg.wardenacademy.data.db.Trainee
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.Avatar
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold

@Composable
fun WelcomeScreen(vm: AppViewModel, onSignedIn: () -> Unit) {
    val trainees by vm.trainees.collectAsState()
    var name by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var toDelete by remember { mutableStateOf<Trainee?>(null) }

    WardenScaffold(title = vm.course.title) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Mining Warden Training", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Learn the functions, powers and duties of a Mining Warden under the Mining Act 1992, " +
                        "test yourself after each module, and climb the leaderboard.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (trainees.isNotEmpty()) {
                item { Text("Continue as", style = MaterialTheme.typography.titleMedium) }
                items(trainees, key = { it.id }) { t ->
                    SectionCard(Modifier.clickable { vm.select(t.id); onSignedIn() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(t.name)
                            Spacer(Modifier.width(12.dp))
                            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                Text(t.name, fontWeight = FontWeight.SemiBold)
                                Text("${t.staffId} • ${t.province}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { toDelete = t }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove trainee")
                            }
                        }
                    }
                }
            }

            item {
                SectionCard {
                    Text("New trainee", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it; error = null },
                        label = { Text("Full name") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffId, onValueChange = { staffId = it; error = null },
                        label = { Text("Staff / Trainee ID") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ProvincePicker(province) { province = it; error = null }
                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            vm.register(name, staffId, province) { err ->
                                if (err == null) onSignedIn() else error = err
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Start training") }
                }
            }
        }
    }

    toDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Remove ${t.name}?") },
            text = { Text("This permanently deletes this trainee's progress and leaderboard points from this device.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteTrainee(t); toDelete = null }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProvincePicker(value: String, onPick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true,
            label = { Text("Province") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Transparent overlay so a tap anywhere on the field opens the menu.
        Box(Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PNG_PROVINCES.forEach { p ->
                DropdownMenuItem(text = { Text(p) }, onClick = { onPick(p); expanded = false })
            }
        }
    }
}
