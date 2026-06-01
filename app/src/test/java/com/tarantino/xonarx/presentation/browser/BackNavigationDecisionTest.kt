package com.tarantino.xonarx.presentation.browser

import com.tarantino.xonarx.domain.model.Tab
import com.tarantino.xonarx.presentation.main.MainUiState
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class BackNavigationDecisionTest {

    @Test
    fun `when tab can go back, return GoBack`() {
        val result = decideBackAction(
            canGoBack = true,
            tabCount = 2,
            hasActiveTab = true,
            isLastTabBlank = false
        )
        assertEquals(BackAction.GoBack, result)
    }

    @Test
    fun `when tab cannot go back and multiple tabs, return CloseTab`() {
        val result = decideBackAction(
            canGoBack = false,
            tabCount = 2,
            hasActiveTab = true,
            isLastTabBlank = false
        )
        assertEquals(BackAction.CloseTab, result)
    }

    @Test
    fun `when tab cannot go back and is last tab with content, return CloseTab`() {
        val result = decideBackAction(
            canGoBack = false,
            tabCount = 1,
            hasActiveTab = true,
            isLastTabBlank = false
        )
        assertEquals(BackAction.CloseTab, result)
    }

    @Test
    fun `when tab cannot go back and is last blank tab, return MinimizeApp`() {
        val result = decideBackAction(
            canGoBack = false,
            tabCount = 1,
            hasActiveTab = true,
            isLastTabBlank = true
        )
        assertEquals(BackAction.MinimizeApp, result)
    }

    @Test
    fun `when no tabs exist, return MinimizeApp`() {
        val result = decideBackAction(
            canGoBack = false,
            tabCount = 0,
            hasActiveTab = false,
            isLastTabBlank = false
        )
        assertEquals(BackAction.MinimizeApp, result)
    }

    private fun decideBackAction(
        canGoBack: Boolean,
        tabCount: Int,
        hasActiveTab: Boolean,
        isLastTabBlank: Boolean
    ): BackAction {
        return when {
            canGoBack -> BackAction.GoBack
            tabCount > 1 -> BackAction.CloseTab
            hasActiveTab && isLastTabBlank -> BackAction.MinimizeApp
            hasActiveTab -> BackAction.CloseTab
            else -> BackAction.MinimizeApp
        }
    }

    enum class BackAction {
        GoBack, CloseTab, MinimizeApp
    }
}
