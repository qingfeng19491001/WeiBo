package com.example.weibo.video.player

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.weibo.R
import kotlin.math.abs
import kotlin.text.take

@androidx.media3.common.util.UnstableApi
class ExoVideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val playerManager: ExoVideoPlayerManager =
        ExoVideoPlayerManager.getInstance(context)

    private val playerView: PlayerView = PlayerView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        useController = false
        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        isClickable = false
        isFocusable = false
    }

    private var currentVideoUrl = ""
    private var viewTag = ""
    private var autoPlay = true
    var onPlaybackEnded: (() -> Unit)? = null 

    private var callbackRegistered = false

    private val coverImageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private val controlLayout: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = android.view.Gravity.BOTTOM
        }
        setPadding(16, 16, 16, 16)
        setBackgroundColor(0x80000000.toInt())
        visibility = View.GONE
    }

    private val progressBar: SeekBar
    private val tvCurrentTime: TextView
    private val tvTotalTime: TextView
    private val ivPlayPause: ImageView
    private val centerPlayPauseButton: ImageView

    private var gestureDetector: GestureDetectorCompat? = null
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var dragMode = DragMode.NONE

    private val handler = Handler(Looper.getMainLooper())
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    private var progressUpdateRunnable: Runnable? = null
    private var lastProgressSaveTime = 0L
    private val PROGRESS_SAVE_INTERVAL = 5000L

    private enum class DragMode {
        NONE, VOLUME, BRIGHTNESS, PROGRESS
    }

    init {
        ensurePlayerCallbackRegistered()

        addView(playerView)
        isClickable = true
        isFocusable = true

        addView(coverImageView)

        addView(controlLayout)

        val progressLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        tvCurrentTime = TextView(context).apply {
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "00:00"
        }

        progressBar = SeekBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 8, 0)
            }
            max = 1000
            progress = 0
        }

        tvTotalTime = TextView(context).apply {
            textSize = 12f
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "00:00"
        }

        progressLayout.addView(tvCurrentTime)
        progressLayout.addView(progressBar)
        progressLayout.addView(tvTotalTime)

        ivPlayPause = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            setImageResource(R.drawable.ic_play)
            setPadding(16, 16, 16, 16)
        }

        controlLayout.addView(progressLayout)
        controlLayout.elevation = 10f
        controlLayout.isClickable = true

        centerPlayPauseButton = ImageView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            setImageResource(R.drawable.ic_play)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x80000000.toInt())
            }
            visibility = View.GONE
            elevation = 15f
            isClickable = true
        }
        addView(centerPlayPauseButton)

        setupClickListeners()
        setupGestureDetector()

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val player = playerManager.getPlayer(viewTag) ?: return
                    val duration = player.duration
                    if (duration > 0) {
                        val position = ((progress * duration) / 1000).coerceIn(0L, duration)
                        player.seekTo(position)
                        updateTimeDisplay()
                        Log.d(TAG, "Seek to: ${position}ms (progress: $progress/1000)")
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDragging = true
                Log.d(TAG, "Start dragging progress bar")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDragging = false
                Log.d(TAG, "Stop dragging progress bar")
                updateProgress()
            }
        })
    }

    private fun ensurePlayerCallbackRegistered() {
        if (callbackRegistered) return
        callbackRegistered = true
        
        
    }

    private fun setupClickListeners() {
        centerPlayPauseButton.setOnClickListener {
            togglePlayPause()
            showControlBar()
            handler.postDelayed({
                if (!isDragging) {
                    hideControlBar()
                }
            }, C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS)
        }
    }

    private fun setupGestureDetector() {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                initialTouchX = e.x
                initialTouchY = e.y
                isDragging = false
                dragMode = DragMode.NONE
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                togglePlayPause()
                showControlBar()
                showCenterPlayPauseButton()
                handler.postDelayed({
                    if (!isDragging) {
                        hideControlBar()
                        hideCenterPlayPauseButton()
                    }
                }, C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1 == null) return false

                if (!isDragging) {
                    val deltaX = abs(e2.x - initialTouchX)
                    val deltaY = abs(e2.y - initialTouchY)

                    if (deltaY > deltaX && deltaY > 50f) {
                        dragMode = if (e2.x < width / 2) {
                            DragMode.BRIGHTNESS
                        } else {
                            DragMode.VOLUME
                        }
                        isDragging = true
                    } else if (deltaX > deltaY && deltaX > 50f) {
                        dragMode = DragMode.PROGRESS
                        isDragging = true
                    }
                }

                if (isDragging) {
                    when (dragMode) {
                        DragMode.VOLUME -> adjustVolume(e2.y - e1.y)
                        DragMode.BRIGHTNESS -> adjustBrightness(e2.y - e1.y)
                        DragMode.PROGRESS -> adjustProgress(e2.x - e1.x)
                        DragMode.NONE -> {}
                    }
                }

                return true
            }
        }

        gestureDetector = GestureDetectorCompat(context, gestureListener)
        setOnTouchListener { v, event ->
            val handled = gestureDetector?.onTouchEvent(event) ?: false
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
                true
            } else {
                handled
            }
        }
    }

    private fun adjustVolume(deltaY: Float) {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volumeDelta = ((-deltaY) / height * maxVolume).toInt()
        val newVolume = (currentVolume + volumeDelta).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        Log.d(TAG, "Volume adjusted: $newVolume/$maxVolume")
    }

    private fun adjustBrightness(deltaY: Float) {
        val activity = context as? Activity ?: return
        val layoutParams = activity.window.attributes
        val currentBrightness = layoutParams.screenBrightness
        val brightnessDelta = (-deltaY) / height
        val newBrightness = (currentBrightness + brightnessDelta).coerceIn(0.01f, 1.0f)
        layoutParams.screenBrightness = newBrightness
        activity.window.attributes = layoutParams
        Log.d(TAG, "Brightness adjusted: $newBrightness")
    }

    private fun adjustProgress(deltaX: Float) {
        val player = playerManager.getPlayer(viewTag) ?: return
        val duration = player.duration
        if (duration > 0) {
            val currentPosition = player.currentPosition
            val progressDelta = ((deltaX / width) * duration).toLong()
            val newPosition = (currentPosition + progressDelta).coerceIn(0L, duration)
            player.seekTo(newPosition)
            updateProgress()
        }
    }

    fun setVideoData(
        videoUrl: String,
        coverUrl: String,
        tag: String,
        autoPlay: Boolean = true
    ) {
        Log.d(TAG, "setVideoData called: tag=$tag, url=${videoUrl.take(100)}, autoPlay=$autoPlay")
        currentVideoUrl = videoUrl

        
        if (viewTag.isNotBlank() && viewTag != tag) {
            playerManager.unregisterCallback(viewTag)
        }
        viewTag = tag
        this.autoPlay = autoPlay

        playerManager.registerCallback(
            tag,
            object : ExoVideoPlayerManager.PlayerCallback {
                override fun onRenderedFirstFrame(tag: String) {
                    if (tag != viewTag) return
                    handler.post {
                        coverImageView.visibility = View.GONE
                        updatePlayPauseButton(isPlaying())
                        startProgressUpdate()
                    }
                }

                override fun onReadyPlaying(tag: String) {
                    if (tag != viewTag) return
                    handler.post {
                        coverImageView.visibility = View.GONE
                        updatePlayPauseButton(true)
                        startProgressUpdate()
                    }
                }

                override fun onBuffering(tag: String) {
                    if (tag != viewTag) return
                    handler.post {
                        coverImageView.visibility = View.VISIBLE
                    }
                }

                override fun onPlaybackEnded(tag: String) {
                    if (tag != viewTag) return
                    handler.post {
                        coverImageView.visibility = View.VISIBLE
                        stopProgressUpdate()
                        onPlaybackEnded?.invoke()
                    }
                }

                override fun onPlayerError(tag: String, error: Exception) {
                    if (tag != viewTag) return
                    Log.e(TAG, "Playback error, tag=$tag", error)
                    handler.post {
                        coverImageView.visibility = View.VISIBLE
                        stopProgressUpdate()
                    }
                }
            }
        )

        if (coverUrl.isNotEmpty()) {
            Glide.with(context)
                .load(coverUrl)
                .placeholder(R.drawable.ic_video_placeholder)
                .error(R.drawable.ic_video_placeholder)
                .centerCrop()
                .into(coverImageView)
            coverImageView.visibility = View.VISIBLE
        } else {
            coverImageView.setImageResource(R.drawable.ic_video_placeholder)
            coverImageView.visibility = View.VISIBLE
        }

        val isCurrentlyPlaying =
            playerManager.getCurrentPlayingTag() == tag && playerManager.isPlaying(tag)
        
        coverImageView.visibility = if (isCurrentlyPlaying) View.GONE else View.VISIBLE
        Log.d(
            TAG,
            "setVideoData completed: currentVideoUrl=${currentVideoUrl.take(100)}, viewTag=$viewTag"
        )
    }

    fun startPlay() {
        if (currentVideoUrl.isEmpty() || viewTag.isEmpty()) {
            Log.w(
                TAG,
                "Cannot start play: videoUrl or viewTag is empty, videoUrl=${currentVideoUrl.take(50)}, viewTag=$viewTag"
            )
            return
        }

        
        coverImageView.visibility = View.VISIBLE

        Log.d(TAG, "Starting playback, tag: $viewTag, URL: ${currentVideoUrl.take(100)}")
        val currentPlayingTag = playerManager.getCurrentPlayingTag()
        if (currentPlayingTag != null && currentPlayingTag != viewTag) {
            Log.d(TAG, "Stopping previous video: $currentPlayingTag, starting new: $viewTag")
            playerManager.stop(currentPlayingTag)
        }

        
        playerManager.playVideo(currentVideoUrl, viewTag, playerView, autoPlay)

        
        if (autoPlay) {
            val player = playerManager.getPlayer(viewTag)
            updatePlayPauseButton(player?.isPlaying == true)
            if (player != null && player.isPlaying) {
                startProgressUpdate()
            }
        }
    }

    private fun startProgressUpdate() {
        Log.d(TAG, "Starting progress update, viewTag=$viewTag")
        stopProgressUpdate()
        lastProgressSaveTime = System.currentTimeMillis()
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                updateTimeDisplay()

                val currentTime = System.currentTimeMillis()
                val player = playerManager.getPlayer(viewTag)
                if (currentTime - lastProgressSaveTime >= PROGRESS_SAVE_INTERVAL &&
                    player != null && player.isPlaying && player.currentPosition > 0
                ) {
                    val currentPosition = player.currentPosition
                    val duration = player.duration
                    if (currentPosition > C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS &&
                        duration > 0 &&
                        currentPosition < duration - 5000
                    ) {
                        playerManager.saveVideoProgress(viewTag, currentPosition)
                        lastProgressSaveTime = currentTime
                        Log.d(
                            TAG,
                            "Auto-saved progress: tag=$viewTag, progress=${currentPosition}ms"
                        )
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(progressUpdateRunnable!!)
    }

    private fun stopProgressUpdate() {
        progressUpdateRunnable?.let {
            handler.removeCallbacks(it)
            progressUpdateRunnable = null
        }
    }

    private fun updateProgress() {
        val player = playerManager.getPlayer(viewTag) ?: return
        val duration = player.duration
        val position = player.currentPosition
        if (duration > 0 && !isDragging) {
            val progress = ((1000 * position / duration).toInt()).coerceIn(0, 1000)
            progressBar.progress = progress
            Log.d(TAG, "Progress updated: $progress/1000, position=${position}ms, duration=${duration}ms")
        }
    }

    private fun updateTimeDisplay() {
        val player = playerManager.getPlayer(viewTag) ?: return
        val duration = player.duration
        val position = player.currentPosition
        tvCurrentTime.text = formatTime(position)
        tvTotalTime.text = formatTime(duration)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        centerPlayPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun showCenterPlayPauseButton() {
        Log.d(TAG, "Showing center play/pause button")
        centerPlayPauseButton.visibility = View.VISIBLE
    }

    private fun hideCenterPlayPauseButton() {
        Log.d(TAG, "Hiding center play/pause button")
        centerPlayPauseButton.visibility = View.GONE
    }

    fun pausePlay() {
        val player = playerManager.getPlayer(viewTag) ?: return
        val currentPosition = player.currentPosition
        if (currentPosition > 0) {
            playerManager.saveVideoProgress(viewTag, currentPosition)
            Log.d(TAG, "Saved progress on pause: tag=$viewTag, progress=${currentPosition}ms")
        }
        player.pause()
    }

    fun resumePlay() {
        playerManager.getPlayer(viewTag)?.play()
    }

    fun stopPlay() {
        val player = playerManager.getPlayer(viewTag) ?: return
        val currentPosition = player.currentPosition
        if (currentPosition > 0) {
            playerManager.saveVideoProgress(viewTag, currentPosition)
            Log.d(TAG, "Saved progress on stop: tag=$viewTag, progress=${currentPosition}ms")
        }
        player.stop()
        coverImageView.visibility = View.VISIBLE
        stopProgressUpdate()
    }

    fun showControlBar() {
        Log.d(TAG, "Showing control bar")
        controlLayout.visibility = View.VISIBLE
    }

    fun hideControlBar() {
        Log.d(TAG, "Hiding control bar")
        controlLayout.visibility = View.GONE
    }
    
    fun showControlBarWithAutoHide() {
        showControlBar()
        handler.postDelayed({
            if (!isDragging) {
                hideControlBar()
            }
        }, C.DEFAULT_MAX_SEEK_TO_PREVIOUS_POSITION_MS)
    }

    fun togglePlayPause() {
        Log.d(TAG, "togglePlayPause called, viewTag=$viewTag")
        val player = playerManager.getPlayer(viewTag)
        if (player != null) {
            val wasPlaying = player.isPlaying
            Log.d(TAG, "Player found, isPlaying=$wasPlaying")
            if (wasPlaying) {
                Log.d(TAG, "Pausing playback")
                pausePlay()
            } else {
                Log.d(TAG, "Resuming playback")
                resumePlay()
            }
        } else {
            Log.d(TAG, "Player not found, starting playback")
            startPlay()
        }
    }

    fun isPlaying(): Boolean = playerManager.isPlaying(viewTag)

    fun bindLifecycle(lifecycleOwner: LifecycleOwner) {
        playerManager.bindLifecycle(lifecycleOwner)
    }

    fun getPlayerView(): PlayerView = playerView

    fun release() {
        stopProgressUpdate()
        val player = playerManager.getPlayer(viewTag)
        if (player != null) {
            val currentPosition = player.currentPosition
            if (currentPosition > 0) {
                playerManager.saveVideoProgress(viewTag, currentPosition)
                Log.d(TAG, "Saved progress on release: tag=$viewTag, progress=${currentPosition}ms")
            }
        }
        if (viewTag.isNotBlank()) {
            playerManager.unregisterCallback(viewTag)
        }
        if (playerManager.getCurrentPlayingTag() == viewTag) {
            playerManager.stop(viewTag)
        }
        currentVideoUrl = ""
        viewTag = ""
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    companion object {
        private const val TAG = "ExoVideoPlayerView"
    }
}

