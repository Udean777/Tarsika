package com.ssajudn.tarsika.feature.gallery.domain.model

data class DevicePhotoMutationSummary(
    val deletedUris: Set<String> = emptySet(),
    val notFoundUris: Set<String> = emptySet(),
    val approvalRequiredUris: Set<String> = emptySet(),
)
