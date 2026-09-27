package com.ssajudn.tarsika.feature.gallery.domain.model

data class TrashMoveSummary(
    val movedPhotoKeys: Set<String>,
    val notMovedPhotoKeys: Set<String>,
)
