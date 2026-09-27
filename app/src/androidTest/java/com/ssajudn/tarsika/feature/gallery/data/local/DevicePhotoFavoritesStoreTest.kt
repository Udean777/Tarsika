package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DevicePhotoFavoritesStoreTest {
    @Test
    fun favoriteTogglePersistsLocallyAndCanBeUndone() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val store = DevicePhotoFavoritesStore(context)
            val photo =
                DevicePhoto(
                    id = System.nanoTime(),
                    uri = "content://test/device-photo",
                    displayName = "local-test.jpg",
                    dateTakenMillis = 1L,
                    width = 1,
                    height = 1,
                    volumeName = "local-test-volume",
                )
            val key = photo.favoriteKey()

            assertFalse(store.favorites.first().contains(key))
            store.toggle(photo)
            assertTrue(store.favorites.first().contains(key))
            store.toggle(photo)
            assertFalse(store.favorites.first().contains(key))
        }
}
