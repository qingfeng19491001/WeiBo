package com.example.weibo.ui.home.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ShareDialog(
    postId: String,
    postContent: String,
    onDismiss: () -> Unit,
    onShareAction: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissSheet() {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分享",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { dismissSheet() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭"
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.6.dp)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ShareOption(
                    text = "分享到微博",
                    onClick = {
                        onShareAction("weibo")
                        android.widget.Toast.makeText(context, "已分享到微博", android.widget.Toast.LENGTH_SHORT).show()
                        dismissSheet()
                    }
                )
                ShareOption(
                    text = "复制链接",
                    onClick = {
                        copyToClipboard(context, postContent)
                        onShareAction("copy")
                        dismissSheet()
                    }
                )
                ShareOption(
                    text = "收藏",
                    onClick = {
                        onShareAction("favorite")
                        android.widget.Toast.makeText(context, "已收藏", android.widget.Toast.LENGTH_SHORT).show()
                        dismissSheet()
                    }
                )
                ShareOption(
                    text = "更多",
                    onClick = {
                        shareViaSystem(context, postContent)
                        onShareAction("system")
                        dismissSheet()
                    }
                )
            }

            OutlinedButton(
                onClick = { dismissSheet() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("取消")
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun ShareOption(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Divider()
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("post", text))
    android.widget.Toast.makeText(context, "已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show()
}

private fun shareViaSystem(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享至"))
}









