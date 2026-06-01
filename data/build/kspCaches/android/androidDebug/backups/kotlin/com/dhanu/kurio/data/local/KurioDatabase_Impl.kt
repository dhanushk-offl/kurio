package com.dhanu.kurio.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.dhanu.kurio.`data`.local.dao.HistoryDao
import com.dhanu.kurio.`data`.local.dao.HistoryDao_Impl
import com.dhanu.kurio.`data`.local.dao.ModelDao
import com.dhanu.kurio.`data`.local.dao.ModelDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class KurioDatabase_Impl : KurioDatabase() {
  private val _historyDao: Lazy<HistoryDao> = lazy {
    HistoryDao_Impl(this)
  }

  private val _modelDao: Lazy<ModelDao> = lazy {
    ModelDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "01fefac51eb1c76d8926f299d436d950", "566e335cbcaf486c27d34fc2767587d6") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `history` (`id` TEXT NOT NULL, `text` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `wordCount` INTEGER NOT NULL, `characterCount` INTEGER NOT NULL, `modelId` TEXT NOT NULL, `language` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `models` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `provider` TEXT NOT NULL, `license` TEXT NOT NULL, `language` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `ramUsageMb` INTEGER NOT NULL, `speedRating` INTEGER NOT NULL, `accuracyRating` INTEGER NOT NULL, `downloadUrl` TEXT NOT NULL, `checksum` TEXT NOT NULL, `version` TEXT NOT NULL, `recommendedDeviceClass` TEXT NOT NULL, `status` TEXT NOT NULL, `filePath` TEXT, `isExperimental` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '01fefac51eb1c76d8926f299d436d950')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `history`")
        connection.execSQL("DROP TABLE IF EXISTS `models`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHistory.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("text", TableInfo.Column("text", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("dateMillis", TableInfo.Column("dateMillis", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("wordCount", TableInfo.Column("wordCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("characterCount", TableInfo.Column("characterCount", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("modelId", TableInfo.Column("modelId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsHistory.put("language", TableInfo.Column("language", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoHistory: TableInfo = TableInfo("history", _columnsHistory, _foreignKeysHistory,
            _indicesHistory)
        val _existingHistory: TableInfo = read(connection, "history")
        if (!_infoHistory.equals(_existingHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |history(com.dhanu.kurio.data.local.entity.HistoryEntity).
              | Expected:
              |""".trimMargin() + _infoHistory + """
              |
              | Found:
              |""".trimMargin() + _existingHistory)
        }
        val _columnsModels: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsModels.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("provider", TableInfo.Column("provider", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("license", TableInfo.Column("license", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("language", TableInfo.Column("language", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("sizeBytes", TableInfo.Column("sizeBytes", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("ramUsageMb", TableInfo.Column("ramUsageMb", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("speedRating", TableInfo.Column("speedRating", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("accuracyRating", TableInfo.Column("accuracyRating", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("downloadUrl", TableInfo.Column("downloadUrl", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("checksum", TableInfo.Column("checksum", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("version", TableInfo.Column("version", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("recommendedDeviceClass", TableInfo.Column("recommendedDeviceClass",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("filePath", TableInfo.Column("filePath", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsModels.put("isExperimental", TableInfo.Column("isExperimental", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysModels: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesModels: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoModels: TableInfo = TableInfo("models", _columnsModels, _foreignKeysModels,
            _indicesModels)
        val _existingModels: TableInfo = read(connection, "models")
        if (!_infoModels.equals(_existingModels)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |models(com.dhanu.kurio.data.local.entity.ModelEntity).
              | Expected:
              |""".trimMargin() + _infoModels + """
              |
              | Found:
              |""".trimMargin() + _existingModels)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "history", "models")
  }

  public override fun clearAllTables() {
    super.performClear(false, "history", "models")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(HistoryDao::class, HistoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ModelDao::class, ModelDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun historyDao(): HistoryDao = _historyDao.value

  public override fun modelDao(): ModelDao = _modelDao.value
}
