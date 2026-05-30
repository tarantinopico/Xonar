package com.tarantino.xonarx.domain.usecase

import com.tarantino.xonarx.domain.model.AppPreferences
import com.tarantino.xonarx.domain.model.Identity
import com.tarantino.xonarx.domain.repository.IdentityRepository
import com.tarantino.xonarx.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class IdentityManagerTest {

    @Test
    fun testCreateIdentity() = runTest {
        val mockIdentityRepo = mockk<IdentityRepository>(relaxed = true)
        val mockSettingsRepo = mockk<SettingsRepository>(relaxed = true)
        
        every { mockSettingsRepo.preferences } returns flowOf(AppPreferences())

        val manager = IdentityManager(mockIdentityRepo, mockSettingsRepo)

        val newIdentity = manager.createIdentity("Work", 0xFFFF00)
        
        assertEquals("Work", newIdentity.displayName)
        assertEquals(0xFFFF00, newIdentity.color)
        
        coVerify(exactly = 1) { mockIdentityRepo.createIdentity(any()) }
        coVerify(exactly = 1) { mockSettingsRepo.updateLastActiveIdentityId(newIdentity.id) }
    }
}
