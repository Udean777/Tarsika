package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.TarsikaTopBar
import com.ssajudn.tarsika.core.ui.components.LocalVaultBanner
import com.ssajudn.tarsika.feature.gallery.domain.DevicePhotoDateFormatter
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import java.time.Instant

@Composable
fun DeviceAlbumsScreen(
    state: DeviceGalleryState,
    userAlbums: List<com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum>,
    albumActions: UserAlbumUiActions,
    onRefresh: () -> Unit,
    photoAccess: DevicePhotoAccessState,
    onOpenAlbum: (String) -> Unit,
    onOpenTrash: () -> Unit = {},
    onOpenVault: () -> Unit = {},
) {
    val requestPhotoAccess = photoAccess.requestAccess
    var showCreateDialog by remember { mutableStateOf(false) }
    var albumName by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        TarsikaTopBar(
            title = stringResource(R.string.nav_albums),
            actions = {
                IconButton(onClick = {
                    albumName = ""
                    showCreateDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_album))
                }
                IconButton(
                    onClick = onOpenTrash,
                ) { Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.trash_title)) }
                IconButton(
                    onClick = onOpenVault,
                ) { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.hidden_album_title)) }
                if ((state as? DeviceGalleryState.Content)?.access == PhotoLibraryAccess.SELECTED) {
                    androidx.compose.material3.TextButton(onClick = requestPhotoAccess) {
                        Text(stringResource(R.string.change_photo_access))
                    }
                }
            },
        )
        when (val current = state) {
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
                if (userAlbums.isEmpty()) {
                    GalleryMessage(
                        title = stringResource(R.string.device_albums_empty_title),
                        description =
                            stringResource(
                                if (current.access == PhotoLibraryAccess.SELECTED) {
                                    R.string.device_albums_selected_empty_description
                                } else {
                                    R.string.device_albums_empty_description
                                },
                            ),
                        actionLabel =
                            if (current.access == PhotoLibraryAccess.SELECTED) {
                                stringResource(
                                    R.string.change_photo_access,
                                )
                            } else {
                                null
                            },
                        onAction = if (current.access == PhotoLibraryAccess.SELECTED) requestPhotoAccess else null,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            stringResource(R.string.my_albums),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 164.dp),
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(userAlbums, key = { "user:${it.id}" }) { album ->
                                UserPhotoAlbumCard(album, emptyList(), {
                                    onOpenAlbum("user:${album.id}")
                                }, { albumActions.rename(album.id, it) }, { albumActions.delete(album.id) })
                            }
                        }
                    }
                }

            DeviceGalleryState.Error ->
                GalleryMessage(
                    title = stringResource(R.string.device_gallery_error_title),
                    description = stringResource(R.string.device_gallery_error_description),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                )

            is DeviceGalleryState.Content -> {
                val albums = current.albums.filter { it.photoCount > 0 }
                LocalVaultBanner(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.nav_albums), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            pluralStringResource(R.plurals.album_count, albums.size + userAlbums.size, albums.size + userAlbums.size),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (current.access == PhotoLibraryAccess.SELECTED) {
                    Text(
                        stringResource(R.string.limited_photo_access),
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (albums.isEmpty() && userAlbums.isEmpty() && !current.isInitialLoad) {
                    GalleryMessage(
                        title = stringResource(R.string.device_albums_empty_title),
                        description =
                            stringResource(
                                if (current.access == PhotoLibraryAccess.SELECTED) {
                                    R.string.device_albums_selected_empty_description
                                } else {
                                    R.string.device_albums_empty_description
                                },
                            ),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                } else {
                    val duplicateNames = albums.groupingBy { it.displayName }.eachCount()
                    BoxWithConstraints(Modifier.weight(1f)) {
                        val horizontalPadding = if (maxWidth >= 600.dp) 24.dp else 16.dp
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 164.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    start = horizontalPadding,
                                    end = horizontalPadding,
                                    top = 4.dp,
                                    bottom = 20.dp,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (userAlbums.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(R.string.my_albums),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                }
                            }
                            items(userAlbums, key = { "user:${it.id}" }) { album ->
                                UserPhotoAlbumCard(
                                    album = album,
                                    photos = current.photos.filter { it.favoriteKey() in album.photoKeys },
                                    onClick = { onOpenAlbum("user:${album.id}") },
                                    onRename = { newName -> albumActions.rename(album.id, newName) },
                                    onDelete = { albumActions.delete(album.id) },
                                )
                            }
                            if (albums.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(R.string.device_albums_heading),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                    )
                                }
                            }
                            items(albums, key = DeviceAlbum::id) { album ->
                                DeviceAlbumCard(
                                    album = album,
                                    showPath = duplicateNames[album.displayName] ?: 0 > 1,
                                    onClick = { onOpenAlbum(album.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.create_album)) },
            text = {
                OutlinedTextField(value = albumName, onValueChange = {
                    albumName = it
                }, label = { Text(stringResource(R.string.album_name)) }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    albumActions.create(albumName)
                    showCreateDialog = false
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun DeviceAlbumCard(
    album: DeviceAlbum,
    showPath: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier.semantics(mergeDescendants = true) {
                contentDescription = "${album.displayName}, ${album.photoCount} foto"
            },
    ) {
        Column {
            AsyncImage(
                model = album.coverUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(
                    album.displayName.ifBlank { stringResource(R.string.device_album_fallback_name) },
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    pluralStringResource(R.plurals.device_album_photo_count, album.photoCount, album.photoCount),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (album.newestPhotoMillis > 0L) {
                    Text(
                        DevicePhotoDateFormatter.photoTimestamp(Instant.ofEpochMilli(album.newestPhotoMillis)),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (showPath && !album.relativePath.isNullOrBlank()) {
                    Text(
                        album.relativePath,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserPhotoAlbumCard(
    album: UserPhotoAlbum,
    photos: List<DevicePhoto>,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var name by remember(album.name) { mutableStateOf(album.name) }
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            AsyncImage(
                model = photos.firstOrNull()?.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Row(
                Modifier.fillMaxWidth().padding(start = 10.dp, top = 6.dp, end = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(album.name, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.titleSmall)
                IconButton(
                    onClick = { editing = true },
                ) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.rename_album)) }
                IconButton(
                    onClick = { confirmingDelete = true },
                ) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_album)) }
            }
            Text(
                pluralStringResource(R.plurals.device_album_photo_count, album.photoKeys.size, album.photoKeys.size),
                modifier = Modifier.padding(start = 10.dp, bottom = 9.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(stringResource(R.string.rename_album)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.album_name)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(name)
                    editing = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text(stringResource(R.string.delete_album)) },
            text = { Text(stringResource(R.string.delete_album_description)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    confirmingDelete = false
                }) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
