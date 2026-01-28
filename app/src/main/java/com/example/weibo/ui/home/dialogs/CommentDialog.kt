package com.example.weibo.ui.home.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CommentDialog(
    postId: String,
    onDismiss: () -> Unit,
    onCommentSent: (String) -> Unit
) {
    val context = LocalContext.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp

    var commentText by remember { mutableStateOf(TextFieldValue("")) }
    val comments = remember { mutableStateListOf<CommentItem>() }

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
                .heightIn(max = (screenHeightDp.dp * 0.55f))
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "评论",
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

            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(max = 400.dp)
                    .fillMaxWidth()
            ) {
                items(comments) { comment ->
                    CommentItemView(comment = comment)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 40.dp),
                    placeholder = { Text("写评论...") },
                    singleLine = false,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                val sendButtonGradient = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFB74D),
                        Color(0xFFFFD180)
                    )
                )

                val isSendEnabled = commentText.text.isNotBlank()

                Box(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .defaultMinSize(minWidth = 60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = if (isSendEnabled) sendButtonGradient else Brush.linearGradient(
                                colors = listOf(Color(0xFFFDBF8C), Color(0xFFFDBF8C))
                            )
                        )
                ) {
                    TextButton(
                        onClick = {
                            if (isSendEnabled) {
                                val newComment = CommentItem(
                                    id = System.currentTimeMillis().toString(),
                                    username = "我",
                                    content = commentText.text,
                                    time = "刚刚"
                                )
                                comments.add(0, newComment)
                                onCommentSent(commentText.text)
                                commentText = TextFieldValue("")

                                Toast.makeText(
                                    context,
                                    "评论成功",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = isSendEnabled,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text("发送")
                    }
                }
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun CommentItemView(comment: CommentItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = comment.username,
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = comment.time,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = comment.content,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

data class CommentItem(
    val id: String,
    val username: String,
    val content: String,
    val time: String
)






