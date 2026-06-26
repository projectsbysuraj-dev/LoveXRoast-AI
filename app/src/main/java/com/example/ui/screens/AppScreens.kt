package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoveRoastItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

// Helper function to copy text to clipboard
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("LoveRoastAI", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}

// Robust helper function to parse 4 Hinglish options from response
fun parseHinglishOptions(text: String): List<String> {
    val results = mutableListOf<String>()
    val cleanText = text.trim().removeSurrounding("\"").removeSurrounding("'")
    val lines = cleanText.split("\n")
    var currentOption = ""
    
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        
        // Match standard list indices like "1.", "2.", "3.", "4." or option headers
        val isHeader = trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("4.") ||
                       trimmed.startsWith("1)") || trimmed.startsWith("2)") || trimmed.startsWith("3)") || trimmed.startsWith("4)") ||
                       trimmed.lowercase().startsWith("option") || trimmed.lowercase().startsWith("result")
        
        if (isHeader) {
            if (currentOption.isNotBlank()) {
                results.add(currentOption.trim())
            }
            var parsedContent = trimmed
            if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("4.") ||
                trimmed.startsWith("1)") || trimmed.startsWith("2)") || trimmed.startsWith("3)") || trimmed.startsWith("4)")) {
                parsedContent = trimmed.substring(2).trim()
            } else {
                val colonIdx = trimmed.indexOf(":")
                if (colonIdx != -1) {
                    parsedContent = trimmed.substring(colonIdx + 1).trim()
                }
            }
            currentOption = parsedContent
        } else {
            if (currentOption.isBlank()) {
                currentOption = trimmed
            } else {
                currentOption += "\n$trimmed"
            }
        }
    }
    
    if (currentOption.isNotBlank()) {
        results.add(currentOption.trim())
    }
    
    // Fallback split by double newlines if no numbering found
    if (results.isEmpty()) {
        val backupOptions = cleanText.split(Regex("\\r?\\n\\r?\\n+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (backupOptions.size >= 2) {
            return backupOptions
        }
    }
    
    return if (results.isNotEmpty()) results else listOf(cleanText)
}

@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeContent)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun NameScreen(viewModel: MainViewModel, isDark: Boolean) {
    val tabName by viewModel.tabNameInput.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val typedText by viewModel.typedOutputText.collectAsStateWithLifecycle()

    AdaptiveContainer {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Generate from Name",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhitePrimary else TextDarkPrimary
        )
        Text(
            text = "Unlock romantic pick-up lines or savage roasts based entirely on a person's name.",
            fontSize = 14.sp,
            color = if (isDark) TextGraySecondary else TextDarkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tabName,
            onValueChange = { viewModel.tabNameInput.value = it },
            placeholder = { Text("Enter a name... (e.g., Alex)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoveRose,
                unfocusedBorderColor = if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder,
                focusedLabelColor = LoveRose,
                focusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                unfocusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                focusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary,
                unfocusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.generateLoveOrRoast("LOVE", "NAME", name = tabName) },
                enabled = tabName.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Love Compliment")
            }

            Button(
                onClick = { viewModel.generateLoveOrRoast("ROAST", "NAME", name = tabName) },
                enabled = tabName.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = RoastFlame),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Savage Roast")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutputDisplaySection(uiState = uiState, typedText = typedText, isDark = isDark, viewModel = viewModel)
    }
}

@Composable
fun SituationScreen(viewModel: MainViewModel, isDark: Boolean) {
    val tabSituation by viewModel.tabSituationInput.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val typedText by viewModel.typedOutputText.collectAsStateWithLifecycle()

    AdaptiveContainer {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Generate from Situation",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhitePrimary else TextDarkPrimary
        )
        Text(
            text = "Enter a specific situation or message to generate the perfect reply.",
            fontSize = 14.sp,
            color = if (isDark) TextGraySecondary else TextDarkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tabSituation,
            onValueChange = { viewModel.tabSituationInput.value = it },
            placeholder = { Text("E.g., She said 'I like your dog, but not you.' what do I reply?") },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoastFlame,
                unfocusedBorderColor = if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder,
                focusedLabelColor = RoastFlame,
                focusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                unfocusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                focusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary,
                unfocusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.generateLoveOrRoast("LOVE", "SITUATION", situation = tabSituation) },
                enabled = tabSituation.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Love Reply")
            }

            Button(
                onClick = { viewModel.generateLoveOrRoast("ROAST", "SITUATION", situation = tabSituation) },
                enabled = tabSituation.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = RoastFlame),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Savage Comeback")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutputDisplaySection(uiState = uiState, typedText = typedText, isDark = isDark, viewModel = viewModel)
    }
}

@Composable
fun ComboScreen(viewModel: MainViewModel, isDark: Boolean) {
    val comboName by viewModel.comboNameInput.collectAsStateWithLifecycle()
    val comboSituation by viewModel.comboSituationInput.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val typedText by viewModel.typedOutputText.collectAsStateWithLifecycle()

    AdaptiveContainer {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Generate Combo Match",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhitePrimary else TextDarkPrimary
        )
        Text(
            text = "Tailor pick-ups and witty disses matching both a person's name and their specific vibe.",
            fontSize = 14.sp,
            color = if (isDark) TextGraySecondary else TextDarkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comboName,
            onValueChange = { viewModel.comboNameInput.value = it },
            placeholder = { Text("Enter their name... (e.g., Sarah)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoveRose,
                unfocusedBorderColor = if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder,
                focusedLabelColor = LoveRose,
                focusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                unfocusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                focusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary,
                unfocusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = comboSituation,
            onValueChange = { viewModel.comboSituationInput.value = it },
            placeholder = { Text("Enter context... (e.g., Left me on read for 3 days)") },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoastFlame,
                unfocusedBorderColor = if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder,
                focusedLabelColor = RoastFlame,
                focusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                unfocusedTextColor = if (isDark) TextWhitePrimary else TextDarkPrimary,
                focusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary,
                unfocusedPlaceholderColor = if (isDark) TextGraySecondary else TextDarkSecondary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.generateLoveOrRoast("LOVE", "COMBO", name = comboName, situation = comboSituation) },
                enabled = comboName.isNotBlank() && comboSituation.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Love Combo")
            }

            Button(
                onClick = { viewModel.generateLoveOrRoast("ROAST", "COMBO", name = comboName, situation = comboSituation) },
                enabled = comboName.isNotBlank() && comboSituation.isNotBlank() && uiState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = RoastFlame),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Roast Combo")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutputDisplaySection(uiState = uiState, typedText = typedText, isDark = isDark, viewModel = viewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, isDark: Boolean) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val history by viewModel.historyState.collectAsStateWithLifecycle()
    val favorites by viewModel.favoritesState.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All Saved", "Favorites")

    val textPrimaryColor = if (isDark) TextWhitePrimary else TextDarkPrimary
    val textSecondaryColor = if (isDark) TextGraySecondary else TextDarkSecondary

    AdaptiveContainer {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "AI Generator History",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search logs...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondaryColor) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = textSecondaryColor)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoveRose,
                unfocusedBorderColor = if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder,
                focusedTextColor = textPrimaryColor,
                unfocusedTextColor = textPrimaryColor,
                focusedPlaceholderColor = textSecondaryColor,
                unfocusedPlaceholderColor = textSecondaryColor
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // History Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = LoveRose,
            divider = { HorizontalDivider(color = (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.15f)) },
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = LoveRose
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (selectedTabIndex == index) LoveRose else textSecondaryColor
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentList = if (selectedTabIndex == 0) history else favorites

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTabIndex == 0) Icons.Default.History else Icons.Default.Favorite,
                        contentDescription = null,
                        tint = textSecondaryColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTabIndex == 0) "No search logs found." else "No favorites added yet.",
                        fontSize = 16.sp,
                        color = textSecondaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Delete All Button
            if (selectedTabIndex == 0 && searchQuery.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.clearAllHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = RoastFlame)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All Logs", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = currentList,
                    key = { item -> item.id }
                ) { item ->
                    HistoryItemCard(
                        item = item,
                        isDark = isDark,
                        onFavoriteToggle = { viewModel.toggleItemFavorite(item) },
                        onDelete = { viewModel.deleteHistoryItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: LoveRoastItem,
    isDark: Boolean,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val textPrimaryColor = if (isDark) TextWhitePrimary else TextDarkPrimary
    val textSecondaryColor = if (isDark) TextGraySecondary else TextDarkSecondary

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDark
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge category
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (item.type == "LOVE") LoveRose.copy(alpha = 0.12f) else RoastFlame.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.type,
                            color = if (item.type == "LOVE") LoveRose else RoastFlame,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.tab,
                        fontSize = 11.sp,
                        color = textSecondaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Time String
                val sdf = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
                val dateStr = remember(item.timestamp) { sdf.format(Date(item.timestamp)) }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = textSecondaryColor,
                    fontWeight = FontWeight.Normal
                )
            }

            // Inputs summary if any
            if (!item.inputName.isNullOrBlank() || !item.inputSituation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            (if (isDark) Color.White else Color.Black).copy(alpha = 0.04f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    if (!item.inputName.isNullOrBlank()) {
                        Text(
                            text = "Name: ${item.inputName}",
                            fontSize = 12.sp,
                            color = textPrimaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!item.inputSituation.isNullOrBlank()) {
                        Text(
                            text = "Situation: ${item.inputSituation}",
                            fontSize = 12.sp,
                            color = textSecondaryColor,
                            fontWeight = FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main output block with parsed options
            val options = remember(item.outputText) { parseHinglishOptions(item.outputText) }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Option ${index + 1}",
                                fontSize = 11.sp,
                                color = textSecondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 18.sp
                            )
                        }
                        IconButton(
                            onClick = { copyToClipboard(context, option) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy Option ${index + 1}",
                                tint = textSecondaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(6.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Log",
                        tint = textSecondaryColor.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { copyToClipboard(context, item.outputText) }) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Response",
                            tint = textSecondaryColor
                        )
                    }

                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite Log",
                            tint = if (item.isFavorite) LoveRose else textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutputDisplaySection(
    uiState: UiState,
    typedText: String,
    isDark: Boolean,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val textPrimaryColor = if (isDark) TextWhitePrimary else TextDarkPrimary
    val textSecondaryColor = if (isDark) TextGraySecondary else TextDarkSecondary

    when (uiState) {
        is UiState.Idle -> {
            // Elegant placeholder call to action
            Card(
                colors = CardDefaults.cardColors(containerColor = (if (isDark) SurfaceGlassDark else Color.White).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LoveRose.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ready to Generate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fill in the input details above and tap an action to generate customized replies.",
                        fontSize = 12.sp,
                        color = textSecondaryColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generateRandomReply() },
                        colors = ButtonDefaults.buttonColors(containerColor = LoveRose.copy(alpha = 0.12f), contentColor = LoveRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Surprise Me!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        is UiState.Loading -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = (if (isDark) SurfaceGlassDark else Color.White).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        (if (isDark) SurfaceGlassDarkBorder else SurfaceGlassLightBorder).copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = LoveRose, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Consulting AI Spark...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimaryColor
                    )
                }
            }
        }

        is UiState.Success -> {
            val item = uiState.itemSaved
            val textToParse = if (typedText.length < item.outputText.length) typedText else item.outputText
            val options = remember(textToParse) { parseHinglishOptions(textToParse) }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hinglish AI Options",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.type == "LOVE") LoveRose else RoastFlame,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.toggleItemFavorite(item) }) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (item.isFavorite) LoveRose else textSecondaryColor
                                )
                            }
                            IconButton(onClick = { copyToClipboard(context, item.outputText) }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy All",
                                    tint = textSecondaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        options.forEachIndexed { index, option ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) {
                                        Color.White.copy(alpha = 0.05f)
                                    } else {
                                        Color.Black.copy(alpha = 0.04f)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(
                                                        if (item.type == "LOVE") LoveRose.copy(alpha = 0.2f) else RoastFlame.copy(alpha = 0.2f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = if (item.type == "LOVE") LoveRose else RoastFlame,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Option ${index + 1}",
                                                fontSize = 12.sp,
                                                color = textSecondaryColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = option,
                                            fontSize = 15.sp,
                                            color = textPrimaryColor,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 20.sp
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { copyToClipboard(context, option) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy Option ${index + 1}",
                                            tint = textSecondaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { copyToClipboard(context, item.outputText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.type == "LOVE") LoveRose else RoastFlame,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copy All Options",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        is UiState.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Connection Error",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        fontSize = 13.sp,
                        color = textPrimaryColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
