package com.example.ui.screens.ar

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Constellation
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArSkyMapScreen(
    viewModel: MainViewModel,
    onNavigateToJournal: (String) -> Unit, // passes the selected constellation name to auto-populate form!
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPlaceIndex by viewModel.selectedLocationIndex.collectAsState()
    val placeName by viewModel.currentLocationName.collectAsState()
    val weatherData by viewModel.currentWeatherData.collectAsState()
    val geminiTip by viewModel.recommendationTip.collectAsState()
    val isLoadingTip by viewModel.isLoadingTip.collectAsState()
    
    val isArOn by viewModel.isArCameraOn.collectAsState()
    val dragX by viewModel.skyMapDragX.collectAsState()
    val dragY by viewModel.skyMapDragY.collectAsState()

    var showPlaceSelector by remember { mutableStateOf(false) }
    var selectedConstellation by remember { mutableStateOf<Constellation?>(null) }

    // Accompanist Permission State
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize().background(DeepSpaceBlack)) {
        
        // 1. Core Background: Camera Stream (AR Mode) or Deep Space Nebula Gradient
        if (isArOn && cameraPermissionState.status.isGranted) {
            CameraPreview(modifier = Modifier.fillMaxSize())
            // Semi-transparent blue scrim overlay to make starry grid clearly legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x7703030F), Color(0x990C0D21))))
            )
        } else {
            // High-fidelity nebulous background using color gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NebulaDeepPurple, DeepSpaceBlack),
                            center = Offset(300f, 400f),
                            radius = 1200f
                        )
                    )
            )
        }

        // 2. Interactive Interactive Celestial Sky-Map (Custom Canvas)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.skyMapDragX.value += dragAmount.x
                        viewModel.skyMapDragY.value += dragAmount.y
                    }
                }
        ) {
            val textMeasurer = rememberTextMeasurer()
            
            val density = androidx.compose.ui.platform.LocalDensity.current
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val centerX = screenWidthPx / 2f + dragX
            val centerY = screenHeightPx / 2f + dragY

            Canvas(modifier = Modifier.fillMaxSize()) {

                // Draw starry space grid lines (RA & Declination coordinates)
                val gridColor = Color(0x224FF3FF)
                // Concentric circles
                drawCircle(color = gridColor, radius = 200f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                drawCircle(color = gridColor, radius = 400f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
                drawCircle(color = gridColor, radius = 600f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                drawCircle(color = gridColor, radius = 800f, center = Offset(centerX, centerY), style = androidx.compose.ui.graphics.drawscope.Stroke(0.5f))

                // Radial coordinate lines
                for (angle in 0 until 360 step 45) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val endX = centerX + 1000f * cos(angleRad).toFloat()
                    val endY = centerY + 1000f * sin(angleRad).toFloat()
                    drawLine(color = gridColor, start = Offset(centerX, centerY), end = Offset(endX, endY), strokeWidth = 1f)
                }

                // PLOT CONSTELLATIONS
                Constellation.ALL.forEach { constellation ->
                    // Base positioning in our coordinate space
                    val constX = centerX + constellation.xOffset * 600f
                    val constY = centerY + constellation.yOffset * 600f

                    // Draw connecting lines (stars cluster simulation)
                    val lineColor = if (selectedConstellation == constellation) CosmicCyan else Color(0x55FFFFFF)
                    val strokeW = if (selectedConstellation == constellation) 3.dp.toPx() else 1.5.dp.toPx()

                    // Each constellation draws a custom shape
                    val starOffsets = getConstellationStarOffsets(constellation.name)
                    val starPositions = starOffsets.map { Offset(constX + it.x, constY + it.y) }

                    // Connect lines in sequence to design actual geometric constellation lines!
                    for (i in 0 until starPositions.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = starPositions[i],
                            end = starPositions[i + 1],
                            strokeWidth = strokeW
                        )
                    }
                    if (starPositions.size > 2) {
                        // Close loop if needed depending on constellation
                        drawLine(
                            color = lineColor,
                            start = starPositions.last(),
                            end = starPositions.first(),
                            strokeWidth = strokeW
                        )
                    }

                    // Render individual glowing star points
                    starPositions.forEachIndexed { idx, pos ->
                        val radius = if (idx == 0) 7.dp.toPx() else 4.dp.toPx()
                        val starColor = if (idx == 0) StarlightGold else StarWhite
                        
                        // Outer glow
                        drawCircle(
                            color = starColor.copy(alpha = 0.4f),
                            radius = radius * 2.5f,
                            center = pos
                        )
                        // Star center Core
                        drawCircle(
                            color = starColor,
                            radius = radius,
                            center = pos
                        )
                    }

                    // Render text label above primary star
                    if (starPositions.isNotEmpty()) {
                        val textPos = starPositions.first()
                        val textStyle = TextStyle(
                            color = StarWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(color = Color.Black, blurRadius = 8f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = constellation.name,
                            style = textStyle,
                            topLeft = Offset(textPos.x - 40f, textPos.y - 45f)
                        )
                    }
                }
            }
            
            // Interactive click overlays for constellations
            Constellation.ALL.forEach { constellation ->
                val constX = centerX + constellation.xOffset * 600f
                val constY = centerY + constellation.yOffset * 600f
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .offset(
                            x = (constX - 55f).dp / 2.5f, // scale mapping adjustment
                            y = (constY - 55f).dp / 2.5f
                        )
                        .clip(CircleShape)
                        .clickable { selectedConstellation = constellation }
                )
            }
        }

        // 3. Header Toolbar: Location dropdown & Weather panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 45.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showPlaceSelector = !showPlaceSelector }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = CosmicCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = placeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StarWhite
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand", tint = StarWhite)
                        }

                        // Sync button for offline syncing highlight!
                        IconButton(
                            onClick = { viewModel.syncLogsOffline() },
                            modifier = Modifier.testTag("log_sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = if (viewModel.unsyncedCount.collectAsState().value > 0) StarlightGold else StarWhite
                            )
                        }
                    }

                    // Embedded Weather overview!
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weatherData?.let { data ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = "Cloudiness", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "구름 ${data.cloudCover.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.DeviceThermostat, contentDescription = "Temperature", tint = NebulaPink, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${data.temperature}°C",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                            
                            val stargazingScore = (100 - data.cloudCover).toInt()
                            val scoreStr = when {
                                stargazingScore >= 80 -> "매우 좋음 🌠"
                                stargazingScore >= 50 -> "보통 ⭐️"
                                else -> "흐림 ☁️"
                            }
                            Text(
                                text = "관측지수 $stargazingScore% ($scoreStr)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (stargazingScore >= 70) CosmicCyan else TextSecondary
                            )
                        } ?: Text(
                            text = "실시간 위성 대기 상태 로딩 중...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Suggested Gemini recommendation banner
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = CardBackground.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Cosmic Tip",
                        tint = StarlightGold,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    if (isLoadingTip) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CosmicCyan, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = geminiTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = StarWhite,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Location Selector menu
        if (showPlaceSelector) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showPlaceSelector = false }
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 180.dp, start = 16.dp, end = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("관측 위치 선택", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StarWhite)
                        Spacer(modifier = Modifier.height(8.dp))
                        val spots = listOf("내 GPS 위치", "강릉 안반데기 (해발 1100m)", "화악산 쌈지공원 (해발 860m)", "양평 벗고개 (터널 포인트)", "함백산 만항재 (해발 1330m)")
                        spots.forEachIndexed { index, spot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.changeLocation(index)
                                        showPlaceSelector = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(spot, color = if (currentPlaceIndex == index) CosmicCyan else StarWhite, fontWeight = if (currentPlaceIndex == index) FontWeight.Bold else FontWeight.Normal)
                                if (currentPlaceIndex == index) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = CosmicCyan)
                                }
                            }
                            if (index < spots.size - 1) {
                                HorizontalDivider(color = Color(0x11FFFFFF))
                            }
                        }
                    }
                }
            }
        }

        // 4. FLOATING CONSOLE: AR Mode triggers & Instruction guide
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AR mode Toggle Button!
                FloatingActionButton(
                    onClick = {
                        if (!isArOn) {
                            if (cameraPermissionState.status.isGranted) {
                                viewModel.isArCameraOn.value = true
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        } else {
                            viewModel.isArCameraOn.value = false
                        }
                    },
                    containerColor = if (isArOn) CosmicCyan else CosmicSlate,
                    contentColor = if (isArOn) DeepSpaceBlack else StarWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isArOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "AR Switch"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArOn) "AR 카메라 ON" else "AR 모드 켜기",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Reset Orientation Pan offsets
                IconButton(
                    onClick = {
                        viewModel.skyMapDragX.value = 0f
                        viewModel.skyMapDragY.value = 0f
                        Toast.makeText(context, "별자리 지도를 중앙으로 맞췄습니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.background(CardBackground, CircleShape)
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recenter", tint = StarWhite)
                }
            }
        }

        // 5. Constellation Sheet Dialog overlay when clicked!
        AnimatedVisibility(
            visible = selectedConstellation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 78.dp, start = 8.dp, end = 8.dp)
        ) {
            selectedConstellation?.let { constellation ->
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CosmicCyan, NebulaPink))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = constellation.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(constellation.bestSeason + "철 관측", color = StarlightGold, fontSize = 11.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = CosmicSlate)
                                    )
                                }
                                Text(
                                    text = "주성(가장 밝은 별): ${constellation.brightestStar} | 약어: ${constellation.abbreviation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { selectedConstellation = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = constellation.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarWhite,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = CosmicSlate,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoStories, contentDescription = "Myth", tint = StarlightGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("그리스 로마 신화", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = constellation.mythology,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Write Journal log prompt for this specific constellation!
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onNavigateToJournal(constellation.name)
                                selectedConstellation = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = "Write log", tint = DeepSpaceBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("이 별자리 관측 일지 쓰기 ✍️", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Coordinate mapping parameters to simulate celestial constellations on Canvas cleanly
private fun getConstellationStarOffsets(name: String): List<Offset> {
    return when (name) {
        "오리온자리" -> listOf(
            Offset(-30f, -60f), Offset(30f, -50f), // shoulder stars (Betelgeuse & Bellatrix)
            Offset(0f, -10f), // belt central stars (Mintaka/Alnilam/Alnitak)
            Offset(-15f, -10f), Offset(15f, -10f),
            Offset(-25f, 50f), Offset(25f, 60f) // feet stars (Saiph & Rigel)
        )
        "큰곰자리" -> listOf(
            Offset(-80f, 30f), Offset(-40f, 25f), Offset(-10f, 15f), // handle points of dipper
            Offset(20f, 10f), Offset(30f, -20f), Offset(70f, -25f), Offset(60f, 15f) // bowl points
        )
        "카시오페아자리" -> listOf(
            Offset(-70f, -20f), Offset(-35f, 20f), Offset(0f, -15f), Offset(35f, 22f), Offset(70f, -10f) // W shape
        )
        "황소자리" -> listOf(
            Offset(-50f, -40f), Offset(-10f, -10f), Offset(40f, 10f), Offset(60f, -20f), Offset(20f, -50f)
        )
        "사자자리" -> listOf(
            Offset(-40f, 30f), Offset(-20f, -10f), Offset(-10f, -40f), Offset(20f, -40f), Offset(30f, -10f), Offset(10f, 30f)
        )
        "백조자리" -> listOf(
            Offset(0f, -70f), Offset(0f, 30f), // spine line
            Offset(-60f, -10f), Offset(60f, -10f) // cross wing
        )
        "전갈자리" -> listOf(
            Offset(-50f, -40f), Offset(-15f, -20f), Offset(10f, 0f), Offset(25f, 30f), Offset(15f, 60f), Offset(-15f, 50f)
        )
        else -> listOf(
            Offset(-20f, -20f), Offset(20f, -20f), Offset(20f, 20f), Offset(-20f, 20f)
        )
    }
}
