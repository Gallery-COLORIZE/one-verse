package com.neobible.oneverse.service

import com.neobible.oneverse.dto.BibleVerseDto
import com.neobible.oneverse.dto.Candidate
import com.neobible.oneverse.dto.GeminiContent
import com.neobible.oneverse.dto.GeminiPart
import com.neobible.oneverse.dto.GeminiRequest
import com.neobible.oneverse.dto.GeminiResponse
import com.neobible.oneverse.dto.GenerationConfig
import com.neobible.oneverse.dto.ImageConfig
import com.neobible.oneverse.dto.InlineData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class BibleCardServiceTest : BehaviorSpec({

    Given("Gemini API가 이미지를 반환하는 경우") {
        val fixture = BibleCardServiceFixture()
        val verse = BibleVerseDto(
            customVerse = "God is our refuge and strength",
            originalVerse = "God is our refuge and strength",
            verseSource = "Psalm 46:1"
        )
        val response = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = null,
                                inlineData = InlineData(
                                    mimeType = "image/png",
                                    data = "base64-image-data"
                                )
                            )
                        )
                    )
                )
            )
        )
        val urlSlot = slot<String>()
        val apiKeySlot = slot<String>()
        val requestSlot = slot<GeminiRequest>()

        every { fixture.requestSpec.uri(capture(urlSlot)) } returns fixture.requestSpec
        every { fixture.requestSpec.header("x-goog-api-key", capture(apiKeySlot)) } returns fixture.requestSpec
        every { fixture.requestSpec.contentType(MediaType.APPLICATION_JSON) } returns fixture.requestSpec
        every { fixture.requestSpec.body(capture(requestSlot)) } returns fixture.requestSpec
        every { fixture.responseSpec.body(GeminiResponse::class.java) } returns response

        When("기본 설정으로 카드 이미지를 생성하면") {
            val result = fixture.service.generateVerseCardImageBase64(verse)

            Then("base64 이미지 데이터를 반환한다") {
                result shouldBe "base64-image-data"
            }

            Then("요청 본문과 URL이 올바르게 구성된다") {
                urlSlot.captured shouldBe "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent"
                apiKeySlot.captured shouldBe "test-api-key"

                requestSlot.captured.contents shouldHaveSize 1
                requestSlot.captured.contents.first().parts shouldHaveSize 1
                requestSlot.captured.contents.first().parts.first().text shouldContain verse.customVerse
                requestSlot.captured.contents.first().parts.first().text shouldContain verse.verseSource

                requestSlot.captured.generationConfig shouldBe GenerationConfig(
                    responseModalities = listOf("IMAGE"),
                    imageConfig = ImageConfig(
                        aspectRatio = "4:5",
                        imageSize = "2K"
                    )
                )
            }

            Then("요청 체인은 한 번만 실행된다") {
                verify(exactly = 1) { fixture.restClient.post() }
                verify(exactly = 1) { fixture.requestSpec.retrieve() }
            }
        }
    }

    Given("Gemini 응답에 inline image 데이터가 없는 경우") {
        val fixture = BibleCardServiceFixture()
        val verse = BibleVerseDto(
            customVerse = "Because your steadfast love is better than life",
            originalVerse = "Because your steadfast love is better than life",
            verseSource = "Psalm 63:3"
        )
        val response = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = "no image",
                                inlineData = null
                            )
                        )
                    )
                )
            )
        )

        every { fixture.requestSpec.body(any<GeminiRequest>()) } returns fixture.requestSpec
        every { fixture.responseSpec.body(GeminiResponse::class.java) } returns response

        When("서비스가 이미지 데이터를 추출하려고 하면") {
            Then("런타임 예외를 던진다") {
                shouldThrow<RuntimeException> {
                    fixture.service.generateVerseCardImageBase64(verse)
                }
            }
        }
    }
})

private class BibleCardServiceFixture {
    val service = BibleCardService()
    val restClient: RestClient = mockk()
    val requestSpec: RestClient.RequestBodyUriSpec = mockk()
    val responseSpec: RestClient.ResponseSpec = mockk()

    init {
        setField(service, "restClient", restClient)
        setField(service, "apiKey", "test-api-key")

        every { restClient.post() } returns requestSpec
        every { requestSpec.uri(any<String>()) } returns requestSpec
        every { requestSpec.header(any<String>(), any<String>()) } returns requestSpec
        every { requestSpec.contentType(any<MediaType>()) } returns requestSpec
        every { requestSpec.body(any<GeminiRequest>()) } returns requestSpec
        every { requestSpec.retrieve() } returns responseSpec
    }
}

private fun setField(target: Any, fieldName: String, value: Any?) {
    val field = target.javaClass.getDeclaredField(fieldName)
    field.isAccessible = true
    field.set(target, value)
}
