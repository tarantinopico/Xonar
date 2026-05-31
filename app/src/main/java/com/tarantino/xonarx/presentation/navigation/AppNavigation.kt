package com.tarantino.xonarx.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tarantino.xonarx.presentation.browser.BrowserScreen
import com.tarantino.xonarx.presentation.tabs.TabSwitcherScreen
import com.tarantino.xonarx.presentation.identity.IdentityScreen
import com.tarantino.xonarx.presentation.settings.SettingsScreen
import com.tarantino.xonarx.presentation.settings.PrivacyStatsScreen
import com.tarantino.xonarx.presentation.settings.UserscriptManagerScreen
import com.tarantino.xonarx.presentation.history.HistoryScreen
import com.tarantino.xonarx.presentation.bookmarks.BookmarksScreen
import com.tarantino.xonarx.presentation.downloads.DownloadsScreen
import com.tarantino.xonarx.presentation.notes.NotesScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Browser.route
    ) {
        composable(Screen.Browser.route) {
            BrowserScreen(
                onNavigateToTabSwitcher = { navController.navigate(Screen.TabSwitcher.route) },
                onNavigateToIdentityManager = { navController.navigate(Screen.IdentityManager.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                onNavigateToPrivacyStats = { navController.navigate(Screen.PrivacyStats.route) },
                onNavigateToUserscripts = { navController.navigate(Screen.Userscripts.route) }
            )
        }
        composable(Screen.TabSwitcher.route) {
            TabSwitcherScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.IdentityManager.route) {
            IdentityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PrivacyStats.route) {
            PrivacyStatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Userscripts.route) {
            UserscriptManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
