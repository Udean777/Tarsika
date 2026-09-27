package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoMutationRepository

class DeleteSelectedDevicePhotosUseCase(private val repository: DevicePhotoMutationRepository) {
    suspend operator fun invoke(
        uris: List<String>,
        consentAlreadyGranted: Boolean = false,
    ): DevicePhotoMutationSummary {
        val uniqueUris = uris.map(String::trim).filter(String::isNotEmpty).distinct()
        require(uniqueUris.isNotEmpty()) { "Select at least one photo to delete." }
        return repository.deletePermanently(uniqueUris, consentAlreadyGranted)
    }
}
