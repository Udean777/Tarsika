package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveDevicePhotosToLocalTrashUseCaseTest {
    @Test
    fun `backs up before deleting and rolls back when delete is denied`() =
        runBlocking {
            val repository = FakeTrashRepository()
            val useCase = MoveDevicePhotosToLocalTrashUseCase(repository)
            val photo = photo()

            val result =
                useCase(listOf(photo)) { uri ->
                    repository.events += "delete:$uri"
                    false
                }

            assertEquals(emptySet<String>(), result.movedPhotoKeys)
            assertEquals(setOf(photoKey(photo)), result.notMovedPhotoKeys)
            assertEquals(listOf("backup:${photo.uri}", "delete:${photo.uri}", "discard:trash-id"), repository.events)
        }

    @Test
    fun `does not duplicate source backups for repeated photos`() =
        runBlocking {
            val repository = FakeTrashRepository()
            val photo = photo()

            val result = MoveDevicePhotosToLocalTrashUseCase(repository)(listOf(photo, photo)) { true }

            assertEquals(setOf(photoKey(photo)), result.movedPhotoKeys)
            assertEquals(1, repository.events.count { it.startsWith("backup:") })
            assertTrue(repository.events.none { it.startsWith("discard:") })
        }

    @Test(expected = CancellationException::class)
    fun `cancellation is propagated after rolling back backup`() {
        runBlocking {
            val repository = FakeTrashRepository()
            try {
                MoveDevicePhotosToLocalTrashUseCase(repository)(listOf(photo())) { throw CancellationException() }
            } finally {
                assertEquals("discard:trash-id", repository.events.last())
            }
        }
    }

    private fun photo() = DevicePhoto(1, "content://photo/1", "photo.jpg", 1, width = 10, height = 10)

    private fun photoKey(photo: DevicePhoto) = photo.favoriteKey()

    private class FakeTrashRepository : LocalTrashRepository {
        val events = mutableListOf<String>()
        override val localEntries: Flow<List<LocalTrashPhoto>> = emptyFlow()

        override suspend fun backupAndRecord(
            uri: String,
            displayName: String,
            mimeType: String,
            sizeBytes: Long,
        ): LocalTrashPhoto {
            events += "backup:$uri"
            return LocalTrashPhoto("trash-id", displayName, mimeType, sizeBytes, 0L)
        }

        override suspend fun discardBackup(id: String) {
            events += "discard:$id"
        }

        override suspend fun restore(
            entry: LocalTrashPhoto,
            destinationUri: String,
        ) = Unit

        override suspend fun deleteForever(entry: LocalTrashPhoto) = Unit

        override suspend fun deleteForever(id: String) = Unit
    }
}
