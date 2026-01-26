package com.example.weibo.ui.picker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.systemBarsConfigForTopBar

class ImagePickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 让该入口 Activity 也完全走 Compose/沉浸式体系
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val alreadySelectedCount = intent.getIntExtra("already_selected_count", 0)
        val maxSelectable = intent.getIntExtra("max_selectable", 18)

        setContent {
            var statusBarBg by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.graphics.Color.Transparent) }

            SetupSystemBars(
                config = systemBarsConfigForTopBar(
                    // ImagePickerScreen 顶栏/Tab 栏为深色 0xFF222222
                    topBarBackground = TopBarBackground.Solid(androidx.compose.ui.graphics.Color(0xFF222222)),
                    statusBarIconsFallbackColor = androidx.compose.ui.graphics.Color(0xFF222222),
                    statusBarColorFallbackColor = androidx.compose.ui.graphics.Color(0xFF222222)
                ).copy(
                    // 深色背景 -> 使用浅色图标（isAppearanceLightStatusBars = false）
                    statusBarDarkIcons = false
                ),
                onFinalColorCalculated = { statusBarBg = it }
            )

            com.example.weibo.core.ui.components.StatusBarPlaceholder(
                backgroundColor = statusBarBg,
                modifier = androidx.compose.ui.Modifier.zIndex(1000f)
            )

            ImagePickerScreen(
                alreadySelectedCount = alreadySelectedCount,
                maxSelectable = maxSelectable,
                onDismiss = { finish() },
                onImagesSelected = { uris ->
                    if (uris.isNotEmpty()) {
                        val resultIntent = Intent().apply {
                            putParcelableArrayListExtra("selected_images", ArrayList<Uri>(uris))
                        }
                        setResult(RESULT_OK, resultIntent)
                    }
                    finish()
                }
            )
        }
    }
}
