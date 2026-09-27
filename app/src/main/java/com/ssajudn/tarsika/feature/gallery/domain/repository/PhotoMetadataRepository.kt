package com.ssajudn.tarsika.feature.gallery.domain.repository

import com.ssajudn.tarsika.feature.gallery.domain.model.PhotoExifMetadata

interface PhotoMetadataRepository {
    suspend fun readExif(uri: String): PhotoExifMetadata
}
