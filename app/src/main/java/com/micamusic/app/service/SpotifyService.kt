package com.micamusic.app.service

import android.content.Context
import android.util.Log
import android.os.PowerManager

// Note: This is a simplified version. In production, uncomment the Spotify imports
// and download the Spotify App Remote SDK from developer.spotify.com
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyService(private val context: Context) {

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val CLIENT_ID = "4f8d66a264394395ad08998058dd6bdd" // This will be replaced with actual client ID
        private const val REDIRECT_URI = "mica-music://callback"
        private const val TAG = "SpotifyService"
    }

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var playerStateSubscription: Subscription<PlayerState>? = null

    interface SpotifyConnectionListener {
        fun onConnected()
        fun onConnectionFailed(error: Throwable)
        fun onDisconnected()
    }

    interface PlaybackListener {
        fun onTrackChanged(track: Track)
        fun onPlaybackStateChanged(isPaused: Boolean, position: Long, duration: Long)
    }

    fun connect(listener: SpotifyConnectionListener) {
        try {
            val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

            SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    try {
                        spotifyAppRemote = appRemote
                        Log.d(TAG, "Connected to Spotify")
                        listener.onConnected()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onConnected callback", e)
                        listener.onConnectionFailed(e)
                    }
                }

                override fun onFailure(error: Throwable) {
                    Log.e(TAG, "Failed to connect to Spotify", error)
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