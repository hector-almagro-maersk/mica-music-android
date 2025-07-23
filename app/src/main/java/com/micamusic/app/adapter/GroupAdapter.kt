package com.micamusic.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.micamusic.app.R
import com.micamusic.app.model.Artist
import com.micamusic.app.model.Song
import com.micamusic.app.model.SongGroup

class GroupAdapter(
    private var groups: List<SongGroup>,
    private val onSongClick: (Artist, Song, String) -> Unit,
    private val onAlbumPlayClick: (SongGroup, String) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var currentPlayingSong: Song? = null
    private var currentPlayingArtist: Artist? = null
    private var currentPlayingLanguage: String? = null
    private var isSpotifyPremium: Boolean = true // Default to premium

    fun setCurrentPlaying(artist: Artist?, song: Song?, language: String?) {
        currentPlayingArtist = artist
        currentPlayingSong = song
        currentPlayingLanguage = language
        notifyDataSetChanged()
    }

    fun setSpotifyPremium(isPremium: Boolean) {
        isSpotifyPremium = isPremium
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<SongGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val groupImage: ImageView = itemView.findViewById(R.id.groupImage)
        private val groupExpandIcon: ImageView = itemView.findViewById(R.id.groupExpandIcon)
        private val songsRecyclerView: RecyclerView = itemView.findViewById(R.id.songsRecyclerView)
        private val groupHeader: View = itemView.findViewById(R.id.groupHeader)
        private val albumPlayFlags: View = itemView.findViewById(R.id.albumPlayFlags)
        private val albumSpanishFlag: TextView = itemView.findViewById(R.id.albumSpanishFlag)
        private val albumEnglishFlag: TextView = itemView.findViewById(R.id.albumEnglishFlag)

        fun bind(group: SongGroup) {
            groupName.text = group.name
            Glide.with(itemView.context)
                .load(group.imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(groupImage)

            if (isSpotifyPremium) {
                // Premium user: Show expandable film dropdown
                groupExpandIcon.visibility = View.VISIBLE
                albumPlayFlags.visibility = View.GONE

                val artistAdapter = ArtistAdapter(group.artists, onSongClick)
                artistAdapter.setCurrentPlaying(currentPlayingArtist, currentPlayingSong, currentPlayingLanguage)
                songsRecyclerView.apply {
                    layoutManager = LinearLayoutManager(itemView.context)
                    adapter = artistAdapter
                }

                if (group.isExpanded) {
                    songsRecyclerView.visibility = View.VISIBLE
                    songsRecyclerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    songsRecyclerView.visibility = View.GONE
                }

                groupExpandIcon.animate().rotation(if (group.isExpanded) 180f else 0f).setDuration(250).start()

                groupHeader.setOnClickListener {
                    group.isExpanded = !group.isExpanded
                    notifyItemChanged(adapterPosition)
                }
            } else {
                // Non-premium user: Show album play flags, hide dropdown
                groupExpandIcon.visibility = View.GONE
                albumPlayFlags.visibility = View.VISIBLE
                songsRecyclerView.visibility = View.GONE

                // Set up album play flags click listeners
                albumSpanishFlag.setOnClickListener {
                    onAlbumPlayClick(group, "spanish")
                }

                albumEnglishFlag.setOnClickListener {
                    onAlbumPlayClick(group, "english")
                }

                // Disable group header expansion for non-premium
                groupHeader.setOnClickListener(null)
            }
        }
    }
}
