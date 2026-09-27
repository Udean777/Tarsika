package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.tarsika.feature.gallery.domain.repository.UserPhotoAlbumRepository
import com.ssajudn.tarsika.feature.gallery.domain.usecase.CreateUserPhotoAlbumUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.MovePhotosBetweenUserAlbumsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserAlbumsViewModel(
    private val repository: UserPhotoAlbumRepository,
    private val createAlbum: CreateUserPhotoAlbumUseCase,
    private val movePhotos: MovePhotosBetweenUserAlbumsUseCase,
) : ViewModel() {
    val albums = repository.albums.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val effects = GalleryUiEffects()

    fun createUserAlbum(
        name: String,
        parentRelativePath: String,
        photoKeys: Set<String>,
    ) = perform { createAlbum(name, parentRelativePath, photoKeys) }

    fun renameUserAlbum(
        id: String,
        name: String,
    ) = perform {
        require(name.isNotBlank())
        repository.rename(id, name.trim())
    }

    fun deleteUserAlbum(id: String) = perform { repository.delete(id) }

    fun addPhotosToUserAlbum(
        id: String,
        photoKeys: Set<String>,
    ) = perform { repository.addPhotos(id, photoKeys) }

    fun movePhotosBetweenAlbums(
        sourceId: String?,
        destinationId: String,
        keys: Set<String>,
    ) = perform {
        movePhotos(keys, sourceId, destinationId)
    }

    fun removePhotosFromUserAlbum(
        id: String,
        photoKeys: Set<String>,
    ) = perform { repository.removePhotos(id, photoKeys) }

    private fun perform(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
            } catch (
                error: CancellationException,
            ) {
                throw error
            } catch (error: Exception) {
                effects.reportFailure(error)
            }
        }
    }
}
