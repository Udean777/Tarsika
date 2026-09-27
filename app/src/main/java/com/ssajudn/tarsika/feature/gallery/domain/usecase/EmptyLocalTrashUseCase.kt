package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashMaintenanceRepository

class EmptyLocalTrashUseCase(private val repository: LocalTrashMaintenanceRepository) {
    suspend operator fun invoke() = repository.deleteAllForever()
}
