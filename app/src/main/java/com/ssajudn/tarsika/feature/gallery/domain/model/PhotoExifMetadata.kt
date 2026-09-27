package com.ssajudn.tarsika.feature.gallery.domain.model

data class PhotoExifMetadata(
    val camera: String? = null,
    val lens: String? = null,
    val aperture: String? = null,
    val exposure: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val location: String? = null,
) {
    val hasValues: Boolean
        get() = listOf(camera, lens, aperture, exposure, iso, focalLength, location).any { it != null }
}
