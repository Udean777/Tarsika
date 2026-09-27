package com.ssajudn.tarsika.feature.gallery.domain.model

data class DevicePhotoLibrary(
    val photos: List<DevicePhoto>,
    val albums: List<DeviceAlbum>,
)
