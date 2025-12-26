package com.nordicwalk.feature.video.presentation

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.util.Locale // 加入 Locale 引用

@OptIn(UnstableApi::class)
@Composable
fun VideoPlaybackScreen(
    videoPath: String,
    onBack: () -> Unit,
    onCapture: () -> Unit,
    viewModel: VideoPlaybackViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val posePoints by viewModel.posePoints.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }
    var isPlayerVisible by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoPath) {
        if (videoPath.isNotEmpty()) {
            val uri = if (videoPath.startsWith("content://")) {
                Uri.parse(videoPath)
            } else {
                Uri.fromFile(java.io.File(videoPath))
            }

            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            viewModel.prepareVideo(videoPath)

            delay(100)
            isPlayerVisible = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                    val format = exoPlayer.videoFormat
                    if (format != null) {
                        videoWidth = format.width
                        videoHeight = format.height
                    }
                }
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                videoWidth = videoSize.width
                videoHeight = videoSize.height
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val currentPos = exoPlayer.currentPosition
            currentPosition = currentPos
            val targetTime = if (isPlaying) currentPos + 100 else currentPos
            viewModel.analyzeFrameAt(targetTime)
            delay(50)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
            }
            Text("健走姿態分析", color = Color.White)
            IconButton(onClick = onCapture) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "下一步", tint = Color.White)
            }
        }

        // 播放區
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        keepScreenOn = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isPlayerVisible) 1f else 0f
                    }
                    .clickable {
                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    }
            )

            if (videoWidth > 0 && videoHeight > 0) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val isRotated = videoWidth > videoHeight && canvasHeight > canvasWidth

                    val effectiveVideoWidth = if (isRotated) videoHeight.toFloat() else videoWidth.toFloat()
                    val effectiveVideoHeight = if (isRotated) videoWidth.toFloat() else videoHeight.toFloat()

                    val videoRatio = effectiveVideoWidth / effectiveVideoHeight
                    val canvasRatio = canvasWidth / canvasHeight

                    var renderWidth = canvasWidth
                    var renderHeight = canvasHeight
                    var offsetX = 0f
                    var offsetY = 0f

                    if (videoRatio > canvasRatio) {
                        renderHeight = canvasWidth / videoRatio
                        offsetY = (canvasHeight - renderHeight) / 2f
                    } else {
                        renderWidth = canvasHeight * videoRatio
                        offsetX = (canvasWidth - renderWidth) / 2f
                    }

                    fun transformPoint(p: com.nordicwalk.feature.video.presentation.PosePoint): Offset {
                        return Offset(
                            offsetX + p.y * renderWidth,
                            offsetY + (1f - p.x) * renderHeight
                        )
                    }

                    if (posePoints.isNotEmpty()) {
                        val leftConnections = listOf(
                            11 to 13, 13 to 15,
                            15 to 17, 15 to 19, 15 to 21, 17 to 19,
                            11 to 23,
                            23 to 25, 25 to 27,
                            27 to 29, 27 to 31, 29 to 31
                        )

                        val rightConnections = listOf(
                            12 to 14, 14 to 16,
                            16 to 18, 16 to 20, 16 to 22, 18 to 20,
                            12 to 24,
                            24 to 26, 26 to 28,
                            28 to 30, 28 to 32, 30 to 32
                        )

                        val shoulderConnection = listOf(11 to 12)
                        val hipConnection = listOf(23 to 24)

                        leftConnections.forEach { (start, end) ->
                            if (start < posePoints.size && end < posePoints.size) {
                                val p1 = posePoints[start]
                                val p2 = posePoints[end]
                                if (p1.confidence > 0.5f && p2.confidence > 0.5f) {
                                    drawLine(Color.Green, transformPoint(p1), transformPoint(p2), 5f)
                                }
                            }
                        }

                        rightConnections.forEach { (start, end) ->
                            if (start < posePoints.size && end < posePoints.size) {
                                val p1 = posePoints[start]
                                val p2 = posePoints[end]
                                if (p1.confidence > 0.5f && p2.confidence > 0.5f) {
                                    drawLine(Color.Red, transformPoint(p1), transformPoint(p2), 5f)
                                }
                            }
                        }

                        (shoulderConnection + hipConnection).forEach { (start, end) ->
                            if (start < posePoints.size && end < posePoints.size) {
                                val p1 = posePoints[start]
                                val p2 = posePoints[end]
                                if (p1.confidence > 0.5f && p2.confidence > 0.5f) {
                                    drawLine(Color.White, transformPoint(p1), transformPoint(p2), 3f)
                                }
                            }
                        }

                        val leftShoulder = if (posePoints.size > 11) posePoints[11] else null
                        val rightShoulder = if (posePoints.size > 12) posePoints[12] else null
                        val leftHip = if (posePoints.size > 23) posePoints[23] else null
                        val rightHip = if (posePoints.size > 24) posePoints[24] else null

                        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null &&
                            leftShoulder.confidence > 0.5f && rightShoulder.confidence > 0.5f &&
                            leftHip.confidence > 0.5f && rightHip.confidence > 0.5f) {

                            val cogX = (leftHip.x + rightHip.x) / 2
                            val cogY = (leftHip.y + rightHip.y) / 2
                            val cogPoint = com.nordicwalk.feature.video.presentation.PosePoint(cogX, cogY, 1.0f)

                            val shoulderX = (leftShoulder.x + rightShoulder.x) / 2
                            val shoulderY = (leftShoulder.y + rightShoulder.y) / 2
                            val shoulderCenterPoint = com.nordicwalk.feature.video.presentation.PosePoint(shoulderX, shoulderY, 1.0f)

                            val screenCOG = transformPoint(cogPoint)
                            val screenShoulderCenter = transformPoint(shoulderCenterPoint)

                            drawLine(
                                color = Color(0xFFFF9800),
                                start = screenShoulderCenter,
                                end = screenCOG,
                                strokeWidth = 6f
                            )

                            var maxFootY = screenCOG.y + 100f
                            for (i in 27..32) {
                                if (i < posePoints.size && posePoints[i].confidence > 0.3f) {
                                    val footScreenPos = transformPoint(posePoints[i])
                                    if (footScreenPos.y > maxFootY) {
                                        maxFootY = footScreenPos.y
                                    }
                                }
                            }
                            if (maxFootY < screenCOG.y + 50f) {
                                maxFootY = size.height
                            }

                            drawLine(
                                color = Color(0xFFFF9800),
                                start = screenCOG,
                                end = Offset(screenCOG.x, maxFootY),
                                strokeWidth = 4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )

                            drawCircle(
                                color = Color(0xFF800080),
                                radius = 10f,
                                center = screenCOG
                            )
                        }

                        for (i in 11 until posePoints.size) {
                            val point = posePoints[i]
                            if (point.confidence > 0.5f) {
                                drawCircle(Color.Yellow, 6f, transformPoint(point))
                            }
                        }
                    }
                }
            }
        }

        // Control Panel
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            if (duration > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), color = Color.White)
                    Text(formatTime(duration), color = Color.White)
                }
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = {
                        exoPlayer.seekTo(it.toLong())
                        currentPosition = it.toLong()
                        viewModel.analyzeFrameAt(it.toLong())
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.Green)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { speed ->
                    Button(
                        onClick = {
                            currentSpeed = speed
                            exoPlayer.playbackParameters = PlaybackParameters(speed)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentSpeed == speed) Color(0xFF4CAF50) else Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f).padding(4.dp).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("${speed}x")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.seekTo(exoPlayer.currentPosition - 33) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "後退", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() }) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "播放", tint = Color.White, modifier = Modifier.size(60.dp))
                }
                IconButton(onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 33) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "前進", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

// 修正：將 formatTime 放在檔案最下方，確保可被呼叫
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
