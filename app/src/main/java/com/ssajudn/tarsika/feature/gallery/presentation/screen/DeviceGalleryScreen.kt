package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.TarsikaTopBar
import com.ssajudn.tarsika.core.ui.components.LocalVaultBanner
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun DeviceGalleryScreen(
    state: DeviceGalleryState,
    favoriteKeys: Set<String>,
    userAlbums: List<com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum>,
    photoAccess: DevicePhotoAccessState,
    actions: GalleryUiActions,
    onMovePhotos: (String?, String, Set<String>) -> Unit,
    onMoveToLocalTrash: suspend (
        List<DevicePhoto>,
        suspend (String) -> Boolean,
    ) -> com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary,
    onSearch: () -> Unit = {},
) {
    val access = photoAccess.access
    val permissionDenied = photoAccess.permissionDenied
    var selectedPhoto by remember { mutableStateOf<DevicePhoto?>(null) }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var thisMonthOnly by remember { mutableStateOf(false) }
    val requestPhotoAccess = photoAccess.requestAccess
    val content = state as? DeviceGalleryState.Content
    val photos = content?.photos.orEmpty()
    val context = LocalContext.current
    val selecting = selectionMode
    val visiblePhotos =
        remember(photos, favoriteKeys, favoritesOnly, thisMonthOnly) {
            val currentMonth = YearMonth.now()
            photos.filter { photo ->
                (!favoritesOnly || photo.favoriteKey() in favoriteKeys) &&
                    (
                        !thisMonthOnly || (
                            photo.dateTakenMillis > 0L &&
                                YearMonth.from(Instant.ofEpochMilli(photo.dateTakenMillis).atZone(ZoneId.systemDefault())) == currentMonth
                        )
                    )
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
                    !selecting -> stringResource(R.string.nav_gallery)
                    selectedKeys.isEmpty() -> stringResource(R.string.selecting_photos)
                    else -> pluralStringResource(R.plurals.selected_photos_count, selectedKeys.size, selectedKeys.size)
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
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_device_photos))
                    }
                    if (access == PhotoLibraryAccess.SELECTED) {
                        TextButton(onClick = requestPhotoAccess) { Text(stringResource(R.string.change_photo_access)) }
                    }
                }
            },
        )
        when (val galleryState = state) {
            DeviceGalleryState.PermissionRequired ->
                PhotoAccessPrompt(
                    actionLabel = stringResource(if (permissionDenied) R.string.open_app_settings else R.string.allow_photo_access),
                    onAction = requestPhotoAccess,
                    permissionDenied = permissionDenied,
                    modifier = Modifier.weight(1f),
                )

            is DeviceGalleryState.Empty ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_empty_title),
                    description =
                        stringResource(
                            if (galleryState.access == PhotoLibraryAccess.SELECTED) {
                                R.string.device_gallery_selected_empty_description
                            } else {
                                R.string.device_gallery_empty_description
                            },
                        ),
                    actionLabel =
                        if (galleryState.access == PhotoLibraryAccess.SELECTED) {
                            stringResource(
                                R.string.change_photo_access,
                            )
                        } else {
                            null
                        },
                    onAction = if (galleryState.access == PhotoLibraryAccess.SELECTED) requestPhotoAccess else null,
                    modifier = Modifier.fillMaxSize(),
                )

            DeviceGalleryState.Error ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_error_title),
                    description = stringResource(R.string.device_gallery_error_description),
                    actionLabel = stringResource(R.string.retry),
                    onAction = actions.refresh,
                    modifier = Modifier.fillMaxSize(),
                )

            is DeviceGalleryState.Content -> {
                Column(Modifier.fillMaxSize()) {
                    LocalVaultBanner(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = !favoritesOnly && !thisMonthOnly,
                            onClick = {
                                favoritesOnly = false
                                thisMonthOnly = false
                            },
                            label = { Text(stringResource(R.string.all_photos)) },
                        )
                        FilterChip(
                            selected = favoritesOnly,
                            onClick = { favoritesOnly = !favoritesOnly },
                            label = { Text(stringResource(R.string.favorites_only)) },
                        )
                        FilterChip(
                            selected = thisMonthOnly,
                            onClick = { thisMonthOnly = !thisMonthOnly },
                            label = { Text(stringResource(R.string.this_month)) },
                        )
                    }
                    if (galleryState.access == PhotoLibraryAccess.SELECTED) {
                        Text(
                            stringResource(R.string.limited_photo_access),
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    if (visiblePhotos.isEmpty() && !galleryState.isInitialLoad) {
                        GalleryMessage(
                            title =
                                stringResource(
                                    if (favoritesOnly) R.string.no_favorite_photos else R.string.no_device_photo_search_results,
                                ),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    } else if (visiblePhotos.isNotEmpty()) {
                        DevicePhotoGrid(
                            photos = visiblePhotos,
                            favoriteKeys = favoriteKeys,
                            selectedKeys = selectedKeys,
                            selectionMode = selectionMode,
                            onToggleSelection = { photo ->
                                selectionMode = true
                                selectedKeys = selectedKeys.toggle(photo.favoriteKey())
                            },
                            onOpen = { selectedPhoto = it },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    }
                    if (selectionMode) {
                        DevicePhotoBulkActions(
                            userAlbums = userAlbums,
                            onMove = onMovePhotos,
                            onMoveToLocalTrash = onMoveToLocalTrash,
                            onDeletePhotos = actions.deleteSelectedPhotos,
                            selectedPhotos = photos.filter { it.favoriteKey() in selectedKeys },
                            onClearSelection = {
                                selectedKeys = emptySet()
                                selectionMode = false
                            },
                            onDeleted = {
                                actions.refresh()
                                selectedKeys = emptySet()
                                selectionMode = false
                            },
                            onTrashed = {
                                actions.refresh()
                                selectedKeys = emptySet()
                                selectionMode = false
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
                onToggleFavorite = actions.toggleFavorite,
                onReadExif = actions.readExif,
                onPrepareEditedCopy = actions.prepareEditedCopy,
                onExportEditedCopy = actions.exportEditedCopy,
                onDiscardEditedCopy = actions.discardEditedCopy,
                onDismiss = { selectedPhoto = null },
            )
        }
    }
}

private fun Set<String>.toggle(key: String): Set<String> = if (key in this) this - key else this + key
