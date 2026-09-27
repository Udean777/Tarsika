package com.ssajudn.tarsika.feature.gallery.presentation
import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.TarsikaTopBar
import com.ssajudn.tarsika.core.ui.components.LocalVaultBanner
import com.ssajudn.tarsika.feature.gallery.domain.DevicePhotoDateFormatter
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbumCatalog
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import java.time.Instant

@Composable
fun DeviceAlbumsScreen(
    state: DeviceGalleryState,
    userAlbums: List<com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum>,
    albumActions: UserAlbumUiActions,
    onRefresh: () -> Unit,
    photoAccess: DevicePhotoAccessState,
    onOpenAlbum: (String) -> Unit,
    onMoveToLocalTrash: suspend (List<DevicePhoto>, suspend (String) -> Boolean) -> TrashMoveSummary,
    onDeletePhotos: suspend (List<String>, Boolean) -> com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary,
    onOpenTrash: () -> Unit = {},
    onOpenVault: () -> Unit = {},
) {
    val requestPhotoAccess = photoAccess.requestAccess
    var showCreateDialog by remember { mutableStateOf(false) }
    var albumName by remember { mutableStateOf("") }
    var selectedPhotoKeys by remember { mutableStateOf(emptySet<String>()) }
    var selectedParentPath by remember { mutableStateOf(DEFAULT_USER_ALBUM_PARENT) }
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var pendingFolderDelete by remember { mutableStateOf<DeviceAlbum?>(null) }
    var intentContinuation by remember { mutableStateOf<Continuation<Boolean>?>(null) }
    var permissionContinuation by remember { mutableStateOf<Continuation<Boolean>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val intentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        intentContinuation?.resume(result.resultCode == Activity.RESULT_OK)
        intentContinuation = null
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionContinuation?.resume(granted)
        permissionContinuation = null
    }
    suspend fun requestIntent(sender: IntentSender): Boolean = suspendCancellableCoroutine { continuation ->
        intentContinuation = continuation
        intentLauncher.launch(IntentSenderRequest.Builder(sender).build())
    }
    suspend fun requestWritePermission(): Boolean = suspendCancellableCoroutine { continuation ->
        permissionContinuation = continuation
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    suspend fun trashAlbum(photos: List<DevicePhoto>): TrashMoveSummary {
        if (photos.isEmpty()) return TrashMoveSummary(emptySet(), emptySet())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = photos.mapNotNull { runCatching { it.uri.toUri() }.getOrNull() }
            if (uris.isEmpty()) return TrashMoveSummary(emptySet(), photos.mapTo(mutableSetOf()) { it.favoriteKey() })
            val movedUris = mutableSetOf<String>()
            for (batch in uris.chunked(2_000)) {
                if (!requestIntent(MediaStore.createTrashRequest(context.contentResolver, batch, true).intentSender)) break
                movedUris += batch.map { it.toString() }
            }
            if (movedUris.isNotEmpty()) onRefresh()
            val movedKeys = photos.filter { it.uri in movedUris }.mapTo(mutableSetOf()) { it.favoriteKey() }
            val allKeys = photos.mapTo(mutableSetOf()) { it.favoriteKey() }
            return TrashMoveSummary(movedKeys, allKeys - movedKeys)
        }
        val summary = onMoveToLocalTrash(photos) { sourceUri ->
            val uri = sourceUri.toUri()
            try {
                context.contentResolver.delete(uri, null, null) > 0
            } catch (error: RecoverableSecurityException) {
                if (requestIntent(error.userAction.actionIntent.intentSender)) {
                    context.contentResolver.delete(uri, null, null) > 0
                } else {
                    false
                }
            } catch (_: SecurityException) {
                val granted =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                        requestWritePermission()
                granted && onDeletePhotos(listOf(sourceUri), false).deletedUris.contains(sourceUri)
            }
        }
        if (summary.movedPhotoKeys.isNotEmpty()) onRefresh()
        return summary
    }
    suspend fun trashUserAlbumContents(
        album: UserPhotoAlbum,
        photos: List<DevicePhoto>,
    ) {
        val albumPhotos = photos.filter { it.favoriteKey() in album.photoKeys }
        val movedKeys = trashAlbum(albumPhotos).movedPhotoKeys.intersect(album.photoKeys)
        if (movedKeys.isEmpty()) return
        if (movedKeys.containsAll(album.photoKeys)) {
            albumActions.delete(album.id)
        } else {
            albumActions.removePhotos(album.id, movedKeys)
        }
    }
    val content = state as? DeviceGalleryState.Content
    val availablePhotos = content?.photos.orEmpty()
    val availablePhotoKeys = availablePhotos.mapTo(mutableSetOf()) { it.favoriteKey() }
    val visibleUserAlbums = userAlbums.filter { album -> album.photoKeys.any { it in availablePhotoKeys } }
    val folderOptions =
        (listOf(DEFAULT_USER_ALBUM_PARENT) + content?.albums.orEmpty().mapNotNull { it.relativePath })
            .map { it.trim().trim('/') }
            .filter(String::isNotBlank)
            .distinct()
    Column(Modifier.fillMaxSize()) {
        TarsikaTopBar(
            title = stringResource(R.string.nav_albums),
            actions = {
                IconButton(onClick = {
                    albumName = ""
                    selectedPhotoKeys = emptySet()
                    selectedParentPath = DEFAULT_USER_ALBUM_PARENT
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
                if (visibleUserAlbums.isEmpty()) {
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
                            items(visibleUserAlbums, key = { "user:${it.id}" }) { album ->
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
                            pluralStringResource(R.plurals.album_count, albums.size + visibleUserAlbums.size, albums.size + visibleUserAlbums.size),
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
                if (albums.isEmpty() && visibleUserAlbums.isEmpty() && !current.isInitialLoad) {
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
                            if (visibleUserAlbums.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(R.string.my_albums),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                }
                            }
                            items(visibleUserAlbums, key = { "user:${it.id}" }) { album ->
                                UserPhotoAlbumCard(
                                    album = album,
                                    photos = current.photos.filter { it.favoriteKey() in album.photoKeys },
                                    onClick = { onOpenAlbum("user:${album.id}") },
                                    onRename = { newName -> albumActions.rename(album.id, newName) },
                                    onDelete = { scope.launch { runCatching { trashUserAlbumContents(album, current.photos) } } },
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
                                    onDelete = { pendingFolderDelete = album },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    pendingFolderDelete?.let { album ->
        AlertDialog(
            onDismissRequest = { pendingFolderDelete = null },
            title = { Text(stringResource(R.string.delete_device_album_title, album.displayName)) },
            text = { Text(pluralStringResource(R.plurals.delete_device_album_description, album.photoCount, album.photoCount)) },
            confirmButton = {
                TextButton(onClick = {
                    val photos = content?.photos.orEmpty().filter { DeviceAlbumCatalog.albumKey(it) == album.id }
                    pendingFolderDelete = null
                    scope.launch { runCatching { trashAlbum(photos) } }
                }) { Text(stringResource(R.string.trash_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingFolderDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                folderMenuExpanded = false
            },
            title = { Text(stringResource(R.string.create_album)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = albumName,
                        onValueChange = { value ->
                            if (value.none { it == '/' || it == '\\' }) albumName = value
                        },
                        label = { Text(stringResource(R.string.album_name)) },
                        singleLine = true,
                    )
                    Text(stringResource(R.string.album_parent_folder), style = MaterialTheme.typography.labelLarge)
                    Box {
                        androidx.compose.material3.OutlinedButton(onClick = { folderMenuExpanded = true }) {
                            Text(selectedParentPath, maxLines = 1)
                        }
                        DropdownMenu(expanded = folderMenuExpanded, onDismissRequest = { folderMenuExpanded = false }) {
                            folderOptions.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder) },
                                    onClick = {
                                        selectedParentPath = folder
                                        folderMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.album_initial_photos), style = MaterialTheme.typography.labelLarge)
                    if (availablePhotos.isEmpty()) {
                        Text(
                            stringResource(R.string.album_requires_photo),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 72.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(availablePhotos, key = DevicePhoto::favoriteKey) { photo ->
                                val key = photo.favoriteKey()
                                val isSelected = key in selectedPhotoKeys
                                Surface(
                                    onClick = {
                                        selectedPhotoKeys =
                                            if (isSelected) selectedPhotoKeys - key else selectedPhotoKeys + key
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.aspectRatio(1f),
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = photo.uri,
                                            contentDescription = photo.displayName,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop,
                                        )
                                        if (isSelected) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                            ) {
                                                Text(
                                                    "✓",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.album_selected_photo_count, selectedPhotoKeys.size),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    albumActions.create(albumName, selectedParentPath, selectedPhotoKeys)
                    showCreateDialog = false
                }, enabled = albumName.isNotBlank() && selectedPhotoKeys.isNotEmpty()) { Text(stringResource(R.string.create)) }
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
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                    role = Role.Button,
                ).semantics(mergeDescendants = true) {
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
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.open_album)) },
                onClick = {
                    menuExpanded = false
                    onClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_device_album_action)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    onDelete()
                },
            )
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
    var menuExpanded by remember { mutableStateOf(false) }
    var name by remember(album.name) { mutableStateOf(album.name) }
    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true },
                role = Role.Button,
            ),
        ) {
            Column {
                AsyncImage(
                    model = photos.firstOrNull()?.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Row(
                    Modifier.fillMaxWidth().padding(start = 10.dp, top = 6.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(album.name, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    pluralStringResource(R.plurals.device_album_photo_count, album.photoKeys.size, album.photoKeys.size),
                    modifier = Modifier.padding(start = 10.dp, bottom = 9.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    album.relativePath,
                    modifier = Modifier.padding(start = 10.dp, bottom = 9.dp),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.open_album)) },
                onClick = {
                    menuExpanded = false
                    onClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.rename_album)) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    editing = true
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_album)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    confirmingDelete = true
                },
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

private const val DEFAULT_USER_ALBUM_PARENT = "Pictures/Tarsika"
