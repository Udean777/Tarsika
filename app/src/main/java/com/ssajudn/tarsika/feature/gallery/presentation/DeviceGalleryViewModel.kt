package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoFavorites
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoReader
import com.ssajudn.tarsika.feature.gallery.domain.repository.PhotoEditorRepository
import com.ssajudn.tarsika.feature.gallery.domain.repository.PhotoMetadataRepository
import com.ssajudn.tarsika.feature.gallery.domain.usecase.DeleteSelectedDevicePhotosUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.PrepareEditedPhotoCopyUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DeviceGalleryState {
    data object PermissionRequired : DeviceGalleryState

    data class Empty(val access: PhotoLibraryAccess) : DeviceGalleryState

    data class Content(
        val photos: List<DevicePhoto>,
        val albums: List<DeviceAlbum>,
        val access: PhotoLibraryAccess,
        val isInitialLoad: Boolean = false,
    ) : DeviceGalleryState

    data object Error : DeviceGalleryState
}

class DeviceGalleryViewModel(
    private val photoReader: DevicePhotoReader,
    private val favorites: DevicePhotoFavorites,
    private val metadata: PhotoMetadataRepository,
    private val prepareEditedCopyUseCase: PrepareEditedPhotoCopyUseCase,
    private val editor: PhotoEditorRepository,
    private val deleteSelectedPhotosUseCase: DeleteSelectedDevicePhotosUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow<DeviceGalleryState>(DeviceGalleryState.PermissionRequired)
    val state: StateFlow<DeviceGalleryState> = mutableState.asStateFlow()
    val favoriteKeys: StateFlow<Set<String>> =
        favorites.favorites.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptySet(),
        )
    val effects = GalleryUiEffects()
    private var access: PhotoLibraryAccess? = null
    private var observation: Job? = null

    fun updateAccess(
        newAccess: PhotoLibraryAccess,
        forceRefresh: Boolean = false,
    ) {
        if (!forceRefresh && newAccess == access && observation?.isActive == true) return
        val previousState = mutableState.value
        val keepCurrentState =
            newAccess == access &&
                (previousState is DeviceGalleryState.Content || previousState is DeviceGalleryState.Empty)
        access = newAccess
        observation?.cancel()
        if (newAccess == PhotoLibraryAccess.NONE) {
            mutableState.value = DeviceGalleryState.PermissionRequired
            return
        }
        if (!keepCurrentState) {
            mutableState.value =
                DeviceGalleryState.Content(
                    photos = emptyList(),
                    albums = emptyList(),
                    access = newAccess,
                    isInitialLoad = true,
                )
        }
        observation =
            viewModelScope.launch {
                try {
                    photoReader.observeDeviceLibrary().collect { library ->
                        mutableState.value =
                            if (library.photos.isEmpty()) {
                                DeviceGalleryState.Empty(newAccess)
                            } else {
                                DeviceGalleryState.Content(library.photos, library.albums, newAccess)
                            }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: SecurityException) {
                    mutableState.value = DeviceGalleryState.PermissionRequired
                } catch (error: Exception) {
                    if (!keepCurrentState) mutableState.value = DeviceGalleryState.Error
                    effects.reportFailure(error)
                }
            }
    }

    fun refresh() {
        access?.let { updateAccess(it, forceRefresh = true) }
    }

    fun toggleFavorite(photo: DevicePhoto) = launchOperation { favorites.toggle(photo) }

    suspend fun readPhotoExif(uri: String) = metadata.readExif(uri)

    suspend fun prepareEditedPhotoCopy(
        photo: DevicePhoto,
        crop: PhotoCropRequest,
    ): PreparedPhotoCopy = prepareEditedCopyUseCase(photo.uri, photo.displayName, crop)

    suspend fun exportEditedPhotoCopy(
        copy: PreparedPhotoCopy,
        destinationUri: String,
    ) = editor.exportCopy(copy, destinationUri)

    suspend fun discardEditedPhotoCopy(copy: PreparedPhotoCopy) = editor.discardPreparedCopy(copy)

    suspend fun deleteSelectedPhotos(
        uris: List<String>,
        consentAlreadyGranted: Boolean = false,
    ) = try {
        deleteSelectedPhotosUseCase(uris, consentAlreadyGranted)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        effects.reportFailure(error)
        throw error
    }

    private fun launchOperation(operation: suspend () -> Unit) {
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
