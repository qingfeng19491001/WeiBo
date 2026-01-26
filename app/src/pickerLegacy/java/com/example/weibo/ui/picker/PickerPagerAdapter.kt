package com.example.weibo.ui.picker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 图片选择器ViewPager适配器
 * 完全复原backup模块的PickerPagerAdapter功能
 */
class PickerPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        MediaGridFragment.newInstance(MediaType.ALL),
        MediaGridFragment.newInstance(MediaType.IMAGE),
        MediaGridFragment.newInstance(MediaType.VIDEO)
    )

    fun getFragments(): List<MediaGridFragment> = fragments

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}








