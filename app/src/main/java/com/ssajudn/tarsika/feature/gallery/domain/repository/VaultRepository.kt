package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.DecryptedVaultPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

interface VaultRepository {
    val photos: Flow<List<VaultPhoto>>

    fun ensureKey()

    suspend fun import(
        sourceUri: String,
        name: String,
        sizeBytes: Long,
    )

    suspend fun read(id: String): DecryptedVaultPhoto

    suspend fun delete(id: String)

    suspend fun writePlainTo(
        id: String,
        output: OutputStream,
        maxBytes: Long = Long.MAX_VALUE,
    )

    fun estimatedBytes(): Flow<Long>
}
