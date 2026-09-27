package com.ssajudn.tarsika.feature.gallery.presentation
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto

internal fun shareDevicePhotos(
    context: Context,
    photos: List<DevicePhoto>,
) {
    val uris = photos.mapNotNull { runCatching { it.uri.toUri() }.getOrNull() }
    if (uris.isEmpty()) return
    val intent =
        if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.clipData =
        ClipData.newUri(context.contentResolver, "Foto", uris.first()).apply {
            uris.drop(1).forEach { addItem(ClipData.Item(it)) }
        }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_photos)))
}
