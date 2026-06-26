package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.SplashLoadingScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDark) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashLoadingScreen(onDismiss = { showSplash = false })
                } else {
                    MainAppLayout(viewModel = viewModel, isDark = isDark)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    viewModel: MainViewModel,
    isDark: Boolean
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val textPrimaryColor = if (isDark) TextWhitePrimary else TextDarkPrimary
    val textSecondaryColor = if (isDark) TextGraySecondary else TextDarkSecondary

    // --- Dynamic Premium Gradient Background ---
    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                DeepSpaceBg,
                DeepSpaceBg,
                Color(0xFF161021), // Soft neon purple highlight
                Color(0xFF1B1015)  // Soft warm rose highlight
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                CleanRoseBg,
                Color(0xFFFFF2F4),
                Color(0xFFFFF5EE) // Soft orange highlight
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Glassmorphic Bottom Navigation
            Column {
                Divider(
                    color = (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.15f),
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = if (isDark) DeepSpaceBg.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = {
                            viewModel.resetState()
                            viewModel.selectTab(0)
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Name") },
                        label = { Text("Name", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LoveRose,
                            selectedTextColor = LoveRose,
                            indicatorColor = LoveRose.copy(alpha = 0.12f),
                            unselectedIconColor = textSecondaryColor,
                            unselectedTextColor = textSecondaryColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            viewModel.resetState()
                            viewModel.selectTab(1)
                        },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Situation") },
                        label = { Text("Situation", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoastFlame,
                            selectedTextColor = RoastFlame,
                            indicatorColor = RoastFlame.copy(alpha = 0.12f),
                            unselectedIconColor = textSecondaryColor,
                            unselectedTextColor = textSecondaryColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = {
                            viewModel.resetState()
                            viewModel.selectTab(2)
                        },
                        icon = { Icon(Icons.Default.Adjust, contentDescription = "Combo") },
                        label = { Text("Combo", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoastFlame,
                            selectedTextColor = RoastFlame,
                            indicatorColor = RoastFlame.copy(alpha = 0.12f),
                            unselectedIconColor = textSecondaryColor,
                            unselectedTextColor = textSecondaryColor
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = {
                            viewModel.resetState()
                            viewModel.selectTab(3)
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                        label = { Text("Logs", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGold,
                            selectedTextColor = AccentGold,
                            indicatorColor = AccentGold.copy(alpha = 0.12f),
                            unselectedIconColor = textSecondaryColor,
                            unselectedTextColor = textSecondaryColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- PREMIUM HEADER BAR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(LoveRose, RoastFlame)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Love & Roast AI",
                            style = TextStyle(
                                brush = Brush.horizontalGradient(listOf(LoveRose, RoastFlame)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                    }

                    // Theme Toggle + Monospace Badge (Premium look)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // UTC Badge for ChatGPT high-tech feel
                        Box(
                            modifier = Modifier
                                .background(
                                    (if (isDark) Color.White else Color.Black).copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "REAL-TIME",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = textSecondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleTheme() }
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = textPrimaryColor
                            )
                        }
                    }
                }

                // --- MAIN TAB CONTENT ROUTER ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(350),
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            0 -> NameScreen(viewModel = viewModel, isDark = isDark)
                            1 -> SituationScreen(viewModel = viewModel, isDark = isDark)
                            2 -> ComboScreen(viewModel = viewModel, isDark = isDark)
                            3 -> HistoryScreen(viewModel = viewModel, isDark = isDark)
                        }
                    }
                }
            }
        }
    }
}
