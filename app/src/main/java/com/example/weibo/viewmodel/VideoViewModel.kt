package com.example.weibo.viewmodel

import com.example.weibo.base.BaseViewModel
import com.example.weibo.database.entity.VideoBean
import com.example.weibo.network.api.KaiYanApi
import com.example.weibo.util.VideoUrlResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val kaiYanApi: KaiYanApi
) : BaseViewModel() {

    private val resolvedUrlCache = LinkedHashMap<String, String>(128, 0.75f, true)
    private val resolvingKeys = mutableSetOf<String>()

    private fun cacheKeyOf(video: VideoBean): String = video.resolvedBvid()

    fun getResolvedUrlFromCache(video: VideoBean): String? = resolvedUrlCache[cacheKeyOf(video)]

    private fun putResolvedUrlToCache(key: String, url: String) {
        resolvedUrlCache[key] = url
        if (resolvedUrlCache.size > 100) {
            val iterator = resolvedUrlCache.entries.iterator()
            if (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }
    }

    fun preResolveAround(position: Int, radius: Int = 1) {
        val list = _recommendVideoList.value
        if (list.isEmpty()) return

        val start = (position - radius).coerceAtLeast(0)
        val end = (position + radius).coerceAtMost(list.size - 1)

        for (i in start..end) {
            val video = list[i]
            val key = cacheKeyOf(video)
            if (key.isBlank()) continue
            if (resolvedUrlCache.containsKey(key)) continue
            if (resolvingKeys.contains(key)) continue

            resolvingKeys.add(key)
            launchSafe {
                try {
                    val resolved = VideoUrlResolver.resolvePlayUrl(video.videoUrl)
                    if (resolved.isNotBlank()) {
                        putResolvedUrlToCache(key, resolved)
                    }
                } finally {
                    resolvingKeys.remove(key)
                }
            }
        }
    }

    enum class TabType {
        RECOMMEND,
        FEATURED
    }

    private val _selectedTab = MutableStateFlow(TabType.RECOMMEND)
    val selectedTab: StateFlow<TabType> = _selectedTab.asStateFlow()

    fun selectTab(tab: TabType) {
        _selectedTab.value = tab
        if (tab == TabType.FEATURED && _featuredVideoList.value.isEmpty()) {
            refreshFeatured()
        }
    }

    private val _recommendVideoList = MutableStateFlow<List<VideoBean>>(emptyList())
    val recommendVideoList: StateFlow<List<VideoBean>> = _recommendVideoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _featuredVideoList = MutableStateFlow<List<VideoBean>>(emptyList())
    val featuredVideoList: StateFlow<List<VideoBean>> = _featuredVideoList.asStateFlow()

    init {
        refreshRecommend()
    }

    fun refreshRecommend() {
        launchSafe {
            try {
                _isLoading.value = true
                _error.value = null

                val response = withContext(Dispatchers.IO) {
                    kaiYanApi.getFollowVideos(start = 0, num = 20, newest = true)
                }

                if (response.isSuccessful) {
                    val videoList = response.body()?.itemList?.mapNotNull { item ->
                        convertToVideoBean(item)
                    } ?: emptyList()

                    _recommendVideoList.value = videoList
                } else {
                    _error.value = "获取推荐视频失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "获取推荐视频失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshFeatured() {
        launchSafe {
            try {
                _isLoading.value = true
                _error.value = null

                val response = withContext(Dispatchers.IO) {
                    kaiYanApi.getFollowVideos(start = 0, num = 20, newest = false)
                }

                if (response.isSuccessful) {
                    val videoList = response.body()?.itemList?.mapNotNull { item ->
                        convertToVideoBean(item)
                    } ?: emptyList()

                    _featuredVideoList.value = videoList
                } else {
                    _error.value = "获取精选视频失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "获取精选视频失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun convertToVideoBean(item: com.example.weibo.network.model.KaiYanVideoItem): VideoBean? {
        val videoDetail = item.data.content?.data ?: return null

        return VideoBean(
            id = videoDetail.id.toString(),
            coverUrl = videoDetail.cover?.feed ?: "",
            videoUrl = videoDetail.playUrl ?: "",
            avatarUrl = videoDetail.author?.icon ?: "",
            username = videoDetail.author?.name ?: "未知用户",
            description = videoDetail.descriptionPgc ?: videoDetail.description ?: "",
            musicName = "",
            likeCount = videoDetail.consumption?.collectionCount?.toLong() ?: 0L,
            commentCount = videoDetail.consumption?.replyCount?.toLong() ?: 0L,
            shareCount = videoDetail.consumption?.shareCount?.toLong() ?: 0L,
            durationSec = videoDetail.duration,
            isLiked = false,
            bvid = videoDetail.id.toString()
        )
    }

    private val _playPosition = MutableStateFlow<Int?>(null)
    val playPosition: StateFlow<Int?> = _playPosition.asStateFlow()

    fun requestPlayPosition(position: Int) {
        _playPosition.value = position
    }

    fun clearPlayPosition() {
        _playPosition.value = null
    }

    fun like(position: Int) {
        val currentTab = _selectedTab.value
        val mutableList = when (currentTab) {
            TabType.RECOMMEND -> _recommendVideoList.value.toMutableList()
            TabType.FEATURED -> _featuredVideoList.value.toMutableList()
        }

        if (position in mutableList.indices) {
            val video = mutableList[position]
            if (!video.isLiked) {
                mutableList[position] = video.copy(
                    isLiked = true,
                    likeCount = video.likeCount + 1
                )
                when (currentTab) {
                    TabType.RECOMMEND -> _recommendVideoList.value = mutableList
                    TabType.FEATURED -> _featuredVideoList.value = mutableList
                }
            }
        }
    }

    fun unlike(position: Int) {
        val currentTab = _selectedTab.value
        val mutableList = when (currentTab) {
            TabType.RECOMMEND -> _recommendVideoList.value.toMutableList()
            TabType.FEATURED -> _featuredVideoList.value.toMutableList()
        }

        if (position in mutableList.indices) {
            val video = mutableList[position]
            if (video.isLiked) {
                mutableList[position] = video.copy(
                    isLiked = false,
                    likeCount = (video.likeCount - 1).coerceAtLeast(0)
                )
                when (currentTab) {
                    TabType.RECOMMEND -> _recommendVideoList.value = mutableList
                    TabType.FEATURED -> _featuredVideoList.value = mutableList
                }
            }
        }
    }

    fun toggleLike(position: Int) {
        val currentTab = _selectedTab.value
        val list = when (currentTab) {
            TabType.RECOMMEND -> _recommendVideoList.value
            TabType.FEATURED -> _featuredVideoList.value
        }
        val isLiked = list.getOrNull(position)?.isLiked == true
        if (isLiked) unlike(position) else like(position)
    }

    private var recommendPage = 0
    private var featuredPage = 0

    fun loadMoreRecommend() {
        launchSafe {
            try {
                _isLoading.value = true
                recommendPage++

                val response = withContext(Dispatchers.IO) {
                    kaiYanApi.getFollowVideos(start = recommendPage * 20, num = 20, newest = true)
                }

                if (response.isSuccessful) {
                    val videoList = response.body()?.itemList?.mapNotNull { item ->
                        convertToVideoBean(item)
                    } ?: emptyList()

                    val currentList = _recommendVideoList.value.toMutableList()
                    currentList.addAll(videoList)
                    _recommendVideoList.value = currentList
                } else {
                    _error.value = "加载更多失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "加载更多失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreFeatured() {
        launchSafe {
            try {
                _isLoading.value = true
                featuredPage++

                val response = withContext(Dispatchers.IO) {
                    kaiYanApi.getFollowVideos(start = featuredPage * 20, num = 20, newest = false)
                }

                if (response.isSuccessful) {
                    val videoList = response.body()?.itemList?.mapNotNull { item ->
                        convertToVideoBean(item)
                    } ?: emptyList()

                    val currentList = _featuredVideoList.value.toMutableList()
                    currentList.addAll(videoList)
                    _featuredVideoList.value = currentList
                } else {
                    _error.value = "加载更多失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "加载更多失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        when (_selectedTab.value) {
            TabType.RECOMMEND -> loadMoreRecommend()
            TabType.FEATURED -> loadMoreFeatured()
        }
    }
}
