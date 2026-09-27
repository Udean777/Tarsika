package com.ssajudn.tarsika.feature.gallery

import android.Manifest
import android.os.Build
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.presentation.platform.PhotoLibraryPermissionPolicy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoLibraryPermissionPolicyTest {
    @Test
    fun requestedPermissionsMatchAndroidMediaPermissionModel() {
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PhotoLibraryPermissionPolicy.requestedPermissions(Build.VERSION_CODES.S),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            PhotoLibraryPermissionPolicy.requestedPermissions(Build.VERSION_CODES.TIRAMISU),
        )
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            PhotoLibraryPermissionPolicy.requestedPermissions(Build.VERSION_CODES.UPSIDE_DOWN_CAKE),
        )
    }

    @Test
    fun selectedPhotosPermissionIsNotTreatedAsFullLibraryAccess() {
        assertEquals(
            PhotoLibraryAccess.SELECTED,
            PhotoLibraryPermissionPolicy.accessFor(
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                setOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            ),
        )
    }

    @Test
    fun fullLibraryPermissionTakesPrecedenceOverSelectedAccess() {
        assertEquals(
            PhotoLibraryAccess.ALL,
            PhotoLibraryPermissionPolicy.accessFor(
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                setOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
            ),
        )
    }

    @Test
    fun deniedPermissionsRequireAnExplicitGrant() {
        assertEquals(
            PhotoLibraryAccess.NONE,
            PhotoLibraryPermissionPolicy.accessFor(Build.VERSION_CODES.TIRAMISU, emptySet()),
        )
    }
}
