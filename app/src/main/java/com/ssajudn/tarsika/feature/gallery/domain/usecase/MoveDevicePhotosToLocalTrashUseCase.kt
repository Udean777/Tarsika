package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashRepository
import kotlinx.coroutines.CancellationException

class MoveDevicePhotosToLocalTrashUseCase(private val repository: LocalTrashRepository) {
    suspend operator fun invoke(
        photos: List<DevicePhoto>,
        deleteSource: suspend (String) -> Boolean,
    ): TrashMoveSummary {
        val moved = mutableSetOf<String>()
        val notMoved = mutableSetOf<String>()
        for (photo in photos.distinctBy(DevicePhoto::uri)) {
            val entry =
                repository.backupAndRecord(
                    uri = photo.uri,
                    displayName = photo.displayName,
                    mimeType = photo.mimeType ?: DEFAULT_MIME_TYPE,
                    sizeBytes = photo.sizeBytes,
                )
            try {
                if (deleteSource(photo.uri)) {
                    moved += photo.favoriteKey()
                } else {
                    repository.discardBackup(entry.id)
                    notMoved += photo.favoriteKey()
                }
            } catch (error: CancellationException) {
                repository.discardBackup(entry.id)
                throw error
            } catch (error: Exception) {
                repository.discardBackup(entry.id)
                notMoved += photo.favoriteKey()
            }
        }
        return TrashMoveSummary(moved, notMoved)
    }

    private companion object {
        const val DEFAULT_MIME_TYPE = "image/jpeg"
    }
}
