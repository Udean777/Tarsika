package com.ssajudn.tarsika.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GalleryViewModelFactory<T : ViewModel>(
    private val viewModelClass: Class<T>,
    private val creator: () -> T,
) : ViewModelProvider.Factory {
    override fun <V : ViewModel> create(modelClass: Class<V>): V {
        require(modelClass.isAssignableFrom(viewModelClass)) { "Unsupported ViewModel: ${modelClass.name}" }
        @Suppress("UNCHECKED_CAST")
        return creator() as V
    }
}
