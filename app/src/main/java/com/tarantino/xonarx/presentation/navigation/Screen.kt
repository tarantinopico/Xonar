package com.tarantino.xonarx.presentation.navigation

sealed class Screen(val route: String) {
    object Browser : Screen("browser")
    object TabSwitcher : Screen("tab_switcher")
    object IdentityManager : Screen("identity_manager")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Bookmarks : Screen("bookmarks")
    object Downloads : Screen("downloads")
    object Notes : Screen("notes")
}
