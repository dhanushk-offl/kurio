package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.HistoryEntry
import com.dhanu.kurio.data.local.dao.HistoryDao
import com.dhanu.kurio.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryRepositoryImplTest {

    @Test
    fun `given history entries in dao when getHistory then returns domain models`() = runTest {
        val dao = HistoryDaoMock(entities = listOf(
            HistoryEntity("1", "Hello world", 1000L, 5000L, 2, 11, "whisper-tiny-en", "en"),
            HistoryEntity("2", "Test transcription", 2000L, 3000L, 2, 18, "whisper-tiny-en", "en")
        ))
        val repository = HistoryRepositoryImpl(dao)

        val history = repository.getHistory()

        assertEquals(2, history.size)
        assertEquals("Hello world", history[0].text)
        assertEquals("Test transcription", history[1].text)
    }

    @Test
    fun `given empty dao when getHistory then returns empty list`() = runTest {
        val dao = HistoryDaoMock(entities = emptyList())
        val repository = HistoryRepositoryImpl(dao)

        val history = repository.getHistory()

        assertEquals(0, history.size)
    }
}

class HistoryDaoMock(
    private val entities: List<HistoryEntity>
) : HistoryDao {
    override fun observeAll() = flowOf(entities)

    override suspend fun getAll(): List<HistoryEntity> = entities

    override suspend fun getById(id: String): HistoryEntity? = entities.find { it.id == id }

    override suspend fun insert(entry: HistoryEntity) {}

    override suspend fun delete(entry: HistoryEntity) {}

    override suspend fun deleteById(id: String) {}

    override suspend fun clearAll() {}

    override fun observeCount() = flowOf(entities.size)

    override suspend fun search(query: String): List<HistoryEntity> =
        entities.filter { it.text.contains(query, ignoreCase = true) }

    override suspend fun trimToSize(limit: Int) {}
}
