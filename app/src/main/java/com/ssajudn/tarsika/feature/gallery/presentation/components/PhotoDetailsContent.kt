package com.ssajudn.tarsika.feature.gallery.presentation
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.DevicePhotoDateFormatter
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoExifMetadata
import java.time.Instant

@Composable
internal fun PhotoDetailsContent(
    photo: DevicePhoto,
    exif: PhotoExifMetadata?,
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth().padding(bottom = 20.dp),
    ) {
        Text(
            stringResource(R.string.photo_details),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            photo.displayName,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            if (photo.dateTakenMillis > 0L) {
                DevicePhotoDateFormatter.photoTimestamp(
                    Instant.ofEpochMilli(photo.dateTakenMillis),
                )
            } else {
                stringResource(R.string.metadata_unavailable)
            },
            modifier = Modifier.padding(top = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailCard(
                stringResource(R.string.file_size),
                if (photo.sizeBytes > 0L) {
                    Formatter.formatFileSize(
                        context,
                        photo.sizeBytes,
                    )
                } else {
                    stringResource(R.string.metadata_unavailable)
                },
                Modifier.weight(1f),
            )
            DetailCard(
                stringResource(R.string.photo_dimensions),
                if (photo.width > 0 && photo.height > 0) {
                    stringResource(
                        R.string.photo_dimensions_value,
                        photo.width,
                        photo.height,
                    )
                } else {
                    stringResource(R.string.metadata_unavailable)
                },
                Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        DetailCard(
            stringResource(R.string.photo_folder),
            photo.relativePath?.trimEnd('/')?.takeIf(String::isNotBlank)
                ?: photo.bucketDisplayName?.takeIf(String::isNotBlank)
                ?: stringResource(R.string.metadata_unavailable),
            Modifier.fillMaxWidth(),
        )
        if (exif != null && exif.hasValues) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.camera_metadata),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.height(8.dp))
            listOfNotNull(
                exif.camera?.let { stringResource(R.string.camera_model) to it },
                exif.lens?.let { stringResource(R.string.lens_model) to it },
                exif.aperture?.let { stringResource(R.string.aperture) to it },
                exif.exposure?.let { stringResource(R.string.shutter_speed) to it },
                exif.iso?.let { stringResource(R.string.iso) to it },
                exif.focalLength?.let { stringResource(R.string.focal_length) to it },
                exif.location?.let { stringResource(R.string.gps_location) to it },
            ).forEach { (label, value) ->
                DetailCard(label, value, Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun DetailCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
