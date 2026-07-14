package com.neobible.oneverse.service

import com.neobible.oneverse.dto.BibleVerseDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.ai.chat.client.ChatClient

class BibleVerseServiceTest : BehaviorSpec({

    Given("오늘의 말씀") {
        val aiContent = """
            <data>
                <custom-verse>커스텀말씀1</custom-verse>
                <original-verse>원본말씀1</original-verse>
                <verse-source>원본구절</verse-source>
            </data>
        """.trimIndent()
        val fixture = BibleVerseServiceFixture(aiContent = aiContent)
        val service = fixture.createService()

        When("오늘의 말씀 요청시(TODAY)") {
            val result = service.getBibleMessage("TODAY", null)

            Then("DTO 형태 반환") {
                result shouldBe BibleVerseDto(
                    customVerse = "커스텀말씀1",
                    originalVerse = "원본말씀1",
                    verseSource = "원본구절"
                )
            }

            Then("프롬프트도 정상 요청") {
                verify(exactly = 1) {
                    fixture.requestSpec.user(match<String> { it.isNotBlank() })
                }
                verify(exactly = 1) {
                    fixture.callSpec.content()
                }
            }
        }
    }

    Given("상황별 말씀") {
        val aiContent = """
            <data>
                <custom-verse>커스텀말씀2</custom-verse>
                <original-verse>원본말씀2</original-verse>
                <verse-source>원본구절</verse-source>
            </data>
        """.trimIndent()
        val fixture = BibleVerseServiceFixture(aiContent = aiContent)
        val service = fixture.createService()
        val situationInput = "기분이 안좋아"

        When("상황별 말씀 입력(SITUATION)") {
            val result = service.getBibleMessage("SITUATION", situationInput)

            Then("DTO 형태로 반환") {
                result shouldBe BibleVerseDto(
                    customVerse = "커스텀말씀2",
                    originalVerse = "원본말씀2",
                    verseSource = "원본구절"
                )
            }

            Then("프롬프트도 정상 요청") {
                verify(exactly = 1) {
                    fixture.requestSpec.user(situationInput)
                }
            }
        }
    }

    Given("상황별 말씀 입력값이 없는 경우") {
        val aiContent = """
            <data>
                <custom-verse>커스텀말씀3</custom-verse>
                <original-verse>원본말씀3</original-verse>
                <verse-source>원본구절</verse-source>
            </data>
        """.trimIndent()
        val fixture = BibleVerseServiceFixture(aiContent = aiContent)
        val service = fixture.createService()

        When("상황별 말씀을 입력 없이 요청") {
            val result = service.getBibleMessage("SITUATION", null)

            Then("기본 프롬프트로 요청 후 DTO 형태로 반환") {
                result shouldBe BibleVerseDto(
                    customVerse = "커스텀말씀3",
                    originalVerse = "원본말씀3",
                    verseSource = "원본구절"
                )
                verify(exactly = 1) {
                    fixture.requestSpec.user(match<String> { it.isNotBlank() })
                }
            }
        }
    }

    Given("이전에 받은 말씀을 제외하고 다시 요청하는 경우") {
        val aiContent = """
            <data>
                <custom-verse>커스텀말씀</custom-verse>
                <original-verse>두려워 말라 내가 너와 함께 함이니라</original-verse>
                <verse-source>이사야 41:10</verse-source>
            </data>
        """.trimIndent()
        val fixture = BibleVerseServiceFixture(aiContent = aiContent)
        val service = fixture.createService()

        When("말씀을 연속으로 요청") {
            service.getBibleMessage("TODAY", null)
            service.getBibleMessage("TODAY", null, "이사야 41:10")

            Then("두 번째 요청에는 이전 말씀 제외 조건을 포함") {
                fixture.userPrompts.size shouldBe 2
                fixture.userPrompts[0] shouldBe "성경에서 삶의 지혜와 위로, 감사가 되는 구절을 하나 골라 정해진 형식으로 출력해줘."
                fixture.userPrompts[1] shouldContain "이전에 추천한 말씀은 제외"
                fixture.userPrompts[1] shouldContain "이사야 41:10"
            }
        }
    }

    Given("AI 응답 본문이 비어있는 경우") {
        val fixture = BibleVerseServiceFixture(aiContent = null)
        val service = fixture.createService()

        When("말씀 요청") {
            val result = service.getBibleMessage("SITUATION", "위로가 필요해")

            Then("실패 DTO 반환") {
                result.customVerse shouldBe ""
                result.originalVerse.isNotBlank() shouldBe true
                result.verseSource shouldBe "Error"
            }
        }
    }

    Given("AI 호출 중 예외가 발생하는 경우") {
        val builder = mockk<ChatClient.Builder>()
        val chatClient = mockk<ChatClient>()
        val requestSpec = mockk<ChatClient.ChatClientRequestSpec>()
        val systemPrompt = slot<String>()

        every { builder.defaultSystem(capture(systemPrompt)) } returns builder
        every { builder.build() } returns chatClient
        every { chatClient.prompt() } returns requestSpec
        every { requestSpec.user(any<String>()) } throws IllegalStateException("AI 게이트웨이 타임아웃")

        val service = BibleVerseService(builder)

        When("말씀 요청") {
            val result = service.getBibleMessage("SITUATION", "마음이 불안해")

            Then("예외 메시지를 포함한 오류 DTO 반환") {
                result.customVerse shouldBe ""
                result.originalVerse shouldContain "AI 게이트웨이 타임아웃"
                result.verseSource shouldBe "Error"
            }
        }
    }

    Given("XML 말씀 응답") {
        val fixture = BibleVerseServiceFixture(aiContent = "")
        val service = fixture.createService()
        val xml = """
            <data>
                <custom-verse>
                    커스텀말씀4
                </custom-verse>
                <original-verse>
                    원본말씀4
                </original-verse>
                <verse-source>
                    원본구절
                </verse-source>
            </data>
        """.trimIndent()

        When("XML 파싱") {
            val result = service.parseVerseXml(xml)

            Then("태그별 값을 trim 하여 DTO 형태로 반환") {
                result shouldBe BibleVerseDto(
                    customVerse = "커스텀말씀4",
                    originalVerse = "원본말씀4",
                    verseSource = "원본구절"
                )
            }
        }
    }
})

private class BibleVerseServiceFixture(
    private val aiContent: String?
) {
    val builder: ChatClient.Builder = mockk()
    val chatClient: ChatClient = mockk()
    val requestSpec: ChatClient.ChatClientRequestSpec = mockk()
    val callSpec: ChatClient.CallResponseSpec = mockk()
    val userPrompts = mutableListOf<String>()
    private val systemPrompt = slot<String>()

    fun createService(): BibleVerseService {
        every { builder.defaultSystem(capture(systemPrompt)) } returns builder
        every { builder.build() } returns chatClient
        every { chatClient.prompt() } returns requestSpec
        every { requestSpec.user(capture(userPrompts)) } returns requestSpec
        every { requestSpec.call() } returns callSpec
        every { callSpec.content() } returns aiContent

        return BibleVerseService(builder)
    }
}
