package com.micamusic.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.micamusic.app.adapter.ArtistAdapter
import com.micamusic.app.model.Artist
import com.micamusic.app.model.Language
import com.micamusic.app.service.DataService
import com.micamusic.app.service.SpotifyService

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var spotifyService: SpotifyService
    private lateinit var dataService: DataService
    
    private var currentLanguage = Language.SPANISH
    private var artists: List<Artist> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initServices()
        initUI()
        loadData()
        connectToSpotify()
    }

    private fun initServices() {
        spotifyService = SpotifyService(this)
        dataService = DataService(this)
    }

    private fun initUI() {
        recyclerView = findViewById(R.id.recyclerView)
        
        artistAdapter = ArtistAdapter(
            artists = artists,
            currentLanguage = currentLanguage,
            onArtistClick = { artist -> playArtistSong(artist) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = artistAdapter
        }

        // Set up language toggle buttons
        findViewById<android.widget.TextView>(R.id.spanishFlag).setOnClickListener {
            switchLanguage(Language.SPANISH)
        }
        
        findViewById<android.widget.TextView>(R.id.englishFlag).setOnClickListener {
            switchLanguage(Language.ENGLISH)
        }
    }

    private fun loadData() {
        artists = dataService.loadArtists()
        artistAdapter.updateArtists(artists)
    }

    private fun connectToSpotify() {
        spotifyService.connect(object : SpotifyService.SpotifyConnectionListener {
            override fun onConnected() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connected to Spotify", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onConnectionFailed(error: Throwable) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.authentication_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Disconnected from Spotify", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun switchLanguage(language: Language) {
        if (currentLanguage != language) {
            currentLanguage = language
            artistAdapter.updateLanguage(language)
            
            val languageText = when (language) {
                Language.SPANISH -> getString(R.string.language_spanish)
                Language.ENGLISH -> getString(R.string.language_english)
            }
            Toast.makeText(this, "Switched to $languageText", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playArtistSong(artist: Artist) {
        val songUri = when (currentLanguage) {
            Language.SPANISH -> artist.spanishSong
            Language.ENGLISH -> artist.englishSong
        }

        songUri?.let { uri ->
            if (spotifyService.isConnected()) {
                spotifyService.playTrack(uri)
                Toast.makeText(this, "Playing ${artist.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.connecting_spotify), Toast.LENGTH_SHORT).show()
                connectToSpotify()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyService.disconnect()
    }
}