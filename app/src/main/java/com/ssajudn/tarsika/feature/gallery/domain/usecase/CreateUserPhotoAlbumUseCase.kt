package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository

class CreateUserPhotoAlbumUseCase(private val repository: UserPhotoAlbumRepository) {
    suspend operator fun invoke(
        name: String,
        parentRelativePath: String,
        photoKeys: Set<String>,
    ) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Album name cannot be empty." }
        require(cleanName.none { it == '/' || it == '\\' }) { "Album name cannot contain a path separator." }
        require(photoKeys.isNotEmpty()) { "Select at least one photo for the album." }
        val cleanParentPath = parentRelativePath.trim().trim('/')
        require(cleanParentPath.isNotEmpty()) { "Choose a folder for the album." }
        repository.create(cleanName, "$cleanParentPath/$cleanName/", photoKeys)
    }
}
