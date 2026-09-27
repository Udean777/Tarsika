package com.ssajudn.tarsika.feature.gallery.presentation.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess

internal object PhotoLibraryPermissionPolicy {
    fun requestedPermissions(sdkInt: Int): Array<String> =
        when {
            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                arrayOf(
                    READ_MEDIA_IMAGES_PERMISSION,
                    READ_MEDIA_VISUAL_USER_SELECTED_PERMISSION,
                )

            sdkInt >= Build.VERSION_CODES.TIRAMISU -> arrayOf(READ_MEDIA_IMAGES_PERMISSION)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun accessFor(
        sdkInt: Int,
        grantedPermissions: Set<String>,
    ): PhotoLibraryAccess =
        when {
            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                READ_MEDIA_IMAGES_PERMISSION in grantedPermissions -> PhotoLibraryAccess.ALL

            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                READ_MEDIA_VISUAL_USER_SELECTED_PERMISSION in grantedPermissions -> PhotoLibraryAccess.SELECTED

            sdkInt >= Build.VERSION_CODES.TIRAMISU && READ_MEDIA_IMAGES_PERMISSION in grantedPermissions ->
                PhotoLibraryAccess.ALL

            sdkInt < Build.VERSION_CODES.TIRAMISU && Manifest.permission.READ_EXTERNAL_STORAGE in grantedPermissions ->
                PhotoLibraryAccess.ALL

            else -> PhotoLibraryAccess.NONE
        }

    fun currentAccess(context: Context): PhotoLibraryAccess {
        val granted =
            requestedPermissions(Build.VERSION.SDK_INT)
                .filter {
                    ContextCompat.checkSelfPermission(
                        context,
                        it,
                    ) == PackageManager.PERMISSION_GRANTED
                }
                .toSet()
        return accessFor(Build.VERSION.SDK_INT, granted)
    }

    private const val READ_MEDIA_IMAGES_PERMISSION = "android.permission.READ_MEDIA_IMAGES"
    private const val READ_MEDIA_VISUAL_USER_SELECTED_PERMISSION = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
}
