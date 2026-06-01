package com.dhanu.kurio.data.update

import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.data.remote.dto.UpdateCheckResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class UpdateManagerImplTest {

    @Test
    fun `given newer version available on stable channel when checkForUpdates then returns hasUpdate true`() = runTest {
        val mockApi = KurioApiMock(
            UpdateCheckResponse(
                version = "1.1.0",
                title = "New Update",
                critical = false,
                description = "Bug fixes and new models",
                features = listOf("Offline Moonshine support"),
                releaseDate = 1000L,
                downloadUrl = "https://example.com/kurio.apk"
            )
        )
        val updateManager = UpdateManagerImpl(mockApi)

        val result = updateManager.checkForUpdates("1.0.0", ReleaseChannel.STABLE)

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow()
        assertTrue(checkResult.hasUpdate)
        assertEquals("1.1.0", checkResult.latestVersion?.version)
        assertEquals("New Update", checkResult.latestVersion?.title)
    }

    @Test
    fun `given same version available on stable channel when checkForUpdates then returns hasUpdate false`() = runTest {
        val mockApi = KurioApiMock(
            UpdateCheckResponse(
                version = "1.0.0",
                title = "Current Version",
                critical = false,
                description = "Up to date",
                features = emptyList(),
                releaseDate = 1000L,
                downloadUrl = null
            )
        )
        val updateManager = UpdateManagerImpl(mockApi)

        val result = updateManager.checkForUpdates("1.0.0", ReleaseChannel.STABLE)

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow()
        assertFalse(checkResult.hasUpdate)
    }
}

class KurioApiMock(
    private val expectedResponse: UpdateCheckResponse
) : KurioApi(io.ktor.client.HttpClient()) {
    override suspend fun checkForUpdate(currentVersion: String, channel: String): UpdateCheckResponse {
        return expectedResponse
    }
}
