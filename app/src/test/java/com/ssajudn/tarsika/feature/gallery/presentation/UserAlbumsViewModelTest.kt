package com.ssajudn.tarsika.feature.gallery.presentation

import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository
import com.ssajudn.tarsika.feature.gallery.domain.usecase.CreateUserPhotoAlbumUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.MovePhotosBetweenUserAlbumsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class UserAlbumsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `create action trims the name through domain use case`() =
        runTest {
            val repository = FakeUserPhotoAlbumRepository()
            val viewModel =
                UserAlbumsViewModel(
                    repository,
                    CreateUserPhotoAlbumUseCase(repository),
                    MovePhotosBetweenUserAlbumsUseCase(repository),
                )

            viewModel.createUserAlbum("  Weekend  ")
            advanceUntilIdle()

            assertEquals(listOf("Weekend"), repository.createdNames)
        }

    private class FakeUserPhotoAlbumRepository : UserPhotoAlbumRepository {
        override val albums: Flow<List<UserPhotoAlbum>> = MutableStateFlow(emptyList())
        val createdNames = mutableListOf<String>()

        override suspend fun create(name: String) {
            createdNames += name
        }

        override suspend fun rename(
            id: String,
            name: String,
        ) = Unit

        override suspend fun delete(id: String) = Unit

        override suspend fun addPhotos(
            albumId: String,
            photoKeys: Set<String>,
        ) = Unit

        override suspend fun removePhotos(
            albumId: String,
            photoKeys: Set<String>,
        ) = Unit
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}
