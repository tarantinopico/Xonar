package com.tarantino.xonarx

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.tarantino.xonarx.domain.repository.AppPreferences
import com.tarantino.xonarx.domain.repository.SettingsRepository
import com.tarantino.xonarx.domain.repository.ThemeMode
import com.tarantino.xonarx.presentation.navigation.AppNavigation
import com.tarantino.xonarx.presentation.theme.XonarTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by settingsRepository.preferences.collectAsState(initial = AppPreferences())
            
            val isDark = when (prefs.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            XonarTheme(
                darkTheme = isDark,
                dynamicColor = prefs.useMaterialYou
            ) {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
