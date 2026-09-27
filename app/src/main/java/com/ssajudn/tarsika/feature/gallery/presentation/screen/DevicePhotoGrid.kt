package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.ArchiveDateLabel
import com.ssajudn.tarsika.feature.gallery.domain.DevicePhotoDateFormatter
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import java.time.Instant

@Composable
internal fun DevicePhotoGrid(
    photos: List<DevicePhoto>,
    onOpen: (DevicePhoto) -> Unit,
    modifier: Modifier = Modifier,
    favoriteKeys: Set<String> = emptySet(),
    selectedKeys: Set<String> = emptySet(),
    selectionMode: Boolean = selectedKeys.isNotEmpty(),
    onToggleSelection: (DevicePhoto) -> Unit = {},
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val columns =
            when {
                maxWidth >= 900.dp -> 5
                maxWidth >= 600.dp -> 4
                else -> 3
            }
        val horizontalInset = if (maxWidth >= 600.dp) 24.dp else 16.dp
        val dateUnavailable = stringResource(R.string.photo_date_unknown)
        val rows =
            photos.groupBy { photo ->
                if (photo.dateTakenMillis > 0L) {
                    DevicePhotoDateFormatter.galleryDay(Instant.ofEpochMilli(photo.dateTakenMillis))
                } else {
                    dateUnavailable
                }
            }
        // Per-photo items let the remaining images animate into positions left by removed photos.
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = horizontalInset, top = 8.dp, end = horizontalInset, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            rows.forEach { (date, dayPhotos) ->
                item(
                    key = "device-photo-date-$date",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    ArchiveDateLabel(
                        date,
                        Modifier.padding(start = 8.dp, top = 14.dp, bottom = 8.dp, end = 8.dp),
                        count = dayPhotos.size,
                    )
                }
                items(
                    items = dayPhotos,
                    key = { photo -> "device-photo-${photo.favoriteKey()}" },
                ) { photo ->
                    val key = photo.favoriteKey()
                    DevicePhotoTile(
                        photo = photo,
                        isFavorite = key in favoriteKeys,
                        isSelected = key in selectedKeys,
                        selectionMode = selectionMode,
                        onOpen = { onOpen(photo) },
                        onToggleSelection = { onToggleSelection(photo) },
                        modifier = Modifier.animateItem().aspectRatio(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DevicePhotoTile(
    photo: DevicePhoto,
    isFavorite: Boolean,
    isSelected: Boolean,
    selectionMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = if (selectionMode) onToggleSelection else onOpen
    val stateLabel =
        buildList {
            add(photo.displayName.ifBlank { stringResource(R.string.gallery_photo_description) })
            if (isFavorite) add(stringResource(R.string.favorite_state_label))
            if (isSelected) add(stringResource(R.string.selected_state_label))
        }.joinToString(", ")
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(onClick = action, onLongClick = onToggleSelection, role = Role.Button)
                .semantics(mergeDescendants = true) {
                    contentDescription = stateLabel
                    selected = isSelected
                },
    ) {
        Box {
            AsyncImage(
                model = photo.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (isFavorite || selectionMode) {
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.60f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        } else if (isFavorite) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GalleryMessage(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    loading: Boolean = false,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(Modifier.size(34.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                Icons.Default.Collections,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (description != null) {
            Spacer(Modifier.size(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.size(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
