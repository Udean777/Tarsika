package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.TarsikaTopBar
import com.ssajudn.tarsika.core.ui.components.LocalVaultBanner
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.feature.gallery.domain.usecase.SearchDevicePhotosUseCase

@Composable
internal fun DevicePhotoSearchScreen(
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
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<DevicePhoto?>(null) }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var selectionMode by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val content = state as? DeviceGalleryState.Content
    val results =
        remember(content?.photos, query, favoriteKeys, favoritesOnly) {
            SearchDevicePhotosUseCase(content?.photos.orEmpty(), query, favoriteKeys, favoritesOnly)
        }
    val selectedPhotos = content?.photos.orEmpty().filter { it.favoriteKey() in selectedKeys }

    Column(Modifier.fillMaxSize()) {
        TarsikaTopBar(
            title =
                when {
                    !selectionMode -> stringResource(R.string.nav_search)
                    selectedKeys.isEmpty() -> stringResource(R.string.selecting_photos)
                    else -> pluralStringResource(R.plurals.selected_photos_count, selectedKeys.size, selectedKeys.size)
                },
            onBack =
                if (selectionMode) {
                    (
                        {
                            selectedKeys = emptySet()
                            selectionMode = false
                        }
                    )
                } else {
                    onBack
                },
            actions = {
                if (selectionMode) {
                    IconButton(onClick = {
                        selectedKeys = emptySet()
                        selectionMode = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_selection))
                    }
                } else if (results.isNotEmpty()) {
                    IconButton(onClick = { selectionMode = true }) {
                        Icon(Icons.Default.CheckBox, contentDescription = stringResource(R.string.select_photos))
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
                    onAction = photoAccess.requestAccess,
                    permissionDenied = photoAccess.permissionDenied,
                    modifier = Modifier.weight(1f),
                )

            is DeviceGalleryState.Error ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_error_title),
                    description = stringResource(R.string.device_gallery_error_description),
                    actionLabel = stringResource(R.string.retry),
                    onAction = actions.refresh,
                    modifier = Modifier.fillMaxSize(),
                )

            is DeviceGalleryState.Empty ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_empty_title),
                    description = stringResource(R.string.device_gallery_empty_description),
                    modifier = Modifier.fillMaxSize(),
                )

            is DeviceGalleryState.Content -> {
                LocalVaultBanner(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                    label = { Text(stringResource(R.string.search_device_photos)) },
                    placeholder = { Text(stringResource(R.string.search_device_photos_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = !favoritesOnly },
                        label = { Text(stringResource(R.string.favorites_only)) },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    )
                    Text(
                        pluralStringResource(R.plurals.search_results_count, results.size, results.size),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (results.isEmpty() && !state.isInitialLoad) {
                    GalleryMessage(
                        title =
                            stringResource(
                                if (favoritesOnly && query.isBlank()) {
                                    R.string.no_favorite_photos
                                } else {
                                    R.string.no_device_photo_search_results
                                },
                            ),
                        description = stringResource(R.string.no_device_photo_search_results_description),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                } else if (results.isNotEmpty()) {
                    DevicePhotoGrid(
                        photos = results,
                        favoriteKeys = favoriteKeys,
                        selectedKeys = selectedKeys,
                        selectionMode = selectionMode,
                        onToggleSelection = { photo ->
                            selectionMode = true
                            val key = photo.favoriteKey()
                            selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key
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
                        selectedPhotos = selectedPhotos,
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

    selectedPhoto?.let { photo ->
        val index = results.indexOfFirst { it.favoriteKey() == photo.favoriteKey() }
        if (index >= 0) {
            PhotoViewerDialog(
                photos = results,
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
