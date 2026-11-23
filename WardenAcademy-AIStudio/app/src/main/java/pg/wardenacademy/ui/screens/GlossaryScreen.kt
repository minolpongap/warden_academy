package pg.wardenacademy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pg.wardenacademy.ui.AppViewModel
import pg.wardenacademy.ui.components.SectionCard
import pg.wardenacademy.ui.components.WardenScaffold

@Composable
fun GlossaryScreen(vm: AppViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    val terms = vm.course.glossary
        .filter { query.isBlank() || it.term.contains(query, true) || it.definition.contains(query, true) }
        .sortedBy { it.term }

    WardenScaffold(title = "Glossary", tabScreen = true) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text("Search terms") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            items(terms, key = { it.term }) { t ->
                SectionCard {
                    Text(t.term, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(t.definition, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
