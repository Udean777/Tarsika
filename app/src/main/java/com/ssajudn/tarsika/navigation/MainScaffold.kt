package com.ssajudn.tarsika.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssajudn.tarsika.R
import com.ssajudn.tarsika.core.ui.components.ArchiveNavigationBar
import com.ssajudn.tarsika.core.ui.components.ArchiveNavigationItem
import com.ssajudn.tarsika.core.ui.components.ArchiveNavigationRail
import com.ssajudn.tarsika.navigation.AppDestination.AlbumDetail
import com.ssajudn.tarsika.navigation.AppDestination.Albums
import com.ssajudn.tarsika.navigation.AppDestination.Gallery
import com.ssajudn.tarsika.navigation.AppDestination.HiddenAlbum
import com.ssajudn.tarsika.navigation.AppDestination.PhotoSearch
import com.ssajudn.tarsika.navigation.AppDestination.Trash

@Composable
internal fun MainScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val items =
        listOf(
            ArchiveNavigationItem(Gallery.route, R.string.nav_photos, Icons.Default.PhotoLibrary),
            ArchiveNavigationItem(Albums.route, R.string.nav_albums, Icons.Default.Folder),
        )
    val isAlbumDetail = currentRoute?.startsWith("${AlbumDetail.route}/") == true
    val selectedRoute =
        when {
            isAlbumDetail -> Albums.route
            currentRoute == PhotoSearch.route -> Gallery.route
            else -> currentRoute
        }
    val isDetail = isAlbumDetail || currentRoute == Trash.route || currentRoute == HiddenAlbum.route

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 600.dp) {
            Row(Modifier.fillMaxSize()) {
                ArchiveNavigationRail(items, selectedRoute, onNavigate, Modifier.fillMaxHeight())
                Scaffold(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                ) { innerPadding ->
                    content(Modifier.fillMaxSize().padding(innerPadding))
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!isDetail) ArchiveNavigationBar(items, selectedRoute, onNavigate)
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { innerPadding ->
                content(Modifier.fillMaxSize().padding(innerPadding))
            }
        }
    }
}
