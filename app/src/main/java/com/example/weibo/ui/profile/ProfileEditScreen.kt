package com.example.weibo.ui.profile

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.weibo.core.ui.components.TopBarContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }
    
    var nickname by remember { mutableStateOf(prefs.getString("nickname", "用户名") ?: "用户名") }
    var intro by remember { mutableStateOf(prefs.getString("intro", "暂无简介") ?: "暂无简介") }
    var gender by remember { mutableStateOf(prefs.getString("gender", "男") ?: "男") }
    var birthday by remember { mutableStateOf(prefs.getString("birthday", "去填写") ?: "去填写") }
    var relationship by remember { mutableStateOf(prefs.getString("relationship", "去填写") ?: "去填写") }
    var hometown by remember { mutableStateOf(prefs.getString("hometown", "其他") ?: "其他") }
    
    
    fun saveProfile() {
        prefs.edit().apply {
            putString("nickname", nickname)
            putString("intro", intro)
            putString("gender", gender)
            putString("birthday", birthday)
            putString("relationship", relationship)
            putString("hometown", hometown)
            apply()
        }
    }
    
    TopBarContainer(
        topBarBackground = com.example.weibo.core.ui.components.TopBarBackground.Solid(Color.White),
        topBar = {
            
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        saveProfile()
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        Toast.makeText(context, "菜单功能", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "菜单",
                            tint = Color.Black
                        )
                    }
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
                
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
    ) {
        Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        
                        
                    }
                    
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    Toast.makeText(context, "头像挂件功能", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_crown),
                                contentDescription = null,
                                tint = Color(0xFFFF8C00),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "头像挂件",
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    Toast.makeText(context, "更换头像功能", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "更换头像",
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            
            ProfileEditSection {
                
                var showNicknameDialog by remember { mutableStateOf(false) }
                var nicknameInput by remember { mutableStateOf(nickname) }
                
                
                ProfileEditItem(
                    label = "昵称",
                    value = nickname,
                    showEditIcon = true,
                    onClick = {
                        nicknameInput = nickname
                        showNicknameDialog = true
                    }
                )
                
                if (showNicknameDialog) {
                    AlertDialog(
                        onDismissRequest = { showNicknameDialog = false },
                        title = { Text("修改昵称") },
                        text = {
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                placeholder = { Text("请输入昵称（4-30个字符）") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val trimmed = nicknameInput.trim()
                                    if (trimmed.length in 4..30) {
                                        nickname = trimmed
                                        saveProfile()
                                        showNicknameDialog = false
                                    } else {
                                        Toast.makeText(context, "昵称长度需在4-30个字符", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("提交")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNicknameDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                ProfileEditItem(
                    label = "",
                    value = "加入微博认证",
                    customLabel = {
                        Surface(
                            color = Color(0xFFFF8C00),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "微博认证",
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    },
                    onClick = {
                        Toast.makeText(context, "微博认证功能", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                var showIntroDialog by remember { mutableStateOf(false) }
                var introInput by remember { mutableStateOf(intro) }
                
                
                ProfileEditItem(
                    label = "简介",
                    value = intro,
                    onClick = {
                        introInput = intro
                        showIntroDialog = true
                    }
                )
                
                if (showIntroDialog) {
                    AlertDialog(
                        onDismissRequest = { showIntroDialog = false },
                        title = { Text("修改简介") },
                        text = {
                            OutlinedTextField(
                                value = introInput,
                                onValueChange = { introInput = it },
                                placeholder = { Text("请输入简介") },
                                maxLines = 3
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    intro = introInput.trim()
                                    saveProfile()
                                    showIntroDialog = false
                                }
                            ) {
                                Text("提交")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showIntroDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
            
            
            ProfileEditSection {
                
                var showGenderDialog by remember { mutableStateOf(false) }
                
                
                ProfileEditItem(
                    label = "性别",
                    value = gender,
                    onClick = {
                        showGenderDialog = true
                    }
                )
                
                if (showGenderDialog) {
                    AlertDialog(
                        onDismissRequest = { showGenderDialog = false },
                        title = { Text("选择性别") },
                        text = {
                            Column {
                                TextButton(
                                    onClick = {
                                        gender = "男"
                                        saveProfile()
                                        showGenderDialog = false
                                    }
                                ) {
                                    Text("男")
                                }
                                TextButton(
                                    onClick = {
                                        gender = "女"
                                        saveProfile()
                                        showGenderDialog = false
                                    }
                                ) {
                                    Text("女")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showGenderDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                ProfileEditItem(
                    label = "生日",
                    value = birthday,
                    onClick = {
                        val calendar = Calendar.getInstance()
                        if (birthday != "去填写" && birthday.isNotEmpty()) {
                            try {
                                val parts = birthday.split("-")
                                if (parts.size == 3) {
                                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                }
                            } catch (e: Exception) {
                                
                            }
                        }
                        
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                birthday = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", dayOfMonth)}"
                                saveProfile()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                ProfileEditItem(
                    label = "感情状况",
                    value = relationship,
                    onClick = {
                        Toast.makeText(context, "修改感情状况功能", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                var showHometownDialog by remember { mutableStateOf(false) }
                val hometownOptions = listOf(
                    "中国大陆", "北京", "上海", "广东", "江苏", "浙江",
                    "湖北", "台湾", "香港", "澳门", "其他", "不限", "海外"
                )
                
                
                ProfileEditItem(
                    label = "家乡",
                    value = hometown,
                    onClick = {
                        showHometownDialog = true
                    }
                )
                
                if (showHometownDialog) {
                    AlertDialog(
                        onDismissRequest = { showHometownDialog = false },
                        title = { Text("选择家乡") },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                hometownOptions.forEach { option ->
                                    TextButton(
                                        onClick = {
                                            hometown = option
                                            saveProfile()
                                            showHometownDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(option)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showHometownDialog = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
            
            
            ProfileEditSection {
                
                ProfileEditItem(
                    label = "教育信息",
                    value = "添加",
                    valueColor = Color(0xFF2196F3),
                    showArrow = false,
                    onClick = {
                        Toast.makeText(context, "添加教育信息功能", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                ProfileEditItem(
                    label = "工作信息",
                    value = "添加",
                    valueColor = Color(0xFF2196F3),
                    showArrow = false,
                    onClick = {
                        Toast.makeText(context, "添加工作信息功能", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            
            ProfileEditSection {
                
                ProfileEditItem(
                    label = "注册时间",
                    value = "2022-06-05",
                    showArrow = false,
                    onClick = {}
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 16.dp)
                )
                
                
                ProfileEditItem(
                    label = "阳光信用",
                    value = "509",
                    onClick = {
                        Toast.makeText(context, "阳光信用功能", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    })
}

@Composable
private fun ProfileEditSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileEditItem(
    label: String,
    value: String,
    showEditIcon: Boolean = false,
    showArrow: Boolean = true,
    valueColor: Color = Color(0xFF999999),
    customLabel: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (customLabel != null) {
            customLabel()
        } else {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black
            )
            if (showEditIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = null,
                    tint = Color(0xFF999999),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor
        )
        if (showArrow) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color(0xFF999999),
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}

