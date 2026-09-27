package com.ssajudn.tarsika.feature.gallery.domain.model

data class PhotoCropRequest(
    val rotationDegrees: Int,
    val aspectRatio: Float,
    val focusX: Float,
    val focusY: Float,
)

data class PreparedPhotoCopy(val filePath: String, val suggestedFileName: String)
