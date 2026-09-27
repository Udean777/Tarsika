package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.TarsikaTopBar
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbumCatalog
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey

@Composable
fun DeviceAlbumDetailScreen(
    state: DeviceGalleryState,
    favoriteKeys: Set<String>,
    userAlbums: List<com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum>,
    albumId: String,
    photoAccess: DevicePhotoAccessState,
    galleryActions: GalleryUiActions,
    albumActions: UserAlbumUiActions,
    onMoveToLocalTrash: suspend (
        List<DevicePhoto>,
        suspend (String) -> Boolean,
    ) -> com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary,
    onBack: () -> Unit,
) {
    val requestPhotoAccess = photoAccess.requestAccess
    val content = state as? DeviceGalleryState.Content
    val isUserAlbum = albumId.startsWith("user:")
    val userAlbumId = albumId.removePrefix("user:")
    val userAlbum = if (isUserAlbum) userAlbums.firstOrNull { it.id == userAlbumId } else null
    val photos =
        content?.photos.orEmpty().filter {
            if (isUserAlbum) it.favoriteKey() in userAlbum?.photoKeys.orEmpty() else DeviceAlbumCatalog.albumKey(it) == albumId
        }
    val album = content?.albums?.firstOrNull { it.id == albumId && it.photoCount > 0 }
    val count = photos.size
    var selectedPhoto by remember { mutableStateOf<DevicePhoto?>(null) }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var selectionMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selecting = selectionMode
    LaunchedEffect(state, albumId) {
        if (!isUserAlbum) {
            when (val currentState = state) {
                is DeviceGalleryState.Content ->
                    if (!currentState.isInitialLoad && currentState.albums.none { it.id == albumId && it.photoCount > 0 }) onBack()
                is DeviceGalleryState.Empty -> onBack()
                else -> Unit
            }
        }
    }
    LaunchedEffect(photos) {
        val availableKeys = photos.mapTo(mutableSetOf()) { it.favoriteKey() }
        val remaining = selectedKeys.intersect(availableKeys)
        selectedKeys = remaining
        if (remaining.isEmpty() && photos.isEmpty()) selectionMode = false
    }

    Column(Modifier.fillMaxSize()) {
        TarsikaTopBar(
            title =
                when {
                    !selecting ->
                        (userAlbum?.name ?: album?.displayName)?.ifBlank { stringResource(R.string.device_album_fallback_name) }
                            ?: stringResource(R.string.nav_albums)
                    selectedKeys.isEmpty() -> stringResource(R.string.selecting_photos)
                    else -> pluralStringResource(R.plurals.selected_photos_count, selectedKeys.size, selectedKeys.size)
                },
            onBack = {
                if (selecting) {
                    selectedKeys = emptySet()
                    selectionMode = false
                } else {
                    onBack()
                }
            },
            actions = {
                if (selecting) {
                    if (selectedKeys.isNotEmpty()) {
                        IconButton(onClick = { shareDevicePhotos(context, photos.filter { it.favoriteKey() in selectedKeys }) }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_selected_photos))
                        }
                    }
                    IconButton(onClick = {
                        selectedKeys = emptySet()
                        selectionMode = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_selection))
                    }
                } else {
                    if (photos.isNotEmpty()) {
                        IconButton(onClick = { selectionMode = true }) {
                            Icon(Icons.Default.CheckBox, contentDescription = stringResource(R.string.select_photos))
                        }
                    }
                    if (content?.access == PhotoLibraryAccess.SELECTED) {
                        androidx.compose.material3.TextButton(onClick = requestPhotoAccess) {
                            Text(stringResource(R.string.change_photo_access))
                        }
                    }
                }
            },
        )
        when (state) {
            DeviceGalleryState.PermissionRequired ->
                PhotoAccessPrompt(
                    actionLabel =
                        stringResource(
                            if (photoAccess.permissionDenied) R.string.open_app_settings else R.string.allow_photo_access,
                        ),
                    onAction = requestPhotoAccess,
                    permissionDenied = photoAccess.permissionDenied,
                    modifier = Modifier.weight(1f),
                )

            is DeviceGalleryState.Empty ->
                if (userAlbum != null) {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            pluralStringResource(R.plurals.device_album_photo_count, 0, 0),
                            modifier = Modifier.padding(start = 20.dp, top = 8.dp),
                        )
                        GalleryMessage(
                            title = stringResource(R.string.user_album_empty_title),
                            description = stringResource(R.string.user_album_empty_description),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                } else {
                    GalleryMessage(
                        title = stringResource(R.string.device_album_unavailable_title),
                        description = stringResource(R.string.device_album_unavailable_description),
                        actionLabel = stringResource(R.string.back_to_albums),
                        onAction = onBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

            DeviceGalleryState.Error ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_error_title),
                    description = stringResource(R.string.device_gallery_error_description),
                    actionLabel = stringResource(R.string.retry),
                    onAction = galleryActions.refresh,
                    modifier = Modifier.fillMaxSize(),
                )

            is DeviceGalleryState.Content -> {
                if (content?.isInitialLoad == true) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                } else if (if (isUserAlbum) userAlbum == null else album == null) {
                    GalleryMessage(
                        title = stringResource(R.string.device_album_unavailable_title),
                        description = stringResource(R.string.device_album_unavailable_description),
                        actionLabel = stringResource(R.string.back_to_albums),
                        onAction = onBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(Modifier.fillMaxWidth().padding(start = 20.dp, top = 2.dp, end = 20.dp, bottom = 8.dp)) {
                        Text(
                            pluralStringResource(R.plurals.device_album_photo_count, count, count),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        (
                            if (isUserAlbum) {
                                stringResource(
                                    R.string.user_album_local_note,
                                )
                            } else {
                                album?.relativePath
                            }
                        )?.takeIf { it.isNotBlank() }?.let {
                                path ->
                            Text(
                                path,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (content?.access == PhotoLibraryAccess.SELECTED) {
                            Text(
                                stringResource(R.string.limited_photo_access),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                    DevicePhotoGrid(
                        photos = photos,
                        onOpen = { selectedPhoto = it },
                        modifier = Modifier.weight(1f),
                        favoriteKeys = favoriteKeys,
                        selectedKeys = selectedKeys,
                        selectionMode = selectionMode,
                        onToggleSelection = { photo ->
                            selectionMode = true
                            val key = photo.favoriteKey()
                            selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key
                        },
                    )
                    if (selectionMode) {
                        DevicePhotoBulkActions(
                            userAlbums = userAlbums,
                            onMove = albumActions.movePhotos,
                            onMoveToLocalTrash = onMoveToLocalTrash,
                            onDeletePhotos = galleryActions.deleteSelectedPhotos,
                            selectedPhotos = photos.filter { it.favoriteKey() in selectedKeys },
                            onClearSelection = {
                                selectedKeys = emptySet()
                                selectionMode = false
                            },
                            onDeleted = {
                                galleryActions.refresh()
                                selectedKeys = emptySet()
                                selectionMode = false
                            },
                            onTrashed = {
                                galleryActions.refresh()
                                selectedKeys = emptySet()
                                selectionMode = false
                            },
                            currentAlbumId = userAlbum?.id,
                            onRemoveMembership =
                                if (userAlbum != null) {
                                    (
                                        {
                                            albumActions.removePhotos(userAlbum.id, selectedKeys)
                                            selectedKeys = emptySet()
                                            selectionMode = false
                                        }
                                    )
                                } else {
                                    null
                                },
                        )
                    }
                }
            }
        }
    }
    selectedPhoto?.let { photo ->
        val index = photos.indexOfFirst { it.favoriteKey() == photo.favoriteKey() }
        if (index >= 0) {
            PhotoViewerDialog(
                photos = photos,
                initialPhotoIndex = index,
                favoriteKeys = favoriteKeys,
                onToggleFavorite = galleryActions.toggleFavorite,
                onReadExif = galleryActions.readExif,
                onPrepareEditedCopy = galleryActions.prepareEditedCopy,
                onExportEditedCopy = galleryActions.exportEditedCopy,
                onDiscardEditedCopy = galleryActions.discardEditedCopy,
                onDismiss = { selectedPhoto = null },
            )
        }
    }
}
