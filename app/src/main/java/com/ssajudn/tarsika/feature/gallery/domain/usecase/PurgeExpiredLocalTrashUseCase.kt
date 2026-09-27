package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.TrashRetentionPolicy
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashMaintenanceRepository

class PurgeExpiredLocalTrashUseCase(private val repository: LocalTrashMaintenanceRepository) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
        repository.deleteExpiredBefore(nowMillis - TrashRetentionPolicy.LOCAL_RETENTION_MILLIS)
    }
}
