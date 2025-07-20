package com.micamusic.app.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micamusic.app.model.Artist
import java.io.IOException

class DataService(private val context: Context) {

    fun loadArtists(): List<Artist> {
        return try {
            val jsonString = context.assets.open("artists.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Artist>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}