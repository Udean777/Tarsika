package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object SearchDevicePhotosUseCase {
    operator fun invoke(
        photos: List<DevicePhoto>,
        query: String,
        favoriteKeys: Set<String>,
        favoritesOnly: Boolean,
    ): List<DevicePhoto> {
        val needle = query.trim().normalized()
        return photos.filter { photo ->
            (!favoritesOnly || photo.favoriteKey() in favoriteKeys) &&
                (needle.isEmpty() || searchableText(photo).any { it.contains(needle) })
        }
    }

    private fun searchableText(photo: DevicePhoto): List<String> {
        val localDate =
            photo.dateTakenMillis.takeIf { it > 0L }?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            }
        return listOfNotNull(
            photo.displayName,
            photo.bucketDisplayName,
            photo.relativePath,
            localDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            localDate?.let {
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()).format(it)
            },
        ).map { it.normalized() }
    }

    private fun String.normalized(): String = trim().lowercase(Locale.ROOT)
}
