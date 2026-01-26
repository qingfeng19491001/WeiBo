package com.example.weibo.ui.livestream

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage


class LiveStreamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        
        enableEdgeToEdge()
        
        setContent {
            MaterialTheme {
                LiveStreamScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}


@Composable
fun LiveStreamScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val liveStreamInfo by viewModel.liveStreamInfo.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    var commentInput by remember { mutableStateOf("") }
    
    val commentsListState = rememberLazyListState()
    
    
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            commentsListState.animateScrollToItem(comments.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C)) 
    ) {
        
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF333333)) 
        ) {
            
            Text(
                text = "视频播放器",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x80000000)) 
                .padding(8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            
            AsyncImage(
                model = liveStreamInfo.hostAvatarUrl,
                contentDescription = "主播头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                
                Text(
                    text = liveStreamInfo.hostName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                
                Text(
                    text = liveStreamInfo.viewerCount,
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC) 
                )
            }
            
            
            
            Button(
                onClick = {
                    viewModel.onFollowClicked()
                    Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8615) 
                )
            ) {
                Text(
                    text = "关注",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
        
        
        
        LazyColumn(
            state = commentsListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
                .align(Alignment.BottomStart)
                .offset(y = (-60).dp), 
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            
            OutlinedTextField(
                value = commentInput,
                onValueChange = { commentInput = it },
                placeholder = {
                    Text(
                        text = "一起聊聊吧",
                        fontSize = 20.sp,
                        color = Color(0xFF999999) 
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,
                    color = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF444444), 
                    unfocusedContainerColor = Color(0xFF444444) 
                ),
                shape = RoundedCornerShape(20.dp), 
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            
            
            Button(
                onClick = {
                    if (commentInput.isNotBlank()) {
                        viewModel.sendComment(commentInput)
                        commentInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8615) 
                )
            ) {
                Text(
                    text = "发送",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
private fun CommentItem(
    comment: Comment
) {
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
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            
            Text(
                text = "刚刚",
                fontSize = 12.sp,
                color = Color(0xFF666666), 
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        
        Text(
            text = comment.content,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}


data class Comment(
    val id: Int,
    val username: String, 
    val content: String,
    val level: Int 
)

