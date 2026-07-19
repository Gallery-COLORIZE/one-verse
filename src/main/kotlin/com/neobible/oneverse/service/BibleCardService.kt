package com.neobible.oneverse.service

import com.neobible.oneverse.dto.BibleVerseDto
import com.neobible.oneverse.dto.Content
import com.neobible.oneverse.dto.GeminiRequest
import com.neobible.oneverse.dto.GeminiResponse
import com.neobible.oneverse.dto.GenerationConfig
import com.neobible.oneverse.dto.ImageConfig
import com.neobible.oneverse.dto.Part
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class BibleCardService {

    private val restClient = RestClient.create()
    private val apiKey = System.getenv("GOOGLE_API_KEY")

    fun generateVerseCardImageBase64(
        verse: BibleVerseDto,
        model: String = "gemini-3.1-flash-image",
        aspectRatio: String = "4:5",
        imageSize: String = "2K"
    ): String {
        val prompt = buildVerseCardImagePrompt(verse)
        return requestGeminiVerseCardImageBase64(
            prompt = prompt,
            model = model,
            aspectRatio = aspectRatio,
            imageSize = imageSize
        )
    }

    private fun buildVerseCardImagePrompt(verse: BibleVerseDto): String {
        return """
            거칠고 질감 있는 현대적인 세로형 기독교 말씀카드 이미지를 생성하십시오.
        
            요구 사항:
            - 강력하고 압도적인 대조를 가지는 원시적인 레이아웃을 사용하십시오.
            - 차갑고 어두운 흑백 색조를 기본 팔레트로 사용하여 고립감과 단호함을 표현하십시오.
            - 대담하고 무거우며 강인한 서체로 중앙에 배치하십시오. 단, 구절이 너무 크지는 작게 만드시오.
            - 텍스트 주변에 넓은 여백을 두어 말씀의 무게감을 강조하십시오.
            - 따옴표, 라벨, 워터마크를 추가하지 마십시오. 단, OneVerse 라는 문구는 작게 우측 하단에 반투명하게 추가하십시오.
            - 단일 이미지만 출력하십시오.
            - 아래의 한국어 텍스트를 정확하게 그대로 포함하십시오.
            - 구절 출처는 하단에 더 작은 크기의 단호한 서체로 배치하십시오.
            - 이미지는 반드시 핵심 구절과 어울리는 이미지로 생성하십시오.
        
            핵심 구절:
            ${verse.customVerse}
        
            구절 출처:
            ${verse.verseSource}
        """.trimIndent()
    }


    private fun requestGeminiVerseCardImageBase64(
        prompt: String,
        model: String,
        aspectRatio: String,
        imageSize: String
    ): String {

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent"

        // 요청 본문 조립
        val requestBody = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                responseModalities = listOf("IMAGE"), // 이미지 모델 호출 시 명시 권장
                imageConfig = ImageConfig(
                    aspectRatio = aspectRatio,
                    imageSize = imageSize
                )
            )
        )

        val response = restClient.post()
            .uri(url)
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(GeminiResponse::class.java)

        val imagePart = response?.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull { it.inlineData != null }
            ?: throw RuntimeException("이미지 생성에 실패했거나 응답이 비어있습니다.")

        val inlineData = imagePart.inlineData!!


        return inlineData.data
    }
}