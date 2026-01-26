package com.example.weibo.viewmodel

import com.example.weibo.base.BaseViewModel
import com.example.weibo.ui.discover.TabType
import com.example.weibo.ui.discover.model.BaiduHotSearchItem
import com.example.weibo.ui.discover.network.BiliSearchApi
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val biliSearchApi: BiliSearchApi
) : BaseViewModel() {

    private val _baiduHotSearch = MutableStateFlow<List<BaiduHotSearchItem>>(emptyList())
    val baiduHotSearch: StateFlow<List<BaiduHotSearchItem>> = _baiduHotSearch.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow<TabType>(TabType.HOT)
    val selectedTab: StateFlow<TabType> = _selectedTab.asStateFlow()

    private val _searchHint = MutableStateFlow<String?>(null)
    val searchHint: StateFlow<String?> = _searchHint.asStateFlow()

    init {
        _selectedTab.value = TabType.HOT
        loadDefaultSearchHint()
        loadBaiduHotSearch()
    }

    
    fun refresh() {
        launchSafe {
            _isLoading.value = true
            _error.value = null
            try {
                coroutineScope {
                    awaitAll(
                        async { loadDefaultSearchHintInternal() },
                        async { loadBaiduHotSearchInternal() }
                    )
                }
            } catch (e: Exception) {
                _error.value = "刷新失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    private suspend fun loadBaiduHotSearchInternal() {
        val resp = withContext(Dispatchers.IO) {
            biliSearchApi.getHotSearchSquare(limit = 50, platform = "web")
        }
        if (resp.isSuccessful) {
            val data = resp.body()?.getAsJsonObject("data")
            
            val trending = data?.getAsJsonObject("trending")
            val list = trending?.getAsJsonArray("list")
            val items = list?.mapIndexedNotNull { index, item ->
                item.asJsonObject.toBaiduHotSearchItem(index)
            } ?: emptyList()
            _baiduHotSearch.value = items
            _error.value = null
        } else {
            _error.value = "获取热搜数据失败: ${resp.code()}"
        }
    }

    
    private suspend fun loadDefaultSearchHintInternal() {
        val resp = withContext(Dispatchers.IO) {
            biliSearchApi.getDefaultSearch()
        }
        if (resp.isSuccessful) {
            val data = resp.body()?.getAsJsonObject("data")
            
            val keyword = data?.get("show_name")?.asString
            _searchHint.value = keyword ?: "大家正在搜：热门内容"
        } else {
            _searchHint.value = "大家正在搜：热门内容"
        }
    }

    
    fun loadBaiduHotSearch() {
        launchSafe {
            try {
                _isLoading.value = true
                loadBaiduHotSearchInternal()
            } catch (e: Exception) {
                _error.value = "获取热搜数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    fun loadDefaultSearchHint() {
        launchSafe {
            try {
                loadDefaultSearchHintInternal()
            } catch (_: Exception) {
                _searchHint.value = "大家正在搜：热门内容"
            }
        }
    }

    
    fun onTabSelected(tabType: TabType) {
        _selectedTab.value = tabType
    }

    
    fun onSearchClicked(query: String) {
        
    }

    
    private fun JsonElement.asStringOrNull(): String? = if (isJsonNull) null else asString

    
    private fun com.google.gson.JsonObject.toBaiduHotSearchItem(index: Int): BaiduHotSearchItem {
        val showName = get("show_name")?.asStringOrNull()
        return BaiduHotSearchItem(
            index = index,
            title = get("keyword")?.asString ?: "",
            hot = get("icon")?.asStringOrNull()?.let {
                if (it.contains("recmd")) "荐"
                else if (it.contains("new")) "新"
                else if (it.contains("hot")) "热"
                else ""
            } ?: "",
            url = get("link")?.asString ?: get("url")?.asString ?: "",
            subtitle = showName,
            desc = showName,
            heat = get("heat")?.asLong
        )
    }
}
