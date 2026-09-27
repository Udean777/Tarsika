package com.ssajudn.tarsika.feature.gallery.data.device

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhotoMutationSummary
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoMutationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreDevicePhotoMutationRepository(
    private val contentResolver: ContentResolver,
) : DevicePhotoMutationRepository {
    override suspend fun deletePermanently(
        uris: List<String>,
        consentAlreadyGranted: Boolean,
    ): DevicePhotoMutationSummary =
        withContext(Dispatchers.IO) {
            val parsedUris = uris.distinct().associateWith(Uri::parse)
            if (Build.VERSION.SDK_INT >= VERSION_CODES.R && !consentAlreadyGranted) {
                return@withContext DevicePhotoMutationSummary(approvalRequiredUris = parsedUris.keys)
            }
            val deleted = mutableSetOf<String>()
            val notFound = mutableSetOf<String>()
            val approvalRequired = mutableSetOf<String>()
            parsedUris.forEach { (uriString, uri) ->
                when (deleteUri(uri)) {
                    DeleteOutcome.DELETED -> deleted += uriString
                    DeleteOutcome.NOT_FOUND -> notFound += uriString
                    DeleteOutcome.APPROVAL_REQUIRED -> approvalRequired += uriString
                }
            }
            DevicePhotoMutationSummary(deleted, notFound, approvalRequired)
        }

    private fun deleteUri(uri: Uri): DeleteOutcome =
        if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
            deleteUriOnAndroidQOrLater(uri)
        } else if (contentResolver.delete(uri, null, null) > 0) {
            DeleteOutcome.DELETED
        } else {
            DeleteOutcome.NOT_FOUND
        }

    @RequiresApi(VERSION_CODES.Q)
    private fun deleteUriOnAndroidQOrLater(uri: Uri): DeleteOutcome =
        try {
            if (contentResolver.delete(uri, null, null) > 0) DeleteOutcome.DELETED else DeleteOutcome.NOT_FOUND
        } catch (_: RecoverableSecurityException) {
            DeleteOutcome.APPROVAL_REQUIRED
        }

    private enum class DeleteOutcome {
        DELETED,
        NOT_FOUND,
        APPROVAL_REQUIRED,
    }
}
