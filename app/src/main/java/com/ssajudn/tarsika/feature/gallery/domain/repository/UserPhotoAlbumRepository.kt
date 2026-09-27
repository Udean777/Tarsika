package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import kotlinx.coroutines.flow.Flow

interface UserPhotoAlbumRepository {
    val albums: Flow<List<UserPhotoAlbum>>

    suspend fun create(
        name: String,
        relativePath: String,
        photoKeys: Set<String>,
    )

    suspend fun rename(
        id: String,
        name: String,
    )

    suspend fun delete(id: String)

    suspend fun addPhotos(
        albumId: String,
        photoKeys: Set<String>,
    )

    suspend fun removePhotos(
        albumId: String,
        photoKeys: Set<String>,
    )
}
