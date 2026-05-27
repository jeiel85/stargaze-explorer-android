package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ar.ArSkyMapScreen
import com.example.ui.screens.community.CommunityScreen
import com.example.ui.screens.journal.JournalScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.spots.StargazingSpotsScreen
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicDarkBlue
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  
  private val requestNotificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { _ -> }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Proactively request post notifications permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        
        // Navigation / Tab Controller State
        var selectedScreenTab by remember { mutableStateOf(0) }
        
        // State to link transitioning constellation name from Map view to Journal view form!
        var prePopulatedConstellationName by remember { mutableStateOf<String?>(null) }

        Scaffold(
          bottomBar = {
            NavigationBar(
              containerColor = CosmicDarkBlue,
              tonalElevation = 8.dp,
              modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .testTag("app_navigation_bar")
            ) {
              val items = listOf(
                NavigationItem("지도로 탐험", Icons.Default.Explore, "ar_tab"),
                NavigationItem("나의 도감", Icons.Default.AutoStories, "journal_tab"),
                NavigationItem("관측가이더", Icons.Default.Map, "spots_tab"),
                NavigationItem("커뮤니티", Icons.Default.Groups, "community_tab"),
                NavigationItem("프로필·알림", Icons.Default.AccountCircle, "profile_tab")
              )

              items.forEachIndexed { index, item ->
                NavigationBarItem(
                  selected = selectedScreenTab == index,
                  onClick = { selectedScreenTab = index },
                  icon = { Icon(item.icon, contentDescription = item.label) },
                  label = { Text(item.label, fontSize = 10.sp, maxLines = 1) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepSpaceBlack,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = CosmicCyan,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = CosmicCyan
                  ),
                  modifier = Modifier.testTag(item.tag)
                )
              }
            }
          },
          modifier = Modifier.fillMaxSize().background(DeepSpaceBlack)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(DeepSpaceBlack)
              .padding(bottom = innerPadding.calculateBottomPadding()) // manual bottom padding for custom float layout!
          ) {
            when (selectedScreenTab) {
              0 -> ArSkyMapScreen(
                viewModel = viewModel,
                onNavigateToJournal = { constName ->
                  prePopulatedConstellationName = constName
                  selectedScreenTab = 1 // jump straight to the journal form!
                }
              )
              1 -> JournalScreen(
                viewModel = viewModel,
                preSelectedConstellationName = prePopulatedConstellationName,
                onClearPreSelection = { prePopulatedConstellationName = null }
              )
              2 -> StargazingSpotsScreen(viewModel = viewModel)
              3 -> CommunityScreen(viewModel = viewModel)
              4 -> ProfileScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

data class NavigationItem(
  val label: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val tag: String
)

