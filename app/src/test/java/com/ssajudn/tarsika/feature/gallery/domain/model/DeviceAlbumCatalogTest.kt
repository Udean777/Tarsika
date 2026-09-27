package com.ssajudn.tarsika.feature.gallery.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceAlbumCatalogTest {
    @Test
    fun `groups photos by volume and bucket and picks newest cover`() {
        val photos =
            listOf(
                photo(id = 1, bucket = "camera", volume = "external_primary", millis = 10),
                photo(id = 2, bucket = "camera", volume = "external_primary", millis = 30),
                photo(id = 3, bucket = "downloads", volume = "external_primary", millis = 20),
            )

        val albums = DeviceAlbumCatalog.fromPhotos(photos)

        assertEquals(2, albums.size)
        assertEquals(2, albums.first().photoCount)
        assertEquals(30L, albums.first().newestPhotoMillis)
        assertEquals("photo-2", albums.first().coverUri)
        assertEquals("Camera", albums.first().displayName)
    }

    @Test
    fun `same display name in different buckets remains separate`() {
        val first = photo(id = 1, bucket = "camera-a", volume = "external_primary", millis = 10)
        val second = photo(id = 2, bucket = "camera-b", volume = "external_primary", millis = 20)

        val albums = DeviceAlbumCatalog.fromPhotos(listOf(first, second))

        assertEquals(2, albums.size)
        assertNotEquals(albums[0].id, albums[1].id)
        assertEquals("DCIM/Camera", albums.first { it.bucketId == "camera-a" }.relativePath)
    }

    @Test
    fun `same bucket id on different volumes remains separate`() {
        val primary = photo(id = 1, bucket = "42", volume = "external_primary", millis = 10)
        val removable = photo(id = 2, bucket = "42", volume = "AB12-CD34", millis = 20)

        val albums = DeviceAlbumCatalog.fromPhotos(listOf(primary, removable))

        assertEquals(2, albums.size)
        assertTrue(albums.map(DeviceAlbum::volumeName).containsAll(listOf("external_primary", "AB12-CD34")))
    }

    @Test
    fun `relative path groups rows without bucket id`() {
        val first = photo(id = 1, bucket = null, volume = "external_primary", path = "Pictures/Trips/", millis = 10)
        val second = photo(id = 2, bucket = null, volume = "external_primary", path = "Pictures/Trips", millis = 20)
        val other = photo(id = 3, bucket = null, volume = "external_primary", path = "Pictures/Family/", millis = 15)

        val albums = DeviceAlbumCatalog.fromPhotos(listOf(first, second, other))

        assertEquals(2, albums.size)
        assertEquals(2, albums.first { it.relativePath == "Pictures/Trips" }.photoCount)
    }

    private fun photo(
        id: Long,
        bucket: String?,
        volume: String,
        millis: Long,
        path: String = "DCIM/Camera/",
    ) = DevicePhoto(
        id = id,
        uri = "photo-$id",
        displayName = "image-$id.jpg",
        dateTakenMillis = millis,
        width = 100,
        height = 100,
        volumeName = volume,
        bucketId = bucket,
        bucketDisplayName = "Camera",
        relativePath = path,
    )
}
