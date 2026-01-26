package com.example.weibo.data.mock

import com.example.weibo.data.model.Post
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MockWeiboData {
    private val gson = Gson()
    private val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun getMockPosts(): List<Post> {
        val now = System.currentTimeMillis()
        val posts = mutableListOf<Post>()

        val mockData = listOf(
            MockPostData(
                content = "今天天气真好，适合出去走走。分享一张美图给大家～",
                images = listOf("https://picsum.photos/400/300?random=1"),
                likes = 128,
                comments = 45,
                shares = 12,
                hoursAgo = 2
            ),
            MockPostData(
                content = "最近在学新技能，感觉每天都在进步。坚持就是胜利！",
                images = emptyList(),
                likes = 256,
                comments = 89,
                shares = 23,
                hoursAgo = 5
            ),
            MockPostData(
                content = "周末和朋友一起聚餐，美食配美景，生活就该这样享受～",
                images = listOf(
                    "https://picsum.photos/400/300?random=2",
                    "https://picsum.photos/400/300?random=3",
                    "https://picsum.photos/400/300?random=4"
                ),
                likes = 512,
                comments = 156,
                shares = 67,
                hoursAgo = 8
            ),
            MockPostData(
                content = "分享一个有趣的小故事：今天在路上遇到一只可爱的小猫，它一直跟着我，最后我给它买了点吃的。小动物真的很治愈～",
                images = listOf("https://picsum.photos/400/300?random=5"),
                likes = 1024,
                comments = 234,
                shares = 89,
                hoursAgo = 12
            ),
            MockPostData(
                content = "新的一天开始了，加油！",
                images = emptyList(),
                likes = 64,
                comments = 12,
                shares = 5,
                hoursAgo = 15
            ),
            MockPostData(
                content = "今天读了一本好书，推荐给大家：《思考，快与慢》。这本书让我对思维方式有了新的认识。",
                images = emptyList(),
                likes = 189,
                comments = 67,
                shares = 34,
                hoursAgo = 18
            ),
            MockPostData(
                content = "晚上做了顿丰盛的晚餐，虽然简单但很满足。生活就是由这些小小的幸福组成的。",
                images = listOf(
                    "https://picsum.photos/400/300?random=6",
                    "https://picsum.photos/400/300?random=7"
                ),
                likes = 345,
                comments = 98,
                shares = 45,
                hoursAgo = 20
            ),
            MockPostData(
                content = "今天完成了一个重要项目，虽然过程很累，但看到成果的那一刻，所有的努力都值得了。",
                images = emptyList(),
                likes = 678,
                comments = 145,
                shares = 78,
                hoursAgo = 24
            ),
            MockPostData(
                content = "周末去公园散步，看到很多人在运动，自己也跟着跑了几圈。运动真的能让人心情变好！",
                images = listOf(
                    "https://picsum.photos/400/300?random=8",
                    "https://picsum.photos/400/300?random=9",
                    "https://picsum.photos/400/300?random=10",
                    "https://picsum.photos/400/300?random=11"
                ),
                likes = 432,
                comments = 123,
                shares = 56,
                hoursAgo = 30
            ),
            MockPostData(
                content = "今天学到了一个新知识点，分享给大家：坚持每天学习一点点，时间久了就会发现巨大的进步。",
                images = emptyList(),
                likes = 567,
                comments = 178,
                shares = 90,
                hoursAgo = 36
            )
        )

        mockData.forEachIndexed { index, data ->
            val timestamp = now - (data.hoursAgo * 3600 * 1000)
            val imagesJson = gson.toJson(data.images)
            val formattedTime = sdf.format(Date(timestamp))
            val views = (data.likes + data.comments) * 10

            posts.add(
                Post(
                    id = "mock_weibo_${index + 1}",
                    username = "微博用户",
                    avatar = "https://picsum.photos/100/100?random=${index + 100}",
                    timestamp = formattedTime,
                    source = "热门",
                    content = data.content,
                    likes = data.likes,
                    comments = data.comments,
                    shares = data.shares,
                    views = views,
                    isLiked = false,
                    createdAt = timestamp,
                    imagesJson = imagesJson
                )
            )
        }

        return posts
    }

    private data class MockPostData(
        val content: String,
        val images: List<String>,
        val likes: Int,
        val comments: Int,
        val shares: Int,
        val hoursAgo: Int
    )
}







