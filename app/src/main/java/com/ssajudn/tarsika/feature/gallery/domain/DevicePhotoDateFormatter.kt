package com.ssajudn.tarsika.feature.gallery.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DevicePhotoDateFormatter {
    private const val GALLERY_DAY_PATTERN = "d MMMM yyyy"
    private const val PHOTO_TIMESTAMP_PATTERN = "d MMMM yyyy, HH:mm"

    fun galleryDay(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = formatter(GALLERY_DAY_PATTERN, Locale.getDefault(), zoneId).format(instant)

    fun photoTimestamp(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = formatter(PHOTO_TIMESTAMP_PATTERN, Locale.getDefault(), zoneId).format(instant)

    private fun formatter(
        pattern: String,
        locale: Locale,
        zoneId: ZoneId,
    ): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale).withZone(zoneId)
}
