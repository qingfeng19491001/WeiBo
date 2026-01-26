package com.example.weibo.ui.livestream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LiveStreamViewModel @Inject constructor() : ViewModel() {

    private val _liveStreamInfo = MutableStateFlow(
        LiveStreamInfo(
            hostName = "主持人高杰",
            hostAvatarUrl = "https://example.com/avatar.png",
            viewerCount = "85.1万观看",
            heatScore = "热度榜·1.3万热度"
        )
    )
    val liveStreamInfo: StateFlow<LiveStreamInfo> = _liveStreamInfo.asStateFlow()

    private val _comments = MutableStateFlow<MutableList<Comment>>(mutableListOf())
    val comments: StateFlow<MutableList<Comment>> = _comments.asStateFlow()

    private var commentIdCounter = 0

    init {
        startReceivingComments()
    }

    private fun startReceivingComments() {
        viewModelScope.launch {
            val mockComments = listOf(
                Comment(++commentIdCounter, "布要熬夜啦", "周深好棒!", 7),
                Comment(++commentIdCounter, "潘美祖", "不好听！戴个佛珠吧", 7),
                Comment(++commentIdCounter, "潘美祖", "..................", 7),
                Comment(++commentIdCounter, "潘美祖", "周深改个名字叫周末吧", 7)
            )

            mockComments.forEach { comment ->
                delay(2000)
                addComment(comment)
            }
        }
    }

    private fun addComment(comment: Comment) {
        val currentList = _comments.value.toMutableList()
        currentList.add(comment)
        if (currentList.size > 100) {
            currentList.removeAt(0)
        }
        _comments.value = currentList
    }

    fun sendComment(content: String) {
        commentIdCounter++
        val newComment = Comment(commentIdCounter, "我", content, 10)
        addComment(newComment)
    }

    fun onFollowClicked() {
        
    }
}


data class LiveStreamInfo(
    val hostName: String,
    val hostAvatarUrl: String,
    val viewerCount: String,
    val heatScore: String
)

