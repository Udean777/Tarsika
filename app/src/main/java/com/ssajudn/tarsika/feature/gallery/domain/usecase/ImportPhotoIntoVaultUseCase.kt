package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.repository.VaultRepository

class ImportPhotoIntoVaultUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(
        sourceUri: String,
        name: String,
        sizeBytes: Long,
    ) {
        require(sourceUri.isNotBlank()) { "Photo URI cannot be empty." }
        val cleanName = name.trim().take(MAX_NAME_LENGTH).ifBlank { DEFAULT_NAME }
        require(sizeBytes <= MAX_DECLARED_SIZE_BYTES || sizeBytes <= 0L) { "This image is larger than the vault import limit." }
        repository.import(sourceUri, cleanName, sizeBytes)
    }

    private companion object {
        const val MAX_NAME_LENGTH = 255
        const val MAX_DECLARED_SIZE_BYTES = 100L * 1024L * 1024L
        const val DEFAULT_NAME = "photo"
    }
}
