package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashRepository
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashMaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class RoomLocalTrashRepository(
    private val resolver: ContentResolver,
    private val dao: LocalTrashDao,
    private val directory: File,
) : LocalTrashRepository, LocalTrashMaintenanceRepository {
    override val localEntries: Flow<List<LocalTrashPhoto>> =
        dao.observeAll().map { rows ->
            rows.map {
                LocalTrashPhoto(
                    it.id,
                    it.displayName,
                    it.mimeType,
                    it.sizeBytes,
                    it.trashedAtMillis,
                    File(directory, it.backupFileName).toUri().toString(),
                )
            }
        }

    init {
        directory.mkdirs()
    }

    private suspend fun backup(
        uri: Uri,
        displayName: String,
        mimeType: String,
        sizeBytes: Long,
    ): LocalTrashEntity =
        withContext(Dispatchers.IO) {
            val name = "${UUID.randomUUID()}.trash"
            val file = File(directory, name)
            val input = resolver.openInputStream(uri) ?: error("Photo is no longer available")
            try {
                input.use { source -> file.outputStream().use { source.copyTo(it) } }
                LocalTrashEntity(
                    UUID.randomUUID().toString(),
                    uri.toString(),
                    displayName,
                    mimeType,
                    sizeBytes.coerceAtLeast(0L),
                    System.currentTimeMillis(),
                    name,
                )
            } catch (error: Throwable) {
                file.delete()
                throw error
            }
        }

    override suspend fun backupAndRecord(
        uri: String,
        displayName: String,
        mimeType: String,
        sizeBytes: Long,
    ): LocalTrashPhoto {
        val sourceUri = uri.toUri()
        val entry = backup(sourceUri, displayName, mimeType, sizeBytes)
        return try {
            dao.insert(entry)
            LocalTrashPhoto(
                entry.id,
                entry.displayName,
                entry.mimeType,
                entry.sizeBytes,
                entry.trashedAtMillis,
                File(directory, entry.backupFileName).toUri().toString(),
            )
        } catch (error: Throwable) {
            File(directory, entry.backupFileName).delete()
            throw error
        }
    }

    override suspend fun discardBackup(id: String) =
        withContext(Dispatchers.IO) {
            dao.findById(id)?.let { entry ->
                dao.delete(id)
                File(directory, entry.backupFileName).delete()
            }
            Unit
        }

    override suspend fun restore(
        entry: LocalTrashPhoto,
        destinationUri: String,
    ) = withContext(Dispatchers.IO) {
        val destination = destinationUri.toUri()
        val storedEntry = dao.findById(entry.id) ?: error("Trash item is no longer available")
        val input = File(directory, storedEntry.backupFileName).inputStream()
        val output = resolver.openOutputStream(destination, "w") ?: error("Unable to open destination")
        input.use { source -> output.use { source.copyTo(it) } }
        dao.delete(entry.id)
        File(directory, storedEntry.backupFileName).delete()
        Unit
    }

    override suspend fun deleteForever(entry: LocalTrashPhoto) =
        withContext(Dispatchers.IO) {
            val storedEntry = dao.findById(entry.id) ?: return@withContext
            dao.delete(entry.id)
            File(directory, storedEntry.backupFileName).delete()
            Unit
        }

    override suspend fun deleteForever(id: String) =
        withContext(Dispatchers.IO) {
            dao.findById(id)?.let { entry ->
                dao.delete(id)
                File(directory, entry.backupFileName).delete()
            }
            Unit
        }

    override suspend fun deleteAllForever() =
        withContext(Dispatchers.IO) {
            val entries = dao.findAll()
            deleteBackupFiles(entries)
            dao.deleteAll()
            Unit
        }

    override suspend fun deleteExpiredBefore(trashedBeforeMillis: Long) =
        withContext(Dispatchers.IO) {
            val entries = dao.findExpired(trashedBeforeMillis)
            deleteBackupFiles(entries)
            dao.deleteExpired(trashedBeforeMillis)
            Unit
        }

    private fun deleteBackupFiles(entries: List<LocalTrashEntity>) {
        entries.forEach { entry ->
            val backup = File(directory, entry.backupFileName)
            check(!backup.exists() || backup.delete()) { "Unable to permanently remove a trash item." }
        }
    }
}
