package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserPhotoAlbumRepositoryTest {
    private lateinit var database: UserAlbumDatabase
    private lateinit var repository: RoomUserPhotoAlbumRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UserAlbumDatabase::class.java).build()
        repository = RoomUserPhotoAlbumRepository(database.albums())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun albumAndMembershipRowsMapToDomainModel() =
        runBlocking {
            repository.create("  Family  ", "Pictures/Family", setOf("external:1", "external:2"))

            val albumWithPhotos = repository.albums.first().single()

            assertEquals("Family", albumWithPhotos.name)
            assertEquals("Pictures/Family/", albumWithPhotos.relativePath)
            assertEquals(setOf("external:1", "external:2"), albumWithPhotos.photoKeys)
        }
}
