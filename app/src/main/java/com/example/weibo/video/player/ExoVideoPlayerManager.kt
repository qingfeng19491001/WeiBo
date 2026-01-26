package com.example.weibo.video.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import java.io.File
import java.lang.reflect.InvocationTargetException
import kotlin.text.take

@androidx.media3.common.util.UnstableApi
class ExoVideoPlayerManager private constructor(context: Context) {

    private val applicationContext: Context = context.applicationContext
    private val videoProgressMap = mutableMapOf<String, Long>()
    private val cacheDir = File(applicationContext.cacheDir, "exoplayer_cache")

    private val cache: SimpleCache by lazy {
        SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024))
    }

    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(AccessibilityNodeInfoCompat.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_MAX_LENGTH)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        DefaultDataSourceFactory(applicationContext, cacheDataSourceFactory)
    }

    private val mediaSourceFactory: DefaultMediaSourceFactory by lazy {
        DefaultMediaSourceFactory(dataSourceFactory)
    }

    private var exoPlayer: ExoPlayer? = null
    private var currentPlayerView: PlayerView? = null
    private var currentPlayingTag: String? = null

    
    private var preloadPlayer: ExoPlayer? = null
    private var currentPreloadTag: String? = null

    companion object {
        private const val TAG = "ExoVideoPlayerManager"
        @Volatile
        private var INSTANCE: ExoVideoPlayerManager? = null

        fun getInstance(context: Context): ExoVideoPlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExoVideoPlayerManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private fun createPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                50000,
                120000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                5000
            )
            .build()

        return ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false
            }
    }


    private fun getOrCreatePreloadPlayer(): ExoPlayer {
        if (preloadPlayer == null) {
            preloadPlayer = createPlayer()
            Log.d(TAG, "Created preload ExoPlayer instance")
        }
        return preloadPlayer!!
    }

    fun saveVideoProgress(viewTag: String, progressMs: Long) {
        if (progressMs > 0) {
            videoProgressMap[viewTag] = progressMs
            Log.d(TAG, "Saved video progress for tag: $viewTag, progress: ${progressMs}ms")
        }
    }

    fun getVideoProgress(viewTag: String): Long {
        return videoProgressMap[viewTag] ?: 0L
    }

    fun clearVideoProgress(viewTag: String) {
        videoProgressMap.remove(viewTag)
        Log.d(TAG, "Cleared video progress for tag: $viewTag")
    }

    
    interface PlayerCallback {
        fun onRenderedFirstFrame(tag: String)
        fun onPlaybackEnded(tag: String)
        fun onPlayerError(tag: String, error: Exception)
        fun onReadyPlaying(tag: String)
        fun onBuffering(tag: String)
    }

    private val callbacksByTag = mutableMapOf<String, PlayerCallback>()

    fun registerCallback(tag: String, callback: PlayerCallback) {
        callbacksByTag[tag] = callback
    }

    fun unregisterCallback(tag: String) {
        callbacksByTag.remove(tag)
    }

    private fun callbackForCurrentTag(): PlayerCallback? {
        val tag = currentPlayingTag ?: return null
        return callbacksByTag[tag]
    }

    private val playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            val tag = currentPlayingTag ?: return
            callbackForCurrentTag()?.onRenderedFirstFrame(tag)
        }

        override fun onPlaybackStateChanged(state: Int) {
            val tag = currentPlayingTag ?: return
            val cb = callbackForCurrentTag() ?: return
            val player = exoPlayer ?: return

            when (state) {
                Player.STATE_BUFFERING, Player.STATE_IDLE -> {
                    cb.onBuffering(tag)
                }
                Player.STATE_READY -> {
                    if (player.isPlaying) {
                        cb.onReadyPlaying(tag)
                    }
                }
                Player.STATE_ENDED -> {
                    
                    val duration = player.duration
                    val position = player.currentPosition
                    if (duration > 0 && (duration - position) <= 1500) {
                        cb.onPlaybackEnded(tag)
                    } else {
                        Log.w(TAG, "Ignore STATE_ENDED (likely misfire): tag=$tag, pos=$position, dur=$duration")
                    }
                }
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            val tag = currentPlayingTag ?: return
            callbackForCurrentTag()?.onPlayerError(tag, error)
        }
    }

    private fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = createPlayer().apply {
                addListener(playerListener) 
            }
            Log.d(TAG, "Created ExoPlayer instance with optimized LoadControl")
        }
        return exoPlayer!!
    }

    fun playVideo(
        videoUrl: String,
        viewTag: String,
        playerView: PlayerView,
        autoPlay: Boolean = true
    ) {
        if (videoUrl.isEmpty()) {
            Log.w(TAG, "Video URL is empty for tag: $viewTag")
            return
        }

        val player = getOrCreatePlayer()
        val currentMediaItem = if (player.mediaItemCount > 0) player.currentMediaItem else null
        val currentMediaUrl = currentMediaItem?.mediaId ?: ""

        val needsSwitch = !(currentPlayingTag == viewTag && currentMediaUrl == videoUrl)

        if (!needsSwitch && player.isPlaying) {
            Log.d(TAG, "Already playing the same video, tag: $viewTag, URL: ${videoUrl.take(100)}")
            return
        }

        if (needsSwitch) {
            if (currentPlayingTag != null && currentPlayingTag != viewTag) {
                Log.d(TAG, "Switching from tag: $currentPlayingTag to tag: $viewTag")
            } else if (currentPlayingTag == viewTag && currentMediaUrl != videoUrl) {
                Log.d(
                    TAG,
                    "Same tag but different URL, updating video: ${currentMediaUrl.take(50)} -> ${videoUrl.take(50)}"
                )
            }

            player.pause()
            player.stop()
            player.clearMediaItems()

            if (currentPlayerView != null && currentPlayerView != playerView) {
                currentPlayerView?.player = null
                Log.d(TAG, "Unbound old PlayerView")
            }
            Log.d(TAG, "Old video stopped and cleared, preparing new video")
        }

        if (currentPlayerView == playerView) {
            if (playerView.player != player) {
                playerView.player = player
                Log.d(TAG, "Re-bound PlayerView to player")
            }
        } else {
            currentPlayerView?.player = null
            currentPlayerView = playerView
            playerView.player = player
            Log.d(TAG, "Bound new PlayerView to player")
        }

        currentPlayingTag = viewTag

        try {
            Log.d(
                TAG,
                "Configuring video player, tag: $viewTag, URL: ${videoUrl.take(100)}, autoPlay=$autoPlay"
            )
            val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .setMediaId(videoUrl)
            .build()

            if (player.mediaItemCount > 0) {
                player.clearMediaItems()
            }
            player.setMediaItem(mediaItem)
            player.prepare()

            val savedProgress = getVideoProgress(viewTag)
            if (savedProgress > 0) {
                Log.d(
                    TAG,
                    "Found saved progress for tag: $viewTag, progress: ${savedProgress}ms, seeking to position"
                )
                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            val duration = player.duration
                            if (duration > 0 && savedProgress < duration) {
                                player.seekTo(savedProgress)
                                Log.d(
                                    TAG,
                                    "Seeked to saved progress: ${savedProgress}ms (duration: ${duration}ms)"
                                )
                            } else {
                                Log.d(
                                    TAG,
                                    "Saved progress ${savedProgress}ms is invalid (duration: ${duration}ms), starting from beginning"
                                )
                            }
                            player.removeListener(this)
                        }
                    }
                })
            }

            Log.d(TAG, "Video prepared, setting playWhenReady=$autoPlay")
            if (autoPlay) {
                player.playWhenReady = true
                player.play()
                Log.d(
                    TAG,
                    "playWhenReady=true and play() called, state=${player.playbackState}, isPlaying=${player.isPlaying}"
                )
                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            if (!player.isPlaying) {
                                Log.d(TAG, "STATE_READY but not playing, calling play() again")
                                player.play()
                            }
                            player.removeListener(this)
                        }
                    }
                })
            }

            Log.d(
                TAG,
                "Video prepared and ${if (autoPlay) "should be playing" else "ready"}, tag: $viewTag, playWhenReady: ${player.playWhenReady}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring or starting video playback for tag: $viewTag", e)
            e.printStackTrace()
        }
    }

    fun preloadVideo(videoUrl: String, viewTag: String) {
        if (videoUrl.isEmpty()) return

        
        val player = getOrCreatePreloadPlayer()

        
        if (currentPreloadTag == viewTag) return
        currentPreloadTag = viewTag

        try {
            
            
            player.pause()
            player.stop()
            player.clearMediaItems()

            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem,  true)
            player.prepare()
            player.playWhenReady = false
            Log.d(TAG, "Preloading video prepared, tag: $viewTag, URL: ${videoUrl.take(100)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading video, tag: $viewTag", e)
        }
    }

    @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun unbindPlayerView() {
        currentPlayerView?.player = null
        currentPlayerView = null
    }

    fun pause(tag: String? = null) {
        val player = exoPlayer ?: return
        if (tag == null || currentPlayingTag == tag) {
            player.pause()
        }
    }

    fun resume(tag: String? = null) {
        val player = exoPlayer ?: return
        if (tag == null || currentPlayingTag == tag) {
            player.play()
        }
    }

    fun stop(tag: String? = null) {
        val player = exoPlayer ?: return
        if (tag == null || currentPlayingTag == tag) {
            val currentTag = currentPlayingTag
            if (currentTag != null) {
                val currentPosition = player.currentPosition
                if (currentPosition > 0) {
                    saveVideoProgress(currentTag, currentPosition)
                    Log.d(
                        TAG,
                        "Saved progress before stop: tag=$currentTag, progress=${currentPosition}ms"
                    )
                }
            }
            player.stop()
            player.clearMediaItems()
        }
    }

    @Throws(IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentPlayerView?.player = null
        currentPlayerView = null
        currentPlayingTag = null
        Log.d(TAG, "ExoPlayer released")
    }

    fun getCurrentPlayingTag(): String? = currentPlayingTag

    fun isPlaying(tag: String): Boolean {
        val player = exoPlayer
        return player != null && currentPlayingTag == tag && player.isPlaying
    }

    fun getPlayer(tag: String): ExoPlayer? {
        return if (currentPlayingTag == tag) exoPlayer else null
    }

    fun bindLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    val player = exoPlayer
                    if (player != null && currentPlayingTag != null) {
                        player.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    val player = exoPlayer
                    if (player != null && currentPlayingTag != null) {
                        player.play()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    stop()
                }
                else -> {}
            }
        })
        Log.d(TAG, "Lifecycle bound to PlayerManager.")
    }
}








