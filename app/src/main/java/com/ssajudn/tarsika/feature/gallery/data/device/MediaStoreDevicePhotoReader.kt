package com.ssajudn.tarsika.feature.gallery.data.device

import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.ssajudn.tarsika.feature.gallery.domain.model.DeviceAlbumCatalog
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoLibrary
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaStoreDevicePhotoReader(
    private val contentResolver: ContentResolver,
) : DevicePhotoReader {
    private val collectionUri: Uri
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

    override fun observeDeviceLibrary(): Flow<DevicePhotoLibrary> =
        callbackFlow {
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(
                        selfChange: Boolean,
                        uri: Uri?,
                    ) {
                        trySend(Unit)
                    }
                }
            contentResolver.registerContentObserver(
                collectionUri,
                true,
                observer,
            )
            trySend(Unit)
            awaitClose { contentResolver.unregisterContentObserver(observer) }
        }.conflate()
            .map {
                withContext(Dispatchers.IO) {
                    val photos = queryDevicePhotos()
                    DevicePhotoLibrary(photos, DeviceAlbumCatalog.fromPhotos(photos))
                }
            }

    private fun queryDevicePhotos(): List<DevicePhoto> {
        val projection =
            buildList {
                addAll(
                    listOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                        MediaStore.Images.ImageColumns.BUCKET_ID,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    ),
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(MediaStore.Images.Media.RELATIVE_PATH)
                    add(MediaStore.MediaColumns.VOLUME_NAME)
                }
            }.toTypedArray()
        val sortOrder =
            "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        return contentResolver.query(
            collectionUri,
            projection,
            null,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)
            val bucketNameColumn =
                cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
            val relativePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val volumeNameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.VOLUME_NAME)
            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * MILLIS_PER_SECOND
                    val volumeName =
                        if (volumeNameColumn >= 0) cursor.getString(volumeNameColumn) else null
                    val itemCollectionUri = itemCollectionUri(volumeName)
                    add(
                        DevicePhoto(
                            id = id,
                            uri = ContentUris.withAppendedId(itemCollectionUri, id).toString(),
                            displayName = cursor.getString(nameColumn).orEmpty(),
                            dateTakenMillis = dateTaken.takeIf { it > 0L } ?: dateAdded,
                            sizeBytes =
                                if (sizeColumn >= 0) {
                                    cursor.getLong(sizeColumn)
                                        .coerceAtLeast(0L)
                                } else {
                                    0L
                                },
                            width = cursor.getInt(widthColumn),
                            height = cursor.getInt(heightColumn),
                            volumeName = volumeName ?: DEFAULT_VOLUME_NAME,
                            bucketId = cursor.stringOrNull(bucketIdColumn),
                            bucketDisplayName = cursor.stringOrNull(bucketNameColumn),
                            relativePath = cursor.stringOrNull(relativePathColumn),
                            mimeType = cursor.stringOrNull(mimeTypeColumn),
                        ),
                    )
                }
            }.sortedByDescending(DevicePhoto::dateTakenMillis)
        }.orEmpty()
    }

    private fun itemCollectionUri(volumeName: String?): Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && volumeName != null) {
            MediaStore.Images.Media.getContentUri(volumeName)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    private fun android.database.Cursor.stringOrNull(columnIndex: Int): String? =
        if (columnIndex >= 0 && !isNull(columnIndex)) getString(columnIndex) else null

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
        const val DEFAULT_VOLUME_NAME = "external"
    }
}
