package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import com.ssajudn.tarsika.feature.gallery.domain.repository.PhotoEditorRepository

class PrepareEditedPhotoCopyUseCase(private val repository: PhotoEditorRepository) {
    suspend operator fun invoke(
        sourceUri: String,
        sourceName: String,
        crop: PhotoCropRequest,
    ): PreparedPhotoCopy {
        require(sourceUri.isNotBlank()) { "Photo URI cannot be empty." }
        require(crop.rotationDegrees in 0..359 && crop.rotationDegrees % 90 == 0) { "Rotation must be a multiple of 90 degrees." }
        require(crop.aspectRatio.isFinite() && crop.aspectRatio > 0f) { "Crop aspect ratio must be positive." }
        require(crop.focusX.isFinite() && crop.focusX in 0f..1f) { "Horizontal crop focus must be between 0 and 1." }
        require(crop.focusY.isFinite() && crop.focusY in 0f..1f) { "Vertical crop focus must be between 0 and 1." }
        return repository.prepareCopy(sourceUri, sourceName, crop)
    }
}
