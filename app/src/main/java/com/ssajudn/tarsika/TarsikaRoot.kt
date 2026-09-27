package com.ssajudn.tarsika

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun TarsikaRoot() {
    if (LocalInspectionMode.current) return
    val application = LocalContext.current.applicationContext as? TarsikaApplication ?: return
    AppContent(application.appContainer)
}
