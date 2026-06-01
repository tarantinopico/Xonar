package com.tarantino.xonarx.domain.usecase

import com.tarantino.xonarx.domain.model.Identity
import com.tarantino.xonarx.domain.repository.IdentityRepository
import com.tarantino.xonarx.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the core concept of Xonar: Identity Isolation.
 * 
 * An [Identity] acts as an independent session bucket. To ensure absolute data isolation,
 * Xonar relies on `ProfileStore` to segregate WebView persistent data per identity.
 * 
 * Invariants:
 * 1. Data belonging to Identity A MUST NOT be accessible when Identity B is active.
 * 2. Active WebView allocations should use the active identity's ID to fetch the correct profile.
 */
@Singleton
class IdentityManager @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository
) {
    val allIdentities: Flow<List<Identity>> = identityRepository.getAllIdentities()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeIdentity: Flow<Identity?> = settingsRepository.preferences
        .map { it.lastActiveIdentityId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else identityRepository.observeIdentityById(id)
        }

    suspend fun createIdentity(displayName: String, color: Int, defaultSearchEngine: String = "Google"): Identity {
        val newIdentity = Identity(
            id = UUID.randomUUID().toString(),
            displayName = displayName,
            color = color,
            iconName = null,
            defaultSearchEngine = defaultSearchEngine,
            privacyFlags = 0,
            lockSettings = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            orderIndex = 0,
            isIncognitoTemplate = false
        )
        identityRepository.createIdentity(newIdentity)
        if (settingsRepository.preferences.first().lastActiveIdentityId == null) {
            settingsRepository.updateLastActiveIdentityId(newIdentity.id)
        }
        return newIdentity
    }

    suspend fun switchIdentity(identityId: String) {
        settingsRepository.updateLastActiveIdentityId(identityId)
    }

    suspend fun deleteIdentity(identity: Identity) {
        identityRepository.deleteIdentity(identity)
        val prefs = settingsRepository.preferences.first()
        if (prefs.lastActiveIdentityId == identity.id) {
            val remain = identityRepository.getAllIdentities().first()
            settingsRepository.updateLastActiveIdentityId(remain.firstOrNull()?.id)
        }
    }
}
