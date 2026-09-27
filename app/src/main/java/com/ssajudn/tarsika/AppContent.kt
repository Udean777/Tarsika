package com.ssajudn.tarsika

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.tarsika.app.di.AppContainer
import com.ssajudn.tarsika.domain.model.ThemeMode
import com.ssajudn.tarsika.navigation.MainNavGraph
import com.ssajudn.tarsika.ui.theme.TarsikaTheme

@Composable
fun AppContent(container: AppContainer) {
    val themeStore = container.themePreferenceStore
    val themeMode by themeStore.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val isDarkTheme =
        when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    TarsikaTheme(darkTheme = isDarkTheme) {
        Surface {
            MainNavGraph(
                viewModelFactories = container.mainViewModelFactories,
            )
        }
    }
}
