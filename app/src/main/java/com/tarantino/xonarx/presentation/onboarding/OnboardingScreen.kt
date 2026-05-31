package com.tarantino.xonarx.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarantino.xonarx.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.completeOnboarding()
            onFinished()
        }
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var step by remember { mutableIntStateOf(0) }
    
    val steps = listOf(
        OnboardingStepData(
            title = "Welcome to Xonar",
            description = "A premium, identity-aware browser designed for privacy and productivity.",
            icon = Icons.Default.Star
        ),
        OnboardingStepData(
            title = "Identities",
            description = "Keep your work, personal, and private browsing completely separated. Each identity has its own tabs, cookies, and history.",
            icon = Icons.Default.Security
        ),
        OnboardingStepData(
            title = "Powerful Tabs",
            description = "Organize with tab groups and access your favorite sites quickly from the new tab dashboard.",
            icon = Icons.Default.Tab
        )
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val currentStep = steps[step]
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    currentStep.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = currentStep.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = currentStep.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == step) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (step < steps.size - 1) {
                            step++
                        } else {
                            viewModel.completeOnboarding(onComplete)
                        }
                    }
                ) {
                    Text(if (step < steps.size - 1) "Next" else "Get Started")
                }
            }
        }
    }
}

data class OnboardingStepData(
    val title: String,
    val description: String,
    val icon: ImageVector
)
