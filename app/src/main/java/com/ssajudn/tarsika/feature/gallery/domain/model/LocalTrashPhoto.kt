package com.ssajudn.tarsika.feature.gallery.domain.model

data class LocalTrashPhoto(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val trashedAtMillis: Long,
    val thumbnailUri: String? = null,
)
