package com.ssajudn.tarsika.feature.gallery.presentation

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.ui.theme.TarsikaSheetShape
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@Composable
internal fun DevicePhotoBulkActions(
    userAlbums: List<com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum>,
    onMove: (String?, String, Set<String>) -> Unit,
    onMoveToLocalTrash: suspend (
        List<DevicePhoto>,
        suspend (String) -> Boolean,
    ) -> com.ssajudn.tarsika.feature.gallery.domain.model.TrashMoveSummary,
    onDeletePhotos: suspend (List<String>, Boolean) -> DevicePhotoMutationSummary,
    selectedPhotos: List<DevicePhoto>,
    onClearSelection: () -> Unit,
    onDeleted: () -> Unit,
    onTrashed: () -> Unit,
    currentAlbumId: String? = null,
    onRemoveMembership: (() -> Unit)? = null,
) {
    if (selectedPhotos.isEmpty()) return
    val context = LocalContext.current
    var showAlbumPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTrashConfirmation by remember { mutableStateOf(false) }
    var intentContinuation by remember { mutableStateOf<Continuation<Boolean>?>(null) }
    var permissionContinuation by remember { mutableStateOf<Continuation<Boolean>?>(null) }
    val scope = rememberCoroutineScope()
    val intentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            intentContinuation?.resume(result.resultCode == Activity.RESULT_OK)
            intentContinuation = null
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionContinuation?.resume(granted)
            permissionContinuation = null
        }

    suspend fun requestIntent(sender: IntentSender): Boolean =
        suspendCancellableCoroutine { continuation ->
            intentContinuation = continuation
            intentLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }

    suspend fun requestWritePermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            permissionContinuation = continuation
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

    Surface(
        shape = TarsikaSheetShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                pluralStringResource(
                    R.plurals.selected_count_with_size,
                    selectedPhotos.size,
                    selectedPhotos.size,
                    Formatter.formatFileSize(
                        context,
                        selectedPhotos.sumOf {
                            it.sizeBytes.coerceAtLeast(0L)
                        },
                    ),
                ),
                style = MaterialTheme.typography.labelMedium,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { shareDevicePhotos(context, selectedPhotos) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text(stringResource(R.string.share_action), modifier = Modifier.padding(start = 6.dp), maxLines = 1, softWrap = false)
                }
                OutlinedButton(onClick = { showAlbumPicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Album, contentDescription = null)
                    Text(stringResource(R.string.move_to_album), modifier = Modifier.padding(start = 6.dp), maxLines = 1, softWrap = false)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showTrashConfirmation = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Text(stringResource(R.string.trash_action), modifier = Modifier.padding(start = 6.dp), maxLines = 1, softWrap = false)
                }
                OutlinedButton(onClick = { showDeleteConfirmation = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text(stringResource(R.string.delete_action), modifier = Modifier.padding(start = 6.dp), maxLines = 1, softWrap = false)
                }
            }
            if (onRemoveMembership != null) {
                Row(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onRemoveMembership, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text(
                            stringResource(R.string.remove_from_album),
                            modifier = Modifier.padding(start = 6.dp),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }

    if (showAlbumPicker) {
        AlertDialog(
            onDismissRequest = { showAlbumPicker = false },
            title = { Text(stringResource(R.string.add_to_album_title)) },
            text = {
                if (userAlbums.isEmpty()) {
                    Text(stringResource(R.string.no_custom_albums))
                } else {
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        userAlbums.forEach { album ->
                            TextButton(
                                onClick = {
                                    val keys = selectedPhotos.mapTo(mutableSetOf()) { it.favoriteKey() }
                                    onMove(currentAlbumId, album.id, keys)
                                    showAlbumPicker = false
                                    onClearSelection()
                                },
                            ) { Text("${album.name} (${album.photoKeys.size})") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAlbumPicker = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_selected_title)) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.delete_selected_description,
                        selectedPhotos.size,
                        selectedPhotos.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    val uris = selectedPhotos.mapNotNull { runCatching { it.uri.toUri() }.getOrNull() }
                    if (uris.isNotEmpty()) {
                        scope.launch {
                            runCatching {
                                when {
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                        requestPlatformMediaOperation(
                                            context.contentResolver,
                                            uris,
                                            trash = false,
                                            requestConsent = ::requestIntent,
                                        )
                                    }
                                    Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                                        deleteOnAndroidQ(
                                            context.contentResolver,
                                            uris,
                                            ::requestIntent,
                                        )
                                    else -> {
                                        val granted =
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                                PackageManager.PERMISSION_GRANTED || requestWritePermission()
                                        val uriStrings = uris.map(Uri::toString)
                                        granted && onDeletePhotos(uriStrings, false).deletedUris.containsAll(uriStrings)
                                    }
                                }
                            }.onSuccess { if (it) onDeleted() }
                        }
                    }
                }) { Text(stringResource(R.string.delete_permanently), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (showTrashConfirmation) {
        AlertDialog(
            onDismissRequest = { showTrashConfirmation = false },
            title = { Text(stringResource(R.string.trash_selected_title)) },
            text = {
                Text(
                    pluralStringResource(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            R.plurals.trash_selected_platform_description
                        } else {
                            R.plurals.trash_selected_local_description
                        },
                        selectedPhotos.size,
                        selectedPhotos.size,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showTrashConfirmation = false
                    scope.launch {
                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val uris = selectedPhotos.mapNotNull { runCatching { it.uri.toUri() }.getOrNull() }
                                if (uris.isEmpty()) {
                                    false
                                } else {
                                    requestPlatformMediaOperation(
                                        context.contentResolver,
                                        uris,
                                        trash = true,
                                        requestConsent = ::requestIntent,
                                    )
                                }
                            } else {
                                val summary =
                                    onMoveToLocalTrash(selectedPhotos) { sourceUri ->
                                        val uri = sourceUri.toUri()
                                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                                            deleteOnAndroidQ(context.contentResolver, listOf(uri), ::requestIntent)
                                        } else {
                                            val granted =
                                                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                                    PackageManager.PERMISSION_GRANTED || requestWritePermission()
                                            granted && onDeletePhotos(listOf(sourceUri), false).deletedUris.contains(sourceUri)
                                        }
                                    }
                                summary.movedPhotoKeys.isNotEmpty()
                            }
                        }.onSuccess { if (it) onTrashed() }
                    }
                }) { Text(stringResource(R.string.trash_action)) }
            },
            dismissButton = { TextButton(onClick = { showTrashConfirmation = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private suspend fun deleteOnAndroidQ(
    resolver: ContentResolver,
    uris: List<Uri>,
    requestConsent: suspend (IntentSender) -> Boolean,
): Boolean {
    for (uri in uris) {
        try {
            resolver.delete(uri, null, null)
        } catch (error: RecoverableSecurityException) {
            if (!requestConsent(error.userAction.actionIntent.intentSender)) return false
            resolver.delete(uri, null, null)
        }
    }
    return true
}

@RequiresApi(Build.VERSION_CODES.R)
private suspend fun requestPlatformMediaOperation(
    resolver: ContentResolver,
    uris: List<Uri>,
    trash: Boolean,
    requestConsent: suspend (IntentSender) -> Boolean,
): Boolean {
    for (batch in uris.chunked(2_000)) {
        val request =
            if (trash) {
                MediaStore.createTrashRequest(resolver, batch, true)
            } else {
                MediaStore.createDeleteRequest(resolver, batch)
            }
        if (!requestConsent(request.intentSender)) return false
    }
    return true
}
