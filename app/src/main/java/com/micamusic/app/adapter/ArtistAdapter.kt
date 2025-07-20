package com.micamusic.app.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.micamusic.app.R
import com.micamusic.app.model.Artist
import com.micamusic.app.model.Language

class ArtistAdapter(
    private var artists: List<Artist>,
    private var currentLanguage: Language,
    private val onArtistClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount(): Int = artists.size

    fun updateLanguage(language: Language) {
        currentLanguage = language
        notifyDataSetChanged()
    }

    fun updateArtists(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistImage: ImageView = itemView.findViewById(R.id.artistImage)
        private val artistName: TextView = itemView.findViewById(R.id.artistName)
        private val songStatus: TextView = itemView.findViewById(R.id.songStatus)

        fun bind(artist: Artist) {
            artistName.text = artist.name
            
            val songUri = when (currentLanguage) {
                Language.SPANISH -> artist.spanishSong
                Language.ENGLISH -> artist.englishSong
            }
            
            val isAvailable = songUri != null
            
            // Update status text
            songStatus.text = if (isAvailable) {
                "Available in ${currentLanguage.name.lowercase().replaceFirstChar { it.uppercase() }}"
            } else {
                itemView.context.getString(R.string.song_unavailable)
            }
            
            // Load image with Glide
            Glide.with(itemView.context)
                .load(artist.imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(artistImage)
            
            // Apply grayscale filter if song is not available
            if (isAvailable) {
                artistImage.colorFilter = null
                itemView.alpha = 1.0f
                itemView.isClickable = true
                itemView.setOnClickListener { onArtistClick(artist) }
            } else {
                val matrix = ColorMatrix().apply { setSaturation(0f) }
                artistImage.colorFilter = ColorMatrixColorFilter(matrix)
                itemView.alpha = 0.5f
                itemView.isClickable = false
                itemView.setOnClickListener(null)
            }
        }
    }
}