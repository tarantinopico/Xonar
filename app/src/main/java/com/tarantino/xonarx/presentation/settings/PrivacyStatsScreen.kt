package com.tarantino.xonarx.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.tarantino.xonarx.domain.usecase.AdBlockerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

@HiltViewModel
class PrivacyStatsViewModel @Inject constructor(
    private val adBlockerEngine: AdBlockerEngine
) : ViewModel() {
    val isBlockingEnabled = adBlockerEngine.isBlockingEnabled
    val blockedAdsCount = adBlockerEngine.blockedAdsCount
    
    fun resetStats() {
        adBlockerEngine.resetStats()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrivacyStatsViewModel = hiltViewModel()
) {
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()
    val blockedAdsCount by viewModel.blockedAdsCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isBlockingEnabled) {
                Text(
                    text = "Ad blocking is disabled.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = blockedAdsCount.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ads and Trackers Blocked",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(onClick = { viewModel.resetStats() }) {
                    Text("Reset Statistics")
                }
            }
        }
    }
}
