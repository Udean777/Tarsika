package com.ssajudn.tarsika.navigation

sealed class AppDestination(val route: String) {
    data object Gallery : AppDestination("gallery")

    data object Albums : AppDestination("albums")

    data object AlbumDetail : AppDestination("album")

    data object PhotoSearch : AppDestination("photo-search")

    data object Trash : AppDestination("trash")

    data object HiddenAlbum : AppDestination("hidden-album")
}
