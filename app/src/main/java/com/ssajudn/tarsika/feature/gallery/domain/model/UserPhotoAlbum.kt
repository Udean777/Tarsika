package com.ssajudn.tarsika.feature.gallery.domain.model

data class UserPhotoAlbum(
    val id: String,
    val name: String,
    val createdAtMillis: Long,
    val photoKeys: Set<String>,
    val relativePath: String = "Pictures/Tarsika/",
)
