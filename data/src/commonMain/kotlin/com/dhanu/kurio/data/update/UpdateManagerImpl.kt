package com.dhanu.kurio.data.update

import com.dhanu.kurio.core.model.AppVersion
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.model.UpdateCheckResult
import com.dhanu.kurio.core.util.TimeUtils
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.domain.repository.UpdateManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UpdateManagerImpl(
    private val api: KurioApi
) : UpdateManager {

    override suspend fun checkForUpdates(
        currentVersion: String,
        channel: ReleaseChannel
    ): Result<UpdateCheckResult> = withContext(Dispatchers.IO) {
        try {
            val apiChannel = when (channel) {
                ReleaseChannel.STABLE -> "stable"
                ReleaseChannel.BETA -> "beta"
                ReleaseChannel.DEVELOPER_PREVIEW -> "dev"
            }

            Napier.d(tag = "UpdateManager") { "Checking for updates on channel: $apiChannel. Current: $currentVersion" }
            val response = api.checkForUpdate(currentVersion, apiChannel)

            val hasUpdate = isNewerVersion(currentVersion, response.version)

            val latestVersion = AppVersion(
                version = response.version,
                title = response.title,
                critical = response.critical,
                description = response.description,
                features = response.features,
                releaseDate = response.releaseDate,
                downloadUrl = response.downloadUrl
            )

            val checkResult = UpdateCheckResult(
                hasUpdate = hasUpdate,
                latestVersion = latestVersion,
                announcements = emptyList(),
                checkTimestamp = TimeUtils.now()
            )

            Napier.d(tag = "UpdateManager") { "Update check completed. Has update: $hasUpdate, Latest: ${response.version}" }
            Result.success(checkResult)
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "UpdateManager") { "Failed to check for updates" }
            Result.failure(e)
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        return try {
            val currentParts = current.split("-").first().split(".").map { it.toInt() }
            val latestParts = latest.split("-").first().split(".").map { it.toInt() }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val currentVal = currentParts.getOrNull(i) ?: 0
                val latestVal = latestParts.getOrNull(i) ?: 0
                if (latestVal > currentVal) return true
                if (currentVal > latestVal) return false
            }
            false
        } catch (_: Exception) {
            // Fallback: direct string comparison
            current != latest
        }
    }
}
