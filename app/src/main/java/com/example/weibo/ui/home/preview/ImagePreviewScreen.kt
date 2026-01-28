package com.example.weibo.ui.home.preview

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.chrisbanes.photoview.PhotoView
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewScreen(
    imageUrls: List<String>,
    currentPosition: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = remember(context) { ImageLoader(context) }

    val currentPage = remember {
        mutableIntStateOf(currentPosition.coerceIn(0, (imageUrls.size - 1).coerceAtLeast(0)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ViewPager2(ctx).apply {
                    orientation = ViewPager2.ORIENTATION_HORIZONTAL
                    offscreenPageLimit = 1

                    adapter = object : RecyclerView.Adapter<PhotoViewHolder>() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
                            val photoView = PhotoView(parent.context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                maximumScale = 5.0f
                                minimumScale = 1.0f

                                setOnPhotoTapListener { _, _, _ ->
                                    onDismiss()
                                }

                                var downX = 0f
                                var downY = 0f
                                var isHorizontalDrag = false

                                setOnTouchListener { v, event ->
                                    val pv = v as PhotoView

                                    val isAtMinScale = pv.scale <= 1.0001f
                                    val isMultiTouch = event.pointerCount >= 2

                                    when (event.actionMasked) {
                                        MotionEvent.ACTION_DOWN -> {
                                            downX = event.x
                                            downY = event.y
                                            isHorizontalDrag = false
                                            pv.parent?.requestDisallowInterceptTouchEvent(true)
                                        }

                                        MotionEvent.ACTION_POINTER_DOWN -> {
                                            pv.parent?.requestDisallowInterceptTouchEvent(true)
                                        }

                                        MotionEvent.ACTION_MOVE -> {
                                            if (isAtMinScale && !isMultiTouch && !isHorizontalDrag) {
                                                val dx = event.x - downX
                                                val dy = event.y - downY
                                                if (abs(dx) > abs(dy) * 1.2f && abs(dx) > 8f) {
                                                    isHorizontalDrag = true
                                                    pv.parent?.requestDisallowInterceptTouchEvent(false)
                                                }
                                            }
                                        }

                                        MotionEvent.ACTION_UP,
                                        MotionEvent.ACTION_CANCEL -> {
                                            isHorizontalDrag = false
                                            pv.parent?.requestDisallowInterceptTouchEvent(false)
                                        }
                                    }

                                    if (isMultiTouch || !isAtMinScale) {
                                        pv.onTouchEvent(event)
                                    } else if (isHorizontalDrag) {
                                        false
                                    } else {
                                        pv.onTouchEvent(event)
                                    }
                                }
                            }
                            return PhotoViewHolder(photoView)
                        }

                        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
                            val photoView = holder.photoView

                            photoView.setScale(1f, false)

                            val req = ImageRequest.Builder(photoView.context)
                                .data(imageUrls[position])
                                .target(photoView)
                                .build()
                            imageLoader.enqueue(req)
                        }

                        override fun getItemCount(): Int = imageUrls.size
                    }

                    registerOnPageChangeCallback(
                        object : ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                currentPage.intValue = position
                            }
                        }
                    )

                    setCurrentItem(currentPage.intValue, false)
                }
            }
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "关闭",
                tint = Color.White
            )
        }

        Text(
            text = "${currentPage.intValue + 1} / ${imageUrls.size}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

private class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val photoView: PhotoView = itemView as PhotoView
}
