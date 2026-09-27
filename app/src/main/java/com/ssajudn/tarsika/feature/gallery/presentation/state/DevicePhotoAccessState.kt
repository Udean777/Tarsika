package com.ssajudn.tarsika.feature.gallery.presentation
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.presentation.platform.PhotoLibraryPermissionPolicy

data class DevicePhotoAccessState(
    val access: PhotoLibraryAccess,
    val permissionDenied: Boolean,
    val requestAccess: () -> Unit,
)

@Composable
fun rememberDevicePhotoAccess(onAccessChanged: (PhotoLibraryAccess, Boolean) -> Unit): DevicePhotoAccessState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var access by remember(context) { mutableStateOf(PhotoLibraryPermissionPolicy.currentAccess(context)) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val updateAccess = rememberUpdatedState(onAccessChanged)
    val permissionLauncher =
        rememberLauncherForActivityResult(RequestMultiplePermissions()) {
            access = PhotoLibraryPermissionPolicy.currentAccess(context)
            permissionDenied = access == PhotoLibraryAccess.NONE
        }

    LaunchedEffect(context) {
        access = PhotoLibraryPermissionPolicy.currentAccess(context)
    }
    LaunchedEffect(access) { updateAccess.value(access, false) }
    DisposableEffect(lifecycleOwner, context) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val resumedAccess = PhotoLibraryPermissionPolicy.currentAccess(context)
                    access = resumedAccess
                    updateAccess.value(resumedAccess, true)
                    if (access != PhotoLibraryAccess.NONE) permissionDenied = false
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestAccess =
        remember(access, permissionDenied, context, permissionLauncher) {
            {
                if (permissionDenied) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                } else {
                    permissionLauncher.launch(PhotoLibraryPermissionPolicy.requestedPermissions(Build.VERSION.SDK_INT))
                }
            }
        }
    return DevicePhotoAccessState(
        access = access,
        permissionDenied = permissionDenied,
        requestAccess = requestAccess,
    )
}
