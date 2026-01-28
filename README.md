# Weibo（Compose）

<div align="center">

![Language](https://img.shields.io/badge/language-Kotlin-blue)
![Android](https://img.shields.io/badge/platform-Android%2026+-green)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-orange)
![Build](https://img.shields.io/badge/build-Gradle%20KTS-yellow)


[快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目展示](#-项目展示) • [贡献](#-贡献指南)

</div>

---

## 📱 项目介绍

一个基于 **Jetpack Compose** 的微博风格 Demo，实现信息流、图片九宫格/大图预览、媒体选择器、沉浸式状态栏、Lottie 底部导航、竖滑视频流播放、直播间礼物特效与点赞漂浮动画等核心功能。


### ✨ 核心特性

- **信息流 + 九宫格图片**：`app/src/main/java/com/example/weibo/ui/home/components/PostList.kt`（`PostImageGrid`）
- **图片大图预览（缩放/分页）**：`app/src/main/java/com/example/weibo/ui/home/preview/ImagePreviewScreen.kt`（PhotoView + ViewPager2）
- **媒体选择器（图片/视频/拍照、多选、视频缩略图）**：`app/src/main/java/com/example/weibo/ui/picker/ImagePickerScreen.kt`
- **可复用沉浸式状态栏**：`app/src/main/java/com/example/weibo/core/ui/components/SystemBarsController.kt`（`SetupSystemBars`）
- **底部导航 Lottie 动画**：`app/src/main/java/com/example/weibo/core/ui/components/LottieBottomNavigation.kt` + `app/src/main/res/raw/*_nav.json`
- **竖滑视频流播放（Media3 ExoPlayer）+ 预解析/预加载**：
  - `app/src/main/java/com/example/weibo/ui/video/VideoScreen.kt`
  - `app/src/main/java/com/example/weibo/video/player/ExoVideoPlayerManager.kt`
  - `app/src/main/java/com/example/weibo/video/player/ExoVideoPlayerView.kt`
  - `app/src/main/java/com/example/weibo/util/VideoUrlResolver.kt`
- **视频页点赞动画（双击点赞 + 粒子爆裂）**：
  - `app/src/main/java/com/example/weibo/ui/video/LikeBurstOverlay.kt`
  - `app/src/main/java/com/example/weibo/ui/video/LikeBurstEffect.kt`
- **直播间礼物特效（SVGA 队列播放）+ 点赞漂浮**：
  - `app/src/main/java/com/example/weibo/ui/livestream/LiveStreamScreen.kt`
  - `app/src/main/java/com/example/weibo/ui/livestream/LiveLikeBurst.kt`
  - 资源：`app/src/main/assets/ga21.svga` ~ `ga32.svga`

---

## 📸 项目展示
### 亮点展示
| 直播礼物特效 | 直播点赞喷射动画 | 视频双击点赞粒子动画 |
|:---:|:---:|:---:|
| ![直播礼物特效](https://github.com/user-attachments/assets/64d46325-ccd6-44ca-85f8-e75b85eb8ae2) | ![直播点赞喷射动画](https://github.com/user-attachments/assets/54408dfc-8c48-4c09-8605-dbca9527e22f) | ![视频双击点赞粒子动画](https://github.com/user-attachments/assets/3dcb27f8-c599-4e3a-96e5-2454406b36b5) |

| 图片选择器、九宫格 | 大图预览 | 热搜列表吸顶 | 动画底部导航栏 |
|:---:|:---:|:---:|:---:|
| ![图片选择器、九宫格](https://github.com/user-attachments/assets/a096dada-0e54-452b-ae09-7d8fe35b63cf) | ![大图预览](https://github.com/user-attachments/assets/7f5a47d1-1d05-4292-9fe5-fa01fd5de49a) | ![热搜列表吸顶](https://github.com/user-attachments/assets/56b15983-2b43-475a-aa63-97efb9ea74db) | ![动画底部导航栏](https://github.com/user-attachments/assets/59083094-7517-4196-b649-9e4143ccef8a) |


### 页面入口与演示路径

- **主入口**：`app/src/main/java/com/example/weibo/ui/MainActivity.kt`（`MainActivity` -> `MainScreen()`）
- **底部 Tab 切换**：`selectedIndex` 控制
  - `0` 首页：`app/src/main/java/com/example/weibo/ui/home/HomeScreen.kt`
  - `1` 视频：`app/src/main/java/com/example/weibo/ui/video/VideoScreen.kt`
  - `2` 发现：`app/src/main/java/com/example/weibo/ui/discover/DiscoverScreen.kt`
  - `3` 消息：`app/src/main/java/com/example/weibo/ui/message/MessageScreen.kt`
  - `4` 我的：`app/src/main/java/com/example/weibo/ui/profile/ProfileScreen.kt`

- **信息流图片预览链路**：
  - `HomeScreen` -> `PostList.onImageClick` -> `MainActivity.imagePreviewData` -> `ImagePreviewScreen`

- **直播入口链路**：
  - `HomeScreen` 的 `AddMenuPopup` -> `Intent(context, LiveStreamActivity::class.java)` -> `LiveStreamScreen`

- **发微博入口链路**：
  - `HomeScreen` 的 `AddMenuPopup` -> `onNavigateToWritePost()` -> `MainActivity.showWritePost` -> `WritePostScreen`

---

## 🏗️ 项目结构

```
WeiBo/
├── app/
│   ├── src/main/java/com/example/weibo/
│   │   ├── ui/
│   │   │   ├── MainActivity.kt                 # 主入口、页面路由（状态控制显示/隐藏）
│   │   │   ├── home/                           # 首页信息流、频道、弹窗、图片预览入口
│   │   │   ├── home/components/PostList.kt     # 信息流 + 九宫格
│   │   │   ├── home/preview/ImagePreviewScreen.kt  # PhotoView + ViewPager2 大图预览
│   │   │   ├── post/WritePostScreen.kt         # 发微博：选图/表情/图片网格
│   │   │   ├── picker/ImagePickerScreen.kt     # 媒体选择器：图片/视频/拍照/多选
│   │   │   ├── video/                          # 竖滑视频流 + 点赞动效
│   │   │   ├── livestream/                     # 直播间：SVGA 礼物 + 点赞漂浮
│   │   │   └── preview/MediaPreviewScreen.kt   # MediaPreviewActivity（当前仓库未检索到调用点）
│   │   ├── video/player/                       # ExoPlayer 封装（含缓存与预加载）
│   │   ├── util/VideoUrlResolver.kt            # 播放地址解析（B站/开眼 API）
│   │   ├── viewmodel/                          # ViewModel：Main/Video/Discover/...
│   │   └── network/                            # Retrofit API + 数据模型
│   ├── src/main/assets/                         # SVGA 礼物资源 ga21~ga32
│   └── src/main/res/raw/                        # Lottie 底导 JSON
└── build.gradle.kts / app/build.gradle.kts
```

### 模块/目录职责

| 目录/模块 | 语言 | 职责 | 关键依赖 |
|------|------|------|--------|
| **app** | Kotlin | Compose UI、页面逻辑、动效实现、网络与本地数据 | Compose、Navigation、Hilt、Retrofit、Room、Media3、Lottie、SVGA、PhotoView、Coil |
| **ui/** | Kotlin | 页面层：Home/Video/LiveStream/Picker/Post 等 | Compose |
| **video/player/** | Kotlin | ExoPlayer 管理 + 自定义播放器 View（手势、控制条、进度保存、缓存/预加载） | androidx.media3 |
| **core/ui/components/** | Kotlin | 可复用 UI：沉浸式状态栏、TopBar 容器、Lottie 底导等 | Compose |

---

## 🛠️ 技术栈

### 编译环境（来自 `app/build.gradle.kts`）

- **Language**：Kotlin
- **Target/Compile SDK**：34
- **Min SDK**：26
- **JDK**：17
- **UI**：Jetpack Compose

### 核心依赖（来自 `app/build.gradle.kts`）

#### UI & 动画
- **Jetpack Compose**（BOM）
- **Lottie**（含 `lottie-compose`）
- **SVGAPlayer-Android** `2.6.1`（直播礼物特效）

#### 图片
- **Coil Compose**（信息流/头像/封面等）
- **PhotoView**（大图预览缩放）

#### 视频
- **AndroidX Media3 ExoPlayer**（播放）
- **Media3 datasource-okhttp**（数据源）

#### 网络/数据
- **Retrofit + OkHttp + Gson**
- **Room**（含 Paging）
- **Paging Compose**

#### 依赖注入
- **Hilt**（`@AndroidEntryPoint`、`hiltViewModel()`）

---

## 🚀 快速开始

### 前置要求

1. **Android Studio**：任意支持 Compose 的版本
2. **JDK**：17
3. **Gradle**：使用项目自带 wrapper

### 编译与运行

1️⃣ 打开项目
- 用 Android Studio 打开根目录 `WeiBo/`

2️⃣ Sync
- 等待 Gradle Sync 完成

3️⃣ 运行
- 选择 `app` 配置，Run 到模拟器/真机

---

## 🔐 权限说明

媒体选择器与拍照会动态申请权限（`app/src/main/java/com/example/weibo/ui/picker/ImagePickerScreen.kt`）：

- Android 13+：`READ_MEDIA_IMAGES`、`READ_MEDIA_VIDEO`、`CAMERA`
- Android 12-：`READ_EXTERNAL_STORAGE`、`CAMERA`

> 如果用户拒绝权限，选择器会提示并退出。

---

## 🎨 动效系统详解

### 1. 直播礼物特效（SVGA 队列播放）

**实现文件**：`app/src/main/java/com/example/weibo/ui/livestream/LiveStreamScreen.kt`

**实现要点**：
- 礼物资源存放在 `app/src/main/assets/ga21.svga` ~ `ga32.svga`
- 使用 `giftSvgaQueue: MutableStateList<String>` 作为队列
- `currentGiftSvga == null` 且队列非空时自动出队播放
- `SvgaOverlay` 使用 `AndroidView` 包装 `SVGAImageView`
  - `SVGAParser.decodeFromAssets(assetName, ...)` 异步解析
  - `loops = 1`、`clearsAfterStop = true`
  - `SVGACallback.onFinished()` 回调后清空当前礼物，进入下一条

### 2. 直播点赞漂浮动画（Canvas 粒子）

**实现文件**：
- `app/src/main/java/com/example/weibo/ui/livestream/LiveStreamScreen.kt`
- `app/src/main/java/com/example/weibo/ui/livestream/LiveLikeBurst.kt`

**实现要点**：
- 点赞按钮点击递增 `likeToken`
- 通过 `onGloballyPositioned { coords.localToRoot(...) }` 获取按钮中心点 root 坐标作为发射点
- `LiveLikeBurstOverlay` 在 `Canvas` 中生成多颗粒子，基于时间更新位置与透明度并重绘

### 3. 视频页双击点赞动画（点赞图标 + 爆裂粒子）

**实现文件**：
- `app/src/main/java/com/example/weibo/ui/video/VideoScreen.kt`
- `app/src/main/java/com/example/weibo/ui/video/LikeBurstOverlay.kt`
- `app/src/main/java/com/example/weibo/ui/video/LikeBurstEffect.kt`

**实现要点**：
- `detectTapGestures(onDoubleTap)` 获取双击坐标
- 点赞 Icon：`Animatable(scale/alpha)` 实现 pop + fade，并按坐标 `offset`
- `LikeBurstOverlay`：以点击坐标为中心绘制扩散粒子
- 右侧点赞按钮：缩放反馈 + `LikeBurstEffect` 小爆裂

### 4. 信息流图片预览（缩放 + 分页）

**实现文件**：`app/src/main/java/com/example/weibo/ui/home/preview/ImagePreviewScreen.kt`

**实现要点**：
- `AndroidView` 内部承载 `ViewPager2`
- 每页使用 `PhotoView` 实现缩放/拖拽
- 触控冲突处理：当处于最小缩放且单指横向拖拽时，让 `ViewPager2` 接管翻页

### 5. Lottie 底部导航栏

**实现文件**：`app/src/main/java/com/example/weibo/core/ui/components/LottieBottomNavigation.kt`

**实现要点**：
- 每个 Tab 对应一个 `res/raw/*.json`
- 通过 `playToken` + `animateLottieCompositionAsState` 控制“选中时播放一次”

### 6. 可复用沉浸式状态栏（自动取色/图标深浅色）

**实现文件**：`app/src/main/java/com/example/weibo/core/ui/components/SystemBarsController.kt`

**实现要点**：
- `SetupSystemBars(SystemBarsConfig)` 统一设置：
  - `WindowCompat.setDecorFitsSystemWindows(window, !immersive)`
  - `statusBarColor` / `navigationBarColor`
  - `isAppearanceLightStatusBars`（深浅色图标）
- `TopBarBackground.Solid`：根据颜色亮度自动推导图标深浅色
- `TopBarBackground.Image`：通过 Palette（依赖 `androidx.palette:palette-ktx`）计算背景色与图标深浅

### 7. 竖滑视频流：预解析 + 预加载 + 播放器封装

**实现文件**：
- `app/src/main/java/com/example/weibo/ui/video/VideoScreen.kt`
- `app/src/main/java/com/example/weibo/viewmodel/VideoViewModel.kt`
- `app/src/main/java/com/example/weibo/util/VideoUrlResolver.kt`
- `app/src/main/java/com/example/weibo/video/player/ExoVideoPlayerManager.kt`
- `app/src/main/java/com/example/weibo/video/player/ExoVideoPlayerView.kt`

**两段式解析/预热**：
1. `VideoViewModel.preResolveAround(position, radius)` 预解析并写入 `resolvedUrlCache`（LRU 100）
2. `ExoVideoPlayerManager.preloadVideo(url, tag)` 使用独立 `preloadPlayer` 仅 `prepare()`，预热缓存与连接

**`VideoUrlResolver` 解析逻辑**：
- 直链：直接返回
- B 站 API：解析 `data.durl[0].url`
- 开眼 playUrl API：解析 `urls[0].url` 或 `url`
- 失败：fallback 返回原始 `playUrl`

**`ExoVideoPlayerView` 封装点**：
- 自定义 `FrameLayout` 包含 Media3 `PlayerView`（关闭默认 controller）+ 封面图 + 控制条
- 交互：单击播放/暂停、SeekBar 拖动、左右滑动调进度、上下滑动调亮度/音量
- 进度保存：播放中每 5 秒自动保存（`saveVideoProgress`），暂停/停止/释放也会保存
- 生命周期：`bindLifecycle(lifecycleOwner)` 委托给 `ExoVideoPlayerManager.bindLifecycle()` 处理 `ON_PAUSE/ON_RESUME/ON_DESTROY`

---

## 💡 核心数据流

```
MainActivity
  ├─> MainViewModel.selectedBottomNavIndex (StateFlow)
  └─> when(selectedIndex)
      ├─> HomeScreen
      │    ├─> PostList.onImageClick -> imagePreviewData -> ImagePreviewScreen
      │    └─> AddMenuPopup.onLive -> startActivity(LiveStreamActivity)
      ├─> VideoScreen
      │    ├─> VideoViewModel.recommendVideoList/selectedTab (StateFlow)
      │    ├─> preResolveAround -> resolvedUrlCache
      │    └─> ExoVideoPlayerManager (play + preload + cache)
      └─> ...

VideoUrlResolver (IO)
  └─> HttpURLConnection -> JSON 解析 -> 返回真实播放 URL 或 fallback
```

---

## 🗺️ 开发路线图

- [x] Compose 主框架 + 底部 Lottie 导航
- [x] 首页信息流 + 九宫格图片
- [x] PhotoView 大图预览（信息流入口）
- [x] 发布页 + 媒体选择器（图片/视频/拍照）
- [x] 竖滑视频流（Media3 ExoPlayer）+ 预解析/预加载
- [x] 视频页双击点赞动效（点赞Icon + 粒子）
- [x] 直播间礼物 SVGA 队列播放 + 点赞漂浮

---

## 🤝 贡献指南

欢迎 Issue、PR 和讨论。

### 提交 Issue
- **Bug**：描述现象、复现步骤、预期/实际结果、日志
- **建议**：说明场景、期望效果、可参考实现

### 提交 PR
1. Fork 本仓库
2. 新建分支：`git checkout -b feature/YourFeature`
3. 提交：`git commit -m "feat: ..."`
4. 推送：`git push origin feature/YourFeature`
5. 发起 Pull Request

---


---

## 📞 联系方式

- Issues：使用本仓库的 GitHub Issues

---

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [AndroidX Media3 / ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- [Airbnb Lottie](https://airbnb.design/lottie/)
- [直播礼物特效SVGA格式资源](https://blog.csdn.net/gitblog_09816/article/details/142889731)
- [PhotoView](https://github.com/Baseflow/PhotoView)

---

<div align="center">

[⬆ 回到顶部](#weibocomposedemo)

**⭐ 如果觉得项目有帮助，请给个 Star！**

</div>
