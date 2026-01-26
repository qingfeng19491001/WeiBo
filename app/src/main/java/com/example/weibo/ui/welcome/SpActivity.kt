package com.example.weibo.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import com.example.weibo.ui.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import androidx.core.content.edit


class SpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SpScreen {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SpScreen(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("ad_preferences", Context.MODE_PRIVATE)
    }

    
    val adImages = remember {
        listOf(
            R.drawable.img_ad1,
            R.drawable.img_ad2,
            R.drawable.img_ad3
        )
    }

    
    val currentAdPosition = remember {
        val last = prefs.getInt("last_ad_position", -1)
        val next = getRandomAdPosition(last, adImages.size)
        prefs.edit { putInt("last_ad_position", next) }
        next
    }

    val selectedAdImages = remember { listOf(adImages[currentAdPosition]) }

    var countdown by remember { mutableIntStateOf(5) }
    var navigating by remember { mutableStateOf(false) }

    
    LaunchedEffect(Unit) {
        
        launch {
            while (countdown > 0 && !navigating) {
                delay(1000)
                countdown--
                if (countdown == 0 && !navigating) {
                    navigating = true
                    onNavigateToMain()
                }
            }
        }
        
        delay(5000)
        if (!navigating) {
            navigating = true
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { selectedAdImages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = selectedAdImages[page]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        
        Text(
            text = if (countdown > 0) "跳过 $countdown" else "跳过",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color(0x70000000), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .clickable(enabled = !navigating) {
                    if (!navigating) {
                        navigating = true
                        onNavigateToMain()
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun getRandomAdPosition(lastPosition: Int, totalSize: Int): Int {
    if (totalSize <= 1) return 0
    var pos: Int
    do {
        pos = Random().nextInt(totalSize)
    } while (pos == lastPosition)
    return pos
}