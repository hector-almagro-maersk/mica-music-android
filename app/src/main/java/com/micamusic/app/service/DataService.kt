package com.micamusic.app.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.micamusic.app.model.SongGroup
import java.io.IOException

class DataService(private val context: Context) {

    fun loadSongGroups(): List<SongGroup> {
        return try {
            val jsonString = context.assets.open("song_groups.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<SongGroup>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }
}