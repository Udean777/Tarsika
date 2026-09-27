package com.ssajudn.tarsika.feature.gallery.presentation
import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.DevicePhotoDateFormatter
import com.ssajudn.tarsika.feature.gallery.domain.model.LocalTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.SystemTrashPhoto
import kotlinx.coroutines.launch

@Composable
internal fun DeviceTrashScreen(
    trashState: DeviceTrashState,
    actions: TrashUiActions,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val localItems = trashState.localItems
    val platformItems = trashState.platformItems
    var refreshToken by remember { mutableIntStateOf(0) }
    var platformToDelete by remember { mutableStateOf<SystemTrashPhoto?>(null) }
    var localToRestore by remember { mutableStateOf<LocalTrashPhoto?>(null) }
    var localToDelete by remember { mutableStateOf<LocalTrashPhoto?>(null) }
    var selectedItem by remember { mutableStateOf<TrashGridItem?>(null) }
    val queryError = trashState.platformLoadFailed
    val gridItems = mutableListOf<TrashGridItem>()
    for (item in platformItems) {
        gridItems +=
            TrashGridItem(
                key = "platform-${item.uri}",
                uri = item.uri,
                name = item.name,
                detail =
                    item.expiresAtMillis?.let {
                        stringResource(
                            R.string.trash_expires_at,
                            DevicePhotoDateFormatter.photoTimestamp(java.time.Instant.ofEpochMilli(it)),
                        )
                    } ?: stringResource(R.string.trash_expiry_unknown),
                size = Formatter.formatFileSize(context, item.sizeBytes),
                platformPhoto = item,
            )
    }
    for (item in localItems) {
        gridItems +=
            TrashGridItem(
                key = "local-${item.id}",
                uri = item.thumbnailUri,
                name = item.displayName,
                detail =
                    stringResource(
                        R.string.trash_local_since,
                        DevicePhotoDateFormatter.photoTimestamp(java.time.Instant.ofEpochMilli(item.trashedAtMillis)),
                    ),
                size = Formatter.formatFileSize(context, item.sizeBytes),
                localPhoto = item,
            )
    }
    LaunchedEffect(refreshToken) { actions.refreshPlatform() }
    val platformLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                refreshToken++
                actions.refreshPlatform()
            }
            platformToDelete = null
        }
    val scope = rememberCoroutineScope()
    val createDocument =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/*")) { destination ->
            val entry = localToRestore
            if (destination != null && entry != null) {
                scope.launch {
                    actions.restore(entry, destination.toString())
                    localToRestore = null
                    refreshToken++
                }
            }
        }

    Column(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(
            Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
            Text(stringResource(R.string.trash_title), style = MaterialTheme.typography.titleLarge)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Text(
                stringResource(R.string.legacy_trash_note),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (queryError) {
            Text(
                stringResource(R.string.trash_load_error),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (gridItems.isEmpty() && !queryError) {
            GalleryMessage(
                title = stringResource(R.string.trash_empty_title),
                description = stringResource(R.string.trash_empty_description),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val columns =
                    when {
                        maxWidth >= 900.dp -> 5
                        maxWidth >= 600.dp -> 4
                        else -> 3
                    }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(gridItems, key = { it.key }) { item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier =
                                Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(onClick = { selectedItem = item }, role = Role.Button)
                                    .semantics { contentDescription = item.name },
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                item.uri?.let { uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(item.name) },
            text = { Text("${item.detail} • ${item.size}") },
            confirmButton = {
                TextButton(onClick = {
                    item.platformPhoto?.let { photo ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            platformLauncher.launch(
                                IntentSenderRequest.Builder(
                                    createPlatformTrashRequest(context.contentResolver, photo.uri.toUri()),
                                ).build(),
                            )
                        }
                    }
                    item.localPhoto?.let { photo ->
                        localToRestore = photo
                        createDocument.launch(photo.displayName)
                    }
                    selectedItem = null
                }) { Text(stringResource(R.string.restore)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    item.platformPhoto?.let { platformToDelete = it }
                    item.localPhoto?.let { localToDelete = it }
                    selectedItem = null
                }) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
        )
    }
    platformToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { platformToDelete = null },
            title = { Text(stringResource(R.string.delete_permanently)) },
            text = { Text(stringResource(R.string.trash_permanent_confirmation, item.name)) },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        platformLauncher.launch(
                            IntentSenderRequest.Builder(
                                createPlatformDeleteRequest(context.contentResolver, item.uri.toUri()),
                            ).build(),
                        )
                    }
                }) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { platformToDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    localToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { localToDelete = null },
            title = { Text(stringResource(R.string.delete_permanently)) },
            text = { Text(stringResource(R.string.trash_permanent_confirmation, item.displayName)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.deleteForever(item.id)
                    localToDelete = null
                }) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { localToDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun createPlatformTrashRequest(
    resolver: android.content.ContentResolver,
    uri: android.net.Uri,
): android.content.IntentSender = MediaStore.createTrashRequest(resolver, listOf(uri), false).intentSender

@RequiresApi(Build.VERSION_CODES.R)
private fun createPlatformDeleteRequest(
    resolver: android.content.ContentResolver,
    uri: android.net.Uri,
): android.content.IntentSender = MediaStore.createDeleteRequest(resolver, listOf(uri)).intentSender

private data class TrashGridItem(
    val key: String,
    val uri: String?,
    val name: String,
    val detail: String,
    val size: String,
    val platformPhoto: SystemTrashPhoto? = null,
    val localPhoto: LocalTrashPhoto? = null,
)
