package com.example.weibo.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.R
import com.example.weibo.ui.login.PinyinInitialUtils
import kotlinx.coroutines.launch
import kotlin.math.max
import java.util.Locale


class CountryPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                SetupSystemBars(
                    SystemBarsConfig(
                        immersive = true,
                        autoStatusBarIcons = true,
                        autoStatusBarColor = true,
                        topBarBackground = TopBarBackground.Solid(Color.White),
                        statusBarIconsFallbackColor = Color.White
                    )
                )
                CountryPickerScreen(
                    onBack = { finish() },
                    onCountrySelected = { name, code ->
                        val result = Intent().apply {
                            putExtra("country_name", name)
                            putExtra("country_code", code)
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                )
            }
        }
    }
}


private data class CountryRow(
    val isHeader: Boolean,
    val title: String,
    val name: String? = null,
    val code: String? = null
)


@Composable
fun CountryPickerScreen(
    onBack: () -> Unit,
    onCountrySelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    
    val countryNames = remember {
        context.resources.getStringArray(R.array.country_names).toList()
    }
    val countryCodes = remember {
        context.resources.getStringArray(R.array.country_codes).toList()
    }
    
    
    val (rows, sectionLabels, sectionToPosition) = remember(countryNames, countryCodes) {
        
        val commonNames = listOf("中国", "中国香港", "中国台湾", "中国澳门")
        val commonCodes = listOf("86", "852", "886", "853")
        
        
        val grouped = countryNames.indices.groupBy { index ->
            Character.toUpperCase(PinyinInitialUtils.getInitial(countryNames[index]))
        }
        
        val sortedKeys = grouped.keys.sorted()
        val rowsList = mutableListOf<CountryRow>()
        val sectionToPositionList = mutableListOf<Int>()
        val sectionLabelsList = mutableListOf<String>()
        
        
        val commonStartPosition = rowsList.size
        sectionToPositionList.add(commonStartPosition)
        sectionLabelsList.add("常")
        rowsList.add(CountryRow(true, "常用", null, null))
        
        commonNames.forEachIndexed { index, name ->
            rowsList.add(CountryRow(false, "常用", name, commonCodes[index]))
        }
        
        
        sortedKeys.forEach { key ->
            val sectionStartPosition = rowsList.size
            sectionToPositionList.add(sectionStartPosition)
            sectionLabelsList.add(key.toString())
            rowsList.add(CountryRow(true, key.toString(), null, null))
            
            val indices = grouped[key] ?: emptyList()
            indices.sortedBy { countryNames[it] }.forEach { countryIndex ->
                rowsList.add(CountryRow(false, key.toString(), countryNames[countryIndex], countryCodes[countryIndex]))
            }
        }
        
        Triple(rowsList, sectionLabelsList, sectionToPositionList)
    }
    
    val listState = rememberLazyListState()
    var selectedIndexLetter by remember { mutableIntStateOf(-1) }
    var showFloatingIndicator by remember { mutableStateOf(false) }
    var floatingIndicatorText by remember { mutableStateOf("") }
    
    
    val indexLetters = remember {
        listOf("常", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
               "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            com.example.weibo.core.ui.components.StatusBarPlaceholder(backgroundColor = Color.White)

            
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                
                
                Text(
                    text = "选择国家/地区",
                    fontSize = 18.sp,
                    color = Color(0xFF222222), 
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(rows) { index, row ->
                    if (row.isHeader) {
                        
                        
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF5F5F5) 
                        ) {
                            Text(
                                text = row.title,
                                fontSize = 14.sp,
                                color = Color(0xFF999999), 
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .padding(horizontal = 16.dp)
                                    .wrapContentHeight(Alignment.CenterVertically)
                            )
                        }
                    } else {
                        
                        
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable {
                                    row.name?.let { name ->
                                        row.code?.let { code ->
                                            onCountrySelected(name, code)
                                        }
                                    }
                                },
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                
                                Text(
                                    text = row.name ?: "",
                                    fontSize = 16.sp,
                                    color = Color(0xFF222222), 
                                    modifier = Modifier.weight(1f)
                                )
                                
                                
                                
                                
                                Text(
                                    text = row.code?.let { 
                                        String.format(Locale.getDefault(), "%04d", it.toIntOrNull() ?: 0)
                                    } ?: "",
                                    fontSize = 16.sp,
                                    color = Color(0xFF999999) 
                                )
                            }
                        }
                    }
                }
            }
        }
        
        
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(24.dp)
                .fillMaxHeight()
                .padding(top = 8.dp, bottom = 32.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            selectedIndexLetter = -1
                            showFloatingIndicator = false
                        },
                        onDragCancel = {
                            selectedIndexLetter = -1
                            showFloatingIndicator = false
                        },
                        onDrag = { change, _ ->
                            val y = change.position.y
                            val containerHeight = size.height.toFloat()
                            val indexCount = indexLetters.size
                            val letterHeight = containerHeight / indexCount
                            val position = max(0, minOf((y / letterHeight).toInt(), indexCount - 1))
                            
                            if (position != selectedIndexLetter) {
                                selectedIndexLetter = position
                                val letter = indexLetters[position]
                                floatingIndicatorText = letter
                                showFloatingIndicator = true
                                
                                
                                val sectionIndex = sectionLabels.indexOf(letter)
                                if (sectionIndex >= 0 && sectionIndex < sectionToPosition.size) {
                                    val targetPosition = sectionToPosition[sectionIndex]
                                    scope.launch {
                                        listState.animateScrollToItem(targetPosition)
                                    }
                                }
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            indexLetters.forEachIndexed { index, letter ->
                
                
                Text(
                    text = letter,
                    fontSize = if (index == selectedIndexLetter) 16.sp else 12.sp,
                    color = if (index == selectedIndexLetter) Color.Black else Color(0xFF9AA4B2), 
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        
        
        if (showFloatingIndicator) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                shape = CircleShape,
                color = Color(0xFFF5F5F5), 
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = floatingIndicatorText,
                        fontSize = 48.sp,
                        color = Color(0xFFFF9500), 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

