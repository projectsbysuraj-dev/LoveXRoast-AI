package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class ResponsePart(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class ResponseContent(
    @Json(name = "parts") val parts: List<ResponsePart>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: ResponseContent?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)
