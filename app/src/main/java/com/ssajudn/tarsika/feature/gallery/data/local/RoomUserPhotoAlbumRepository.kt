package com.ssajudn.tarsika.feature.gallery.data.local

import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class RoomUserPhotoAlbumRepository(private val dao: UserAlbumDao) : UserPhotoAlbumRepository {
    override val albums: Flow<List<UserPhotoAlbum>> =
        combine(dao.observeAlbums(), dao.observeMemberships()) { albums, memberships ->
            albums.map { album ->
                UserPhotoAlbum(
                    album.id,
                    album.name,
                    album.createdAtMillis,
                    memberships.filter {
                        it.albumId == album.id
                    }.mapTo(mutableSetOf()) { it.photoKey },
                    album.relativePath,
                )
            }
        }

    override suspend fun create(
        name: String,
        relativePath: String,
        photoKeys: Set<String>,
    ) {
        val cleanName = name.trim()
        val cleanPath = relativePath.trim().trim('/')
        require(cleanName.isNotEmpty())
        require(cleanPath.isNotEmpty())
        require(photoKeys.isNotEmpty())
        val albumId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        dao.insertAlbumWithPhotos(
            UserAlbumEntity(albumId, cleanName, now, "$cleanPath/"),
            photoKeys.map { UserAlbumPhotoEntity(albumId, it, now) },
        )
    }

    override suspend fun rename(
        id: String,
        name: String,
    ) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty())
        dao.renameAlbum(id, cleanName)
    }

    override suspend fun delete(id: String) = dao.deleteAlbum(id)

    override suspend fun addPhotos(
        albumId: String,
        photoKeys: Set<String>,
    ) {
        val now = System.currentTimeMillis()
        dao.addPhotos(photoKeys.map { UserAlbumPhotoEntity(albumId, it, now) })
    }

    override suspend fun removePhotos(
        albumId: String,
        photoKeys: Set<String>,
    ) = dao.removePhotos(albumId, photoKeys.toList())
}
