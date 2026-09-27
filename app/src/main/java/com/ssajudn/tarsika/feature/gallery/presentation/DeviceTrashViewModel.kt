package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.SystemTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary
import com.ssajudn.tarsika.feature.gallery.domain.repository.LocalTrashRepository
import com.ssajudn.tarsika.feature.gallery.domain.repository.SystemTrashRepository
import com.ssajudn.tarsika.feature.gallery.domain.usecase.MoveDevicePhotosToLocalTrashUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.EmptyLocalTrashUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.PurgeExpiredLocalTrashUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceTrashState(
    val localItems: List<LocalTrashPhoto> = emptyList(),
    val platformItems: List<SystemTrashPhoto> = emptyList(),
    val isLoadingPlatform: Boolean = false,
    val platformLoadFailed: Boolean = false,
)

class DeviceTrashViewModel(
    private val localTrash: LocalTrashRepository,
    private val systemTrash: SystemTrashRepository,
    private val moveToTrash: MoveDevicePhotosToLocalTrashUseCase,
    private val emptyLocalTrashUseCase: EmptyLocalTrashUseCase,
    private val purgeExpiredLocalTrash: PurgeExpiredLocalTrashUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DeviceTrashState())
    val state: StateFlow<DeviceTrashState> = mutableState.asStateFlow()
    val effects = GalleryUiEffects()

    init {
        viewModelScope.launch {
            try {
                purgeExpiredLocalTrash()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                effects.reportFailure(error)
            }
            localTrash.localEntries.collect { items ->
                mutableState.value = mutableState.value.copy(localItems = items)
            }
        }
    }

    fun refreshPlatformTrash() =
        perform {
            mutableState.value = mutableState.value.copy(isLoadingPlatform = true, platformLoadFailed = false)
            try {
                val items = systemTrash.queryPlatformTrash()
                mutableState.value = mutableState.value.copy(platformItems = items, isLoadingPlatform = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value =
                    mutableState.value.copy(
                        platformItems = emptyList(),
                        isLoadingPlatform = false,
                        platformLoadFailed = true,
                    )
                throw error
            }
        }

    suspend fun movePhotos(
        photos: List<DevicePhoto>,
        deleteSource: suspend (String) -> Boolean,
    ): TrashMoveSummary =
        try {
            moveToTrash(photos, deleteSource)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            effects.reportFailure(error)
            throw error
        }

    fun restore(
        item: LocalTrashPhoto,
        destinationUri: String,
    ) = perform { localTrash.restore(item, destinationUri) }

    fun deleteForever(id: String) = perform { localTrash.deleteForever(id) }

    fun emptyLocalTrash() = perform { emptyLocalTrashUseCase() }

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
