package com.example.weibo.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.example.weibo.core.ui.components.StatusBarPlaceholder
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat


class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                SetupSystemBars(
                    SystemBarsConfig(
                        immersive = true,
                        autoStatusBarIcons = true,
                        autoStatusBarColor = true,
                        topBarBackground = TopBarBackground.Solid(Color.White),
                        statusBarIconsFallbackColor = Color.White
                    )
                )
                WelcomeScreen(
                    onNavigateToMain = {
                        startActivity(Intent(this, com.example.weibo.ui.MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}


@Composable
fun WelcomeScreen(
    onNavigateToMain: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(800)
        onNavigateToMain()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            StatusBarPlaceholder(backgroundColor = Color.White)

            
            
            
            
            Text(
                text = "随时随地\n                发现新鲜事！",
                fontSize = 36.sp,
                fontFamily = FontFamily.Default, 
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(397.dp)
                    .height(74.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 156.dp)
            )
            
            
            
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                
                
                Image(
                    painter = painterResource(id = com.example.weibo.R.drawable.ic_weibo),
                    contentDescription = "微博Logo",
                    modifier = Modifier
                        .width(49.dp)
                        .height(45.dp)
                )
                
                
                
                Text(
                    text = "微博",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

