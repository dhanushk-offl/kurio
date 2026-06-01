package com.dhanu.kurio.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.dhanu.kurio.`data`.local.entity.HistoryEntity
import javax.`annotation`.processing.Generated
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
public class HistoryDao_Impl(
  __db: RoomDatabase,
) : HistoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfHistoryEntity: EntityInsertAdapter<HistoryEntity>

  private val __deleteAdapterOfHistoryEntity: EntityDeleteOrUpdateAdapter<HistoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfHistoryEntity = object : EntityInsertAdapter<HistoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `history` (`id`,`text`,`dateMillis`,`durationMs`,`wordCount`,`characterCount`,`modelId`,`language`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HistoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.text)
        statement.bindLong(3, entity.dateMillis)
        statement.bindLong(4, entity.durationMs)
        statement.bindLong(5, entity.wordCount.toLong())
        statement.bindLong(6, entity.characterCount.toLong())
        statement.bindText(7, entity.modelId)
        statement.bindText(8, entity.language)
      }
    }
    this.__deleteAdapterOfHistoryEntity = object : EntityDeleteOrUpdateAdapter<HistoryEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `history` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HistoryEntity) {
        statement.bindText(1, entity.id)
      }
    }
  }

  public override suspend fun insert(entry: HistoryEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfHistoryEntity.insert(_connection, entry)
  }

  public override suspend fun delete(entry: HistoryEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfHistoryEntity.handle(_connection, entry)
  }

  public override fun observeAll(): Flow<List<HistoryEntity>> {
    val _sql: String = "SELECT * FROM history ORDER BY dateMillis DESC"
    return createFlow(__db, false, arrayOf("history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfCharacterCount: Int = getColumnIndexOrThrow(_stmt, "characterCount")
        val _columnIndexOfModelId: Int = getColumnIndexOrThrow(_stmt, "modelId")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: MutableList<HistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HistoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpCharacterCount: Int
          _tmpCharacterCount = _stmt.getLong(_columnIndexOfCharacterCount).toInt()
          val _tmpModelId: String
          _tmpModelId = _stmt.getText(_columnIndexOfModelId)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _item =
              HistoryEntity(_tmpId,_tmpText,_tmpDateMillis,_tmpDurationMs,_tmpWordCount,_tmpCharacterCount,_tmpModelId,_tmpLanguage)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<HistoryEntity> {
    val _sql: String = "SELECT * FROM history ORDER BY dateMillis DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfCharacterCount: Int = getColumnIndexOrThrow(_stmt, "characterCount")
        val _columnIndexOfModelId: Int = getColumnIndexOrThrow(_stmt, "modelId")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: MutableList<HistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HistoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpCharacterCount: Int
          _tmpCharacterCount = _stmt.getLong(_columnIndexOfCharacterCount).toInt()
          val _tmpModelId: String
          _tmpModelId = _stmt.getText(_columnIndexOfModelId)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _item =
              HistoryEntity(_tmpId,_tmpText,_tmpDateMillis,_tmpDurationMs,_tmpWordCount,_tmpCharacterCount,_tmpModelId,_tmpLanguage)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): HistoryEntity? {
    val _sql: String = "SELECT * FROM history WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfCharacterCount: Int = getColumnIndexOrThrow(_stmt, "characterCount")
        val _columnIndexOfModelId: Int = getColumnIndexOrThrow(_stmt, "modelId")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: HistoryEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpCharacterCount: Int
          _tmpCharacterCount = _stmt.getLong(_columnIndexOfCharacterCount).toInt()
          val _tmpModelId: String
          _tmpModelId = _stmt.getText(_columnIndexOfModelId)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _result =
              HistoryEntity(_tmpId,_tmpText,_tmpDateMillis,_tmpDurationMs,_tmpWordCount,_tmpCharacterCount,_tmpModelId,_tmpLanguage)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM history"
    return createFlow(__db, false, arrayOf("history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun search(query: String): List<HistoryEntity> {
    val _sql: String =
        "SELECT * FROM history WHERE text LIKE '%' || ? || '%' ORDER BY dateMillis DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfWordCount: Int = getColumnIndexOrThrow(_stmt, "wordCount")
        val _columnIndexOfCharacterCount: Int = getColumnIndexOrThrow(_stmt, "characterCount")
        val _columnIndexOfModelId: Int = getColumnIndexOrThrow(_stmt, "modelId")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: MutableList<HistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HistoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpWordCount: Int
          _tmpWordCount = _stmt.getLong(_columnIndexOfWordCount).toInt()
          val _tmpCharacterCount: Int
          _tmpCharacterCount = _stmt.getLong(_columnIndexOfCharacterCount).toInt()
          val _tmpModelId: String
          _tmpModelId = _stmt.getText(_columnIndexOfModelId)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _item =
              HistoryEntity(_tmpId,_tmpText,_tmpDateMillis,_tmpDurationMs,_tmpWordCount,_tmpCharacterCount,_tmpModelId,_tmpLanguage)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String) {
    val _sql: String = "DELETE FROM history WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM history"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun trimToSize(limit: Int) {
    val _sql: String =
        "DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY dateMillis DESC LIMIT ?)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
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
