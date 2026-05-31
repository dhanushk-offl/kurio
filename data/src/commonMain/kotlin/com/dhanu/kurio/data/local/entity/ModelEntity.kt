package com.dhanu.kurio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.model.DeviceClass
import com.dhanu.kurio.core.model.ModelStatus

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val license: String,
    val language: String,
    val sizeBytes: Long,
    val ramUsageMb: Int,
    val speedRating: Int,
    val accuracyRating: Int,
    val downloadUrl: String,
    val checksum: String,
    val version: String,
    val recommendedDeviceClass: String,
    val status: String,
    val filePath: String?,
    val isExperimental: Boolean
) {
    fun toDomain(): SpeechModel = SpeechModel(
        id = id,
        name = name,
        description = description,
        provider = provider,
        license = license,
        language = language,
        sizeBytes = sizeBytes,
        ramUsageMb = ramUsageMb,
        speedRating = speedRating,
        accuracyRating = accuracyRating,
        downloadUrl = downloadUrl,
        checksum = checksum,
        version = version,
        recommendedDeviceClass = DeviceClass.valueOf(recommendedDeviceClass),
        status = ModelStatus.valueOf(status),
        isExperimental = isExperimental
    )

    companion object {
        fun fromDomain(model: SpeechModel, filePath: String? = null): ModelEntity = ModelEntity(
            id = model.id,
            name = model.name,
            description = model.description,
            provider = model.provider,
            license = model.license,
            language = model.language,
            sizeBytes = model.sizeBytes,
            ramUsageMb = model.ramUsageMb,
            speedRating = model.speedRating,
            accuracyRating = model.accuracyRating,
            downloadUrl = model.downloadUrl,
            checksum = model.checksum,
            version = model.version,
            recommendedDeviceClass = model.recommendedDeviceClass.name,
            status = model.status.name,
            filePath = filePath,
            isExperimental = model.isExperimental
        )
    }
}
