package com.ssajudn.tarsika.feature.gallery.presentation

import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary
import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoExifMetadata
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary

data class GalleryUiActions(
    val refresh: () -> Unit,
    val toggleFavorite: (DevicePhoto) -> Unit,
    val readExif: suspend (String) -> PhotoExifMetadata,
    val prepareEditedCopy: suspend (DevicePhoto, PhotoCropRequest) -> PreparedPhotoCopy,
    val exportEditedCopy: suspend (PreparedPhotoCopy, String) -> Unit,
    val discardEditedCopy: suspend (PreparedPhotoCopy) -> Unit,
    val deleteSelectedPhotos: suspend (List<String>, Boolean) -> DevicePhotoMutationSummary,
)

data class UserAlbumUiActions(
    val create: (String) -> Unit,
    val rename: (String, String) -> Unit,
    val delete: (String) -> Unit,
    val addPhotos: (String, Set<String>) -> Unit,
    val removePhotos: (String, Set<String>) -> Unit,
    val movePhotos: (String?, String, Set<String>) -> Unit,
)

data class TrashUiActions(
    val refreshPlatform: () -> Unit,
    val movePhotos: suspend (List<DevicePhoto>, suspend (String) -> Boolean) -> TrashMoveSummary,
    val restore: (LocalTrashPhoto, String) -> Unit,
    val deleteForever: (String) -> Unit,
)

data class HiddenAlbumUiActions(
    val ensureKey: suspend () -> Unit,
    val readPhoto: suspend (String) -> com.ssajudn.tarsika.feature.gallery.domain.model.DecryptedVaultPhoto,
    val importPhoto: suspend (String, String, Long) -> Unit,
    val deletePhoto: (String) -> Unit,
)
