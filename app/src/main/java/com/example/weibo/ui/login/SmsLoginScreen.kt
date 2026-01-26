package com.example.weibo.ui.login

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import com.example.weibo.ui.MainActivity
import java.util.Timer
import java.util.TimerTask


class SmsLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                SmsLoginScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}


@Composable
fun SmsLoginScreen(
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit
) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+86") }
    var isAgreementChecked by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var timer by remember { mutableStateOf<Timer?>(null) }
    
    
    val countryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("country_code")
            if (code != null) {
                countryCode = "+$code"
            }
        }
    }
    
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                "你拒绝了通知权限，可能无法及时收到验证码提醒",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        createNotificationChannel(context)
    }
    
    
    DisposableEffect(Unit) {
        onDispose {
            timer?.cancel()
        }
    }
    
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
                text = "短信验证码登录",
                fontSize = 28.sp,
                color = Color(0xFF333333), 
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            
            Text(
                text = "未注册手机号验证通过后将自动注册",
                fontSize = 14.sp,
                color = Color(0xFF999999) 
            )
        }
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            
            Row(
                modifier = Modifier
                    .clickable {
                        val intent = Intent(context, CountryPickerActivity::class.java)
                        countryPickerLauncher.launch(intent)
                    }
                    .height(50.dp)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                
                Text(
                    text = countryCode,
                    fontSize = 16.sp,
                    color = Color(0xFF333333) 
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "选择国家代码",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF333333)
                )
            }
            
            
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFFE0E0E0)) 
                    .padding(end = 12.dp)
            )
            
            
            
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 11) phone = it },
                placeholder = {
                    Text(
                        text = "手机号",
                        fontSize = 16.sp,
                        color = Color(0xFFCCCCCC) 
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF333333) 
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                placeholder = {
                    Text(
                        text = "验证码",
                        fontSize = 16.sp,
                        color = Color(0xFFCCCCCC) 
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF333333) 
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            
            
            
            TextButton(
                onClick = {
                    if (phone.isEmpty()) {
                        Toast.makeText(context, "请输入手机号码", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (!isAgreementChecked) {
                        showPrivacyDialog(context) {
                            isAgreementChecked = true
                            startCountdown(
                                currentTimer = timer,
                                onTimerChange = { newTimer -> timer = newTimer },
                                onCountdownChange = { newCountdown -> countdown = newCountdown }
                            )
                            Toast.makeText(context, "验证码666666已发送", Toast.LENGTH_SHORT).show()
                            showNotification(context, "验证码", "666666", "请勿泄露，点击查看详情")
                        }
                        return@TextButton
                    }
                    startCountdown(
                        currentTimer = timer,
                        onTimerChange = { newTimer -> timer = newTimer },
                        onCountdownChange = { newCountdown -> countdown = newCountdown }
                    )
                    Toast.makeText(context, "验证码666666已发送", Toast.LENGTH_SHORT).show()
                    showNotification(context, "验证码", "666666", "请勿泄露，点击查看详情")
                },
                enabled = countdown == 0,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = if (countdown > 0) countdown.toString() else "获取验证码",
                    fontSize = 14.sp,
                    color = Color(0xFF6688A6) 
                )
            }
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
                if (phone.isEmpty()) {
                    Toast.makeText(context, "请输入手机号", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (code.isEmpty()) {
                    Toast.makeText(context, "请输入验证码", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isAgreementChecked) {
                    Toast.makeText(context, "请同意用户协议和隐私条款", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (code == "666666") {
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                    return@Button
                }
                
                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? ComponentActivity)?.finish()
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
        
        
        
        Text(
            text = "帮助",
            fontSize = 14.sp,
            color = Color(0xFF6688A6), 
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .clickable {
                    openCustomTab(context, "https://kefu.weibo.com/".toUri())
                },
            textAlign = TextAlign.End
        )
        
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            
            IconButton(
                onClick = {
                    context.startActivity(Intent(context, PasswordLoginActivity::class.java))
                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.weibo.R.mipmap.ic_email),
                    contentDescription = "邮箱登录",
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


private fun startCountdown(
    currentTimer: Timer?,
    onTimerChange: (Timer?) -> Unit,
    onCountdownChange: (Int) -> Unit
) {
    currentTimer?.cancel()
    var count = 60
    onCountdownChange(count)
    
    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            count--
            if (count > 0) {
                onCountdownChange(count)
            } else {
                onCountdownChange(0)
                timer.cancel()
                onTimerChange(null)
            }
        }
    }, 1000, 1000)
    onTimerChange(timer)
}


private fun showPrivacyDialog(context: Context, onAgree: () -> Unit) {
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle("隐私协议")
        .setMessage("为保障与你相关的合法权益，请阅读并同意用户协议、隐私条款")
        .setCancelable(false)
        .setNegativeButton("不同意") { dialog, _ -> dialog.dismiss() }
        .setPositiveButton("同意") { dialog, _ ->
            dialog.dismiss()
            onAgree()
        }
        .show()
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


@Suppress("SameParameterValue")
private fun showNotification(context: Context, title: String, text: String, info: String) {
    val notification = NotificationCompat.Builder(context, "sms_notification_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(info))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(1, notification)
}


private fun createNotificationChannel(context: Context) {
    @Suppress("Unnecessary; `SDK_INT` is always >= 26")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "sms_notification_channel",
            "SmsLogin Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel for SMS login verification codes"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

