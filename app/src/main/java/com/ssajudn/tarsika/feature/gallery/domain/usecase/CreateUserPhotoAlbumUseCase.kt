package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository

class CreateUserPhotoAlbumUseCase(private val repository: UserPhotoAlbumRepository) {
    suspend operator fun invoke(name: String) {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "Album name cannot be empty." }
        repository.create(cleanName)
    }
}
