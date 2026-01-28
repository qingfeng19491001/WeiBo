package com.example.weibo.ui.post

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weibo.R
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.systemBarsConfigForTopBar
import com.example.weibo.ui.picker.ImagePickerScreen
import com.example.weibo.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun WritePostScreen(
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    onSystemBarsConfigChange: (com.example.weibo.core.ui.components.SystemBarsConfig) -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val density = LocalDensity.current

    var content by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val writePostBarsConfig: SystemBarsConfig = remember {
        systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color.White),
            statusBarIconsFallbackColor = Color.White,
            statusBarColorFallbackColor = Color.White
        ).copy(statusBarDarkIcons = true)
    }

    val pickerBarsConfig: SystemBarsConfig = remember {
        systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color(0xFF222222)),
            statusBarIconsFallbackColor = Color(0xFF222222),
            statusBarColorFallbackColor = Color(0xFF222222)
        ).copy(statusBarDarkIcons = false, autoStatusBarIcons = false, autoStatusBarColor = true)
    }

    val isSendButtonEnabled = content.text.isNotBlank() || selectedImages.isNotEmpty()

    val userPrefs = remember {
        context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }
    var username by remember { mutableStateOf(userPrefs.getString("nickname", "用户名") ?: "用户名") }

    DisposableEffect(userPrefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == "nickname") {
                username = sharedPrefs.getString("nickname", "用户名") ?: "用户名"
            }
        }
        userPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            userPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val emojis = remember {
        listOf(
            "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊",
            "😋", "😎", "😍", "😘", "🥰", "😗", "😙", "😚", "🙂", "🤗",
            "🤔", "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐",
            "😯", "😪"
        )
    }
    val recentEmojis = remember { emojis.take(8) }

    val onImagesSelected: (List<Uri>) -> Unit = { uris ->
        if (uris.isNotEmpty()) {
            val remaining = 18 - selectedImages.size
            val toAdd = uris.take(remaining)
            selectedImages = selectedImages + toAdd
            if (uris.size > remaining) {
                android.widget.Toast.makeText(context, "最多只能选择18张图片", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        onSystemBarsConfigChange(writePostBarsConfig)
        showImagePicker = false
    }

    var keyboardHeight by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight > 0) {
            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val keypadHeight = view.height - rect.bottom
            keyboardHeight = (keypadHeight / density.density).toInt()
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    fun openImagePicker() {
        if (selectedImages.size >= 18) {
            android.widget.Toast.makeText(context, "最多只能选择18张图片", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        onSystemBarsConfigChange(pickerBarsConfig)
        showImagePicker = true
    }

    fun insertEmoji(emoji: String) {
        val text = content.text
        val selection = content.selection
        val start = max(selection.start, 0)
        val end = max(selection.end, 0)
        val newText = text.replaceRange(start, end, emoji)
        content = TextFieldValue(
            text = newText,
            selection = TextRange(start + emoji.length)
        )
    }

    val sendButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFB74D),
            Color(0xFFFFD180)
        )
    )

    val emojiPanelHeight = 220.dp

    val bottomBarModifier = Modifier
        .fillMaxWidth()
        .then(
            if (showEmojiPanel) {
                Modifier
            } else {
                Modifier.imePadding()
            }
        )

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    com.example.weibo.core.ui.components.StatusBarPlaceholder(backgroundColor = Color.White)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "取消",
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "发微博",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = username,
                                fontSize = 12.sp,
                                color = Color(0xFF999999)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    brush = if (isSendButtonEnabled) sendButtonGradient else Brush.linearGradient(
                                        colors = listOf(Color(0xFFFDBF8C), Color(0xFFFDBF8C))
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = isSendButtonEnabled) {
                                    if (isSendButtonEnabled) {
                                        val images = selectedImages.map { it.toString() }
                                        viewModel.publishPost(content.text, images)
                                        android.widget.Toast.makeText(context, "发布成功", android.widget.Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "发送",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSendButtonEnabled) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Divider(
                        color = Color(0xFFE5E5E5),
                        thickness = 0.5.dp
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                modifier = bottomBarModifier
            ) {
                Column {
                    Divider(
                        color = Color(0xFFE5E5E5),
                        thickness = 0.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_picture),
                            contentDescription = "图片",
                            tint = Color(0xFF666666),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { openImagePicker() }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.icon_mention),
                            contentDescription = "@",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "GIF",
                            color = Color(0xFF666666),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.icon_emotion),
                            contentDescription = "表情",
                            tint = Color(0xFF666666),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    if (showEmojiPanel) {
                                        showEmojiPanel = false
                                        keyboardController?.show()
                                    } else {
                                        keyboardController?.hide()
                                        showEmojiPanel = true
                                    }
                                }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.icon_add),
                            contentDescription = "更多",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (showEmojiPanel) {
                        EmojiPanel(
                            recentEmojis = recentEmojis,
                            allEmojis = emojis,
                            onEmojiClick = { emoji ->
                                insertEmoji(emoji)
                            },
                            modifier = Modifier.height(emojiPanelHeight)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val extraBottomPadding = if (showEmojiPanel) emojiPanelHeight else 0.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = extraBottomPadding)
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        "分享新鲜事...",
                        color = Color(0xFF999999),
                        fontSize = 18.sp
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    textColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                ),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                minLines = 5
            )

            if (selectedImages.isNotEmpty()) {
                ImageGrid(
                    images = selectedImages,
                    onRemove = { index ->
                        selectedImages = selectedImages.toMutableList().apply { removeAt(index) }
                    },
                    onAddMore = { openImagePicker() }
                )
            }
        }
    }

    if (showImagePicker) {
        ImagePickerScreen(
            alreadySelectedCount = selectedImages.size,
            maxSelectable = 18,
            onDismiss = {
                onSystemBarsConfigChange(writePostBarsConfig)
                showImagePicker = false
            },
            onImagesSelected = onImagesSelected
        )
        return
    }
}

/**
 * 图片网格 - 始终使用3×3布局
 * 完全复原原项目的UI细节和功能
 */
@Composable
private fun ImageGrid(
    images: List<Uri>,
    onRemove: (Int) -> Unit,
    onAddMore: () -> Unit
) {
    val maxDisplay = if (images.size <= 9) images.size else 9
    val showAddButton = images.size < 18

    val totalItems = if (showAddButton && maxDisplay < 9) {
        maxDisplay + 1
    } else {
        maxDisplay
    }

    val rows = (totalItems + 2) / 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val startIndex = rowIndex * 3
                val endIndex = minOf(startIndex + 3, totalItems)

                (startIndex until endIndex).forEach { position ->
                    if (position < maxDisplay) {
                        val uri = images[position]
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { onRemove(position) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "删除",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .padding(4.dp)
                                )
                            }

                            if (position == 8 && images.size > 9) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${images.size - 9}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (showAddButton && position == maxDisplay) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                    .clickable(onClick = onAddMore),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加",
                                    tint = Color.Gray
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

@Composable
private fun EmojiPanel(
    recentEmojis: List<String>,
    allEmojis: List<String>,
    onEmojiClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        Text(
            text = "常用表情",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(recentEmojis) { emoji ->
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onEmojiClick(emoji) }
                        .padding(8.dp)
                )
            }
        }

        Divider()

        Text(
            text = "全部表情",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(allEmojis.size) { index ->
                val emoji = allEmojis[index]
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onEmojiClick(emoji) }
                        .padding(4.dp)
                )
            }
        }
    }
}
