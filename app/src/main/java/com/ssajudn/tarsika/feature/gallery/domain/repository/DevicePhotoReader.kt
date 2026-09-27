package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoLibrary
import kotlinx.coroutines.flow.Flow

interface DevicePhotoReader {
    fun observeDeviceLibrary(): Flow<DevicePhotoLibrary>
}
