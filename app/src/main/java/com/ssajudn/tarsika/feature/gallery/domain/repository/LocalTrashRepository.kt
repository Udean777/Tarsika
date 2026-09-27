package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import kotlinx.coroutines.flow.Flow

interface LocalTrashRepository {
    val localEntries: Flow<List<LocalTrashPhoto>>

    suspend fun backupAndRecord(
        uri: String,
        displayName: String,
        mimeType: String,
        sizeBytes: Long,
    ): LocalTrashPhoto

    suspend fun discardBackup(id: String)

    suspend fun restore(
        entry: LocalTrashPhoto,
        destinationUri: String,
    )

    suspend fun deleteForever(entry: LocalTrashPhoto)

    suspend fun deleteForever(id: String)
}
