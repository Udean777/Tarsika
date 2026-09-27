package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.SystemTrashPhoto

interface SystemTrashRepository {
    suspend fun queryPlatformTrash(): List<SystemTrashPhoto>
}
