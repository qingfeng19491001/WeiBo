package com.example.weibo.viewmodel

import com.example.weibo.base.BaseViewModel
import com.example.weibo.network.api.BiliHotApi
import com.example.weibo.ui.discover.model.BaiduHotSearchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import javax.inject.Inject


@HiltViewModel
class HotSearchDetailViewModel @Inject constructor(
    private val biliHotApi: BiliHotApi
) : BaseViewModel() {

    private val _hotSearchList = MutableStateFlow<List<BaiduHotSearchItem>>(emptyList())
    val hotSearchList: StateFlow<List<BaiduHotSearchItem>> = _hotSearchList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    
    fun loadHotSearchData() {
        launchSafe {
            try {
                _isLoading.value = true
                val resp = withContext(Dispatchers.IO) {
                    biliHotApi.getHotwords()
                }

                if (resp.isSuccessful) {
                    val list = resp.body()?.list ?: emptyList()
                    val hotSearchItems = list.mapIndexed { index, item ->
                        val rank = index + 1
                        val hot = when (index) {
                            in 0..2 -> "热"
                            in 3..5 -> "新"
                            else -> ""
                        }
                        val url = "https://search.bilibili.com/all?keyword=${URLEncoder.encode(item.word, "UTF-8")}"
                        val score = item.score
                        val heat = item.score.replace(",", "").toLongOrNull() ?: 0L

                        BaiduHotSearchItem(
                            index = rank,
                            title = item.word,
                            hot = hot,
                            url = url,
                            subtitle = score,
                            desc = null,
                            heat = heat
                        )
                    }

                    _hotSearchList.value = hotSearchItems
                    _error.value = null
                } else {
                    _error.value = "获取热搜数据失败: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = "获取热搜数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}















