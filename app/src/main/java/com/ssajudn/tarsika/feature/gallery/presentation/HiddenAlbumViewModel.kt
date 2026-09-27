package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.tarsika.feature.gallery.domain.model.DecryptedVaultPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import com.ssajudn.tarsika.feature.gallery.domain.repository.VaultRepository
import com.ssajudn.tarsika.feature.gallery.domain.usecase.ImportPhotoIntoVaultUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HiddenAlbumViewModel(
    private val vault: VaultRepository,
    private val importPhotoIntoVaultUseCase: ImportPhotoIntoVaultUseCase,
) : ViewModel() {
    val photos = vault.photos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<VaultPhoto>())
    val effects = GalleryUiEffects()

    suspend fun ensureKey() = vault.ensureKey()

    suspend fun readPhoto(id: String): DecryptedVaultPhoto = vault.read(id)

    suspend fun importPhoto(
        uri: String,
        name: String,
        sizeBytes: Long,
    ) = importPhotoIntoVaultUseCase(uri, name, sizeBytes)

    fun deletePhoto(id: String) = perform { vault.delete(id) }

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
