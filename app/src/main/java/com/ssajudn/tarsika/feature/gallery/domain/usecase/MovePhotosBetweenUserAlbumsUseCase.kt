package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository

class MovePhotosBetweenUserAlbumsUseCase(private val repository: UserPhotoAlbumRepository) {
    suspend operator fun invoke(
        photoKeys: Set<String>,
        sourceAlbumId: String?,
        destinationAlbumId: String,
    ) {
        require(photoKeys.isNotEmpty()) { "Select at least one photo." }
        require(sourceAlbumId != destinationAlbumId) { "Source and destination albums must differ." }
        repository.addPhotos(destinationAlbumId, photoKeys)
        sourceAlbumId?.let { repository.removePhotos(it, photoKeys) }
    }
}
