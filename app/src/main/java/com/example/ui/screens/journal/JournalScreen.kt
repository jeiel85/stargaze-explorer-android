package com.example.ui.screens.journal

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Constellation
import com.example.data.model.ObservationLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun JournalScreen(
    viewModel: MainViewModel,
    preSelectedConstellationName: String?,
    onClearPreSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0: 도감 (Encyclopedia), 1: 일기장 (Photo Journal Logs)
    val logs by viewModel.allLogs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Form inputs state
    var formConstellation by remember { mutableStateOf("오리온자리") }
    var formLocation by remember { mutableStateOf("가평 화악산 쌈지공원") }
    var formNotes by remember { mutableStateOf("") }
    var formRating by remember { mutableStateOf(5f) }
    var formWeather by remember { mutableStateOf("맑음 🌌") }
    var formPhotoKey by remember { mutableStateOf("sim_nebula") } // default simulated photo

    // Sync preselected constellation from AR screen if any!
    LaunchedEffect(preSelectedConstellationName) {
        if (!preSelectedConstellationName.isNullOrEmpty()) {
            formConstellation = preSelectedConstellationName
            showForm = true
            onClearPreSelection()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "관측 기록장 & 도감",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "내가 모은 오프라인 밤하늘 은하 도감",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Sync control indicator
                if (unsyncedCount > 0) {
                    Button(
                        onClick = { viewModel.syncLogsOffline() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(35.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = DeepSpaceBlack, strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("동기화 중...", fontSize = 11.sp, color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = DeepSpaceBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("클라우드 동기화", fontSize = 11.sp, color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs controller
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = DeepSpaceBlack,
                contentColor = CosmicCyan,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("나만의 밤하늘 도감", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("관측 일기장 (${logs.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }

            // Tab contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (activeSubTab == 0) {
                    // TAB 1: ENCYCLOPEDIA GRID
                    val unlockedConstellations = logs.map { it.constellationName }.toSet()
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(Constellation.ALL) { constellation ->
                            val isUnlocked = unlockedConstellations.contains(constellation.name)
                            val matchingLog = logs.firstOrNull { it.constellationName == constellation.name }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUnlocked) CosmicSlate else CosmicDarkBlue.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isUnlocked) 1.5.dp else 1.dp,
                                        color = if (isUnlocked) CosmicCyan.copy(alpha = 0.6f) else Color(0x11FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column {
                                    // Header visual state
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0x334FF3FF), Color(0x66141332))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Visual cosmic illustration mapping placeholder
                                        AsyncImage(
                                            model = getSimulatedSpacePhotoUrl(if (isUnlocked) matchingLog?.photoUri ?: "sim_nebula" else "locked"),
                                            contentDescription = constellation.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        if (isUnlocked) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.3f))
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Stars,
                                                contentDescription = "Unlocked",
                                                tint = StarlightGold,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(20.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.8f))
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Details text
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = constellation.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isUnlocked) StarWhite else TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "밝은 별: ${constellation.brightestStar}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        
                                        if (isUnlocked && matchingLog != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = CosmicCyan, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = matchingLog.date,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CosmicCyan
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "미발견 별자리",
                                                fontSize = 11.sp,
                                                color = NebulaPink,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 2: OBSERVATION PHOTO DIARY LIST
                    if (logs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Empty Log",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(64.dp)
                                    .alpha(0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "관측 일기가 아직 존재하지 않습니다.",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "지도로 가서 별자리를 살펴보고 일기를 작성해보세요!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(logs) { log ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicDarkBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                ) {
                                    Column {
                                        // Top strip
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = log.constellationName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StarWhite
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    
                                                    // Sync Status tag
                                                    if (log.isSynced) {
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text("동기화됨", fontSize = 9.sp, color = CosmicCyan) },
                                                            leadingIcon = { Icon(Icons.Default.CloudQueue, "Synced", tint = CosmicCyan, modifier = Modifier.size(10.dp)) },
                                                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0x1100E5FF))
                                                        )
                                                    } else {
                                                        AssistChip(
                                                            onClick = { viewModel.syncLogsOffline() },
                                                            label = { Text("클라우드 대기", fontSize = 9.sp, color = StarlightGold) },
                                                            leadingIcon = { Icon(Icons.Default.CloudOff, "Unsynced", tint = StarlightGold, modifier = Modifier.size(10.dp)) },
                                                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0x11FFD54F))
                                                        )
                                                    }
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = TextSecondary, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(log.location, fontSize = 11.sp, color = TextSecondary)
                                                }
                                            }

                                            // Star rating representation
                                            Row {
                                                repeat(log.rating.toInt()) {
                                                    Icon(Icons.Default.Star, contentDescription = "Star", tint = StarlightGold, modifier = Modifier.size(14.dp))
                                                }
                                                repeat(5 - log.rating.toInt()) {
                                                    Icon(Icons.Default.StarBorder, contentDescription = "Star empty", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        // Photo block if present!
                                        log.photoUri?.let { photoUri ->
                                            AsyncImage(
                                                model = getSimulatedSpacePhotoUrl(photoUri),
                                                contentDescription = "My Star Obs",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(0.dp))
                                            )
                                        }

                                        // Journal Text Box
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = log.notes,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "관측 일시: ${log.date} ${log.time} | 날씨: ${log.weather}",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                                
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteObservationLog(log)
                                                        Toast.makeText(context, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NebulaPink, modifier = Modifier.size(16.dp))
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
        }

        // Floating Action Button to post observational logs
        FloatingActionButton(
            onClick = { showForm = true },
            containerColor = CosmicCyan,
            contentColor = DeepSpaceBlack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .testTag("add_log_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Observation Log")
        }

        // Observation diary registration sheet Form overlay
        if (showForm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showForm = false }
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Surface(
                    color = CosmicDarkBlue,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) {} // block click propagating to background dismiss Click
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("관측 일제 기록하기 🖌️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StarWhite)
                                IconButton(onClick = { showForm = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Constellation Target Selection
                        item {
                            Text("관측한 별자리", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            var customExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { customExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StarWhite)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(formConstellation)
                                        Icon(Icons.Default.ArrowDropDown, "Dropdown")
                                    }
                                }
                                DropdownMenu(
                                    expanded = customExpanded,
                                    onDismissRequest = { customExpanded = false },
                                    modifier = Modifier.fillMaxWidth().background(CosmicSlate)
                                ) {
                                    Constellation.ALL.map { it.name }.forEach { constellation ->
                                        DropdownMenuItem(
                                            text = { Text(constellation, color = StarWhite) },
                                            onClick = {
                                                formConstellation = constellation
                                                customExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Observation Address
                        item {
                            Text("관측 명소 / 장소", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            OutlinedTextField(
                                value = formLocation,
                                onValueChange = { formLocation = it },
                                placeholder = { Text("장소 이름이나 주소를 입력하세요") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Notes/Diary Content
                        item {
                            Text("나의 밤하늘 관측기 & 사진 메모", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            OutlinedTextField(
                                value = formNotes,
                                onValueChange = { formNotes = it },
                                placeholder = { Text("어젯밤은 달이 밝아 별이 흐릿하게 보였지만 삼태성 주위 가스는 영롱했습니다. 친구들과 함께한 소회를 기재하세요...") },
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite),
                                modifier = Modifier.fillMaxWidth().testTag("notes_input_field")
                            )
                        }

                        // Weather & Atmosphere condition
                        item {
                            Text("별 관측 당시 기상 상태", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            val weatherOptions = listOf("맑음 🌌", "구름 조금 🌤️", "안개 자욱 🌫️", "아주 깜깜함 🌑")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                weatherOptions.forEach { option ->
                                    FilterChip(
                                        selected = formWeather == option,
                                        onClick = { formWeather = option },
                                        label = { Text(option) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CosmicCyan,
                                            selectedLabelColor = DeepSpaceBlack,
                                            labelColor = StarWhite
                                        )
                                    )
                                }
                            }
                        }

                        // Space Photo generation selection (To give NASA-level illustrations automatically)
                        item {
                            Text("천문 사진 파일 첨부", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            val simulatedPhotos = listOf(
                                "sim_nebula" to "오리온 성운",
                                "sim_galaxy" to "안드로메다 나선",
                                "sim_cluster" to "플레이아데스 성단",
                                "sim_comet" to "혜성 관측 꼬리"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                simulatedPhotos.forEach { (key, title) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { formPhotoKey = key }
                                            .background(if (formPhotoKey == key) CosmicCyan else CosmicSlate)
                                            .border(2.dp, if (formPhotoKey == key) CosmicCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            AsyncImage(
                                                model = getSimulatedSpacePhotoUrl(key),
                                                contentDescription = title,
                                                modifier = Modifier.size(45.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(title, fontSize = 8.sp, color = if (formPhotoKey == key) DeepSpaceBlack else StarWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Star rating condition
                        item {
                            Text("오늘 밤 하늘 별빛 별점", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StarlightGold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { starIdx ->
                                    val currentStarVal = (starIdx + 1).toFloat()
                                    Icon(
                                        imageVector = if (formRating >= currentStarVal) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rating Star",
                                        tint = StarlightGold,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable { formRating = currentStarVal }
                                    )
                                }
                            }
                        }

                        // Submit
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (formNotes.trim().isEmpty()) {
                                        Toast.makeText(context, "관측 일지 내용을 한 줄 기재해 주세요!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    viewModel.addObservationLog(
                                        constellation = formConstellation,
                                        location = formLocation,
                                        notes = formNotes,
                                        rating = formRating,
                                        weather = formWeather,
                                        photoUri = formPhotoKey
                                    )

                                    Toast.makeText(context, "오프라인 도감에 새로운 관측 일지가 저장되었습니다! 🌌", Toast.LENGTH_LONG).show()
                                    showForm = false
                                    formNotes = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("도감 및 일 기록 보관하기 💾", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }
}

// Map the keys of simulation photo illustration entries to beautiful, free cosmic NASA Unsplash photos!
fun getSimulatedSpacePhotoUrl(key: String): String {
    return when (key) {
        "sim_orion" -> "https://images.unsplash.com/photo-1543722530-d2c3201371e7?w=500&auto=format&fit=crop&q=60" // deep space dust
        "sim_cassiopeia" -> "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=500&auto=format&fit=crop&q=60" // cluster stars
        "sim_nebula" -> "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=500&auto=format&fit=crop&q=60" // color nebula
        "sim_galaxy" -> "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=500&auto=format&fit=crop&q=60" // swirl galaxy
        "sim_cluster" -> "https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?w=500&auto=format&fit=crop&q=60" // shiny star clusters
        "sim_comet" -> "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?w=500&auto=format&fit=crop&q=60" // space trail tail
        "locked" -> "https://images.unsplash.com/photo-1538370965046-79c0d6907d47?w=500&auto=format&fit=crop&q=60" // dark forest sky
        else -> "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=500&auto=format&fit=crop&q=60" // snowy hills sky
    }
}
