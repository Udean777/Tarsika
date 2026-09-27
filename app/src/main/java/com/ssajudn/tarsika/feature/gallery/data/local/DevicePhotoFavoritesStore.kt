package com.ssajudn.tarsika.feature.gallery.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ssajudn.tarsika.feature.gallery.domain.model.DevicePhoto
import com.ssajudn.tarsika.feature.gallery.domain.model.favoriteKey
import com.ssajudn.tarsika.feature.gallery.domain.repository.DevicePhotoFavorites
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.devicePhotoFavorites by preferencesDataStore(name = "device_photo_favorites")

class DevicePhotoFavoritesStore(private val context: Context) : DevicePhotoFavorites {
    private val favoritesKey = stringSetPreferencesKey("photo_uris")

    override val favorites: Flow<Set<String>> =
        context.devicePhotoFavorites.data.map { preferences ->
            preferences[favoritesKey].orEmpty()
        }

    override suspend fun toggle(photo: DevicePhoto) {
        context.devicePhotoFavorites.edit { preferences ->
            val favorites = preferences[favoritesKey].orEmpty().toMutableSet()
            val key = photo.favoriteKey()
            if (!favorites.add(key)) favorites.remove(key)
            preferences[favoritesKey] = favorites
        }
    }
}
