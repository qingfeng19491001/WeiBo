package com.example.weibo.viewmodel

import com.example.weibo.R
import com.example.weibo.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class TaskCenterViewModel @Inject constructor() : BaseViewModel() {
    
    private val _bannerImages = MutableStateFlow<List<Int>>(emptyList())
    val bannerImages: StateFlow<List<Int>> = _bannerImages.asStateFlow()

    private val _bannerTitles = MutableStateFlow<List<String>>(emptyList())
    val bannerTitles: StateFlow<List<String>> = _bannerTitles.asStateFlow()

    private val _currentBannerPosition = MutableStateFlow(0)
    val currentBannerPosition: StateFlow<Int> = _currentBannerPosition.asStateFlow()

    private val _signInDays = MutableStateFlow(2)
    val signInDays: StateFlow<Int> = _signInDays.asStateFlow()

    private val _balance = MutableStateFlow(0.17)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _points = MutableStateFlow(20)
    val points: StateFlow<Int> = _points.asStateFlow()

    init {
        loadBannerData()
    }

    
    private fun loadBannerData() {
        val images = listOf(
            R.drawable.task_center_banner1,
            R.drawable.task_center_banner2,
            R.drawable.task_center_banner3
        )
        val titles = listOf(
            "视界答题挑战·入围赛 参与答题赢288红包",
            "微博渔场 养鱼赚积分",
            "积分商城 好礼兑换"
        )
        _bannerImages.value = images
        _bannerTitles.value = titles
    }

    
    fun updateCurrentBannerPosition(position: Int) {
        _currentBannerPosition.value = position
    }

    
    fun signInToday(): Boolean {
        val currentDays = _signInDays.value
        _signInDays.value = currentDays + 1
        
        val currentPoints = _points.value
        _points.value = currentPoints + 20
        
        return true
    }

    
    fun withdraw(amount: Double): Boolean {
        val currentBalance = _balance.value
        if (amount <= 0 || amount > currentBalance) {
            return false
        }
        _balance.value = currentBalance - amount
        return true
    }
}















