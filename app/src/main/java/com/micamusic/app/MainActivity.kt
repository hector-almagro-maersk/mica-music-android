package com.micamusic.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.micamusic.app.adapter.GroupAdapter
import com.micamusic.app.model.Artist
import com.micamusic.app.model.Song
import com.micamusic.app.model.SongGroup
import com.micamusic.app.service.DataService
import com.micamusic.app.service.SpotifyService
import com.spotify.protocol.types.Track

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var spotifyService: SpotifyService
    private lateinit var dataService: DataService
    
    // Player control views
    private lateinit var playerControl: LinearLayout
    private lateinit var currentSongTitle: TextView
    private lateinit var currentArtistImage: ImageView
    private lateinit var currentLanguageFlag: TextView
    private lateinit var playPauseButton: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    
    private var songGroups: List<SongGroup> = emptyList()
    private var isPlaying = false
    private var currentSong: Song? = null
    private var currentArtist: Artist? = null
    private var currentLanguage: String? = null
    private var isSpotifyConnected = false
    
    // Para actualizar la seek bar
    private val seekBarUpdateHandler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false
    private var currentPosition: Long = 0
    private var currentDuration: Long = 0
    private var lastUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initServices()
        initUI()
        loadData()
        setupSpotifyPlaybackListener()
    }

    private fun initServices() {
        spotifyService = SpotifyService(this)
        dataService = DataService(this)
    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recyclerView)
        playerControl = findViewById(R.id.playerControl)
        currentSongTitle = findViewById(R.id.currentSongTitle)
        currentArtistImage = findViewById(R.id.currentArtistImage)
        currentLanguageFlag = findViewById(R.id.currentLanguageFlag)
        playPauseButton = findViewById(R.id.playPauseButton)
        seekBar = findViewById(R.id.seekBar)
        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)
        
        groupAdapter = GroupAdapter(
            groups = songGroups,
            onSongClick = { artist, song, language -> playSong(artist, song, language) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = groupAdapter
        }

        // Set up play/pause button
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }
        
        // Set up seek bar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val seekPosition = (progress.toLong() * currentDuration) / 100
                    currentTime.text = formatTime(seekPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                if (isSpotifyConnected && spotifyService.isConnected()) {
                    val seekPosition = (seekBar!!.progress.toLong() * currentDuration) / 100
                    currentPosition = seekPosition // Update our tracked position
                    lastUpdateTime = System.currentTimeMillis() // Reset time tracker
                    spotifyService.seekTo(seekPosition)
                }
            }
        })
    }

    private fun loadData() {
        songGroups = dataService.loadSongGroups()
        groupAdapter.updateGroups(songGroups)
    }

    private fun setupSpotifyPlaybackListener() {
        // Conectar a Spotify cuando se inicia la app
        spotifyService.connect(object : SpotifyService.SpotifyConnectionListener {
            override fun onConnected() {
                runOnUiThread {
                    // Conexión exitosa
                    isSpotifyConnected = true
                    // Ya no necesitamos mostrar mensaje de "conectando"
                }
            }

            override fun onConnectionFailed(error: Throwable) {
                runOnUiThread {
                    // Manejar error de conexión
                    isSpotifyConnected = false
                    // Podrías mostrar un Toast o mensaje de error aquí
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    // Spotify se desconectó
                    isSpotifyConnected = false
                }
            }
        })
    }

    private fun playSong(artist: Artist, song: Song, language: String) {
        if (!isSpotifyConnected || !spotifyService.isConnected()) {
            // Si Spotify no está conectado, mostrar mensaje o reintentar conexión
            return
        }
        
        currentSong = song
        currentArtist = artist
        currentLanguage = language
        
        // Update player control UI
        updatePlayerControl()
        
        // Update adapter to show visual effects
        groupAdapter.setCurrentPlaying(artist, song, language)
        
        // Play the song with playback listener
        spotifyService.playTrack(song.spotifyUri, object : SpotifyService.PlaybackListener {
            override fun onTrackChanged(track: Track) {
                runOnUiThread {
                    // Track changed
                }
            }

            override fun onPlaybackStateChanged(isPaused: Boolean, position: Long, duration: Long) {
                runOnUiThread {
                    isPlaying = !isPaused
                    playPauseButton.text = if (isPaused) "▶️" else "⏸️"
                    
                    currentPosition = position
                    currentDuration = duration
                    lastUpdateTime = System.currentTimeMillis() // Reset time tracker
                    
                    updateSeekBar()
                    updateTimeLabels()
                    
                    // Iniciar o detener las actualizaciones automáticas de la seek bar
                    if (isPlaying) {
                        startSeekBarUpdates()
                    } else {
                        seekBarUpdateHandler.removeCallbacksAndMessages(null)
                    }
                }
            }
        })
        
        isPlaying = true
        playPauseButton.text = "⏸️"
    }
    
    private fun updatePlayerControl() {
        currentSong?.let { song ->
            currentArtist?.let { artist ->
                currentLanguage?.let { language ->
                    currentSongTitle.text = song.title
                    
                    // Cargar imagen del artista
                    val localImagePath = if (artist.imageUrl.isNotBlank()) "file:///android_asset/songs/" + artist.imageUrl else null
                    Glide.with(this)
                        .load(localImagePath)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(currentArtistImage)
                    
                    // Mostrar solo la bandera del idioma
                    when (language) {
                        "spanish" -> currentLanguageFlag.text = "🇪🇸"
                        "english" -> currentLanguageFlag.text = "🇺🇸"
                    }
                    
                    // Show player control
                    playerControl.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun togglePlayPause() {
        if (!isSpotifyConnected || !spotifyService.isConnected()) {
            return
        }
        
        if (isPlaying) {
            spotifyService.pause()
            playPauseButton.text = "▶️"
            isPlaying = false
            seekBarUpdateHandler.removeCallbacksAndMessages(null)
        } else {
            spotifyService.resume()
            playPauseButton.text = "⏸️"
            isPlaying = true
            lastUpdateTime = System.currentTimeMillis()
            startSeekBarUpdates()
        }
    }
    
    private fun updateSeekBar() {
        if (!isUserSeeking && currentDuration > 0) {
            val progress = ((currentPosition * 100) / currentDuration).toInt()
            seekBar.progress = progress
        }
    }
    
    private fun updateTimeLabels() {
        currentTime.text = formatTime(currentPosition)
        totalTime.text = formatTime(currentDuration)
    }
    
    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        seekBarUpdateHandler.removeCallbacksAndMessages(null)
        spotifyService.disconnect()
    }

    override fun onStop() {
        super.onStop()
        // Detener las actualizaciones de la seek bar
        seekBarUpdateHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        // Reanudar las actualizaciones si hay una canción reproduciéndose
        if (isPlaying && currentDuration > 0) {
            lastUpdateTime = System.currentTimeMillis()
            startSeekBarUpdates()
        }
    }

    private fun startSeekBarUpdates() {
        // Primero detener cualquier actualización anterior
        seekBarUpdateHandler.removeCallbacksAndMessages(null)
        
        lastUpdateTime = System.currentTimeMillis()
        seekBarUpdateHandler.post(object : Runnable {
            override fun run() {
                if (isPlaying && !isUserSeeking) {
                    // Incrementar la posición actual basándose en el tiempo transcurrido
                    val currentTime = System.currentTimeMillis()
                    val elapsed = currentTime - lastUpdateTime
                    lastUpdateTime = currentTime
                    
                    if (elapsed > 0) {
                        currentPosition += elapsed
                        // Asegurar que no excedamos la duración total
                        if (currentPosition > currentDuration) {
                            currentPosition = currentDuration
                        }
                    }
                    
                    updateSeekBar()
                    updateTimeLabels()
                }
                
                if (isPlaying) {
                    seekBarUpdateHandler.postDelayed(this, 500) // Actualizar cada 500ms para más fluidez
                }
            }
        })
    }
}