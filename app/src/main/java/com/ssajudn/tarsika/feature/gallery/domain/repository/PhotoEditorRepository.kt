package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy

interface PhotoEditorRepository {
    suspend fun prepareCopy(
        sourceUri: String,
        sourceName: String,
        crop: PhotoCropRequest,
    ): PreparedPhotoCopy

    suspend fun exportCopy(
        copy: PreparedPhotoCopy,
        destinationUri: String,
    )

    suspend fun discardPreparedCopy(copy: PreparedPhotoCopy)
}
