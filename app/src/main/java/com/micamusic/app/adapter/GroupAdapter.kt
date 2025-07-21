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
    private val onSongClick: (Artist, Song, String) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var currentPlayingSong: Song? = null
    private var currentPlayingArtist: Artist? = null
    private var currentPlayingLanguage: String? = null

    fun setCurrentPlaying(artist: Artist?, song: Song?, language: String?) {
        currentPlayingArtist = artist
        currentPlayingSong = song
        currentPlayingLanguage = language
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

        fun bind(group: SongGroup) {
            groupName.text = group.name
            Glide.with(itemView.context)
                .load(group.imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(groupImage)

            val artistAdapter = ArtistAdapter(group.artists, onSongClick)
            artistAdapter.setCurrentPlaying(currentPlayingArtist, currentPlayingSong, currentPlayingLanguage)
            songsRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = artistAdapter
            }

            // Animación de expansión/colapso
            if (group.isExpanded && songsRecyclerView.visibility != View.VISIBLE) {
                expandView(songsRecyclerView)
            } else if (!group.isExpanded && songsRecyclerView.visibility != View.GONE) {
                collapseView(songsRecyclerView)
            }

            // Animar el icono de expansión
            groupExpandIcon.animate().rotation(if (group.isExpanded) 180f else 0f).setDuration(250).start()

            groupHeader.setOnClickListener {
                group.isExpanded = !group.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }

        // Métodos utilitarios para animar expansión/colapso
        private fun expandView(view: View) {
            view.measure(View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED)
            val targetHeight = view.measuredHeight
            view.layoutParams.height = 0
            view.visibility = View.VISIBLE
            val animator = android.animation.ValueAnimator.ofInt(0, targetHeight)
            animator.addUpdateListener { valueAnimator ->
                view.layoutParams.height = valueAnimator.animatedValue as Int
                view.requestLayout()
            }
            animator.duration = 250
            animator.start()
        }

        private fun collapseView(view: View) {
            val initialHeight = view.measuredHeight
            val animator = android.animation.ValueAnimator.ofInt(initialHeight, 0)
            animator.addUpdateListener { valueAnimator ->
                view.layoutParams.height = valueAnimator.animatedValue as Int
                view.requestLayout()
            }
            animator.duration = 250
            animator.start()
            animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    view.visibility = View.GONE
                    view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            })
        }
    }
}
