package com.example.weibo.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import coil.compose.AsyncImage
import com.example.weibo.R
import com.example.weibo.data.model.Post
import com.example.weibo.util.TimeUtils
import com.example.weibo.ui.refresh.ClassicsSwipeRefresh
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostList(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<Post>,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onDeleteClick: ((String) -> Unit)? = null,
    onImageClick: (List<String>, Int) -> Unit,
    showDeleteButton: Boolean = false,
    @Suppress("UNUSED_PARAMETER") showUpdateBar: Boolean = false,
    onRefresh: () -> Unit
) {
    
    val isRefreshing = pagingItems.loadState.refresh is androidx.paging.LoadState.Loading

    ClassicsSwipeRefresh(
        isRefreshing = isRefreshing,
        onRefresh = {
            
            onRefresh()
            pagingItems.refresh()
        },
        modifier = modifier.fillMaxSize(),
        enabled = true,
        headerHeight = 56,
        backgroundColor = Color.White,
        contentColor = Color(0xFFFF6600),
        textColor = Color(0xFF666666),
        showShadow = false
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(vertical = 0.dp)
        ) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems[index]?.id ?: index.toString() }
            ) { index ->
                val post = pagingItems[index]
                post?.let {
                    PostItem(
                        post = it,
                        onLikeClick = { onLikeClick(it.id) },
                        onCommentClick = { onCommentClick(it.id) },
                        onShareClick = { onShareClick(it.id) },
                        onDeleteClick = onDeleteClick?.let { delete -> { delete(it.id) } },
                        onImageClick = { imageIndex -> onImageClick(it.getImages(), imageIndex) },
                        showDeleteButton = showDeleteButton
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

    }
}

@Composable
private fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    onImageClick: (Int) -> Unit,
    showDeleteButton: Boolean
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableIntStateOf(post.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 10.dp)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.avatar.ifEmpty { R.drawable.img_avater1 },
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.avatar_placeholder)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.timestamp,
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "来自 ${post.source}",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                if (showDeleteButton && onDeleteClick != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                            .clickable(onClick = onDeleteClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF333333),
                modifier = Modifier.fillMaxWidth()
            )

            val images = post.getImages()
            if (images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                PostImageGrid(
                    images = images,
                    onImageClick = onImageClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = Color(0xFFEEEEEE),
                thickness = 0.6.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    ActionButton(
                        icon = R.drawable.timeline_icon_redirect,
                        count = post.shares,
                        onClick = onShareClick,
                        tint = Color(0xFF666666)
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ActionButton(
                        icon = R.drawable.timeline_icon_comment,
                        count = post.comments,
                        onClick = onCommentClick,
                        tint = Color(0xFF666666)
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    ActionButton(
                        icon = if (isLiked) R.drawable.timeline_icon_like else R.drawable.timeline_icon_unlike,
                        count = likeCount,
                        onClick = {
                            isLiked = !isLiked
                            likeCount = if (isLiked) likeCount + 1 else maxOf(0, likeCount - 1)
                            onLikeClick()
                        },
                        tint = if (isLiked) Color(0xFFFF6B6B) else Color(0xFF666666)
                    )
                }
            }
        }
    }
}


@Composable
private fun PostImageGrid(
    images: List<String>,
    onImageClick: (Int) -> Unit
) {
    if (images.isEmpty()) {
        return
    }

    if (images.size == 1) {
        AsyncImage(
            model = images[0],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RectangleShape)
                .clickable { onImageClick(0) },
            contentScale = ContentScale.Crop
        )
    } else {
        val maxDisplayCount = minOf(images.size, 9)
        val rows = (maxDisplayCount + 2) / 3

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(rows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val startIndex = rowIndex * 3
                    val endIndex = minOf(startIndex + 3, maxDisplayCount)

                    (startIndex until endIndex).forEach { index ->
                        val url = images[index]

                        Box(modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RectangleShape)
                                    .clickable { onImageClick(index) },
                                contentScale = ContentScale.Crop
                            )

                            if (index == 8 && images.size > 9) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clip(RectangleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${images.size - 9}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    repeat(3 - (endIndex - startIndex)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
private fun ActionButton(
    icon: Int,
    count: Int,
    onClick: () -> Unit,
    tint: Color = Color(0xFF666666)
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = TimeUtils.formatCount(count.toLong()),
            fontSize = 13.sp,
            color = tint
        )
    }
}
