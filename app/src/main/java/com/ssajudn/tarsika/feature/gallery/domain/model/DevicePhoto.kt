package com.ssajudn.tarsika.feature.gallery.domain.model

data class DevicePhoto(
    val id: Long,
    val uri: String,
    val displayName: String,
    val dateTakenMillis: Long,
    val sizeBytes: Long = 0L,
    val width: Int,
    val height: Int,
    val volumeName: String = "external",
    val bucketId: String? = null,
    val bucketDisplayName: String? = null,
    val relativePath: String? = null,
    val mimeType: String? = null,
)

fun DevicePhoto.favoriteKey(): String = "$volumeName:$id"
