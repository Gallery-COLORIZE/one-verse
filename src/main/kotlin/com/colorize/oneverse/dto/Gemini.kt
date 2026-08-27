package com.colorize.oneverse.dto

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class GenerationConfig(
    val responseModalities: List<String>? = listOf("IMAGE"),
    val imageConfig: ImageConfig? = null
)

data class ImageConfig(
    val aspectRatio: String? = null,
    val imageSize: String? = null
)
data class Candidate(
    val content: GeminiContent?
)

data class GeminiContent(
    val parts: List<GeminiPart>?
)

data class GeminiPart(
    val text: String?,
    val inlineData: InlineData?
)

data class InlineData(
    val mimeType: String,
    val data: String // Base64 인코딩된 이미지 문자열
)