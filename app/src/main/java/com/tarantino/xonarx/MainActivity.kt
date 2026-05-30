package com.tarantino.xonarx

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.tarantino.xonarx.presentation.navigation.AppNavigation
import com.tarantino.xonarx.presentation.theme.XonarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XonarTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
