package com.micamusic.app.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.micamusic.app.R
import com.micamusic.app.model.Artist
import com.micamusic.app.model.Song

class ArtistAdapter(
    private var artists: List<Artist>,
    private val onSongClick: (Artist, Song, String) -> Unit // artist, song, language
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    private var currentPlayingSong: Song? = null
    private var currentPlayingArtist: Artist? = null
    private var currentPlayingLanguage: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount(): Int = artists.size

    fun updateArtists(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }
    
    fun setCurrentPlaying(artist: Artist?, song: Song?, language: String?) {
        currentPlayingArtist = artist
        currentPlayingSong = song
        currentPlayingLanguage = language
        notifyDataSetChanged()
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistImage: ImageView = itemView.findViewById(R.id.artistImage)
        
        private val spanishColumn: LinearLayout = itemView.findViewById(R.id.spanishColumn)
        private val spanishSongTitle: TextView = itemView.findViewById(R.id.spanishSongTitle)
        private val spanishFlag: TextView = itemView.findViewById(R.id.spanishFlag)
        private val spanishGlowEffect: View = itemView.findViewById(R.id.spanishGlowEffect)
        
        private val englishColumn: LinearLayout = itemView.findViewById(R.id.englishColumn)
        private val englishSongTitle: TextView = itemView.findViewById(R.id.englishSongTitle)
        private val englishFlag: TextView = itemView.findViewById(R.id.englishFlag)
        private val englishGlowEffect: View = itemView.findViewById(R.id.englishGlowEffect)

        fun bind(artist: Artist) {
            // Load artist image from assets/songs/{imageUrl}
            val localImagePath = if (artist.imageUrl.isNotBlank()) "file:///android_asset/songs/" + artist.imageUrl else null
            Glide.with(itemView.context)
                .load(localImagePath)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(artistImage)

            // Check if this artist/song is currently playing
            val isSpanishPlaying = currentPlayingArtist == artist &&
                                 currentPlayingSong == artist.spanish &&
                                 currentPlayingLanguage == "spanish"
            val isEnglishPlaying = currentPlayingArtist == artist &&
                                 currentPlayingSong == artist.english &&
                                 currentPlayingLanguage == "english"

            // Configure Spanish column
            setupLanguageColumn(
                column = spanishColumn,
                titleView = spanishSongTitle,
                flagView = spanishFlag,
                glowEffect = spanishGlowEffect,
                song = artist.spanish,
                artist = artist,
                language = "spanish",
                isPlaying = isSpanishPlaying
            )
            
            // Configure English column
            setupLanguageColumn(
                column = englishColumn,
                titleView = englishSongTitle,
                flagView = englishFlag,
                glowEffect = englishGlowEffect,
                song = artist.english,
                artist = artist,
                language = "english",
                isPlaying = isEnglishPlaying
            )
        }
        
        private fun setupLanguageColumn(
            column: LinearLayout,
            titleView: TextView,
            flagView: TextView,
            glowEffect: View,
            song: Song?,
            artist: Artist,
            language: String,
            isPlaying: Boolean
        ) {
            if (song != null) {
                // Song is available
                titleView.text = song.title // Mostrar título tal como está (puede estar vacío)
                titleView.setTextColor(itemView.context.getColor(R.color.earth_brown))
                flagView.alpha = 1.0f
                column.alpha = 1.0f
                column.isClickable = true
                column.setOnClickListener { 
                    onSongClick(artist, song, language)
                }
                
                // Show/hide glow effect and highlight based on playing state
                if (isPlaying) {
                    glowEffect.visibility = View.VISIBLE
                    column.setBackgroundResource(R.drawable.selection_highlight)
                    
                    // Add scale effect to flag
                    flagView.scaleX = 1.2f
                    flagView.scaleY = 1.2f
                    
                    // Make text white for better contrast
                    titleView.setTextColor(itemView.context.getColor(R.color.white))
                } else {
                    glowEffect.visibility = View.GONE
                    column.background = null
                    flagView.scaleX = 1.0f
                    flagView.scaleY = 1.0f
                    titleView.setTextColor(itemView.context.getColor(R.color.earth_brown))
                }
            } else {
                // Song is not available (null)
                titleView.text = "" // Campo vacío en lugar de "Not available"
                titleView.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                flagView.alpha = 0.3f
                column.alpha = 0.3f
                column.isClickable = false
                column.setOnClickListener(null)
                column.background = null
                glowEffect.visibility = View.GONE
                flagView.scaleX = 1.0f
                flagView.scaleY = 1.0f
            }
        }
    }
}