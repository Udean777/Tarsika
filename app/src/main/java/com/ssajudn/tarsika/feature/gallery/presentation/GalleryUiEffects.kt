package com.ssajudn.tarsika.feature.gallery.presentation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ssajudn.tarsika.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

enum class GalleryFailure { VALIDATION, PERMISSION, STORAGE, UNKNOWN }

sealed interface GalleryUiEffect {
    data class OperationFailed(val failure: GalleryFailure) : GalleryUiEffect
}

class GalleryUiEffects {
    private val channel = Channel<GalleryUiEffect>(Channel.BUFFERED)
    val events: Flow<GalleryUiEffect> = channel.receiveAsFlow()

    suspend fun reportFailure(error: Throwable) {
        if (error is CancellationException) throw error
        val failure =
            when (error) {
                is IllegalArgumentException -> GalleryFailure.VALIDATION
                is SecurityException -> GalleryFailure.PERMISSION
                is java.io.IOException -> GalleryFailure.STORAGE
                else -> GalleryFailure.UNKNOWN
            }
        channel.send(GalleryUiEffect.OperationFailed(failure))
    }
}

@Composable
fun GalleryEffectHandler(effects: GalleryUiEffects) {
    val context = LocalContext.current
    val failureMessage = stringResource(R.string.gallery_operation_failed)
    LaunchedEffect(effects) {
        effects.events.collect {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
