package com.siaka.ui.screens.onboarding

import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.siaka.R
import com.siaka.ui.theme.Primary
import com.siaka.ui.theme.PrimaryDark
import com.siaka.ui.theme.PrimaryLight
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val title: String,
    val description: String,
    val videoRes: Int? = null,
    val isLastPage: Boolean = false
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var isNavigatingAway by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPage(
            title = "Endless Discovery",
            description = "Generate unique, randomized loops starting and ending at your location. Never ride the same path twice."
        ),
        OnboardingPage(
            title = "Precision Navigation",
            description = "Never miss a turn with our high-contrast, real-time guidance designed for the open road."
        ),
        OnboardingPage(
            title = "Plan Your Perfect Ride.",
            description = "Discover optimized routes tailored to your distance and terrain preferences. Experience precision navigation designed for the focused athlete.",
            isLastPage = true
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val handleFinish = {
        if (!isNavigatingAway) {
            isNavigatingAway = true
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isNavigatingAway
        ) { position ->
            val page = pages[position]
            when {
                page.isLastPage -> LastOnboardingPage(page, handleFinish, isNavigatingAway)
                page.title == "Precision Navigation" -> PrecisionNavigationPage(page)
                else -> StandardOnboardingPage(page)
            }
        }

        // Bottom Controls for standard pages
        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1 && !isNavigatingAway,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = handleFinish) {
                    Text(
                        text = "Skip",
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                PagerIndicator(
                    pageSize = pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(min = 140.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp)
                ) {
                    Text(
                        "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = "Arrow right",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StandardOnboardingPage(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Decorative circular dash-lines background
            Canvas(modifier = Modifier.size(360.dp)) {
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = size.width / 2,
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // Modern, flat card container
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Animation Layer
                when (page.title) {
                    "Endless Discovery" -> EndlessDiscoveryAnimation()
                    else -> {
                        if (page.videoRes != null) {
                            LoopingVideoPlayer(videoResId = page.videoRes)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = Primary.copy(alpha = 0.05f)
                            )
                        }
                    }
                }

                // Stats Bar Overlay (Specific to the Endless Discovery page)
                if (page.title == "Endless Discovery") {
                    RouteStatsOverlay(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun EndlessDiscoveryAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "discovery")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.size(240.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width * 0.35f

        // Generate a wavy path inspired by the screenshot
        val path = Path().apply {
            val points = 120
            for (i in 0..points) {
                val angle = (i.toFloat() / points) * 2 * PI.toFloat()
                // Combinatorial waves for "organic" randomness
                val wave = 12f * sin(angle * 6f) + 8f * cos(angle * 11f) + 5f * sin(angle * 3f)
                val r = baseRadius + wave
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        // 1. Draw the loop with a subtle gradient trail
        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF00B4DB), // Light Blue
                    Color(0xFF0083B0), // Cyan
                    Color(0xFF0052D4), // Royal Blue
                    Color(0xFF00B4DB)
                ),
                center = center
            ),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // 2. Position the dot using PathMeasure
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, true)
        val pos = pathMeasure.getPosition(pathMeasure.length * progress)

        // 3. Draw the outer glow/halo for the indicator
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00B4DB).copy(alpha = 0.3f), Color.Transparent),
                center = pos,
                radius = 40f
            ),
            radius = 40f,
            center = pos
        )

        // 4. Draw the active navigation dot
        drawCircle(
            color = Color(0xFF0052D4),
            radius = 12f,
            center = pos
        )

        // Add a small white highlight on the dot for depth
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = 4f,
            center = Offset(pos.x - 3f, pos.y - 3f)
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LoopingVideoPlayer(videoResId: Int) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val rawUri = "android.resource://${context.packageName}/$videoResId".toUri()
            setMediaItem(MediaItem.fromUri(rawUri))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PrecisionNavigationAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Map Background
        Image(
            painter = painterResource(id = R.drawable.route_map),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Fade at the bottom of the map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            0.95f to Color.White,
                            1.0f to Color.White
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation Instruction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "50m",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Turn Right onto Uhuru Highway",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // Distance Chip
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.route_filled),
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "3.4 km", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Navigation Dot
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp, x = (-10).dp)
        ) {
            // Glow
            Canvas(modifier = Modifier.size(40.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = size.width / 2
                    ),
                    radius = size.width / 2
                )
            }
            // Dot
            Surface(
                modifier = Modifier.size(16.dp).align(Alignment.Center),
                shape = CircleShape,
                color = Primary,
                border = BorderStroke(2.dp, Color.White)
            ) {}
        }
    }
}

@Composable
fun RouteStatsOverlay(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEBECEF))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Distance Stat
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = "Distance",
                tint = PrimaryDark,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "24.5 km",
                color = Primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        // Elevation Stat
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//                imageVector = Icons.Default.Terrain,
//                contentDescription = "Elevation",
//                tint = PrimaryDark,
//                modifier = Modifier.size(18.dp)
//            )
//            Spacer(Modifier.width(8.dp))
//            Text(
//                text = "320m",
//                color = Primary,
//                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
//            )
//        }
    }
}

@Composable
fun PrecisionNavigationPage(page: OnboardingPage) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Map and Overlays (Top area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
        ) {
            PrecisionNavigationAnimation()
        }

        // Text Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(120.dp)) // Space for bottom controls
        }
    }
}

@Composable
fun LastOnboardingPage(page: OnboardingPage, onFinish: () -> Unit, isNavigating: Boolean = false) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Image(
            painter = painterResource(id = R.drawable.onboard),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (isNavigating) 0.8f else 1f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.4f to Color.Transparent,
                            0.7f to Color.White,
                            1.0f to Color.White
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 36.sp),
                color = PrimaryDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !isNavigating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isNavigating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_right),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !isNavigating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PagerIndicator(pageSize: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageSize) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isSelected) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Primary else Color.LightGray)
            )
        }
    }
}

//Preview
@Preview(showBackground = true)
@Composable
fun PagerIndicatorPreview() {
    OnboardingScreen {}
}
