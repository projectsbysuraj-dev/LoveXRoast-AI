package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.LoveRoastDao
import com.example.data.model.Content
import com.example.data.model.GenerateContentRequest
import com.example.data.model.LoveRoastItem
import com.example.data.model.Part
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class LoveRoastRepository(private val loveRoastDao: LoveRoastDao) {

    // --- Local DB flows and methods ---
    val allHistory: Flow<List<LoveRoastItem>> = loveRoastDao.getAllHistory()
    val favorites: Flow<List<LoveRoastItem>> = loveRoastDao.getFavorites()

    fun searchHistory(query: String): Flow<List<LoveRoastItem>> {
        return loveRoastDao.searchHistory("%$query%")
    }

    suspend fun saveItem(item: LoveRoastItem): Long {
        return loveRoastDao.insertItem(item)
    }

    suspend fun deleteItem(item: LoveRoastItem) {
        loveRoastDao.deleteItem(item)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        loveRoastDao.updateFavorite(id, isFavorite)
    }

    suspend fun clearAllHistory() {
        loveRoastDao.clearHistory()
    }

    // --- Remote API with Robust Multi-Model Fallback ---
    suspend fun generateResponse(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check for missing or placeholder keys
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception(
                "Gemini API key is not configured.\n\n" +
                "To fix this, please set your GEMINI_API_KEY securely in the Secrets panel in the AI Studio UI, then rebuild."
            )
        }

        // List of models to try in sequence if a 503, 404, or other transient error is encountered
        val models = listOf(
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-1.5-flash"
        )

        var lastException: Exception? = null

        for (model in models) {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt)
                            )
                        )
                    )
                )

                val response = RetrofitClient.apiService.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )

                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrBlank()) {
                    return responseText
                }
            } catch (e: Exception) {
                lastException = e
                // Log or track model attempt failure to allow proceeding to the next candidate
                System.err.println("Gemini model '$model' failed: ${e.message}. Trying next fallback model...")
            }
        }

        // If all fallbacks failed, throw a descriptive exception including details of the last error
        throw lastException ?: Exception("All Gemini models failed to generate content.")
    }
}
