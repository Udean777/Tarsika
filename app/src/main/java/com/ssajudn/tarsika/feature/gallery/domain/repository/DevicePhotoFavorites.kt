package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import kotlinx.coroutines.flow.Flow

interface DevicePhotoFavorites {
    val favorites: Flow<Set<String>>

    suspend fun toggle(photo: DevicePhoto)
}
