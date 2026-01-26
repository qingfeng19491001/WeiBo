package com.example.weibo.ui.picker

import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val tabs = listOf("全部", "图片", "视频")
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }

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

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        
        com.example.weibo.core.ui.components.StatusBarPlaceholder(
            backgroundColor = Color(0xFF222222),
            modifier = Modifier.zIndex(1000f)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
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

            
            val selectedTabIndex = pagerState.currentPage
            val tabWidths = remember {
                val tabWidthStateList = mutableStateListOf<Dp>()
                repeat(tabs.size) { tabWidthStateList.add(0.dp) }
                tabWidthStateList
            }
            val density = androidx.compose.ui.platform.LocalDensity.current

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF222222),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(
                                    x = tabPositions[selectedTabIndex].left +
                                        (tabPositions[selectedTabIndex].width - tabWidths[selectedTabIndex]) / 2
                                )
                                .width(tabWidths[selectedTabIndex]),
                            height = 2.dp,
                            color = Color(0xFFFF6600)
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTabIndex == index
                    Tab(
                        selected = selected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else Color(0xFF808080),
                                onTextLayout = { textLayoutResult ->
                                    tabWidths[index] = with(density) { textLayoutResult.size.width.toDp() }
                                }
                            )
                        }
                    )
                }
            }

            
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
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
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
    val context = LocalContext.current
    var videoThumb by remember(item.uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item.uri, item.isVideo) {
        if (item.isVideo) {
            videoThumb = withContext(Dispatchers.IO) {
                loadVideoThumbnail(context, item.uri)
            }
        } else {
            videoThumb = null
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        if (item.isVideo) {
            val bmp = videoThumb
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_play),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        } else {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        
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

        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f))
            )

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

private fun loadVideoThumbnail(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val sizePx = (context.resources.displayMetrics.density * 96).toInt().coerceAtLeast(96)
            context.contentResolver.loadThumbnail(uri, android.util.Size(sizePx, sizePx), null)
        } else {
            val id = tryResolveMediaStoreId(context, uri) ?: return null
            @Suppress("DEPRECATION")
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                id,
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
            )
        }
    } catch (_: Throwable) {
        null
    }
}

private fun tryResolveMediaStoreId(context: android.content.Context, uri: Uri): Long? {
    return try {
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)
            ?.use { c ->
                if (c.moveToFirst()) c.getLong(0) else null
            }
    } catch (_: Throwable) {
        null
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
    val images = queryImages(context)
    val videos = queryVideos(context)

    
    val merged = ArrayList<MediaItem>(images.size + videos.size)
    var i = 0
    var j = 0
    while (i < images.size || j < videos.size) {
        val img = images.getOrNull(i)
        val vid = videos.getOrNull(j)
        val takeImage = when {
            img == null -> false
            vid == null -> true
            else -> img.dateAddedSec >= vid.dateAddedSec
        }
        if (takeImage) {
            merged.add(img!!)
            i++
        } else {
            merged.add(vid!!)
            j++
        }
    }

    merged
}

private fun queryImages(context: android.content.Context): List<MediaItem> {
    val list = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val dateAddedSec = cursor.getLong(dateCol)
            val contentUri = ContentUris.withAppendedId(uri, id)
            list.add(MediaItem(contentUri, false, 0L, dateAddedSec))
        }
    }

    return list
}

private fun queryVideos(context: android.content.Context): List<MediaItem> {
    val list = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DURATION
    )
    val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val dateAddedSec = cursor.getLong(dateCol)
            val duration = try {
                cursor.getLong(durCol)
            } catch (_: Exception) {
                0L
            }
            val contentUri = ContentUris.withAppendedId(uri, id)
            list.add(MediaItem(contentUri, true, duration, dateAddedSec))
        }
    }

    return list
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
