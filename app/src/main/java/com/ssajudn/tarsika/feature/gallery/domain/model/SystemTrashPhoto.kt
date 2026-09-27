package com.ssajudn.tarsika.feature.gallery.domain.model

data class SystemTrashPhoto(
    val uri: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val expiresAtMillis: Long?,
)
