package com.micamusic.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.micamusic.app.service.SpotifyService

class SpotifyConnectionActivity : AppCompatActivity(), 
    SpotifyService.SpotifyAuthListener, 
    SpotifyService.SpotifyConnectionListener {

    private lateinit var spotifyService: SpotifyService
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var retryButton: Button
    private var isAuthenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify_connection)

        initViews()
        initSpotifyService()
        startSpotifyAuth()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            startSpotifyAuth()
        }
    }

    private fun initSpotifyService() {
        spotifyService = SpotifyService(this)
    }

    private fun startSpotifyAuth() {
        if (spotifyService.isAuthenticated()) {
            // Ya estamos autenticados, conectar directamente
            connectToSpotify()
        } else {
            // Necesitamos autenticarnos primero
            showAuthenticating()
            isAuthenticating = true
            spotifyService.startAuth(this, this)
        }
    }

    private fun connectToSpotify() {
        showConnecting()
        isAuthenticating = false
        
        spotifyService.connect(this)
    }

    // Implementación de SpotifyAuthListener
    override fun onAuthSuccess(accessToken: String) {
        runOnUiThread {
            // Autenticación exitosa, ahora conectar con App Remote
            connectToSpotify()
        }
    }

    override fun onAuthError(error: String) {
        runOnUiThread {
            showError("Error de autenticación: $error")
        }
    }

    override fun onAuthCancelled() {
        runOnUiThread {
            showError("Autenticación cancelada")
        }
    }

    // Implementación de SpotifyConnectionListener
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
            showError("Error conectando a Spotify: ${error.message}")
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            showError("Desconectado de Spotify")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (isAuthenticating) {
            spotifyService.handleAuthResponse(requestCode, resultCode, data, this)
        }
    }

    private fun showAuthenticating() {
        progressBar.visibility = View.VISIBLE
        statusText.text = "Autenticando con Spotify..."
        retryButton.visibility = View.GONE
    }

    private fun showConnecting() {
        progressBar.visibility = View.VISIBLE
        statusText.text = "Conectando a Spotify..."
        retryButton.visibility = View.GONE
    }

    private fun showError(message: String = "Error conectando a Spotify") {
        progressBar.visibility = View.GONE
        statusText.text = message
        retryButton.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyService.disconnect()
    }
}
