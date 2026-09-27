package com.ssajudn.tarsika.feature.gallery.presentation
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto

@Composable
internal fun ZoomablePhoto(
    photo: DevicePhoto,
    modifier: Modifier = Modifier,
) {
    var scale by remember(photo.uri) { mutableFloatStateOf(1f) }
    var offsetX by remember(photo.uri) { mutableFloatStateOf(0f) }
    var offsetY by remember(photo.uri) { mutableFloatStateOf(0f) }
    var imageUnavailable by remember(photo.uri) { mutableStateOf(false) }
    val transformState =
        remember(photo.uri) {
            TransformableState { _, zoomChange, panChange, _ ->
                val nextScale = (scale * zoomChange).coerceIn(1f, 5f)
                scale = nextScale
                if (nextScale > 1f) {
                    offsetX += panChange.x
                    offsetY += panChange.y
                } else {
                    offsetX = 0f
                    offsetY = 0f
                }
            }
        }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = photo.uri.toUri(),
            contentDescription = photo.displayName,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY)
                    .transformable(state = transformState, canPan = { scale > 1f }),
            contentScale = ContentScale.Fit,
            onError = { imageUnavailable = true },
            onSuccess = { imageUnavailable = false },
        )
        if (imageUnavailable) {
            Text(
                stringResource(R.string.photo_unavailable),
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
