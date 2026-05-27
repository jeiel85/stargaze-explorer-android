package com.example.ui.screens.spots

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StargazingPlace
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StargazingSpotsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val places by viewModel.allPlaces.collectAsState()
    val activePlaceId by viewModel.selectedPlaceId.collectAsState()
    val activeReviews by viewModel.selectedPlaceReviews.collectAsState()

    var showWriteReview by remember { mutableStateOf(false) }
    var reviewContent by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 45.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "인기 별자리 관측 명소",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "은하수와 별똥별이 쏟아지는 대한민국 4대 성지",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Places Horizontal slider
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(places) { place ->
                    val isSelected = activePlaceId == place.id
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CosmicSlate else CosmicDarkBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CosmicCyan else Color(0x11FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectedPlaceId.value = place.id }
                    ) {
                        Column {
                            // Cover spot picture
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                AsyncImage(
                                    model = getSpotPhotoUrl(place.id),
                                    contentDescription = place.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color(0xAA03030F))
                                            )
                                        )
                                )

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Spot Loc", tint = CosmicCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }

                                // Rating Badge
                                Surface(
                                    color = CardBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "Avg Rating", tint = StarlightGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%.1f", place.averageRating),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                    }
                                }
                            }

                            // Body details of the spot
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = place.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Info tag 1: Altitude
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(place.altitude, fontSize = 11.sp, color = StarWhite) },
                                        leadingIcon = { Icon(Icons.Default.FilterHdr, null, tint = CosmicCyan, modifier = Modifier.size(12.dp)) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = CosmicDarkBlue)
                                    )
                                    // Info tag 2: Light pollution
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(place.lightPollution, fontSize = 11.sp, color = StarWhite) },
                                        leadingIcon = { Icon(Icons.Default.BrightnessLow, null, tint = StarlightGold, modifier = Modifier.size(12.dp)) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = CosmicDarkBlue)
                                    )
                                }

                                // Display reviews section dynamically if active!
                                AnimatedVisibility(visible = isSelected) {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        HorizontalDivider(color = Color(0x11FFFFFF))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "방문자 리뷰 (${activeReviews.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CosmicCyan
                                            )
                                            TextButton(
                                                onClick = { showWriteReview = true },
                                                colors = ButtonDefaults.textButtonColors(contentColor = CosmicCyan)
                                            ) {
                                                Icon(Icons.Default.RateReview, contentDescription = "Write", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("리뷰 쓰기", fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (activeReviews.isEmpty()) {
                                            Text(
                                                text = "아직 등록된 별점 리뷰가 없습니다. 첫 리뷰를 전해보세요!",
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(vertical = 12.dp)
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                activeReviews.forEach { review ->
                                                    Surface(
                                                        color = CosmicDarkBlue.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = review.authorName,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = StarWhite
                                                                )
                                                                Row {
                                                                    repeat(review.rating.toInt()) {
                                                                        Icon(Icons.Default.Star, "rating star", tint = StarlightGold, modifier = Modifier.size(10.dp))
                                                                    }
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = review.content,
                                                                fontSize = 12.sp,
                                                                color = TextPrimary
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
                }
            }
        }

        // Write Review Dialog Sheets
        if (showWriteReview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showWriteReview = false }
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("명소 별점 리뷰 쓰기 📝", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StarWhite)
                            IconButton(onClick = { showWriteReview = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = StarWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("오늘 밤하늘은 만족스러우셨나요?", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Star ratings selector
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(5) { starIdx ->
                                val currentVal = (starIdx + 1).toFloat()
                                Icon(
                                    imageVector = if (reviewRating >= currentVal) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating choice",
                                    tint = StarlightGold,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { reviewRating = currentVal }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = reviewContent,
                            onValueChange = { reviewContent = it },
                            placeholder = { Text("빛공해가 적어 아주 잘 보여요! 밤길 비포장로가 험하니 안전운전 필요합니다.") },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite),
                            modifier = Modifier.fillMaxWidth().testTag("review_input_field")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (reviewContent.trim().isEmpty()) {
                                    Toast.makeText(context, "리뷰 내용을 한 줄 입력해 주세요!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val pId = activePlaceId ?: 1
                                viewModel.submitPlaceReview(pId, reviewContent, reviewRating)
                                Toast.makeText(context, "소중한 점수 리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                                showWriteReview = false
                                reviewContent = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("리뷰 작성 완료", color = DeepSpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Map Place IDs to gorgeous Korea starry night Unsplash photography!
fun getSpotPhotoUrl(placeId: Int): String {
    return when (placeId) {
        1 -> "https://images.unsplash.com/photo-1516339901601-2e1b62dc0c45?w=500&auto=format&fit=crop&q=60" // High dark mountains stars
        2 -> "https://images.unsplash.com/photo-1504333631150-c8ab2da93b00?w=500&auto=format&fit=crop&q=60" // Dense starry sky
        3 -> "https://images.unsplash.com/photo-1532978379173-523e16f22106?w=500&auto=format&fit=crop&q=60" // Tunnel stars portal
        4 -> "https://images.unsplash.com/photo-1528722828814-77b9b83aafb2?w=500&auto=format&fit=crop&q=60" // High ridge starlight galaxy
        else -> "https://images.unsplash.com/photo-1543722530-d2c3201371e7?w=500&auto=format&fit=crop&q=60"
    }
}
