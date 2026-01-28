package com.example.weibo.ui.livestream

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weibo.R
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.TopBarContainer
import com.example.weibo.core.ui.components.systemBarsConfigForTopBar
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LiveStreamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val topBg = Color(0xFF1C1C1C)

            SetupSystemBars(
                config = systemBarsConfigForTopBar(
                    topBarBackground = TopBarBackground.Solid(topBg)
                ).copy(
                    statusBarColor = topBg,
                    statusBarDarkIcons = false,
                    autoStatusBarIcons = false,
                    autoStatusBarColor = false
                )
            )

            MaterialTheme {
                LiveStreamScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

data class GiftBannerEvent(
    val id: Int,
    val avatarResId: Int,
    val nickname: String,
    val giftName: String,
    val giftIconName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val liveStreamInfo by viewModel.liveStreamInfo.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()

    var commentInput by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showGiftSheet by remember { mutableStateOf(false) }

    val giftSvgaQueue = remember { mutableStateListOf<String>() }
    var currentGiftSvga by remember { mutableStateOf<String?>(null) }
    var currentGiftToken by remember { mutableStateOf(0) }

    val giftBanners = remember { mutableStateListOf<GiftBannerEvent>() }
    var giftBannerToken by remember { mutableStateOf(0) }

    val prefs = remember {
        context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }
    val myNickname by remember {
        mutableStateOf(prefs.getString("nickname", "用户名") ?: "用户名")
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val avatarSeed = remember { System.currentTimeMillis() }
    val avatarModel = remember(liveStreamInfo.hostAvatarUrl, avatarSeed) {
        "${liveStreamInfo.hostAvatarUrl}?t=$avatarSeed"
    }

    val sendButtonGradient = remember {
        Brush.linearGradient(colors = listOf(Color(0xFFFF8615), Color(0xFFFF8200)))
    }

    val commentsListState = rememberLazyListState()

    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            commentsListState.animateScrollToItem(comments.size - 1)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(isEditing) {
        scope.launch {
            if (isEditing) {
                sheetState.show()
            } else {
                sheetState.hide()
            }
        }
    }

    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(currentGiftSvga, giftSvgaQueue.size) {
        if (currentGiftSvga == null && giftSvgaQueue.isNotEmpty()) {
            currentGiftSvga = giftSvgaQueue.removeAt(0)
            currentGiftToken += 1
        }
    }

    var likeToken by remember { mutableIntStateOf(0) }
    var likeButtonCenterInRoot by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
    ) {
        TopBarContainer(
            topBarBackground = TopBarBackground.Solid(Color(0xFF1C1C1C)),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1C))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = avatarModel,
                        contentDescription = "主播头像",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = com.example.weibo.R.drawable.avatar_placeholder),
                        error = painterResource(id = com.example.weibo.R.drawable.avatar_placeholder)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = liveStreamInfo.hostName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(text = liveStreamInfo.viewerCount, fontSize = 14.sp, color = Color(0xFFCCCCCC))
                    }

                    Button(
                        onClick = {
                            viewModel.onFollowClicked()
                            Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8615))
                    ) {
                        Text(text = "关注", fontSize = 18.sp, color = Color.White)
                    }
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1C1C1C))
                ) {
                    AsyncImage(
                        model = liveStreamInfo.coverUrl,
                        contentDescription = "直播封面",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        state = commentsListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        items(comments) { comment ->
                            CommentItem(comment = comment)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isEditing = true
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2C2C2C), RectangleShape)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "一起聊聊吧",
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF999999),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                painter = painterResource(id = com.example.weibo.R.drawable.ic_live_share),
                                contentDescription = "分享",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = { showGiftSheet = true }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                painter = painterResource(id = com.example.weibo.R.drawable.ic_live_gift),
                                contentDescription = "礼物",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { likeToken += 1 },
                            modifier = Modifier
                                .size(44.dp)
                                .onGloballyPositioned { coords ->
                                    likeButtonCenterInRoot = coords.localToRoot(
                                        androidx.compose.ui.geometry.Offset(
                                            coords.size.width / 2f,
                                            coords.size.height / 2f
                                        )
                                    )
                                }
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.weibo.R.drawable.ic_live_like),
                                contentDescription = "点赞",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                    }
                }
            }
        )

        currentGiftSvga?.let { assetName ->
            SvgaOverlay(
                assetName = assetName,
                playToken = currentGiftToken,
                modifier = Modifier.fillMaxSize(),
                onFinished = { currentGiftSvga = null }
            )
        }

        GiftBannerOverlay(giftBanners = giftBanners, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 150.dp))

        LiveLikeBurstOverlay(
            playToken = likeToken,
            origin = likeButtonCenterInRoot,
            modifier = Modifier.matchParentSize(),
            resIds = listOf(
                R.drawable.ic_live_like_dead,
                R.drawable.ic_live_like_goofy,
                R.drawable.ic_live_like_happy,
                R.drawable.ic_live_like_love,
                R.drawable.ic_live_like_winking
            ),
            durationMs = 1200,
            baseSizePx = 96f
        )

        if (isEditing) {
            ModalBottomSheet(
                onDismissRequest = { isEditing = false },
                sheetState = sheetState,
                containerColor = Color.White,
                scrimColor = Color.Transparent,
                dragHandle = null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(8f)
                            .height(48.dp)
                            .background(Color(0xFFF2F2F2), RectangleShape)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = Color.Black
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF8200)),
                            decorationBox = { innerTextField ->
                                if (commentInput.isBlank()) {
                                    Text(
                                        text = "友善评论，文明发言~",
                                        fontSize = 14.sp,
                                        lineHeight = 18.sp,
                                        color = Color(0xFF999999),
                                        maxLines = 1
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val canSend = commentInput.isNotBlank()

                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .background(
                                brush = if (canSend) sendButtonGradient else Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDBF8C), Color(0xFFFDBF8C))
                                ),
                                shape = RectangleShape
                            )
                            .clickable(enabled = canSend) {
                                if (canSend) {
                                    viewModel.sendComment(commentInput)
                                    commentInput = ""
                                    isEditing = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "发送",
                            fontSize = 16.sp,
                            color = if (canSend) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        if (showGiftSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGiftSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF2A2A2A),
                scrimColor = Color.Transparent,
                dragHandle = null
            ) {
                LiveGiftSheet(
                    onSend = { gift ->
                        giftBanners.add(
                            GiftBannerEvent(
                                id = giftBannerToken + 1,
                                avatarResId = R.drawable.touxiang,
                                nickname = myNickname,
                                giftName = gift.name,
                                giftIconName = gift.iconName
                            )
                        )
                        giftBannerToken += 1

                        giftSvgaQueue.add(gift.svgaAsset)
                        showGiftSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Text(
        text = "${comment.username}：${comment.content}",
        fontSize = 14.sp,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

data class Comment(
    val id: Int,
    val username: String,
    val content: String,
    val level: Int
)

data class LiveGift(
    val id: Int,
    val name: String,
    val price: Int,
    val iconName: String,
    val svgaAsset: String
)

@Composable
private fun LiveGiftSheet(
    onSend: (LiveGift) -> Unit
) {
    val context = LocalContext.current
    val gifts = remember {
        listOf(
            LiveGift(21, "财神到", 1, "g21", "ga21.svga"),
            LiveGift(22, "马车", 2, "g22", "ga22.svga"),
            LiveGift(23, "跑车", 52, "g23", "ga23.svga"),
            LiveGift(24, "香槟", 52, "g24", "ga24.svga"),
            LiveGift(25, "浪漫气球", 99, "g25", "ga25.svga"),
            LiveGift(26, "旋转木马", 2, "g26", "ga26.svga"),
            LiveGift(27, "金屋跑车", 1, "g27", "ga27.svga"),
            LiveGift(28, "直升机", 88, "g28", "ga28.svga"),
            LiveGift(29, "独角兽", 6, "g29", "ga29.svga"),
            LiveGift(30, "城堡", 6, "g30", "ga30.svga"),
            LiveGift(31, "烟花轮船", 6, "g31", "ga31.svga"),
            LiveGift(32, "火箭", 6, "g32", "ga32.svga")
        )
    }

    var selectedIndex by remember { mutableStateOf(0) }
    val selectedGift = gifts.getOrNull(selectedIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "礼物",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp, max = 360.dp)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gifts.size) { index ->
                val gift = gifts[index]
                val isSelected = index == selectedIndex

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0x33FF9D2F) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedIndex = index
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scale = if (isSelected) {
                        val transition = rememberInfiniteTransition(label = "gift_scale")
                        transition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.12f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "gift_scale_anim"
                        ).value
                    } else {
                        1f
                    }

                    val resId = context.resources.getIdentifier(gift.iconName, "drawable", context.packageName)

                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = gift.name,
                        modifier = Modifier
                            .size(52.dp)
                            .scale(scale)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = gift.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${gift.price} 微币",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            val canSend = selectedGift != null
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .width(120.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFB67A2A))
                    .clickable(enabled = canSend) {
                        selectedGift?.let(onSend)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "发送",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SvgaOverlay(
    assetName: String,
    playToken: Int,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var videoEntity by remember(assetName, playToken) { mutableStateOf<SVGAVideoEntity?>(null) }

    LaunchedEffect(assetName, playToken) {
        SVGAParser(context).decodeFromAssets(assetName, object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                videoEntity = videoItem
            }

            override fun onError() {
                videoEntity = null
                onFinished()
            }
        })
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SVGAImageView(ctx).apply {
                loops = 1
                clearsAfterStop = true
                isClickable = false
            }
        },
        update = { view ->
            val entity = videoEntity ?: return@AndroidView
            view.setVideoItem(entity)
            view.callback = object : SVGACallback {
                override fun onFinished() {
                    onFinished()
                }

                override fun onPause() {}
                override fun onRepeat() {}
                override fun onStep(frame: Int, percentage: Double) {}
            }
            view.startAnimation()
        }
    )
}

@Composable
private fun GiftBannerOverlay(
    giftBanners: List<GiftBannerEvent>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        giftBanners.forEach { banner ->
            key(banner.id) {
                GiftBannerItem(banner = banner)
            }
        }
    }
}

@Composable
private fun GiftBannerItem(banner: GiftBannerEvent) {
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
        delay(3000) // Stay for 3 seconds
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF6A3500), Color(0xFF3B1D00))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = banner.avatarResId),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = banner.nickname,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "送出 ${banner.giftName}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val giftIconResId = context.resources.getIdentifier(banner.giftIconName, "drawable", context.packageName)
            if (giftIconResId != 0) {
                Image(
                    painter = painterResource(id = giftIconResId),
                    contentDescription = banner.giftName,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
