package com.ssajudn.tarsika.feature.gallery.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun VaultPhotoTile(
    photo: VaultPhoto,
    unlocked: Boolean,
    readPhoto: suspend (String) -> com.ssajudn.tarsika.feature.gallery.domain.model.DecryptedVaultPhoto,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var thumbnail by remember(photo.id) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(unlocked, photo.id) {
        thumbnail?.recycle()
        thumbnail = null
        if (unlocked) {
            runCatching {
                val payload = readPhoto(photo.id)
                withContext(Dispatchers.Default) {
                    try {
                        decodeVaultPreview(photo.id, payload.bytes, 192)
                    } finally {
                        payload.bytes.fill(0)
                    }
                }
            }.onSuccess { thumbnail = it.bitmap }
        }
    }
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.aspectRatio(1f)) {
        Box {
            thumbnail?.let {
                Image(
                    it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable(onClick = onOpen),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_action),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
    DisposableEffect(photo.id) {
        onDispose {
            thumbnail?.recycle()
        }
    }
}
