package com.ssajudn.tarsika.feature.gallery.data.metadata

import android.content.ContentResolver
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoExifMetadata
import com.ssajudn.tarsika.feature.gallery.domain.repository.PhotoMetadataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExifPhotoMetadataReader(
    private val resolver: ContentResolver,
) : PhotoMetadataRepository {
    override suspend fun readExif(uri: String): PhotoExifMetadata =
        withContext(Dispatchers.IO) {
            try {
                val parsedUri = uri.toUri()
                val stream =
                    resolver.openInputStream(parsedUri) ?: return@withContext PhotoExifMetadata()
                stream.use { input ->
                    val exif = ExifInterface(input)
                    val make =
                        exif.getAttribute(ExifInterface.TAG_MAKE)?.trim()?.takeIf(String::isNotEmpty)
                    val model =
                        exif.getAttribute(ExifInterface.TAG_MODEL)?.trim()?.takeIf(String::isNotEmpty)
                    val lens =
                        exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.trim()
                            ?.takeIf(String::isNotEmpty)
                    val fNumber =
                        exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, Double.NaN)
                            .takeIf(Double::isFinite)
                    val exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.toExposureLabel()
                    val iso =
                        exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, -1)
                            .takeIf { it > 0 }
                            ?: exif.getAttributeInt(LEGACY_ISO_SPEED_RATINGS_TAG, -1)
                                .takeIf { it > 0 }
                    val focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.toFocalLabel()
                    val latLong = exif.latLong?.map(Double::toFloat)
                    PhotoExifMetadata(
                        camera =
                            listOfNotNull(make, model).distinct().joinToString(" ")
                                .ifBlank { null },
                        lens = lens,
                        aperture = fNumber?.let { "f/${"%.1f".format(java.util.Locale.ROOT, it)}" },
                        exposure = exposure,
                        iso = iso?.let { "ISO $it" },
                        focalLength = focal,
                        location =
                            latLong?.let {
                                "${
                                    "%.5f".format(
                                        java.util.Locale.ROOT,
                                        it[0],
                                    )
                                }, ${"%.5f".format(java.util.Locale.ROOT, it[1])}"
                            },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                PhotoExifMetadata()
            }
        }
}

private const val LEGACY_ISO_SPEED_RATINGS_TAG = "ISOSpeedRatings"

private fun String.toExposureLabel(): String? =
    toDoubleOrNull()?.takeIf { it > 0 }?.let { seconds ->
        if (seconds >= 1) {
            "${"%.1f".format(java.util.Locale.ROOT, seconds)} s"
        } else {
            "1/${(1.0 / seconds).toInt().coerceAtLeast(1)} s"
        }
    }

private fun String.toFocalLabel(): String? =
    substringBefore('/').toDoubleOrNull()?.takeIf { it > 0 }?.let {
        "${"%.1f".format(java.util.Locale.ROOT, it)} mm"
    }
