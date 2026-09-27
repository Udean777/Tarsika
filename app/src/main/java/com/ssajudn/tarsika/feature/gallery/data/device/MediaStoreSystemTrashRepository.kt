package com.ssajudn.tarsika.feature.gallery.data.device

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.ssajudn.tarsika.feature.gallery.domain.model.SystemTrashPhoto
import com.ssajudn.tarsika.feature.gallery.domain.repository.SystemTrashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreSystemTrashRepository(
    private val contentResolver: ContentResolver,
) : SystemTrashRepository {
    override suspend fun queryPlatformTrash(): List<SystemTrashPhoto> =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@withContext emptyList()
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection =
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.MediaColumns.DATE_EXPIRES,
                )
            val queryArgs = Bundle().apply { putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY) }
            contentResolver.query(collection, projection, queryArgs, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val expiryColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_EXPIRES)
                buildList {
                    while (cursor.moveToNext()) {
                        val uri = Uri.withAppendedPath(collection, cursor.getLong(idColumn).toString())
                        val expiryMillis =
                            if (expiryColumn >= 0 && !cursor.isNull(expiryColumn)) {
                                cursor.getLong(expiryColumn) * MILLIS_PER_SECOND
                            } else {
                                null
                            }
                        add(
                            SystemTrashPhoto(
                                uri.toString(),
                                cursor.getString(nameColumn).orEmpty(),
                                cursor.getString(mimeColumn).orEmpty(),
                                cursor.getLong(sizeColumn),
                                expiryMillis,
                            ),
                        )
                    }
                }
            }.orEmpty()
        }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
