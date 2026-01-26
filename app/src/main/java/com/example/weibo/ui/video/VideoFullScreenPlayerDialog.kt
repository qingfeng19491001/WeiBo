package com.example.weibo.ui.video

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import com.example.weibo.database.entity.VideoBean
import com.example.weibo.util.VideoUrlResolver
import com.example.weibo.video.player.ExoVideoPlayerView
import kotlinx.coroutines.launch


@OptIn(UnstableApi::class)
@Composable
fun VideoFullScreenPlayerDialog(
    video: VideoBean?,
    position: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    if (video == null) {
        return
    }
    
    var playerView by remember { mutableStateOf<ExoVideoPlayerView?>(null) }
    var resolvedVideoUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    
    LaunchedEffect(video.videoUrl) {
        if (resolvedVideoUrl == null && video.videoUrl.isNotEmpty()) {
            resolvedVideoUrl = VideoUrlResolver.resolvePlayUrl(video.videoUrl)
        }
    }
    
    
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            
            AndroidView(
                factory = { ctx ->
                    ExoVideoPlayerView(ctx).apply {
                        bindLifecycle(lifecycleOwner)
                        
                        
                        scope.launch {
                            val url = resolvedVideoUrl ?: video.videoUrl
                            if (url.isNotEmpty()) {
                                val viewTag = "fullscreen_${position}_${video.resolvedBvid()}"
                                setVideoData(url, video.coverUrl, viewTag, true)
                                startPlay()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}















