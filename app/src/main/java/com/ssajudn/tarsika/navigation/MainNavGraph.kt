package com.ssajudn.tarsika.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssajudn.tarsika.feature.gallery.domain.PhotoLibraryAccess
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceGalleryState
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceGalleryViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceTrashViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.GalleryEffectHandler
import com.ssajudn.tarsika.feature.gallery.presentation.GalleryUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.HiddenAlbumUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.HiddenAlbumViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.TrashUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.UserAlbumUiActions
import com.ssajudn.tarsika.feature.gallery.presentation.UserAlbumsViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.rememberDevicePhotoAccess

@Composable
fun MainNavGraph(viewModelFactories: MainViewModelFactories) {
    val navController = rememberNavController()
    val galleryViewModel: DeviceGalleryViewModel = viewModel(factory = viewModelFactories.gallery)
    val albumsViewModel: UserAlbumsViewModel = viewModel(factory = viewModelFactories.albums)
    val trashViewModel: DeviceTrashViewModel = viewModel(factory = viewModelFactories.trash)
    val vaultViewModel: HiddenAlbumViewModel = viewModel(factory = viewModelFactories.vault)
    val galleryState by galleryViewModel.state.collectAsStateWithLifecycle()
    val favoriteKeys by galleryViewModel.favoriteKeys.collectAsStateWithLifecycle()
    val userAlbums by albumsViewModel.albums.collectAsStateWithLifecycle()
    val trashState by trashViewModel.state.collectAsStateWithLifecycle()
    val vaultPhotos by vaultViewModel.photos.collectAsStateWithLifecycle()
    GalleryEffectHandler(galleryViewModel.effects)
    GalleryEffectHandler(albumsViewModel.effects)
    GalleryEffectHandler(trashViewModel.effects)
    GalleryEffectHandler(vaultViewModel.effects)
    val galleryActions =
        remember(galleryViewModel) {
            GalleryUiActions(
                refresh = galleryViewModel::refresh,
                toggleFavorite = galleryViewModel::toggleFavorite,
                readExif = galleryViewModel::readPhotoExif,
                prepareEditedCopy = galleryViewModel::prepareEditedPhotoCopy,
                exportEditedCopy = galleryViewModel::exportEditedPhotoCopy,
                discardEditedCopy = galleryViewModel::discardEditedPhotoCopy,
                deleteSelectedPhotos = galleryViewModel::deleteSelectedPhotos,
            )
        }
    val albumActions =
        remember(albumsViewModel) {
            UserAlbumUiActions(
                create = albumsViewModel::createUserAlbum,
                rename = albumsViewModel::renameUserAlbum,
                delete = albumsViewModel::deleteUserAlbum,
                addPhotos = albumsViewModel::addPhotosToUserAlbum,
                removePhotos = albumsViewModel::removePhotosFromUserAlbum,
                movePhotos = albumsViewModel::movePhotosBetweenAlbums,
            )
        }
    val trashActions =
        remember(trashViewModel) {
            TrashUiActions(
                refreshPlatform = trashViewModel::refreshPlatformTrash,
                movePhotos = trashViewModel::movePhotos,
                restore = trashViewModel::restore,
                deleteForever = trashViewModel::deleteForever,
            )
        }
    val vaultActions =
        remember(vaultViewModel) {
            HiddenAlbumUiActions(
                ensureKey = vaultViewModel::ensureKey,
                readPhoto = vaultViewModel::readPhoto,
                importPhoto = vaultViewModel::importPhoto,
                deletePhoto = vaultViewModel::deletePhoto,
            )
        }
    val photoAccess = rememberDevicePhotoAccess(galleryViewModel::updateAccess)
    val galleryStateForUi =
        if (galleryState == DeviceGalleryState.PermissionRequired && photoAccess.access != PhotoLibraryAccess.NONE
        ) {
            DeviceGalleryState.Content(
                photos = emptyList(),
                albums = emptyList(),
                access = photoAccess.access,
                isInitialLoad = true,
            )
        } else {
            galleryState
        }
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.startDestinationId) { saveState = true }
            }
        },
    ) { modifier ->
        MainNavHost(
            navController = navController,
            modifier = modifier,
            galleryState = galleryStateForUi,
            favoriteKeys = favoriteKeys,
            userAlbums = userAlbums,
            trashState = trashState,
            vaultPhotos = vaultPhotos,
            galleryActions = galleryActions,
            albumActions = albumActions,
            trashActions = trashActions,
            vaultActions = vaultActions,
            photoAccess = photoAccess,
        )
    }
}
