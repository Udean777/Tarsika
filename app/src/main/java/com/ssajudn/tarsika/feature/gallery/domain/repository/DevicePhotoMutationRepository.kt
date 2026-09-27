package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary

interface DevicePhotoMutationRepository {
    suspend fun deletePermanently(
        uris: List<String>,
        consentAlreadyGranted: Boolean = false,
    ): DevicePhotoMutationSummary
}
