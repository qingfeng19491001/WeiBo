package com.example.weibo.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import com.example.weibo.core.ui.components.TopBarContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }

    TopBarContainer(
        topBar = {
            
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "设置",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        content = {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
            ) {
                
                
                SettingsItem(
                    title = "帐号管理",
                    onClick = {
                        Toast.makeText(context, "帐号管理", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsItem(
                    title = "通知",
                    onClick = {
                        Toast.makeText(context, "通知", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsItem(
                    title = "隐私与安全",
                    onClick = {
                        Toast.makeText(context, "隐私与安全", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsItem(
                    title = "通用设置",
                    onClick = {
                        Toast.makeText(context, "通用设置", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsItem(
                    title = "意见反馈",
                    onClick = {
                        Toast.makeText(context, "意见反馈", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsItem(
                    title = "关于微博",
                    onClick = {
                        Toast.makeText(context, "关于微博", Toast.LENGTH_SHORT).show()
                    }
                )

                
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "夜间模式",
                        fontSize = 16.sp,
                        color = Color(0xFF333333), 
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { isChecked ->
                            darkModeEnabled = isChecked
                            Toast.makeText(
                                context,
                                if (isChecked) "已开启夜间模式" else "已关闭夜间模式",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                
                SettingsItem(
                    title = "清理缓存",
                    onClick = {
                        Toast.makeText(context, "清理缓存", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                
                Text(
                    text = "注销并退出微博",
                    fontSize = 16.sp,
                    color = Color(0xFFE53935), 
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White)
                        .clickable {
                            Toast.makeText(context, "注销并退出微博", Toast.LENGTH_SHORT).show()
                        }
                        .wrapContentHeight(Alignment.CenterVertically),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "《隐私政策》",
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2), 
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "隐私政策", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp)
                    )
                    Text(
                        text = "《个人信息收集清单》",
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "个人信息收集清单", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp)
                    )
                    Text(
                        text = "《第三方合作清单》",
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier
                            .clickable {
                                Toast.makeText(context, "第三方合作清单", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    )
}


@Composable
private fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color(0xFF333333), 
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF999999)
        )
    }
}
