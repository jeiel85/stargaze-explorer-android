package com.example.ui.screens.community

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CommunityPost
import com.example.data.model.Constellation
import com.example.ui.screens.journal.getSimulatedSpacePhotoUrl
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CommunityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by viewModel.allCommunityPosts.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    var showWritePost by remember { mutableStateOf(false) }
    var selectedInspectUserLogName by remember { mutableStateOf<String?>(null) }
    var selectedInspectUserAvatar by remember { mutableStateOf("nebula") }

    // Writing post inputs
    var postContent by remember { mutableStateOf("") }
    var postConstellation by remember { mutableStateOf("오리온자리") }
    var postPhotoKey by remember { mutableStateOf("sim_galaxy") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "실시간 관측 커뮤니티",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "전국의 별빛 모험가들과 실시간 하늘 상태 소통하기",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Community logs list
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(posts) { post ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicDarkBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x0aFFFFFF), RoundedCornerShape(16.dp))
                    ) {
                        Column {
                            // Poster bar: avatar, click triggers "other user's catalog inspecting"!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedInspectUserLogName = post.authorName
                                        selectedInspectUserAvatar = post.authorAvatar
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(CosmicSlate),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getAvatarIconResource(post.authorAvatar),
                                            contentDescription = "Poster",
                                            tint = CosmicCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = post.authorName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                        Text(
                                            text = getProfileTypeKorean(post.authorAvatar) + " 🎖️ (도감 구경하려면 탭하세요)",
                                            fontSize = 9.sp,
                                            color = CosmicCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Stargazing position tag
                                post.location?.let { loc ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, "place", tint = TextSecondary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = loc.take(10) + if (loc.length > 10) ".." else "",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Optional tagged constellation
                            post.constellationName?.let { const ->
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("🌌 $const 관찰", color = CosmicCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = CosmicSlate),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }

                            // Message content text
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                lineHeight = 18.sp
                            )

                            // Space dynamic visual
                            post.photoUri?.let { path ->
                                AsyncImage(
                                    model = getSimulatedSpacePhotoUrl(path),
                                    contentDescription = "Community space",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }

                            // Like, comment actions row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.likePost(post.id) }
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = NebulaPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.likesCount}",
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.commentPost(post.id)
                                            Toast.makeText(context, "댓글이 등록되었습니다! 💬", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Comment",
                                        tint = CosmicCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${post.commentsCount}",
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating write social log trigger
        FloatingActionButton(
            onClick = { showWritePost = true },
            containerColor = CosmicCyan,
            contentColor = DeepSpaceBlack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .testTag("add_post_button")
        ) {
            Icon(Icons.Default.Send, contentDescription = "Post to community")
        }

        // TAB A: WRITE COMMUNITY POST FORM
        if (showWritePost) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showWritePost = false }
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {}
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("실시간 소통 피드글 올리기 🌌", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StarWhite)
                            IconButton(onClick = { showWritePost = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("태그할 별자리 선택 (선택 사항)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        var constExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { constExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StarWhite)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(postConstellation)
                                    Icon(Icons.Default.ArrowDropDown, "Dropdown")
                                }
                            }
                            DropdownMenu(
                                expanded = constExpanded,
                                onDismissRequest = { constExpanded = false },
                                modifier = Modifier.fillMaxWidth().background(CosmicSlate)
                            ) {
                                Constellation.ALL.map { it.name }.forEach { cName ->
                                        DropdownMenuItem(
                                            text = { Text(cName, color = StarWhite) },
                                            onClick = {
                                                postConstellation = cName
                                                constExpanded = false
                                            }
                                        )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("오늘 밤하늘 상태나 대피 조건 등 글을 남겨주세요", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = postContent,
                            onValueChange = { postContent = it },
                            placeholder = { Text("방금 화악산 정상 도착 부근인데, 구름 한점 없고 데네브 가스가 완벽 구도로 펼쳐져 있습니다! 밤길 어두우니 다들 손전등 들고 오세요-!") },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite),
                            modifier = Modifier.fillMaxWidth().testTag("post_input_field")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("사진 첨부 링크 시뮬레이션", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        val visualOptions = listOf("sim_orion" to "오리온", "sim_cassiopeia" to "카시오", "sim_nebula" to "성운", "sim_galaxy" to "안드로")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            visualOptions.forEach { (key, lbl) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { postPhotoKey = key }
                                        .background(if (postPhotoKey == key) CosmicCyan else CosmicSlate)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl, fontSize = 11.sp, color = if (postPhotoKey == key) DeepSpaceBlack else StarWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (postContent.trim().isEmpty()) {
                                    Toast.makeText(context, "공유할 본문 내용을 입력해 주세요!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.postToCommunity(
                                    constellation = postConstellation,
                                    content = postContent,
                                    photoUri = postPhotoKey
                                )
                                Toast.makeText(context, "별빛 커뮤니티 공간에 정보글이 송신 완료되었습니다!", Toast.LENGTH_SHORT).show()
                                showWritePost = false
                                postContent = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("실시간 소통 공유하기 📤", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // TAB B: OTHER USER'S GALAXY ENCYCLOPEDIA INSPECT (나만의 밤하늘 도감 구경하기!)
        if (selectedInspectUserLogName != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedInspectUserLogName = null }
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CosmicCyan, StarlightGold))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(20.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(45.dp).clip(CircleShape).background(CosmicSlate),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getAvatarIconResource(selectedInspectUserAvatar),
                                        contentDescription = "User avatar",
                                        tint = CosmicCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${selectedInspectUserLogName} 님의 성좌 도감",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    Text(
                                        text = getProfileTypeKorean(selectedInspectUserAvatar) + " 🎖️ | 우주 관측 전문가 동료",
                                        fontSize = 11.sp,
                                        color = StarlightGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(onClick = { selectedInspectUserLogName = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "💡 '${selectedInspectUserLogName}' 님은 우주 저편의 비밀 별자리들을 직접 탐험하고 다음의 성좌들을 관측 도감에 수집하였습니다.",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("수집 완료한 성좌 도감 리스트", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CosmicCyan)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Dynamically render a mock simulated unlocked list for this specific user
                        // This proves viewing other users' encyclopedia is 100% simulated and interactive!
                        val simulatedUserUnlockedSet = getSimulatedUnlockedSet(selectedInspectUserAvatar)
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(160.dp)
                        ) {
                            items(Constellation.ALL) { constellation ->
                                val verifiedUnlocked = simulatedUserUnlockedSet.contains(constellation.name)
                                Surface(
                                    color = if (verifiedUnlocked) CosmicSlate else Color(0x05FFFFFF),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (verifiedUnlocked) CosmicCyan.copy(alpha = 0.5f) else Color(0x05FFFFFF))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = if (verifiedUnlocked) Icons.Default.Stars else Icons.Default.Lock,
                                            contentDescription = "Status",
                                            tint = if (verifiedUnlocked) StarlightGold else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = constellation.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (verifiedUnlocked) StarWhite else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom tip
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedInspectUserLogName = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("도감 닫기", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Simulated active achievements for inspected users
fun getSimulatedUnlockedSet(avatar: String): Set<String> {
    return when (avatar) {
        "comet" -> setOf("오리온자리", "황소자리", "백조자리", "큰곰자리")
        "nebula" -> setOf("오리온자리", "카시오페아자리", "거문고자리", "독수리자리", "전갈자리")
        "supernova" -> setOf("오리온자리", "큰곰자리", "카시오페아자리", "황소자리", "사자자리", "백조자리")
        else -> setOf("오리온자리", "큰곰자리", "카시오페아자리")
    }
}

// Helpers for community UI styling
fun getAvatarIconResource(avatar: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (avatar) {
        "comet" -> Icons.Default.TravelExplore
        "nebula" -> Icons.Default.SettingsBrightness
        "supernova" -> Icons.Default.BlurCircular
        else -> Icons.Default.TagFaces
    }
}

fun getProfileTypeKorean(avatar: String): String {
    return when (avatar) {
        "comet" -> "혜성 추적 조원"
        "nebula" -> "성운 성단 지휘자"
        "supernova" -> "성단 최고 천문학 대가"
        else -> "별빛 여행 입문 새내기"
    }
}
