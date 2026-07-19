package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ShopToolTheme
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Instantiate ViewModel with proper Factory
    val viewModel: ShopViewModel by viewModels {
      ShopViewModel.provideFactory(application)
    }

    setContent {
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      
      ShopToolTheme(darkTheme = isDarkMode) {
        var showSplash by remember { mutableStateOf(true) }

        if (showSplash) {
          SplashScreen(
            onSplashFinished = { showSplash = false },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          MainAppShell(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun MainAppShell(viewModel: ShopViewModel) {
  val currentTab by viewModel.currentTab.collectAsState()
  val analysisState by viewModel.analysisState.collectAsState()
  val historyList by viewModel.historyState.collectAsState()
  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val currentLanguage by viewModel.currentLanguage.collectAsState()

  Scaffold(
    bottomBar = {
      // Show bottom navigation bar only when not actively loading
      val shouldShowBottomBar = when (analysisState) {
        is AnalysisUiState.Loading -> false
        else -> true
      }

      if (shouldShowBottomBar) {
        NavigationBar(
          modifier = Modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
        ) {
          // Home Tab
          NavigationBarItem(
            selected = currentTab == "home",
            onClick = { viewModel.selectTab("home") },
            icon = {
              Icon(
                imageVector = if (currentTab == "home") Icons.Default.Home else Icons.Outlined.Home,
                contentDescription = "Home"
              )
            },
            label = { Text("Home") },
            modifier = Modifier.testTag("nav_item_home")
          )

          // History Tab
          NavigationBarItem(
            selected = currentTab == "history",
            onClick = { viewModel.selectTab("history") },
            icon = {
              Icon(
                imageVector = if (currentTab == "history") Icons.Default.History else Icons.Outlined.History,
                contentDescription = "History"
              )
            },
            label = { Text("History") },
            modifier = Modifier.testTag("nav_item_history")
          )

          // Settings Tab
          NavigationBarItem(
            selected = currentTab == "settings",
            onClick = { viewModel.selectTab("settings") },
            icon = {
              Icon(
                imageVector = if (currentTab == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                contentDescription = "Settings"
              )
            },
            label = { Text("Settings") },
            modifier = Modifier.testTag("nav_item_settings")
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Home Tab Controller
      if (currentTab == "home") {
        when (val state = analysisState) {
          is AnalysisUiState.Idle -> {
            HomeScreen(
              onAnalyze = { url -> viewModel.analyzeReel(url) },
              errorMessage = null,
              onClearError = {}
            )
          }
          is AnalysisUiState.Loading -> {
            LoadingScreen(message = state.message)
          }
          is AnalysisUiState.Success -> {
            ResultsScreen(
              url = state.url,
              products = state.products,
              onBack = { viewModel.resetAnalysis() }
            )
          }
          is AnalysisUiState.Error -> {
            HomeScreen(
              onAnalyze = { url -> viewModel.analyzeReel(url) },
              errorMessage = state.message,
              onClearError = { viewModel.resetAnalysis() }
            )
          }
        }
      }

      // 2. History Tab Controller
      if (currentTab == "history") {
        HistoryScreen(
          historyList = historyList,
          onItemClick = { historyItem ->
            viewModel.loadPastResult(historyItem.url, historyItem.products)
            viewModel.selectTab("home")
          },
          onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
          onClearAll = { viewModel.clearAllHistory() }
        )
      }

      // 3. Settings Tab Controller
      if (currentTab == "settings") {
        SettingsScreen(
          isDarkMode = isDarkMode,
          onToggleTheme = { viewModel.toggleTheme() },
          currentLanguage = currentLanguage,
          onSelectLanguage = { lang -> viewModel.setLanguage(lang) }
        )
      }
    }
  }
}
