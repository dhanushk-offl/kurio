package com.dhanu.kurio.domain.repository

import com.dhanu.kurio.core.model.UpdateCheckResult
import com.dhanu.kurio.core.model.ReleaseChannel

interface UpdateManager {
    /**
     * Checks the cloud releases server for updates targeting the specified release channel.
     */
    suspend fun checkForUpdates(currentVersion: String, channel: ReleaseChannel): Result<UpdateCheckResult>
}
