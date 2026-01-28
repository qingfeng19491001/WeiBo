package com.example.weibo.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.weibo.base.BaseViewModel
import com.example.weibo.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: com.example.weibo.data.repository.PostRepository
) : BaseViewModel() {

    val localPostCount: StateFlow<Int> = repository.localPostCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)


    private val _selectedBottomNavIndex = MutableStateFlow(0)
    val selectedBottomNavIndex: StateFlow<Int> = _selectedBottomNavIndex.asStateFlow()

    fun switchBottomNav(index: Int) {
        if (index in 0..4) {
            _selectedBottomNavIndex.value = index
        }
    }


    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun switchTab(index: Int) {
        if (index in 0..1) {
            _currentTab.value = index
        }
    }


    val feedPaging: Flow<PagingData<Post>> = repository.postsPagingFlow()
        .cachedIn(viewModelScope)

    val followPaging: Flow<PagingData<Post>> = repository.followPostsPagingFlow()
        .cachedIn(viewModelScope)

    private val _navigateToHomeFollow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToHomeFollow: SharedFlow<Unit> = _navigateToHomeFollow.asSharedFlow()

    fun refreshHotFromApifox() {
        launchSafe {
        }
    }

    fun refreshPosts() {
        launchSafe {
            repository.refreshPosts()
        }
    }

    fun likePost(postId: String) {
        launchSafe {
            repository.likePost(postId)
        }
    }

    fun sharePost(@Suppress("UNUSED_PARAMETER") postId: String) {
        launchSafe {
        }
    }

    /**
     * 删除帖子
     */
    fun deletePost(postId: String) {
        launchSafe {
            repository.deletePost(postId)
        }
    }

    /**
     * 发布帖子
     * @param content 帖子内容
     * @param images 图片URL列表
     */
    fun publishPost(content: String, images: List<String>) {
        launchSafe {
            try {
                val success = repository.addPost(content, images)
                if (success) {
                    refreshPosts()
                    _navigateToHomeFollow.tryEmit(Unit)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
