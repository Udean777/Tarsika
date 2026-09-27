package com.ssajudn.tarsika.feature.gallery.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.Locale

class DevicePhotoDateFormatterTest {
    @Test
    fun galleryDayUsesCurrentLocaleAtCallTime() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val english = DevicePhotoDateFormatter.galleryDay(Instant.parse("2025-01-02T00:00:00Z"))
            Locale.setDefault(Locale.GERMANY)
            val german = DevicePhotoDateFormatter.galleryDay(Instant.parse("2025-01-02T00:00:00Z"))

            assertTrue(english != german)
        } finally {
            Locale.setDefault(original)
        }
    }
}
