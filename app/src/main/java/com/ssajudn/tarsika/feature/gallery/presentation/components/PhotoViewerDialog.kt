package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoExifMetadata
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotoViewerDialog(
    photos: List<DevicePhoto>,
    initialPhotoIndex: Int,
    favoriteKeys: Set<String>,
    onToggleFavorite: (DevicePhoto) -> Unit,
    onReadExif: suspend (String) -> PhotoExifMetadata,
    onPrepareEditedCopy: suspend (DevicePhoto, PhotoCropRequest) -> PreparedPhotoCopy,
    onExportEditedCopy: suspend (PreparedPhotoCopy, String) -> Unit,
    onDiscardEditedCopy: suspend (PreparedPhotoCopy) -> Unit,
    onDismiss: () -> Unit,
) {
    if (photos.isEmpty()) return
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialPhotoIndex.coerceIn(0, photos.lastIndex)) { photos.size }
    val currentIndex = pagerState.currentPage.coerceIn(photos.indices)
    val currentPhoto = photos[currentIndex]
    var detailsVisible by remember { mutableStateOf(true) }
    var editorVisible by remember { mutableStateOf(false) }
    var exif by remember(currentPhoto.uri) { mutableStateOf<PhotoExifMetadata?>(null) }
    LaunchedEffect(currentPhoto.uri) {
        exif = null
        exif = onReadExif(currentPhoto.uri)
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close_photo_viewer))
                    }
                    Text(
                        stringResource(R.string.photo_details),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { shareDevicePhotos(context, listOf(currentPhoto)) }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share_photo))
                    }
                    IconButton(onClick = { onToggleFavorite(currentPhoto) }) {
                        Icon(
                            imageVector =
                                if (currentPhoto.favoriteKey() in favoriteKeys) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                            contentDescription =
                                stringResource(
                                    if (currentPhoto.favoriteKey() in favoriteKeys) R.string.remove_favorite else R.string.add_favorite,
                                ),
                            tint =
                                if (currentPhoto.favoriteKey() in favoriteKeys) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        )
                    }
                    IconButton(onClick = { detailsVisible = !detailsVisible }) {
                        Icon(
                            imageVector = if (detailsVisible) Icons.Default.VisibilityOff else Icons.Default.Info,
                            contentDescription =
                                stringResource(
                                    if (detailsVisible) R.string.hide_photo_details else R.string.show_photo_details,
                                ),
                        )
                    }
                    IconButton(onClick = { editorVisible = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_photo))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.local_photo_status),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        pluralStringResource(
                            R.plurals.photo_position,
                            currentIndex + 1,
                            currentIndex + 1,
                            photos.size,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)),
                ) { page ->
                    ZoomablePhoto(photos[page], Modifier.fillMaxSize())
                }
                if (detailsVisible) {
                    Column(Modifier.weight(1.15f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        PhotoDetailsContent(currentPhoto, exif)
                    }
                }
            }
        }
    }
    if (editorVisible) {
        PhotoEditorDialog(
            photo = currentPhoto,
            onPrepareCopy = onPrepareEditedCopy,
            onExportCopy = onExportEditedCopy,
            onDiscardCopy = onDiscardEditedCopy,
            onDismiss = { editorVisible = false },
        )
    }
}
