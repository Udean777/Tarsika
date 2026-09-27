package com.ssajudn.tarsika.feature.gallery.domain.usecase

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchDevicePhotosUseCaseTest {
    private val cameraPhoto =
        DevicePhoto(
            id = 1,
            uri = "content://photo/1",
            displayName = "Trip.JPG",
            dateTakenMillis = 1_735_732_800_000,
            width = 1200,
            height = 900,
            volumeName = "external_primary",
            bucketDisplayName = "Camera",
            relativePath = "DCIM/Camera/",
        )
    private val downloadPhoto =
        cameraPhoto.copy(
            id = 2,
            uri = "content://photo/2",
            displayName = "receipt.png",
            dateTakenMillis = 1_735_905_600_000,
            bucketDisplayName = "Downloads",
            relativePath = "Download/",
        )

    @Test
    fun `search matches filename folder path and ISO date`() {
        assertEquals(listOf(cameraPhoto), SearchDevicePhotosUseCase(listOf(cameraPhoto, downloadPhoto), "trip", emptySet(), false))
        assertEquals(listOf(downloadPhoto), SearchDevicePhotosUseCase(listOf(cameraPhoto, downloadPhoto), "downloads", emptySet(), false))
        assertEquals(listOf(cameraPhoto), SearchDevicePhotosUseCase(listOf(cameraPhoto, downloadPhoto), "dcim/camera", emptySet(), false))
        assertEquals(listOf(cameraPhoto), SearchDevicePhotosUseCase(listOf(cameraPhoto, downloadPhoto), "2025-01-01", emptySet(), false))
    }

    @Test
    fun `favorites only includes matching device identities`() {
        assertEquals(
            listOf(downloadPhoto),
            SearchDevicePhotosUseCase(listOf(cameraPhoto, downloadPhoto), "", setOf(downloadPhoto.favoriteKey()), true),
        )
    }

    @Test
    fun `missing date is not treated as the Unix epoch`() {
        val undated = cameraPhoto.copy(id = 3, uri = "content://photo/3", dateTakenMillis = 0L)
        assertEquals(emptyList<DevicePhoto>(), SearchDevicePhotosUseCase(listOf(undated), "1970-01-01", emptySet(), false))
        assertEquals(listOf(undated), SearchDevicePhotosUseCase(listOf(undated), "trip", emptySet(), false))
    }
}
