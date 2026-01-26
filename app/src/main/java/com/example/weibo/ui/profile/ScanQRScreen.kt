package com.example.weibo.ui.profile

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.weibo.R
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun ScanQRScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        
        
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "未授予相机权限，无法扫码", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            CameraPreviewWithZxingAnalyzer(
                modifier = Modifier.fillMaxSize(),
                onResult = { result ->
                    Toast.makeText(context, result.text, Toast.LENGTH_SHORT).show()
                },
                onCameraError = { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                lifecycleOwner = lifecycleOwner
            )
        } else {
            Text(
                text = "需要相机权限才能扫码",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        ScannerOverlay(modifier = Modifier.fillMaxSize())

        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "返回",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_animation")
    val scannerY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_y"
    )

    val density = LocalDensity.current

    Canvas(modifier = modifier) { 
        val canvasWidth = size.width
        val canvasHeight = size.height

        val boxSize = canvasWidth * 0.7f
        val boxLeft = (canvasWidth - boxSize) / 2
        val boxTop = (canvasHeight - boxSize) / 2

        drawRect(Color(0, 0, 0, 150))

        drawRoundRect(
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(24f, 24f),
            color = Color.Transparent,
            blendMode = BlendMode.DstOut
        )

        val cornerLength = with(density) { 20.dp.toPx() }
        val cornerStrokeWidth = with(density) { 4.dp.toPx() }
        val cornerColor = Color.White

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(boxLeft, boxTop + cornerLength)
                lineTo(boxLeft, boxTop)
                lineTo(boxLeft + cornerLength, boxTop)
            },
            color = cornerColor,
            style = Stroke(width = cornerStrokeWidth)
        )

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(boxLeft + boxSize - cornerLength, boxTop)
                lineTo(boxLeft + boxSize, boxTop)
                lineTo(boxLeft + boxSize, boxTop + cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = cornerStrokeWidth)
        )

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(boxLeft, boxTop + boxSize - cornerLength)
                lineTo(boxLeft, boxTop + boxSize)
                lineTo(boxLeft + cornerLength, boxTop + boxSize)
            },
            color = cornerColor,
            style = Stroke(width = cornerStrokeWidth)
        )

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(boxLeft + boxSize - cornerLength, boxTop + boxSize)
                lineTo(boxLeft + boxSize, boxTop + boxSize)
                lineTo(boxLeft + boxSize, boxTop + boxSize - cornerLength)
            },
            color = cornerColor,
            style = Stroke(width = cornerStrokeWidth)
        )

        val lineHeight = with(density) { 2.dp.toPx() }
        val lineY = boxTop + scannerY * boxSize

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFFF8615).copy(alpha = 0.85f),
                    Color.Transparent
                )
            ),
            topLeft = Offset(boxLeft, lineY - lineHeight / 2),
            size = Size(boxSize, lineHeight * 2)
        )
    }
}

@Composable
private fun CameraPreviewWithZxingAnalyzer(
    modifier: Modifier,
    onResult: (Result) -> Unit,
    onCameraError: (String) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current

    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val reader = remember { MultiFormatReader() }

    var lastText by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        val result = decodeWithZxing(reader, imageProxy)
                        if (result != null) {
                            val text = result.text
                            if (text.isNotBlank() && text != lastText) {
                                lastText = text
                                onResult(result)
                            }
                        }
                        imageProxy.close()
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        onCameraError("启动相机失败：${e.message}")
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                analysisExecutor.shutdown()
            } catch (_: Exception) {
            }
        }
    }
}

private fun decodeWithZxing(reader: MultiFormatReader, imageProxy: ImageProxy): Result? {
    val mediaImage = imageProxy.image ?: return null
    val buffer = mediaImage.planes.firstOrNull()?.buffer ?: return null

    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val width = imageProxy.width
    val height = imageProxy.height

    val source = PlanarYUVLuminanceSource(
        bytes,
        width,
        height,
        0,
        0,
        width,
        height,
        false
    )

    val bitmap = BinaryBitmap(HybridBinarizer(source))

    return try {
        reader.decodeWithState(bitmap)
    } catch (_: NotFoundException) {
        null
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}
