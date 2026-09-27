package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import kotlinx.coroutines.launch

@Composable
internal fun PhotoEditorDialog(
    photo: DevicePhoto,
    onPrepareCopy: suspend (DevicePhoto, PhotoCropRequest) -> PreparedPhotoCopy,
    onExportCopy: suspend (PreparedPhotoCopy, String) -> Unit,
    onDiscardCopy: suspend (PreparedPhotoCopy) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val exportErrorMessage = stringResource(R.string.editor_export_error)
    val scope = rememberCoroutineScope()
    val ratios = listOf(0f, 1f, 4f / 3f, 16f / 9f)
    val ratioLabels = listOf(R.string.original_ratio, R.string.square_ratio, R.string.landscape_ratio, R.string.wide_ratio)
    var selectedRatio by remember(photo.uri) { mutableIntStateOf(0) }
    var rotation by remember(photo.uri) { mutableIntStateOf(0) }
    var focusX by remember(photo.uri) { mutableFloatStateOf(0.5f) }
    var focusY by remember(photo.uri) { mutableFloatStateOf(0.5f) }
    var exporting by remember { mutableStateOf(false) }
    var preparedCopy by remember { mutableStateOf<PreparedPhotoCopy?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val saveCopy =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { destination ->
            val copy = preparedCopy
            if (destination != null && copy != null) {
                scope.launch {
                    runCatching { onExportCopy(copy, destination.toString()) }
                        .onSuccess {
                            preparedCopy = null
                            onDismiss()
                        }
                        .onFailure { error = exportErrorMessage }
                }
            } else {
                if (copy != null) scope.launch { onDiscardCopy(copy) }
                preparedCopy = null
            }
        }
    val originalRatio = if (photo.width > 0 && photo.height > 0) photo.width.toFloat() / photo.height else 1f
    val previewRatio =
        when (rotation % 180) {
            90 -> 1f / originalRatio
            else -> originalRatio
        }
    val ratio = if (selectedRatio == 0) previewRatio else ratios[selectedRatio]
    val cropAlignment =
        Alignment { size, space, _ ->
            IntOffset(((space.width - size.width) * focusX).toInt(), ((space.height - size.height) * focusY).toInt())
        }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
                    Text(stringResource(R.string.edit_photo), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = {
                        rotation = (rotation + 90) % 360
                    }) { Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = stringResource(R.string.rotate_photo)) }
                }
                Text(
                    stringResource(R.string.editor_copy_note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.displayName,
                        modifier =
                            Modifier.fillMaxWidth().aspectRatio(
                                ratio.coerceIn(0.45f, 2.2f),
                            ).clip(RoundedCornerShape(8.dp)).graphicsLayer(rotationZ = rotation.toFloat()),
                        contentScale = ContentScale.Crop,
                        alignment = cropAlignment,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ratioLabels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = index == selectedRatio,
                            onClick = { selectedRatio = index },
                            label = { Text(stringResource(label)) },
                        )
                    }
                }
                Text(stringResource(R.string.crop_horizontal), style = MaterialTheme.typography.labelMedium)
                Slider(value = focusX, onValueChange = { focusX = it })
                Text(stringResource(R.string.crop_vertical), style = MaterialTheme.typography.labelMedium)
                Slider(value = focusY, onValueChange = { focusY = it })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp)) }
                Button(
                    onClick = {
                        exporting = true
                        error = null
                        scope.launch {
                            runCatching {
                                onPrepareCopy(photo, PhotoCropRequest(rotation, ratio, focusX, focusY))
                            }.onSuccess { copy ->
                                exporting = false
                                preparedCopy = copy
                                saveCopy.launch(copy.suggestedFileName)
                            }.onFailure {
                                exporting = false
                                error = exportErrorMessage
                            }
                        }
                    },
                    enabled = !exporting,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(stringResource(if (exporting) R.string.exporting_photo else R.string.save_edited_copy)) }
            }
        }
    }
}
