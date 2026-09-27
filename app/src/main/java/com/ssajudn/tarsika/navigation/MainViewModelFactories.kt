package com.ssajudn.tarsika.navigation

import androidx.lifecycle.ViewModelProvider

data class MainViewModelFactories(
    val gallery: ViewModelProvider.Factory,
    val albums: ViewModelProvider.Factory,
    val trash: ViewModelProvider.Factory,
    val vault: ViewModelProvider.Factory,
)
