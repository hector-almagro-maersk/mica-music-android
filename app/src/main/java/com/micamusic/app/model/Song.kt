package com.micamusic.app.model

data class Song(
    val title: String,
    val spotifyUri: String,
    val albumTrackId: String? = null
)
