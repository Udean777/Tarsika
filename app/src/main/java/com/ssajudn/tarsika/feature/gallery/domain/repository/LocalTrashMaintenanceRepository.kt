package com.ssajudn.tarsika.feature.gallery.domain.repository

interface LocalTrashMaintenanceRepository {
    suspend fun deleteAllForever()

    suspend fun deleteExpiredBefore(trashedBeforeMillis: Long)
}
