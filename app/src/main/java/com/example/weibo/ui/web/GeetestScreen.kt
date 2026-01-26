package com.example.weibo.ui.web

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView


class GeetestActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_URL = "extra_url"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra(EXTRA_URL) ?: ""
        
        setContent {
            MaterialTheme {
                GeetestScreen(
                    url = url
                )
            }
        }
    }
}


@Composable
fun GeetestScreen(
    url: String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        
        
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(true)
                        allowFileAccess = true
                        allowContentAccess = true
                        javaScriptCanOpenWindowsAutomatically = true
                        loadsImagesAutomatically = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            url?.let {
                                view?.loadUrl(it)
                            }
                            return true
                        }
                    }
                    
                    
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            
                        }
                        
                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            
                        }
                    }
                    
                    if (url.isNotBlank()) {
                        loadUrl(url)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

