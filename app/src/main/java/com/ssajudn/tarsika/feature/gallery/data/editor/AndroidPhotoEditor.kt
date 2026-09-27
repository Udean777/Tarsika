package com.ssajudn.tarsika.feature.gallery.data.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoCropRequest
import com.ssajudn.tarsika.feature.gallery.domain.model.PreparedPhotoCopy
import com.ssajudn.tarsika.feature.gallery.domain.repository.PhotoEditorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidPhotoEditor(
    private val contentResolver: ContentResolver,
    private val cacheDirectory: File,
) : PhotoEditorRepository {
    init {
        cacheDirectory.mkdirs()
    }

    override suspend fun prepareCopy(
        sourceUri: String,
        sourceName: String,
        crop: PhotoCropRequest,
    ): PreparedPhotoCopy =
        withContext(Dispatchers.IO) {
            val source = decodeSource(contentResolver, sourceUri.toUri())
            var transformed: Bitmap? = null
            try {
                transformed = transformBitmap(source, crop)
                val file = File.createTempFile(TEMP_FILE_PREFIX, ".jpg", cacheDirectory)
                try {
                    file.outputStream().use { output ->
                        check(
                            transformed.compress(
                                Bitmap.CompressFormat.JPEG,
                                JPEG_QUALITY,
                                output,
                            ),
                        )
                    }
                    val baseName =
                        sourceName
                            .substringAfterLast('/')
                            .substringAfterLast('\\')
                            .substringBeforeLast('.', sourceName)
                            .filter { it.isLetterOrDigit() || it == ' ' || it == '-' || it == '_' }
                            .trim()
                            .take(MAX_FILE_NAME_LENGTH)
                            .ifBlank { DEFAULT_FILE_NAME }
                    PreparedPhotoCopy(file.absolutePath, "${baseName}_edited.jpg")
                } catch (error: Throwable) {
                    file.delete()
                    throw error
                }
            } finally {
                if (transformed != null && transformed !== source) transformed.recycle()
                source.recycle()
            }
        }

    override suspend fun discardPreparedCopy(copy: PreparedPhotoCopy) =
        withContext(Dispatchers.IO) {
            deletePreparedFile(copy)
            Unit
        }

    override suspend fun exportCopy(
        copy: PreparedPhotoCopy,
        destinationUri: String,
    ) = withContext(Dispatchers.IO) {
        val source = preparedFile(copy)
        val destination =
            contentResolver.openOutputStream(destinationUri.toUri(), "w")
                ?: error("Cannot write edited image")
        source.inputStream().use { input -> destination.use { output -> input.copyTo(output) } }
        deletePreparedFile(copy)
        Unit
    }

    private fun preparedFile(copy: PreparedPhotoCopy): File {
        val file = File(copy.filePath).canonicalFile
        require(file.parentFile == cacheDirectory.canonicalFile && file.name.startsWith(TEMP_FILE_PREFIX) && file.extension == "jpg") {
            "Invalid prepared photo copy."
        }
        return file
    }

    private fun deletePreparedFile(copy: PreparedPhotoCopy) {
        val file = preparedFile(copy)
        check(!file.exists() || file.delete()) { "Unable to remove temporary edited photo." }
    }

    private fun decodeSource(
        resolver: ContentResolver,
        uri: Uri,
    ): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: error("Source photo not found")
        require(
            bounds.outWidth in 1..MAX_SOURCE_DIMENSION && bounds.outHeight in 1..MAX_SOURCE_DIMENSION,
        ) { "Unsupported image dimensions" }
        var sample = 1
        while (bounds.outWidth / sample > MAX_DECODE_DIMENSION || bounds.outHeight / sample > MAX_DECODE_DIMENSION) sample *= 2
        val decoded =
            resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(
                    it,
                    null,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )
            }
                ?: error("Source photo not found")
        val orientation =
            resolver.openInputStream(uri)?.use { exif ->
                ExifInterface(exif).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        val rotation =
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
                else -> 0
            }
        val flipped =
            orientation in
                setOf(
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
                    ExifInterface.ORIENTATION_FLIP_VERTICAL,
                    ExifInterface.ORIENTATION_TRANSPOSE,
                    ExifInterface.ORIENTATION_TRANSVERSE,
                )
        if (rotation == 0 && !flipped) return decoded
        val matrix =
            Matrix().apply {
                if (flipped) postScale(-1f, 1f)
                if (rotation != 0) postRotate(rotation.toFloat())
            }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            .also { oriented ->
                if (oriented !== decoded) decoded.recycle()
            }
    }

    private fun transformBitmap(
        source: Bitmap,
        crop: PhotoCropRequest,
    ): Bitmap {
        val rotated =
            if (crop.rotationDegrees == 0) {
                source
            } else {
                Bitmap.createBitmap(
                    source,
                    0,
                    0,
                    source.width,
                    source.height,
                    Matrix().apply { postRotate(crop.rotationDegrees.toFloat()) },
                    true,
                )
            }
        val ratio = crop.aspectRatio.coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO)
        val current = rotated.width.toFloat() / rotated.height
        val cropWidth: Int
        val cropHeight: Int
        if (current > ratio) {
            cropHeight = rotated.height
            cropWidth = (cropHeight * ratio).toInt().coerceIn(1, rotated.width)
        } else {
            cropWidth = rotated.width
            cropHeight = (cropWidth / ratio).toInt().coerceIn(1, rotated.height)
        }
        val left =
            ((rotated.width - cropWidth) * crop.focusX).toInt()
                .coerceIn(0, rotated.width - cropWidth)
        val top =
            ((rotated.height - cropHeight) * crop.focusY).toInt()
                .coerceIn(0, rotated.height - cropHeight)
        return try {
            Bitmap.createBitmap(rotated, left, top, cropWidth, cropHeight).also { result ->
                if (rotated !== source && result !== rotated) rotated.recycle()
            }
        } catch (error: Throwable) {
            if (rotated !== source && !rotated.isRecycled) rotated.recycle()
            throw error
        }
    }

    private companion object {
        const val JPEG_QUALITY = 94
        const val TEMP_FILE_PREFIX = "gallery_edited_"
        const val MAX_FILE_NAME_LENGTH = 120
        const val MAX_SOURCE_DIMENSION = 16_000
        const val MAX_DECODE_DIMENSION = 4_096
        const val MIN_ASPECT_RATIO = 0.45f
        const val MAX_ASPECT_RATIO = 2.2f
        const val DEFAULT_FILE_NAME = "photo"
    }
}
