package com.tarantino.xonarx.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.model.Userscript
import com.tarantino.xonarx.domain.repository.AppPreferences
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.UserscriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserscriptManagerViewModel @Inject constructor(
    private val userscriptRepository: UserscriptRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val activeIdentityId = settingsRepository.preferences.stateIn(
        viewModelScope, SharingStarted.Eagerly, AppPreferences()
    )

    var scripts by mutableStateOf<List<Userscript>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            activeIdentityId.collect { prefs ->
                val id = prefs.lastActiveIdentityId
                if (id != null) {
                    userscriptRepository.getScriptsForIdentity(id).collect { s ->
                        scripts = s
                    }
                }
            }
        }
    }

    fun saveScript(name: String, code: String, domain: String?, isCss: Boolean) {
        val identityId = activeIdentityId.value.lastActiveIdentityId ?: return
        viewModelScope.launch {
            userscriptRepository.saveScript(
                Userscript(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    code = code,
                    domain = domain,
                    isEnabled = true,
                    identityId = identityId,
                    isCss = isCss
                )
            )
        }
    }

    fun toggleScript(script: Userscript) {
        viewModelScope.launch {
            userscriptRepository.saveScript(script.copy(isEnabled = !script.isEnabled))
        }
    }

    fun deleteScript(script: Userscript) {
        viewModelScope.launch {
            userscriptRepository.deleteScript(script)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserscriptManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserscriptManagerViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var domain by remember { mutableStateOf("") }
        var isCss by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Userscript / CSS") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("Domain (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCss, onCheckedChange = { isCss = it })
                        Text("This is CSS")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        viewModel.saveScript(name, code, domain.takeIf { it.isNotBlank() }, isCss)
                        showAddDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Userscripts & CSS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Script")
            }
        }
    ) { padding ->
        if (viewModel.scripts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No userscripts added.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(viewModel.scripts) { script ->
                    ListItem(
                        headlineContent = { Text(script.name) },
                        supportingContent = { Text("Domain: ${script.domain ?: "All"} | Type: ${if(script.isCss) "CSS" else "JS"}") },
                        trailingContent = {
                            Row {
                                Switch(
                                    checked = script.isEnabled,
                                    onCheckedChange = { viewModel.toggleScript(script) }
                                )
                                IconButton(onClick = { viewModel.deleteScript(script) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
