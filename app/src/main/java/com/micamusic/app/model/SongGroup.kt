package com.micamusic.app.model

data class SongGroup(
    val name: String,
    val imageUrl: String,
    val artists: List<Artist>,
    var isExpanded: Boolean = false
)
