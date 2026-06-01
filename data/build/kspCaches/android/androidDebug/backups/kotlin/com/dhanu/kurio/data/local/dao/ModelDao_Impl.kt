package com.dhanu.kurio.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.dhanu.kurio.`data`.local.entity.ModelEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ModelDao_Impl(
  __db: RoomDatabase,
) : ModelDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfModelEntity: EntityInsertAdapter<ModelEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfModelEntity = object : EntityInsertAdapter<ModelEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `models` (`id`,`name`,`description`,`provider`,`license`,`language`,`sizeBytes`,`ramUsageMb`,`speedRating`,`accuracyRating`,`downloadUrl`,`checksum`,`version`,`recommendedDeviceClass`,`status`,`filePath`,`isExperimental`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ModelEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.provider)
        statement.bindText(5, entity.license)
        statement.bindText(6, entity.language)
        statement.bindLong(7, entity.sizeBytes)
        statement.bindLong(8, entity.ramUsageMb.toLong())
        statement.bindLong(9, entity.speedRating.toLong())
        statement.bindLong(10, entity.accuracyRating.toLong())
        statement.bindText(11, entity.downloadUrl)
        statement.bindText(12, entity.checksum)
        statement.bindText(13, entity.version)
        statement.bindText(14, entity.recommendedDeviceClass)
        statement.bindText(15, entity.status)
        val _tmpFilePath: String? = entity.filePath
        if (_tmpFilePath == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpFilePath)
        }
        val _tmp: Int = if (entity.isExperimental) 1 else 0
        statement.bindLong(17, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(model: ModelEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfModelEntity.insert(_connection, model)
  }

  public override suspend fun insertAll(models: List<ModelEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfModelEntity.insert(_connection, models)
  }

  public override fun observeAll(): Flow<List<ModelEntity>> {
    val _sql: String = "SELECT * FROM models"
    return createFlow(__db, false, arrayOf("models")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfLicense: Int = getColumnIndexOrThrow(_stmt, "license")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfRamUsageMb: Int = getColumnIndexOrThrow(_stmt, "ramUsageMb")
        val _columnIndexOfSpeedRating: Int = getColumnIndexOrThrow(_stmt, "speedRating")
        val _columnIndexOfAccuracyRating: Int = getColumnIndexOrThrow(_stmt, "accuracyRating")
        val _columnIndexOfDownloadUrl: Int = getColumnIndexOrThrow(_stmt, "downloadUrl")
        val _columnIndexOfChecksum: Int = getColumnIndexOrThrow(_stmt, "checksum")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfRecommendedDeviceClass: Int = getColumnIndexOrThrow(_stmt,
            "recommendedDeviceClass")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfIsExperimental: Int = getColumnIndexOrThrow(_stmt, "isExperimental")
        val _result: MutableList<ModelEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ModelEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpLicense: String
          _tmpLicense = _stmt.getText(_columnIndexOfLicense)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpRamUsageMb: Int
          _tmpRamUsageMb = _stmt.getLong(_columnIndexOfRamUsageMb).toInt()
          val _tmpSpeedRating: Int
          _tmpSpeedRating = _stmt.getLong(_columnIndexOfSpeedRating).toInt()
          val _tmpAccuracyRating: Int
          _tmpAccuracyRating = _stmt.getLong(_columnIndexOfAccuracyRating).toInt()
          val _tmpDownloadUrl: String
          _tmpDownloadUrl = _stmt.getText(_columnIndexOfDownloadUrl)
          val _tmpChecksum: String
          _tmpChecksum = _stmt.getText(_columnIndexOfChecksum)
          val _tmpVersion: String
          _tmpVersion = _stmt.getText(_columnIndexOfVersion)
          val _tmpRecommendedDeviceClass: String
          _tmpRecommendedDeviceClass = _stmt.getText(_columnIndexOfRecommendedDeviceClass)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpFilePath: String?
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          }
          val _tmpIsExperimental: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsExperimental).toInt()
          _tmpIsExperimental = _tmp != 0
          _item =
              ModelEntity(_tmpId,_tmpName,_tmpDescription,_tmpProvider,_tmpLicense,_tmpLanguage,_tmpSizeBytes,_tmpRamUsageMb,_tmpSpeedRating,_tmpAccuracyRating,_tmpDownloadUrl,_tmpChecksum,_tmpVersion,_tmpRecommendedDeviceClass,_tmpStatus,_tmpFilePath,_tmpIsExperimental)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeById(modelId: String): Flow<ModelEntity?> {
    val _sql: String = "SELECT * FROM models WHERE id = ?"
    return createFlow(__db, false, arrayOf("models")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, modelId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfLicense: Int = getColumnIndexOrThrow(_stmt, "license")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfRamUsageMb: Int = getColumnIndexOrThrow(_stmt, "ramUsageMb")
        val _columnIndexOfSpeedRating: Int = getColumnIndexOrThrow(_stmt, "speedRating")
        val _columnIndexOfAccuracyRating: Int = getColumnIndexOrThrow(_stmt, "accuracyRating")
        val _columnIndexOfDownloadUrl: Int = getColumnIndexOrThrow(_stmt, "downloadUrl")
        val _columnIndexOfChecksum: Int = getColumnIndexOrThrow(_stmt, "checksum")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfRecommendedDeviceClass: Int = getColumnIndexOrThrow(_stmt,
            "recommendedDeviceClass")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfIsExperimental: Int = getColumnIndexOrThrow(_stmt, "isExperimental")
        val _result: ModelEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpLicense: String
          _tmpLicense = _stmt.getText(_columnIndexOfLicense)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpRamUsageMb: Int
          _tmpRamUsageMb = _stmt.getLong(_columnIndexOfRamUsageMb).toInt()
          val _tmpSpeedRating: Int
          _tmpSpeedRating = _stmt.getLong(_columnIndexOfSpeedRating).toInt()
          val _tmpAccuracyRating: Int
          _tmpAccuracyRating = _stmt.getLong(_columnIndexOfAccuracyRating).toInt()
          val _tmpDownloadUrl: String
          _tmpDownloadUrl = _stmt.getText(_columnIndexOfDownloadUrl)
          val _tmpChecksum: String
          _tmpChecksum = _stmt.getText(_columnIndexOfChecksum)
          val _tmpVersion: String
          _tmpVersion = _stmt.getText(_columnIndexOfVersion)
          val _tmpRecommendedDeviceClass: String
          _tmpRecommendedDeviceClass = _stmt.getText(_columnIndexOfRecommendedDeviceClass)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpFilePath: String?
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          }
          val _tmpIsExperimental: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsExperimental).toInt()
          _tmpIsExperimental = _tmp != 0
          _result =
              ModelEntity(_tmpId,_tmpName,_tmpDescription,_tmpProvider,_tmpLicense,_tmpLanguage,_tmpSizeBytes,_tmpRamUsageMb,_tmpSpeedRating,_tmpAccuracyRating,_tmpDownloadUrl,_tmpChecksum,_tmpVersion,_tmpRecommendedDeviceClass,_tmpStatus,_tmpFilePath,_tmpIsExperimental)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(modelId: String): ModelEntity? {
    val _sql: String = "SELECT * FROM models WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, modelId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfLicense: Int = getColumnIndexOrThrow(_stmt, "license")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfRamUsageMb: Int = getColumnIndexOrThrow(_stmt, "ramUsageMb")
        val _columnIndexOfSpeedRating: Int = getColumnIndexOrThrow(_stmt, "speedRating")
        val _columnIndexOfAccuracyRating: Int = getColumnIndexOrThrow(_stmt, "accuracyRating")
        val _columnIndexOfDownloadUrl: Int = getColumnIndexOrThrow(_stmt, "downloadUrl")
        val _columnIndexOfChecksum: Int = getColumnIndexOrThrow(_stmt, "checksum")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfRecommendedDeviceClass: Int = getColumnIndexOrThrow(_stmt,
            "recommendedDeviceClass")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfIsExperimental: Int = getColumnIndexOrThrow(_stmt, "isExperimental")
        val _result: ModelEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpLicense: String
          _tmpLicense = _stmt.getText(_columnIndexOfLicense)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpRamUsageMb: Int
          _tmpRamUsageMb = _stmt.getLong(_columnIndexOfRamUsageMb).toInt()
          val _tmpSpeedRating: Int
          _tmpSpeedRating = _stmt.getLong(_columnIndexOfSpeedRating).toInt()
          val _tmpAccuracyRating: Int
          _tmpAccuracyRating = _stmt.getLong(_columnIndexOfAccuracyRating).toInt()
          val _tmpDownloadUrl: String
          _tmpDownloadUrl = _stmt.getText(_columnIndexOfDownloadUrl)
          val _tmpChecksum: String
          _tmpChecksum = _stmt.getText(_columnIndexOfChecksum)
          val _tmpVersion: String
          _tmpVersion = _stmt.getText(_columnIndexOfVersion)
          val _tmpRecommendedDeviceClass: String
          _tmpRecommendedDeviceClass = _stmt.getText(_columnIndexOfRecommendedDeviceClass)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpFilePath: String?
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          }
          val _tmpIsExperimental: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsExperimental).toInt()
          _tmpIsExperimental = _tmp != 0
          _result =
              ModelEntity(_tmpId,_tmpName,_tmpDescription,_tmpProvider,_tmpLicense,_tmpLanguage,_tmpSizeBytes,_tmpRamUsageMb,_tmpSpeedRating,_tmpAccuracyRating,_tmpDownloadUrl,_tmpChecksum,_tmpVersion,_tmpRecommendedDeviceClass,_tmpStatus,_tmpFilePath,_tmpIsExperimental)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<ModelEntity> {
    val _sql: String = "SELECT * FROM models"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfLicense: Int = getColumnIndexOrThrow(_stmt, "license")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfRamUsageMb: Int = getColumnIndexOrThrow(_stmt, "ramUsageMb")
        val _columnIndexOfSpeedRating: Int = getColumnIndexOrThrow(_stmt, "speedRating")
        val _columnIndexOfAccuracyRating: Int = getColumnIndexOrThrow(_stmt, "accuracyRating")
        val _columnIndexOfDownloadUrl: Int = getColumnIndexOrThrow(_stmt, "downloadUrl")
        val _columnIndexOfChecksum: Int = getColumnIndexOrThrow(_stmt, "checksum")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfRecommendedDeviceClass: Int = getColumnIndexOrThrow(_stmt,
            "recommendedDeviceClass")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfIsExperimental: Int = getColumnIndexOrThrow(_stmt, "isExperimental")
        val _result: MutableList<ModelEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ModelEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpLicense: String
          _tmpLicense = _stmt.getText(_columnIndexOfLicense)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpRamUsageMb: Int
          _tmpRamUsageMb = _stmt.getLong(_columnIndexOfRamUsageMb).toInt()
          val _tmpSpeedRating: Int
          _tmpSpeedRating = _stmt.getLong(_columnIndexOfSpeedRating).toInt()
          val _tmpAccuracyRating: Int
          _tmpAccuracyRating = _stmt.getLong(_columnIndexOfAccuracyRating).toInt()
          val _tmpDownloadUrl: String
          _tmpDownloadUrl = _stmt.getText(_columnIndexOfDownloadUrl)
          val _tmpChecksum: String
          _tmpChecksum = _stmt.getText(_columnIndexOfChecksum)
          val _tmpVersion: String
          _tmpVersion = _stmt.getText(_columnIndexOfVersion)
          val _tmpRecommendedDeviceClass: String
          _tmpRecommendedDeviceClass = _stmt.getText(_columnIndexOfRecommendedDeviceClass)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpFilePath: String?
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          }
          val _tmpIsExperimental: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsExperimental).toInt()
          _tmpIsExperimental = _tmp != 0
          _item =
              ModelEntity(_tmpId,_tmpName,_tmpDescription,_tmpProvider,_tmpLicense,_tmpLanguage,_tmpSizeBytes,_tmpRamUsageMb,_tmpSpeedRating,_tmpAccuracyRating,_tmpDownloadUrl,_tmpChecksum,_tmpVersion,_tmpRecommendedDeviceClass,_tmpStatus,_tmpFilePath,_tmpIsExperimental)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActive(): ModelEntity? {
    val _sql: String = "SELECT * FROM models WHERE status = 'ACTIVE' LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfProvider: Int = getColumnIndexOrThrow(_stmt, "provider")
        val _columnIndexOfLicense: Int = getColumnIndexOrThrow(_stmt, "license")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfSizeBytes: Int = getColumnIndexOrThrow(_stmt, "sizeBytes")
        val _columnIndexOfRamUsageMb: Int = getColumnIndexOrThrow(_stmt, "ramUsageMb")
        val _columnIndexOfSpeedRating: Int = getColumnIndexOrThrow(_stmt, "speedRating")
        val _columnIndexOfAccuracyRating: Int = getColumnIndexOrThrow(_stmt, "accuracyRating")
        val _columnIndexOfDownloadUrl: Int = getColumnIndexOrThrow(_stmt, "downloadUrl")
        val _columnIndexOfChecksum: Int = getColumnIndexOrThrow(_stmt, "checksum")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfRecommendedDeviceClass: Int = getColumnIndexOrThrow(_stmt,
            "recommendedDeviceClass")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfIsExperimental: Int = getColumnIndexOrThrow(_stmt, "isExperimental")
        val _result: ModelEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpProvider: String
          _tmpProvider = _stmt.getText(_columnIndexOfProvider)
          val _tmpLicense: String
          _tmpLicense = _stmt.getText(_columnIndexOfLicense)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpSizeBytes: Long
          _tmpSizeBytes = _stmt.getLong(_columnIndexOfSizeBytes)
          val _tmpRamUsageMb: Int
          _tmpRamUsageMb = _stmt.getLong(_columnIndexOfRamUsageMb).toInt()
          val _tmpSpeedRating: Int
          _tmpSpeedRating = _stmt.getLong(_columnIndexOfSpeedRating).toInt()
          val _tmpAccuracyRating: Int
          _tmpAccuracyRating = _stmt.getLong(_columnIndexOfAccuracyRating).toInt()
          val _tmpDownloadUrl: String
          _tmpDownloadUrl = _stmt.getText(_columnIndexOfDownloadUrl)
          val _tmpChecksum: String
          _tmpChecksum = _stmt.getText(_columnIndexOfChecksum)
          val _tmpVersion: String
          _tmpVersion = _stmt.getText(_columnIndexOfVersion)
          val _tmpRecommendedDeviceClass: String
          _tmpRecommendedDeviceClass = _stmt.getText(_columnIndexOfRecommendedDeviceClass)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpFilePath: String?
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          }
          val _tmpIsExperimental: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsExperimental).toInt()
          _tmpIsExperimental = _tmp != 0
          _result =
              ModelEntity(_tmpId,_tmpName,_tmpDescription,_tmpProvider,_tmpLicense,_tmpLanguage,_tmpSizeBytes,_tmpRamUsageMb,_tmpSpeedRating,_tmpAccuracyRating,_tmpDownloadUrl,_tmpChecksum,_tmpVersion,_tmpRecommendedDeviceClass,_tmpStatus,_tmpFilePath,_tmpIsExperimental)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(modelId: String) {
    val _sql: String = "DELETE FROM models WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, modelId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateStatus(modelId: String, status: String) {
    val _sql: String = "UPDATE models SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, modelId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateFilePath(modelId: String, filePath: String?) {
    val _sql: String = "UPDATE models SET filePath = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (filePath == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, filePath)
        }
        _argIndex = 2
        _stmt.bindText(_argIndex, modelId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun resetStatus(modelId: String) {
    val _sql: String = "UPDATE models SET status = 'NOT_DOWNLOADED' WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, modelId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
