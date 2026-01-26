package com.example.weibo.ui.login

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri


class PasswordLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                PasswordLoginScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}


@Composable
fun PasswordLoginScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit
) {
    val context = LocalContext.current
    var phoneOrMail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAgreementChecked by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) 
            .padding(24.dp)
    ) {
        
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, bottom = 60.dp)
        ) {
            
            Text(
                text = "账号密码登录",
                fontSize = 28.sp,
                color = Color(0xFF333333) 
            )
        }
        
        
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            
            
            OutlinedTextField(
                value = phoneOrMail,
                onValueChange = { if (it.length <= 50) phoneOrMail = it },
                placeholder = {
                    Text(
                        text = "手机号或邮箱",
                        fontSize = 16.sp,
                        color = Color(0xFFCCCCCC) 
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF333333) 
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
        }
        
        
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE0E0E0)) 
                .padding(bottom = 20.dp)
        )
        
        
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "密码",
                        fontSize = 16.sp,
                        color = Color(0xFFCCCCCC) 
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF333333) 
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF333333),
                    unfocusedTextColor = Color(0xFF333333),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
        }
        
        
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE0E0E0)) 
                .padding(bottom = 30.dp)
        )
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = isAgreementChecked,
                onCheckedChange = { isAgreementChecked = it }
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            
            
            val agreementText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF999999))) {
                    append("登录注册表示同意")
                }
                withStyle(style = SpanStyle(color = Color(0xFF6688A6))) {
                    append("用户协议、隐私条款")
                }
            }
            
            ClickableText(
                text = agreementText,
                onClick = { offset ->
                    val text = "登录注册表示同意用户协议、隐私条款"
                    if (offset >= text.indexOf("用户协议") && offset < text.indexOf("用户协议") + "用户协议".length) {
                        openCustomTab(context, "https://weibo.com/signup/v5/protocol".toUri())
                    } else if (offset >= text.indexOf("隐私条款") && offset < text.indexOf("隐私条款") + "隐私条款".length) {
                        openCustomTab(context, "https://weibo.com/signup/v5/privacy".toUri())
                    }
                },
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp
                )
            )
        }
        
        
        
        Button(
            onClick = {
                
                Toast.makeText(context, "登录功能开发中", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9500) 
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "登录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.End
        ) {
            
            Text(
                text = "忘记密码",
                fontSize = 14.sp,
                color = Color(0xFF6688A6), 
                modifier = Modifier
                    .clickable {
                        
                        Toast.makeText(context, "忘记密码功能开发中", Toast.LENGTH_SHORT).show()
                    }
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.width(240.dp))
            
            
            Text(
                text = "帮助",
                fontSize = 14.sp,
                color = Color(0xFF6688A6), 
                modifier = Modifier
                    .clickable {
                        openCustomTab(context, "https://kefu.weibo.com/".toUri())
                    }
                    .padding(8.dp)
            )
        }
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            
            IconButton(
                onClick = {
                    context.startActivity(Intent(context, SmsLoginActivity::class.java))
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.weibo.R.mipmap.ic_phone),
                    contentDescription = "手机登录",
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            IconButton(
                onClick = {
                    Toast.makeText(context, "微信登录", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.weibo.R.mipmap.ic_wechat),
                    contentDescription = "微信登录",
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            IconButton(
                onClick = {
                    Toast.makeText(context, "QQ登录", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.weibo.R.mipmap.ic_qq),
                    contentDescription = "QQ登录",
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            IconButton(
                onClick = {
                    Toast.makeText(context, "二维码登录", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.weibo.R.mipmap.ic_other),
                    contentDescription = "二维码登录",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}


private fun openCustomTab(context: Context, uri: Uri) {
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, uri)
    } catch (@Suppress("UNUSED_PARAMETER") e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}















