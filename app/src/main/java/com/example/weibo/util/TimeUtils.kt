package com.example.weibo.util


object TimeUtils {
    
    
    fun formatCount(count: Long): String {
        return when {
            count >= 100000000 -> {
                val yi = count / 100000000.0
                String.format("%.1f亿", yi)
            }
            count >= 10000 -> {
                val wan = count / 10000.0
                String.format("%.1f万", wan)
            }
            else -> count.toString()
        }
    }
    
    
    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}















