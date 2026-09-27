package com.ssajudn.tarsika.feature.gallery.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class VaultPreview(val id: String, val bitmap: Bitmap)

@Composable
internal fun HiddenAlbumScreen(
    photos: List<VaultPhoto>,
    actions: HiddenAlbumUiActions,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val androidVersionRequired = stringResource(R.string.vault_requires_android_11)
    val authUnavailable = stringResource(R.string.vault_auth_unavailable)
    val authNotConfigured = stringResource(R.string.vault_auth_not_configured)
    val authKeyError = stringResource(R.string.vault_key_error)
    val vaultTitle = stringResource(R.string.hidden_album_title)
    val unlockPrompt = stringResource(R.string.vault_unlock_prompt)
    val readError = stringResource(R.string.vault_item_read_error)
    val importError = stringResource(R.string.vault_import_error)
    val window = activity?.window
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var unlocked by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingImports by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var selectedPhotoId by remember { mutableStateOf<String?>(null) }
    var fullPhoto by remember { mutableStateOf<VaultPreview?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val picker =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            pendingImports = uris
        }

    DisposableEffect(window, lifecycleOwner) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    unlocked = false
                    selectedPhotoId = null
                    fullPhoto?.bitmap?.recycle()
                    fullPhoto = null
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            unlocked = false
            selectedPhotoId = null
            fullPhoto?.bitmap?.recycle()
            fullPhoto = null
        }
    }

    fun authenticate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            errorMessage = androidVersionRequired
            return
        }
        val host =
            activity ?: run {
                errorMessage = authUnavailable
                return
            }
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val status = BiometricManager.from(context).canAuthenticate(authenticators)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            errorMessage = authNotConfigured
            return
        }
        loading = true
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { actions.ensureKey() } }
                .onSuccess {
                    val prompt =
                        BiometricPrompt(
                            host,
                            host.mainExecutor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    loading = false
                                    errorMessage = null
                                    unlocked = true
                                }

                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence,
                                ) {
                                    loading = false
                                    errorMessage = errString.toString()
                                }
                            },
                        )
                    val info =
                        BiometricPrompt.PromptInfo.Builder()
                            .setTitle(vaultTitle)
                            .setSubtitle(unlockPrompt)
                            .setAllowedAuthenticators(authenticators)
                            .build()
                    prompt.authenticate(info)
                }
                .onFailure {
                    loading = false
                    errorMessage = authKeyError
                }
        }
    }

    LaunchedEffect(unlocked, selectedPhotoId) {
        val id = selectedPhotoId ?: return@LaunchedEffect
        if (!unlocked) return@LaunchedEffect
        runCatching {
            val payload = actions.readPhoto(id)
            withContext(Dispatchers.Default) {
                try {
                    decodeVaultPreview(id, payload.bytes, 2048)
                } finally {
                    payload.bytes.fill(0)
                }
            }
        }.onSuccess { fullPhoto = it }.onFailure { errorMessage = readError }
    }
    LaunchedEffect(unlocked) {
        if (unlocked) {
            delay(5 * 60 * 1000L)
            unlocked = false
            selectedPhotoId = null
            fullPhoto?.bitmap?.recycle()
            fullPhoto = null
        }
    }
    LaunchedEffect(unlocked, pendingImports) {
        if (!unlocked || pendingImports.isEmpty()) return@LaunchedEffect
        loading = true
        val uris = pendingImports
        pendingImports = emptyList()
        uris.forEach { uri ->
            runCatching {
                val name = queryDisplayName(context, uri)
                val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
                actions.importPhoto(uri.toString(), name, size)
            }.onFailure { errorMessage = it.message ?: importError }
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(
            Modifier.statusBarsPadding().fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
            Text(stringResource(R.string.hidden_album_title), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            if (unlocked) {
                IconButton(onClick = {
                    picker.launch(arrayOf("image/*"))
                }) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.vault_add_photos)) }
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Text(
                stringResource(R.string.vault_requires_android_11),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
            )
        } else if (!unlocked) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.vault_locked_title),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.vault_locked_description),
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.vault_key_warning),
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = ::authenticate, enabled = !loading, modifier = Modifier.padding(top = 18.dp)) {
                    Text(stringResource(if (loading) R.string.loading else R.string.unlock_vault))
                }
                errorMessage?.let { Text(it, modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error) }
            }
        } else {
            Text(
                stringResource(
                    R.string.vault_capacity,
                    android.text.format.Formatter.formatFileSize(
                        context,
                        photos.sumOf {
                            it.sizeBytes
                        },
                    ),
                ),
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            if (photos.isEmpty()) {
                GalleryMessage(
                    title = stringResource(R.string.vault_empty_title),
                    description = stringResource(R.string.vault_empty_description),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(112.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(photos, key = VaultPhoto::id) { item ->
                        VaultPhotoTile(
                            photo = item,
                            unlocked = unlocked,
                            readPhoto = actions.readPhoto,
                            onOpen = { selectedPhotoId = item.id },
                            onDelete = { deleteId = item.id },
                        )
                    }
                }
            }
            if (loading) Text(stringResource(R.string.loading), modifier = Modifier.padding(16.dp))
            errorMessage?.let { Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) }
        }
    }

    fullPhoto?.let { photo ->
        Dialog(onDismissRequest = {
            selectedPhotoId = null
            photo.bitmap.recycle()
            fullPhoto = null
        }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(photo.bitmap.asImageBitmap(), contentDescription = photo.id, modifier = Modifier.fillMaxSize().padding(16.dp))
                    TextButton(
                        onClick = {
                            selectedPhotoId = null
                            photo.bitmap.recycle()
                            fullPhoto = null
                        },
                        modifier =
                            Modifier.align(
                                Alignment.TopEnd,
                            ).padding(top = 28.dp, end = 12.dp),
                    ) { Text(stringResource(R.string.close_photo_viewer)) }
                }
            }
        }
    }
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(stringResource(R.string.delete_action)) },
            text = { Text(stringResource(R.string.vault_delete_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.deletePhoto(id)
                    deleteId = null
                }) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

internal fun decodeVaultPreview(
    id: String,
    bytes: ByteArray,
    maxDimension: Int,
): VaultPreview {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) sample *= 2
    val bitmap =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Unsupported image in hidden album")
    return VaultPreview(id, bitmap)
}

private fun queryDisplayName(
    context: android.content.Context,
    uri: android.net.Uri,
): String {
    context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getString(0)?.take(255) ?: "photo"
    }
    return "photo"
}
