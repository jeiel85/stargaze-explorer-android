package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.community.getAvatarIconResource
import com.example.ui.screens.community.getProfileTypeKorean
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val isAlertOn by viewModel.isAlertEnabled.collectAsState()
    val alertTime by viewModel.selectedAlertHour.collectAsState()
    val notificationsSent by viewModel.notificationSentCount.collectAsState()
    val currentPlaceName by viewModel.currentLocationName.collectAsState()
    val weatherData by viewModel.currentWeatherData.collectAsState()
    val astEvents by viewModel.astronomicalEvents.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf(profile.nickname) }
    var editAvatarKey by remember { mutableStateOf(profile.avatar) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 45.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Title
            item {
                Column {
                    Text(
                        text = "관측 알림 & 프로필",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "지구계 성좌 알림 비축 및 회원 프로필 설정",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 1. Interactive USER PROFILE CARD with badges
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(CosmicSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getAvatarIconResource(profile.avatar),
                                        contentDescription = "User avatar",
                                        tint = CosmicCyan,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = profile.nickname,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    Text(
                                        text = profile.title,
                                        fontSize = 12.sp,
                                        color = CosmicCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Social login mock button triggers setup Dialogue!
                            IconButton(
                                onClick = {
                                    editNickname = profile.nickname
                                    editAvatarKey = profile.avatar
                                    showEditProfile = true
                                },
                                modifier = Modifier.background(CosmicSlate, CircleShape)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = StarWhite, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = Color(0x0aFFFFFF))
                        Spacer(modifier = Modifier.height(12.dp))

                        // BADGE ACHIEVEMENTS SECTION (Dynamically computed from Room data counts!)
                        val loggedCount = logs.size
                        Text(
                            text = "나의 관측 탐사 업적 🎖️",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StarlightGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Badge 1: Starter
                            BadgeTag(
                                title = "첫 관측 개시 💫",
                                isUnlocked = loggedCount > 0,
                                modifier = Modifier.weight(1f)
                            )
                            // Badge 2: Collector
                            BadgeTag(
                                title = "은하수 탐색꾼 🏆",
                                isUnlocked = loggedCount >= 3,
                                modifier = Modifier.weight(1f)
                            )
                            // Badge 3: Master (has local Anbandegi log)
                            val hasAnbandegiLog = logs.any { it.location.contains("안반데기") || it.location.contains("강릉") }
                            BadgeTag(
                                title = "안반개척자 🏔️",
                                isUnlocked = hasAnbandegiLog,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. LOCATION-BASED PUSH NOTIFICATION CONSOLE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "위치 기반 관측 푸시 알림 설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CosmicCyan
                        )
                        Text(
                            text = "관측 예정 장소의 기상/구름 상태를 실시간 감지하여 알림을 전달합니다.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("별자리 관측 시간 알림 설정", fontSize = 14.sp, color = StarWhite, fontWeight = FontWeight.Bold)
                                Text("현 위치: $currentPlaceName", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isArOn(isAlertOn),
                                onCheckedChange = { viewModel.isAlertEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CosmicCyan, checkedTrackColor = CosmicSlate)
                            )
                        }

                        // Notification alert hour config dropdown / slider
                        AnimatedVisibility(visible = isAlertOn) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "알림 송신 목표 시간대: 매일 밤 $alertTime 시 정각",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Slider(
                                    value = alertTime.toFloat(),
                                    onValueChange = { viewModel.selectedAlertHour.value = it.toInt() },
                                    valueRange = 18f..24f,
                                    steps = 5,
                                    colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan)
                                )

                                HorizontalDivider(color = Color(0x0aFFFFFF), modifier = Modifier.padding(vertical = 12.dp))
                                
                                // Dispatch periodic reminders simulate choices
                                Text("푸시 감지 수신 주기", fontSize = 12.sp, color = StarlightGold, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                val cycleOptions = listOf("주말 밤 한정 ⏰", "맑음 조건 감지시 🌌", "매일 밤 10시 🔔")
                                var selectedCycle by remember { mutableStateOf("맑음 조건 감지시 🌌") }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    cycleOptions.forEach { cycle ->
                                        FilterChip(
                                            selected = selectedCycle == cycle,
                                            onClick = { selectedCycle = cycle },
                                            label = { Text(cycle, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CosmicCyan,
                                                selectedLabelColor = DeepSpaceBlack,
                                                labelColor = StarWhite
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // REAL TIME DEVICE NOTIFICATION SIMULATOR IN THE ANDROID DRAWER!
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                val cloudCover = weatherData?.cloudCover ?: 15.0
                                val clouds = cloudCover.toInt()
                                val suitability = if (clouds < 30) "최상! ✨" else "보통 ⭐️"
                                val bodyTxt = "[$currentPlaceName] 구름량 현재 $clouds%로 관측 적합도 $suitability 빨리 나가 별자리를 점검해보세요!"
                                
                                viewModel.testStargazingAlert(
                                    title = "🌌 Stargazer 실시간 관측 적합 알림!",
                                    message = bodyTxt
                                )
                                Toast.makeText(context, "실시간 위치 기반 푸시 알림이 상단 알림창으로 발송되었습니다!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth().testTag("push_alert_test_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Alert Trigger", tint = DeepSpaceBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("실시간 관측 알림 테스트 발송 🔔", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (notificationsSent > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 총 ${notificationsSent}회의 테스트 강제 푸시 알림이 상단 표시줄에 연동 완료되었습니다.",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            // 3. SPECIAL CELESTIAL EVENTS & NOTIFICATION SUBSCRIPTIONS (MANDATORY LOCAL EXPERT)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "☄️ 특별 천문 예경 & 희귀 우주 쇼 예약",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarlightGold
                        )
                        Text(
                            text = "유성우 극대기, 일식, 월식 등 일생에 몇 번 없는 특별한 우주 천격 이벤트를 연동하여 미리 푸시 알림을 예약하세요.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        astEvents.forEach { event ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSlate),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .border(1.dp, if (event.isSubscribed) CosmicCyan.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = when (event.type) {
                                                    "유성우" -> Color(0xFF2E1A47)
                                                    "월식" -> Color(0xFF4C1515)
                                                    "일식" -> Color(0xFF5A3010)
                                                    "행성합" -> Color(0xFF133F3F)
                                                    else -> Color(0xFF1B325F)
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = event.type,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (event.type) {
                                                        "유성우" -> CosmicCyan
                                                        "월식" -> Color(0xFFFF6B6B)
                                                        "일식" -> Color(0xFFFFB84D)
                                                        "행성합" -> CosmicCyan
                                                        else -> StarWhite
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = event.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StarWhite
                                            )
                                            if (event.id == 1 || event.id == 2) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = CosmicCyan.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, CosmicCyan.copy(alpha = 0.6f))
                                                ) {
                                                    Text(
                                                        text = "🔥 이달의 우주쇼!",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CosmicCyan,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = event.date,
                                            fontSize = 10.sp,
                                            color = CosmicCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = event.description,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Subscribe Toggle Button
                                        Button(
                                            onClick = { viewModel.toggleEventSubscription(event.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (event.isSubscribed) Color(0xFF135A3F) else Color(0x15FFFFFF),
                                                contentColor = if (event.isSubscribed) StarWhite else CosmicCyan
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (event.isSubscribed) Icons.Default.CheckCircle else Icons.Default.AddAlert,
                                                    contentDescription = "Subscribe icon",
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (event.isSubscribed) "예약 완료 🟢" else "알림 예약 🔔",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Test Push Trigger Button
                                        OutlinedButton(
                                            onClick = { viewModel.triggerEventImmediateAlert(event.id) },
                                            border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicCyan),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "Immediate Release Send icon",
                                                    tint = CosmicCyan,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "체험 푸시 ⚡️",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. EDIT PROFILE DIALOG OVERLAY (Social mock logins!)
        if (showEditProfile) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showEditProfile = false }
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("소셜 로그인 및 프로필 편집 👤", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StarWhite)
                            IconButton(onClick = { showEditProfile = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                            }
                        }

                        // Nickname field
                        Column {
                            Text("모험가 닉네임 입력", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = editNickname,
                                onValueChange = { editNickname = it },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite),
                                modifier = Modifier.fillMaxWidth().testTag("profile_nickname_field")
                            )
                        }

                        // Avatar switcher
                        Column {
                            Text("천문학 프로필 등급 전리품", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            val avOptions = listOf("nebula" to "성운 지휘자", "comet" to "혜성 추적자", "supernova" to "초신성 대가")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                avOptions.forEach { (avatar, text) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { editAvatarKey = avatar }
                                            .background(if (editAvatarKey == avatar) CosmicCyan else CosmicSlate)
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = getAvatarIconResource(avatar),
                                                contentDescription = text,
                                                tint = if (editAvatarKey == avatar) DeepSpaceBlack else StarWhite,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text, fontSize = 8.sp, color = if (editAvatarKey == avatar) DeepSpaceBlack else StarWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (editNickname.trim().isEmpty()) {
                                    Toast.makeText(context, "사용하실 닉네임을 입력하세요!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.handleSocialLogin(editNickname, editAvatarKey)
                                Toast.makeText(context, "성공적으로 프로필이 동기화 되었습니다!", Toast.LENGTH_SHORT).show()
                                showEditProfile = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("동기화 로그인 완료", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeTag(
    title: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isUnlocked) CosmicSlate else Color(0x05FFFFFF),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isUnlocked) CosmicCyan.copy(alpha = 0.5f) else Color(0x05FFFFFF)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) StarWhite else TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isUnlocked) "달성함 ✨" else "미달성 🔒",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) CosmicCyan else TextSecondary
            )
        }
    }
}

fun isArOn(b: Boolean): Boolean = b
