# 微博风格 Demo (Weibo-Clone-App)

一个基于 Jetpack Compose 实现的微博风格 Demo 项目，包含信息流、图片九宫格/大图预览、媒体选择器、沉浸式状态栏适配、Lottie 底部导航，以及“抖音式”竖滑视频流播放、直播间礼物特效与点赞漂浮动画等。

## ✨ 核心功能

- **首页信息流**：支持无限滚动的动态列表，包含文字、图片（九宫格布局）和视频内容
- **视频播放**：仿抖音的竖滑视频流，支持预加载、点赞动画和全屏播放
- **图片预览**：支持缩放、拖动关闭和左右滑动切换
- **图片选择器**：从相册选择多张图片或拍照
- **直播功能**：模拟直播间，支持发送礼物（SVGA 动画）和点赞漂浮效果
- **用户资料**：展示用户信息和设置选项
- **现代化 UI**：Lottie 动画底部导航、沉浸式状态栏和流畅的转场动画

## 🛠️ 技术栈与架构

- **UI**：Jetpack Compose
- **架构**：MVVM (Model-View-ViewModel)
- **依赖注入**：Hilt
- **网络请求**：Retrofit + OkHttp
- **异步编程**：Kotlin 协程 + Flow
- **本地存储**：Room 数据库
- **图片加载**：Coil
- **视频播放**：ExoPlayer (Media3)
- **动画**：
  - `lottie-compose`：底部导航动画
  - `SVGAPlayer-Android`：直播间礼物动画
  - Jetpack Compose 原生动画 API
- **导航**：`navigation-compose`

## �� 功能实现细节

### 1. Lottie 底部导航

- **实现文件**：`LottieBottomNavigation.kt`
- **核心逻辑**：
  - 每个导航项使用独立的 Lottie 动画
  - 通过 `animateLottieCompositionAsState` 控制动画进度
  - 点击时触发完整动画播放

### 2. 沉浸式状态栏

- **实现文件**：`SystemBarsController.kt`
- **核心逻辑**：
  - 使用 `WindowCompat.setDecorFitsSystemWindows` 实现沉浸式
  - 自动根据顶部栏背景色调整状态栏图标颜色
  - 支持图片背景的取色适配

### 3. 视频流预加载

- **实现文件**：`VideoScreen.kt`, `ExoVideoPlayerManager.kt`
- **核心逻辑**：
  - 使用 `VerticalPager` 实现上下滑动切换
  - 滑动停止后预加载下一条视频
  - 通过 `ExoVideoPlayerManager` 管理播放器实例

### 4. 视频点赞动画

- **实现文件**：`VideoScreen.kt` (`VideoRecommendItem`)
- **核心逻辑**：
  - 双击触发 `detectTapGestures(onDoubleTap)`
  - 点击位置显示心形图标，使用 `Animatable` 实现缩放和透明度动画
  - 同时触发 `LikeBurstOverlay` 粒子效果

### 5. 直播间礼物特效

- **实现文件**：`LiveStreamScreen.kt` (`SvgaOverlay`)
- **核心逻辑**：
  - 使用 `SVGAParser` 加载 `.svga` 动画资源
  - 通过队列管理多个礼物的播放顺序
  - 动画播放完成后自动播放下一个

### 6. 直播间点赞漂浮

- **实现文件**：`LiveLikeBurst.kt` (`LiveLikeBurstOverlay`)
- **核心逻辑**：
  - 使用 `Canvas` 绘制粒子动画
  - 每个粒子有独立的运动轨迹和生命周期
  - 通过 `LaunchedEffect` 驱动动画更新

### 7. 图片九宫格

- **实现文件**：`PostList.kt` (`PostImageGrid`)
- **核心逻辑**：
  - 单张图片：固定高度，保持原比例
  - 2-9 张：3 列网格布局
  - 超过 9 张时在第 9 张显示 "+N" 提示

### 8. 图片预览

- **实现文件**：`ImagePreviewScreen.kt`
- **核心逻辑**：
  - 使用 `PhotoView` 实现图片缩放
  - `ViewPager2` 实现左右滑动切换
  - 支持拖动关闭和手势处理

### 9. 图片选择器

- **实现文件**：`ImagePickerScreen.kt`
- **核心逻辑**：
  - 使用 `MediaStore` 查询媒体文件
  - 支持多选和相机拍照
  - 适配 Android 13+ 权限模型

## ⚙️ 如何运行

1. 克隆仓库
2. 使用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 运行 `app` 模块到模拟器或真机

## 📝 注意事项

- 项目中使用了一些模拟数据，部分功能可能需要后端支持
- 直播功能为模拟实现，实际需要接入直播 SDK
- 图片选择器的权限处理已适配 Android 13+

## �� 开源协议

[MIT License](LICENSE)