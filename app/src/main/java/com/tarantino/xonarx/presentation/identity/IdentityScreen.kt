package com.tarantino.xonarx.presentation.identity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tarantino.xonarx.domain.model.Identity
import com.tarantino.xonarx.domain.usecase.IdentityManager
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import com.tarantino.xonarx.domain.usecase.BiometricAuthManager
import androidx.compose.material.icons.filled.Lock

@HiltViewModel
class IdentityViewModel @Inject constructor(
    private val identityManager: IdentityManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {
    val identities: StateFlow<List<Identity>> = identityManager.allIdentities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val activeIdentity: StateFlow<Identity?> = identityManager.activeIdentity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createIdentity(name: String, color: Int) {
        viewModelScope.launch {
            identityManager.createIdentity(name, color)
        }
    }

    fun switchIdentity(id: String, activity: FragmentActivity?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val identity = identities.value.find { it.id == id }
            if (identity != null && identity.lockSettings == 1) {
                if (activity != null) {
                    val success = biometricAuthManager.authenticate(activity, "Unlock Identity")
                    if (success) {
                        identityManager.switchIdentity(id)
                        onComplete()
                    }
                }
            } else {
                identityManager.switchIdentity(id)
                onComplete()
            }
        }
    }

    fun deleteIdentity(identity: Identity) {
        viewModelScope.launch {
            identityManager.deleteIdentity(identity)
        }
    }

    fun toggleLock(id: String) {
        viewModelScope.launch {
            // Need a way to update identity lock settings in IdentityManager
            // For now, assume a copy update and saving it using a new use case method if available
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityScreen(
    viewModel: IdentityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val identities by viewModel.identities.collectAsState()
    val activeIdentity by viewModel.activeIdentity.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? FragmentActivity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identities") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Identity")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(identities) { identity ->
                IdentityItem(
                    identity = identity,
                    isActive = identity.id == activeIdentity?.id,
                    onClick = {
                        viewModel.switchIdentity(identity.id, activity, onNavigateBack)
                    },
                    onDelete = { viewModel.deleteIdentity(identity) },
                    onToggleLock = { viewModel.toggleLock(identity.id) }
                )
                HorizontalDivider()
            }
        }
        
        if (showCreateDialog) {
            CreateIdentityDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, color -> 
                    viewModel.createIdentity(name, color)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun IdentityItem(
    identity: Identity,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleLock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(identity.color)),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = identity.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleLock) {
            val icon = if (identity.lockSettings == 1) Icons.Default.Lock else Icons.Default.Lock
            Icon(icon, contentDescription = "Toggle Lock", tint = if (identity.lockSettings == 1) MaterialTheme.colorScheme.primary else Color.Gray)
        }
        if (identitiesCanBeDeleted()) {
             IconButton(onClick = onDelete) {
                 Icon(Icons.Default.Delete, contentDescription = "Delete")
             }
        }
    }
}

fun identitiesCanBeDeleted() = true

@Composable
fun CreateIdentityDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf(0xFF6200EE.toInt(), 0xFF03DAC5.toInt(), 0xFFB00020.toInt(), 0xFF3700B3.toInt())
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Identity") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, selectedColor)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
