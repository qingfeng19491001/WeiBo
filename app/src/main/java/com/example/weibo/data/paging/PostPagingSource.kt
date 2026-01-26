package com.example.weibo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.weibo.data.model.Post
import com.example.weibo.data.mock.MockWeiboData


class PostPagingSource(
    private val isFollowFeed: Boolean = false
) : PagingSource<Int, Post>() {
    
    companion object {
        @Volatile
        private var cachedLocalPosts: List<Post> = emptyList()
        
        fun updateLocalPosts(posts: List<Post>) {
            cachedLocalPosts = posts
        }
        
        fun getLocalPosts(): List<Post> = cachedLocalPosts
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            val posts = if (isFollowFeed) {

                val allLocalPosts = getLocalPosts()
                val offset = page * pageSize
                if (offset >= allLocalPosts.size) {
                    emptyList()
                } else {
                    allLocalPosts.subList(offset, minOf(offset + pageSize, allLocalPosts.size))
                }
            } else {

                generateMockPosts(page, pageSize, false)
            }
            
            LoadResult.Page(
                data = posts,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (posts.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private fun generateMockPosts(page: Int, pageSize: Int, isFollow: Boolean): List<Post> {
        if (isFollow) {
            return emptyList()
        }
        
        val all = MockWeiboData.getMockPosts()
        val offset = page * pageSize
        if (offset >= all.size) return emptyList()
        return all.subList(offset, minOf(offset + pageSize, all.size))
    }
}

