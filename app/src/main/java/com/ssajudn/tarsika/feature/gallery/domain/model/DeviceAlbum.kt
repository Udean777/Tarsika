package com.ssajudn.tarsika.feature.gallery.domain.model

import java.nio.charset.StandardCharsets
import java.util.Base64

data class DeviceAlbum(
    val id: String,
    val volumeName: String,
    val bucketId: String?,
    val displayName: String,
    val relativePath: String?,
    val coverUri: String,
    val photoCount: Int,
    val newestPhotoMillis: Long,
)

object DeviceAlbumCatalog {
    fun fromPhotos(photos: List<DevicePhoto>): List<DeviceAlbum> =
        photos
            .groupBy(::albumKey)
            .map { (key, albumPhotos) ->
                val cover = albumPhotos.maxBy(DevicePhoto::dateTakenMillis)
                DeviceAlbum(
                    id = key,
                    volumeName = cover.volumeName,
                    bucketId = cover.bucketId,
                    displayName = cover.bucketDisplayName.orEmpty(),
                    relativePath = cover.relativePath.normalizedPath(),
                    coverUri = cover.uri,
                    photoCount = albumPhotos.size,
                    newestPhotoMillis = cover.dateTakenMillis,
                )
            }
            .sortedByDescending(DeviceAlbum::newestPhotoMillis)

    fun albumKey(photo: DevicePhoto): String {
        val folderIdentity = photo.bucketId ?: photo.relativePath.normalizedPath().orEmpty()
        val bytes = "${photo.volumeName}\u0000$folderIdentity".toByteArray(StandardCharsets.UTF_8)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun String?.normalizedPath(): String? = this?.trim()?.trimEnd('/')?.takeIf(String::isNotEmpty)
}
