package com.ssajudn.tarsika.app.di

import android.content.Context
import com.ssajudn.tarsika.data.local.ThemePreferenceStore
import com.ssajudn.tarsika.feature.gallery.data.device.MediaStoreDevicePhotoMutationRepository
import com.ssajudn.tarsika.feature.gallery.data.device.MediaStoreDevicePhotoReader
import com.ssajudn.tarsika.feature.gallery.data.device.MediaStoreSystemTrashRepository
import com.ssajudn.tarsika.feature.gallery.data.editor.AndroidPhotoEditor
import com.ssajudn.tarsika.feature.gallery.data.local.AndroidEncryptedVaultRepository
import com.ssajudn.tarsika.feature.gallery.data.local.DevicePhotoFavoritesStore
import com.ssajudn.tarsika.feature.gallery.data.local.RoomLocalTrashRepository
import com.ssajudn.tarsika.feature.gallery.data.local.RoomUserPhotoAlbumRepository
import com.ssajudn.tarsika.feature.gallery.data.local.UserAlbumDatabase
import com.ssajudn.tarsika.feature.gallery.data.metadata.ExifPhotoMetadataReader
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoReader
import com.ssajudn.tarsika.feature.gallery.domain.usecase.CreateUserPhotoAlbumUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.DeleteSelectedDevicePhotosUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.ImportPhotoIntoVaultUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.MoveDevicePhotosToLocalTrashUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.MovePhotosBetweenUserAlbumsUseCase
import com.ssajudn.tarsika.feature.gallery.domain.usecase.PrepareEditedPhotoCopyUseCase
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceGalleryViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.DeviceTrashViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.HiddenAlbumViewModel
import com.ssajudn.tarsika.feature.gallery.presentation.UserAlbumsViewModel
import com.ssajudn.tarsika.navigation.MainViewModelFactories
import java.io.File

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val themePreferenceStore = ThemePreferenceStore(appContext)
    private val favorites = DevicePhotoFavoritesStore(appContext)
    private val photoReader: DevicePhotoReader = MediaStoreDevicePhotoReader(appContext.contentResolver)
    private val database = UserAlbumDatabase.get(appContext)
    private val albums = RoomUserPhotoAlbumRepository(database.albums())
    private val vault =
        AndroidEncryptedVaultRepository(appContext.contentResolver, database.vaultPhotos(), File(appContext.filesDir, "vault"))
    private val localTrash = RoomLocalTrashRepository(appContext.contentResolver, database.localTrash(), File(appContext.filesDir, "trash"))
    private val systemTrash = MediaStoreSystemTrashRepository(appContext.contentResolver)
    private val metadata = ExifPhotoMetadataReader(appContext.contentResolver)
    private val editor = AndroidPhotoEditor(appContext.contentResolver, appContext.cacheDir)
    private val photoMutation = MediaStoreDevicePhotoMutationRepository(appContext.contentResolver)

    val mainViewModelFactories =
        MainViewModelFactories(
            gallery =
                GalleryViewModelFactory(DeviceGalleryViewModel::class.java) {
                    DeviceGalleryViewModel(
                        photoReader,
                        favorites,
                        metadata,
                        PrepareEditedPhotoCopyUseCase(editor),
                        editor,
                        DeleteSelectedDevicePhotosUseCase(photoMutation),
                    )
                },
            albums =
                GalleryViewModelFactory(UserAlbumsViewModel::class.java) {
                    UserAlbumsViewModel(albums, CreateUserPhotoAlbumUseCase(albums), MovePhotosBetweenUserAlbumsUseCase(albums))
                },
            trash =
                GalleryViewModelFactory(DeviceTrashViewModel::class.java) {
                    DeviceTrashViewModel(localTrash, systemTrash, MoveDevicePhotosToLocalTrashUseCase(localTrash))
                },
            vault =
                GalleryViewModelFactory(HiddenAlbumViewModel::class.java) {
                    HiddenAlbumViewModel(vault, ImportPhotoIntoVaultUseCase(vault))
                },
        )
}
