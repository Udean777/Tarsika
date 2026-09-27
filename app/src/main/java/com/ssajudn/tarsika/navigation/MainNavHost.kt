package com.ssajudn.tarsika.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ssajudn.tarsika.feature.gallery.domain.model.UserPhotoAlbum
import com.ssajudn.tarsika.feature.gallery.domain.model.VaultPhoto
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceAlbumDetailScreen
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceAlbumsScreen
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceGalleryScreen
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceGalleryState
import com.ssajudn.tarsika.feature.gallery.presentation.DevicePhotoAccessState
import com.ssajudn.tarsika.feature.gallery.presentation.DevicePhotoSearchScreen
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceTrashScreen
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceTrashState
import com.ssajudn.tarsika.feature.gallery.presentation.GalleryUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.HiddenAlbumScreen
import com.ssajudn.tarsika.feature.gallery.presentation.HiddenAlbumUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.TrashUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.UserAlbumUiActions

@Composable
internal fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier,
    galleryState: DeviceGalleryState,
    favoriteKeys: Set<String>,
    userAlbums: List<UserPhotoAlbum>,
    trashState: DeviceTrashState,
    vaultPhotos: List<VaultPhoto>,
    galleryActions: GalleryUiActions,
    albumActions: UserAlbumUiActions,
    trashActions: TrashUiActions,
    vaultActions: HiddenAlbumUiActions,
    photoAccess: DevicePhotoAccessState,
) {
    NavHost(navController, AppDestination.Gallery.route, modifier) {
        composable(AppDestination.Gallery.route) {
            DeviceGalleryScreen(
                state = galleryState,
                favoriteKeys = favoriteKeys,
                userAlbums = userAlbums,
                photoAccess = photoAccess,
                actions = galleryActions,
                onMovePhotos = albumActions.movePhotos,
                onMoveToLocalTrash = trashActions.movePhotos,
                onSearch = { navController.navigate(AppDestination.PhotoSearch.route) },
            )
        }
        composable(AppDestination.PhotoSearch.route) {
            DevicePhotoSearchScreen(
                state = galleryState,
                favoriteKeys = favoriteKeys,
                userAlbums = userAlbums,
                photoAccess = photoAccess,
                actions = galleryActions,
                onMovePhotos = albumActions.movePhotos,
                onMoveToLocalTrash = trashActions.movePhotos,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Trash.route) {
            DeviceTrashScreen(trashState, trashActions) { navController.popBackStack() }
        }
        composable(AppDestination.HiddenAlbum.route) {
            HiddenAlbumScreen(vaultPhotos, vaultActions) { navController.popBackStack() }
        }
        composable(AppDestination.Albums.route) {
            DeviceAlbumsScreen(
                state = galleryState,
                userAlbums = userAlbums,
                albumActions = albumActions,
                onRefresh = galleryActions.refresh,
                photoAccess = photoAccess,
                onOpenAlbum = { albumId -> navController.navigate("${AppDestination.AlbumDetail.route}/$albumId") },
                onOpenTrash = { navController.navigate(AppDestination.Trash.route) },
                onOpenVault = { navController.navigate(AppDestination.HiddenAlbum.route) },
            )
        }
        composable(
            route = "${AppDestination.AlbumDetail.route}/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
        ) { entry ->
            val albumId = entry.arguments?.getString("albumId").orEmpty()
            DeviceAlbumDetailScreen(
                state = galleryState,
                favoriteKeys = favoriteKeys,
                userAlbums = userAlbums,
                albumId = albumId,
                photoAccess = photoAccess,
                galleryActions = galleryActions,
                albumActions = albumActions,
                onMoveToLocalTrash = trashActions.movePhotos,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
