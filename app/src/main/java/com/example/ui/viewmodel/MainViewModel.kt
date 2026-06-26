package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LoveRoastDatabase
import com.example.data.model.LoveRoastItem
import com.example.data.repository.LoveRoastRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val text: String, val itemSaved: LoveRoastItem) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LoveRoastDatabase.getDatabase(application)
    private val repository = LoveRoastRepository(database.loveRoastDao())

    // --- Screen / Navigation State ---
    private val _currentTab = MutableStateFlow(0)
    val currentTab = _currentTab.asStateFlow()

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    // --- Inputs State ---
    val tabNameInput = MutableStateFlow("")
    val tabSituationInput = MutableStateFlow("")
    val comboNameInput = MutableStateFlow("")
    val comboSituationInput = MutableStateFlow("")

    // --- UI State ---
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    // --- Typing Effect State ---
    private val _typedOutputText = MutableStateFlow("")
    val typedOutputText = _typedOutputText.asStateFlow()

    private var typingJob: Job? = null

    // --- History & Search ---
    val searchQuery = MutableStateFlow("")

    private val _history = MutableStateFlow<List<LoveRoastItem>>(emptyList())
    val historyState = _history.asStateFlow()

    val favoritesState: StateFlow<List<LoveRoastItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Theme State ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    init {
        // Reactive search/history observation
        viewModelScope.launch {
            searchQuery
                .debounce(200)
                .collectLatest { query ->
                    val flow = if (query.isBlank()) {
                        repository.allHistory
                    } else {
                        repository.searchHistory(query)
                    }
                    flow.collect { list ->
                        _history.value = list
                    }
                }
        }
    }

    // --- API Interactions ---
    fun generateLoveOrRoast(
        type: String, // "LOVE" or "ROAST"
        tab: String,  // "NAME", "SITUATION", "COMBO"
        name: String = "",
        situation: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _typedOutputText.value = ""
            typingJob?.cancel()

            // Construct specific prompt
            val prompt = when (tab) {
                "NAME" -> {
                    if (type == "LOVE") {
                        "Generate exactly 4 distinct, highly creative, cute, romantic compliments, flirting messages, or pickup lines in Hinglish (Hindi written using English/Latin alphabet, e.g., 'Aapki smile bohot pyaari hai') referencing the person's name: '$name'. Make them extremely modern, confident, and stylish. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    } else {
                        "Generate exactly 4 distinct, incredibly funny, savage, sigma-style, witty roasts in Hinglish (Hindi written using English/Latin alphabet, e.g., 'Rizz to achhi hai par sakal thodi mid hai') referencing the person's name: '$name'. Include modern light internet slang (like 'no cap', 'rizz', 'bruh', 'skull emoji', etc.) for humor, but keep it within standard fun. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    }
                }
                "SITUATION" -> {
                    if (type == "LOVE") {
                        "Generate exactly 4 distinct, modern, charming, highly confident, romantic replies or responses in Hinglish (Hindi written using English/Latin alphabet, e.g., 'Main tumhara mood hamesha set rakhunga') for the following chat/situation: '$situation'. Make them extremely natural, social-media style, cute, and smooth. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    } else {
                        "Generate exactly 4 distinct, witty, savage, sigma-style roast replies or comebacks in Hinglish (Hindi written using English/Latin alphabet) for the following chat/situation: '$situation'. Make them modern, natural, funny, and social-media style. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    }
                }
                "COMBO" -> {
                    if (type == "LOVE") {
                        "Generate exactly 4 distinct, modern, charming, highly confident, romantic replies in Hinglish (Hindi written using English/Latin alphabet) referencing the person named '$name' in the following situation/chat: '$situation'. Make them extremely smooth, cute, and customized to both. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    } else {
                        "Generate exactly 4 distinct, witty, savage, sigma-style roast replies in Hinglish (Hindi written using English/Latin alphabet) referencing the person named '$name' in the following situation/chat: '$situation'. Make them modern, funny, natural, and customized to both. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."
                    }
                }
                else -> ""
            }

            try {
                val rawResult = repository.generateResponse(prompt)
                val cleanedResult = rawResult.trim().removeSurrounding("\"").removeSurrounding("'")

                // Save to Room DB automatically
                val item = LoveRoastItem(
                    type = type,
                    tab = tab,
                    inputName = name.ifBlank { null },
                    inputSituation = situation.ifBlank { null },
                    outputText = cleanedResult,
                    isFavorite = false
                )
                val generatedId = repository.saveItem(item)
                val finalSavedItem = item.copy(id = generatedId)

                _uiState.value = UiState.Success(cleanedResult, finalSavedItem)

                // Start typing animation
                animateTextTyping(cleanedResult)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    // --- Random Generator ---
    fun generateRandomReply() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _typedOutputText.value = ""
            typingJob?.cancel()

            val prompt = "Generate exactly 4 distinct, completely random, ultra-modern, funny, or romantic social-media style lines of your choice in Hinglish (Hindi written using English/Latin alphabet, e.g., 'Bro ka style to high hai par dimaag slow hai') (either savage roasts or smooth pickup lines, or sigma-style meme responses). Make them surprising and viral. Provide exactly 4 numbered options starting with '1.', '2.', '3.', '4.', each on its own line/block. No double quotes, intro, or extra conversational preamble."

            try {
                val rawResult = repository.generateResponse(prompt)
                val cleanedResult = rawResult.trim().removeSurrounding("\"").removeSurrounding("'")

                val type = if (cleanedResult.lowercase().contains("love") || 
                               cleanedResult.lowercase().contains("heart") || 
                               cleanedResult.lowercase().contains("cute") || 
                               cleanedResult.lowercase().contains("compliment") ||
                               cleanedResult.lowercase().contains("pickup")) "LOVE" else "ROAST"

                val item = LoveRoastItem(
                    type = type,
                    tab = "RANDOM",
                    inputName = "Random Soul",
                    inputSituation = "Universal Vibe",
                    outputText = cleanedResult,
                    isFavorite = false
                )
                val generatedId = repository.saveItem(item)
                val finalSavedItem = item.copy(id = generatedId)

                _uiState.value = UiState.Success(cleanedResult, finalSavedItem)
                animateTextTyping(cleanedResult)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred.")
            }
        }
    }

    private fun animateTextTyping(text: String) {
        typingJob = viewModelScope.launch {
            _typedOutputText.value = ""
            for (char in text) {
                _typedOutputText.value += char
                delay(12) // Smooth rapid-typing feel
            }
        }
    }

    fun toggleItemFavorite(item: LoveRoastItem) {
        viewModelScope.launch {
            val newFavStatus = !item.isFavorite
            repository.toggleFavorite(item.id, newFavStatus)
            val currentState = _uiState.value
            if (currentState is UiState.Success && currentState.itemSaved.id == item.id) {
                _uiState.value = currentState.copy(itemSaved = currentState.itemSaved.copy(isFavorite = newFavStatus))
            }
        }
    }

    fun deleteHistoryItem(item: LoveRoastItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
        _typedOutputText.value = ""
        typingJob?.cancel()
    }
}
