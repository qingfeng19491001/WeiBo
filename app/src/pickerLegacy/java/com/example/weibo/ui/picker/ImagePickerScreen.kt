package com.example.weibo.ui.picker

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * 图片选择Screen - Compose版本
 * 完全复原backup模块的ImagePickerActivity UI细节和功能
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImagePickerScreen(
    alreadySelectedCount: Int = 0,
    maxSelectable: Int = 18,
    onDismiss: () -> Unit,
    onImagesSelected: (List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var allMedia by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var selectedItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val actualMaxSelectable = maxSelectable - alreadySelectedCount
    val pagerState = rememberPagerState(initialPage = 0) { 3 }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadMedia(context) { mediaList ->
                allMedia = mediaList
                isLoading = false
            }
        } else {
            android.widget.Toast.makeText(
                context,
                "需要存储和相机权限才能选择媒体文件",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            onDismiss()
        }
    }
    
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempCameraUri
        if (success && uri != null) {
            val newItem = MediaItem(uri, false, 0L)
            selectedItems = selectedItems + newItem
            onImagesSelected(selectedItems.map { it.uri })
            onDismiss()
        }
    }
    
    LaunchedEffect(Unit) {
        checkAndRequestPermission(context, permissionLauncher) {
            loadMedia(context) { mediaList ->
                allMedia = mediaList
                isLoading = false
            }
        }
    }
    
    val images = remember(allMedia) { allMedia.filter { !it.isVideo } }
    val videos = remember(allMedia) { allMedia.filter { it.isVideo } }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部工具栏（完全复原原项目的UI）
                TopAppBar(
                    title = {
                        Text(
                            text = "图片和视频",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF222222)
                    )
                )
                
                // Tab栏（完全复原原项目的UI）
                val selectedTabIndex = pagerState.currentPage
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF222222),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFFFF6600) // 橙色指示器
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("全部", color = if (selectedTabIndex == 0) Color.White else Color(0xFF808080)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("图片", color = if (selectedTabIndex == 1) Color.White else Color(0xFF808080)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        text = { Text("视频", color = if (selectedTabIndex == 2) Color.White else Color(0xFF808080)) }
                    )
                }
                
                // ViewPager内容
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> MediaGridContent(
                            media = allMedia,
                            selectedItems = selectedItems,
                            maxSelectable = actualMaxSelectable,
                            onItemClick = { item ->
                                toggleSelection(
                                    item,
                                    selectedItems,
                                    actualMaxSelectable,
                                    context
                                ) { newSelected ->
                                    selectedItems = newSelected
                                }
                            },
                            onCameraClick = {
                                openCamera(context, takePictureLauncher) { uri ->
                                    tempCameraUri = uri
                                }
                            },
                            isLoading = isLoading
                        )
                        1 -> MediaGridContent(
                            media = images,
                            selectedItems = selectedItems,
                            maxSelectable = actualMaxSelectable,
                            onItemClick = { item ->
                                toggleSelection(
                                    item,
                                    selectedItems,
                                    actualMaxSelectable,
                                    context
                                ) { newSelected ->
                                    selectedItems = newSelected
                                }
                            },
                            onCameraClick = {
                                openCamera(context, takePictureLauncher) { uri ->
                                    tempCameraUri = uri
                                }
                            },
                            isLoading = isLoading
                        )
                        2 -> MediaGridContent(
                            media = videos,
                            selectedItems = selectedItems,
                            maxSelectable = actualMaxSelectable,
                            onItemClick = { item ->
                                toggleSelection(
                                    item,
                                    selectedItems,
                                    actualMaxSelectable,
                                    context
                                ) { newSelected ->
                                    selectedItems = newSelected
                                }
                            },
                            onCameraClick = null,
                            isLoading = isLoading
                        )
                    }
                }
                
                // 底部栏（完全复原原项目的UI）
                Surface(
                    color = Color(0xFF222222),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onImagesSelected(selectedItems.map { it.uri })
                                onDismiss()
                            },
                            enabled = selectedItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6600),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF666666),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                        ) {
                            Text(
                                text = if (selectedItems.isNotEmpty()) "完成 (${selectedItems.size})" else "完成",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGridContent(
    media: List<MediaItem>,
    selectedItems: List<MediaItem>,
    maxSelectable: Int,
    onItemClick: (MediaItem) -> Unit,
    onCameraClick: (() -> Unit)?,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // 相机按钮（仅在图片Tab显示）
            if (onCameraClick != null) {
                item {
                    CameraItem(onClick = onCameraClick)
                }
            }
            
            itemsIndexed(media) { _, item ->
                MediaGridItem(
                    item = item,
                    isSelected = selectedItems.contains(item),
                    selectionOrder = selectedItems.indexOf(item) + 1,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun CameraItem(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "拍照",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MediaGridItem(
    item: MediaItem,
    isSelected: Boolean,
    selectionOrder: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // 缩略图
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 视频时长（如果是视频）
        if (item.isVideo && item.duration > 0) {
            Text(
                text = formatDuration(item.duration),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0x80000000))
                    .padding(4.dp)
            )
        }
        
        // 选中遮罩
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f))
            )
            
            // 选中序号指示器（完全复原原项目的UI）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(Color(0xFFFF6600), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectionOrder.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun toggleSelection(
    item: MediaItem,
    currentSelected: List<MediaItem>,
    maxSelectable: Int,
    context: android.content.Context,
    onSelectionChanged: (List<MediaItem>) -> Unit
) {
    val newSelected = if (currentSelected.contains(item)) {
        currentSelected - item
    } else {
        if (currentSelected.size >= maxSelectable) {
            android.widget.Toast.makeText(
                context,
                "最多只能选择 $maxSelectable 项",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        currentSelected + item
    }
    onSelectionChanged(newSelected)
}

private fun checkAndRequestPermission(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onPermissionGranted: () -> Unit
) {
    val permissionsToRequest = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_VIDEO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO)
        }
    } else {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        permissionsToRequest.add(android.Manifest.permission.CAMERA)
    }

    if (permissionsToRequest.isNotEmpty()) {
        launcher.launch(permissionsToRequest.toTypedArray())
    } else {
        onPermissionGranted()
    }
}

private fun loadMedia(
    context: android.content.Context,
    onMediaLoaded: (List<MediaItem>) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        val mediaList = queryMedia(context)
        kotlinx.coroutines.withContext(Dispatchers.Main) {
            onMediaLoaded(mediaList)
        }
    }
}

private suspend fun queryMedia(context: android.content.Context): List<MediaItem> = withContext(Dispatchers.IO) {
    val mediaList = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Video.Media.DURATION
    )
    val selectionArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )
    val queryUri = MediaStore.Files.getContentUri("external")

    context.contentResolver.query(
        queryUri,
        projection,
        "media_type IN (?,?)",
        selectionArgs,
        "date_added DESC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val mediaType = cursor.getInt(mediaTypeColumn)
            val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

            val duration = if (isVideo) {
                try {
                    cursor.getLong(durationColumn)
                } catch (_: Exception) {
                    0L
                }
            } else {
                0L
            }

            val contentUri = ContentUris.withAppendedId(queryUri, id)
            mediaList.add(MediaItem(contentUri, isVideo, duration))
        }
    }

    mediaList
}

private fun openCamera(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onUriCreated: (Uri) -> Unit
) {
    try {
        val photoFile = File(context.filesDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
        onUriCreated(uri)
        launcher.launch(uri)
    } catch (e: IOException) {
        android.widget.Toast.makeText(
            context,
            "无法打开相机: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val s = seconds % 60
    val m = (seconds / 60) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

