package com.micamusic.app.service

import android.content.Context
import android.util.Log
import android.os.PowerManager
import com.micamusic.app.BuildConfig

// Spotify Auth SDK for OAuth authentication
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

// Spotify App Remote SDK
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyService(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null
    private var accessToken: String? = null

    companion object {
        private const val REDIRECT_URI = "mica-music://callback"
        private const val TAG = "SpotifyService"
        const val REQUEST_CODE_AUTH = 1337
        
        // Scopes needed for App Remote
        private val SCOPES = arrayOf(
            "app-remote-control",
            "user-modify-playback-state",
            "user-read-playback-state"
        )
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var playerStateSubscription: Subscription<PlayerState>? = null

    interface SpotifyConnectionListener {
        fun onConnected()
        fun onConnectionFailed(error: Throwable)
        fun onDisconnected()
    }

    interface SpotifyAuthListener {
        fun onAuthSuccess(accessToken: String)
        fun onAuthError(error: String)
        fun onAuthCancelled()
    }

    interface PlaybackListener {
        fun onTrackChanged(track: Track)
        fun onPlaybackStateChanged(isPaused: Boolean, position: Long, duration: Long)
    }

    /**
     * Inicia el flujo de autenticación OAuth de Spotify
     * Debe ser llamado desde una Activity
     */
    fun startAuth(activity: android.app.Activity, listener: SpotifyAuthListener) {
        try {
            val builder = AuthorizationRequest.Builder(
                BuildConfig.SPOTIFY_CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
            )

            builder.setScopes(SCOPES)
            val request = builder.build()

            AuthorizationClient.openLoginActivity(activity, REQUEST_CODE_AUTH, request)
            
            Log.d(TAG, "Auth flow started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting auth flow", e)
            listener.onAuthError("Error iniciando autenticación: ${e.message}")
        }
    }

    /**
     * Procesa el resultado de la autenticación OAuth
     * Debe ser llamado desde onActivityResult de la Activity
     */
    fun handleAuthResponse(requestCode: Int, resultCode: Int, intent: android.content.Intent?, listener: SpotifyAuthListener) {
        if (requestCode == REQUEST_CODE_AUTH) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    accessToken = response.accessToken
                    Log.d(TAG, "Auth successful, token received")
                    listener.onAuthSuccess(response.accessToken)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Auth error: ${response.error}")
                    listener.onAuthError("Error de autenticación: ${response.error}")
                }
                else -> {
                    Log.d(TAG, "Auth cancelled")
                    listener.onAuthCancelled()
                }
            }
        }
    }

    /**
     * Verifica si tenemos un token de acceso válido
     */
    fun isAuthenticated(): Boolean {
        return !accessToken.isNullOrEmpty()
    }

    fun connect(listener: SpotifyConnectionListener) {
        if (BuildConfig.SIMULATE_SPOTIFY_SUCCESS) {
            // Simula éxito inmediato
            listener.onConnected()
            return
        }
        
        // Verificar que estemos autenticados antes de conectar
        if (!isAuthenticated()) {
            Log.e(TAG, "Not authenticated. Cannot connect to App Remote without valid token.")
            listener.onConnectionFailed(Exception("Usuario no autenticado. Debe completar el flujo de autenticación primero."))
            return
        }
        
        try {
            val connectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(false) // Ya estamos autenticados, no necesitamos mostrar auth view
                .build()

            SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    try {
                        spotifyAppRemote = appRemote
                        Log.d(TAG, "Connected to Spotify App Remote")
                        listener.onConnected()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onConnected callback", e)
                        listener.onConnectionFailed(e)
                    }
                }

                override fun onFailure(error: Throwable) {
                    Log.e(TAG, "Failed to connect to Spotify App Remote", error)
                    listener.onConnectionFailed(error)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception in connect", e)
            listener.onConnectionFailed(e)
        }
    }

    fun disconnect() {
        try {
            playerStateSubscription?.cancel()
            spotifyAppRemote?.let {
                SpotifyAppRemote.disconnect(it)
                spotifyAppRemote = null
            }
            releaseWakeLock()
            Log.d(TAG, "Disconnected from Spotify")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from Spotify", e)
        }
    }

    fun playTrack(trackUri: String, playbackListener: PlaybackListener? = null) {
        spotifyAppRemote?.let { remote ->
            try {
                // Set repeat mode to ONE (repeat current song)
                remote.playerApi.setRepeat(1) // 1 = Repeat.ONE
                    .setResultCallback {
                        Log.d(TAG, "Repeat mode set to ONE (repeat current track)")
                    }
                    .setErrorCallback { error ->
                        Log.e(TAG, "Failed to set repeat mode", error)
                    }

                remote.playerApi.play(trackUri)
                    .setResultCallback {
                        Log.d(TAG, "Playing track: $trackUri")
                        acquireWakeLock()
                    }
                    .setErrorCallback { error ->
                        Log.e(TAG, "Failed to play track: $trackUri", error)
                    }

                // Subscribe to player state changes if listener provided
                playbackListener?.let { listener ->
                    playerStateSubscription?.cancel()
                    
                    remote.playerApi.subscribeToPlayerState()
                        .setEventCallback { playerState ->
                            try {
                                listener.onTrackChanged(playerState.track)
                                listener.onPlaybackStateChanged(
                                    playerState.isPaused,
                                    playerState.playbackPosition,
                                    playerState.track.duration
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in playback listener", e)
                            }
                        }
                        .setErrorCallback { error ->
                            Log.e(TAG, "Error subscribing to player state", error)
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in playTrack", e)
            }
        } ?: run {
            Log.e(TAG, "SpotifyAppRemote is not connected")
        }
    }

    fun pause() {
        try {
            spotifyAppRemote?.playerApi?.pause()
                ?.setResultCallback {
                    Log.d(TAG, "Pausing playback")
                    releaseWakeLock()
                }
                ?.setErrorCallback { error ->
                    Log.e(TAG, "Failed to pause playback", error)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in pause", e)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "mica-music:MusicWakeLock")
            wakeLock?.setReferenceCounted(false)
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire()
            Log.d(TAG, "WakeLock acquired")
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d(TAG, "WakeLock released")
        }
    }

    fun resume() {
        try {
            spotifyAppRemote?.playerApi?.resume()
                ?.setResultCallback {
                    Log.d(TAG, "Resuming playback")
                }
                ?.setErrorCallback { error ->
                    Log.e(TAG, "Failed to resume playback", error)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in resume", e)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            spotifyAppRemote?.playerApi?.seekTo(positionMs)
                ?.setResultCallback {
                    Log.d(TAG, "Seeking to position: $positionMs")
                }
                ?.setErrorCallback { error ->
                    Log.e(TAG, "Failed to seek to position: $positionMs", error)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in seekTo", e)
        }
    }

    fun setRepeatMode(repeatMode: Int) {
        // repeatMode: 0 = OFF, 1 = ONE (repeat track), 2 = ALL (repeat context)
        spotifyAppRemote?.playerApi?.setRepeat(repeatMode)
            ?.setResultCallback {
                val mode = when (repeatMode) {
                    0 -> "OFF"
                    1 -> "ONE (repeat current track)"
                    2 -> "ALL (repeat context)"
                    else -> "UNKNOWN"
                }
                Log.d(TAG, "Repeat mode set to: $mode")
            }
            ?.setErrorCallback { error ->
                Log.e(TAG, "Failed to set repeat mode to: $repeatMode", error)
            }
    }

    fun isConnected(): Boolean {
        return spotifyAppRemote != null
    }
}