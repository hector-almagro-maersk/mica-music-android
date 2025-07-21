package com.micamusic.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.micamusic.app.service.SpotifyService

class SpotifyConnectionActivity : AppCompatActivity() {

    private lateinit var spotifyService: SpotifyService
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify_connection)

        initViews()
        initSpotifyService()
        connectToSpotify()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            connectToSpotify()
        }
    }

    private fun initSpotifyService() {
        spotifyService = SpotifyService(this)
    }

    private fun connectToSpotify() {
        showConnecting()
        
        spotifyService.connect(object : SpotifyService.SpotifyConnectionListener {
            override fun onConnected() {
                runOnUiThread {
                    // Conexión exitosa, ir a MainActivity
                    val intent = Intent(this@SpotifyConnectionActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onConnectionFailed(error: Throwable) {
                runOnUiThread {
                    showError()
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    showError()
                }
            }
        })
    }

    private fun showConnecting() {
        progressBar.visibility = View.VISIBLE
        statusText.text = "Conectando a Spotify..."
        retryButton.visibility = View.GONE
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        statusText.text = "Error conectando a Spotify"
        retryButton.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyService.disconnect()
    }
}
