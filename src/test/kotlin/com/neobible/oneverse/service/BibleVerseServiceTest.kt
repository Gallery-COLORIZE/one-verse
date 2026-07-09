package com.neobible.oneverse.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.ai.chat.client.ChatClient

class BibleVerseServiceTest : BehaviorSpec({

    Given("오늘의 말씀을 요청하면") {
        val fixture = BibleVerseServiceFixture(
            aiContent = "오늘도 하나님의 은혜 안에 머무르십시오. - 시편 23:1"
        )
        val service = fixture.createService()

        When("타입이 TODAY로 전달된다") {
            val result = service.getBibleMessage("TODAY", null)

            Then("AI가 생성한 메시지를 반환한다") {
                result shouldBe "오늘도 하나님의 은혜 안에 머무르십시오. - 시편 23:1"
            }

            Then("오늘의 말씀 요청 프롬프트를 AI에 전달한다") {
                verify(exactly = 1) {
                    fixture.requestSpec.user(match<String> { it.isNotBlank() })
                }
                verify(exactly = 1) {
                    fixture.callSpec.content()
                }
            }
        }
    }

    Given("사용자가 자신의 상황을 입력하면") {
        val fixture = BibleVerseServiceFixture(
            aiContent = "두려움보다 하나님의 동행을 붙드십시오. - 여호수아 1:9"
        )
        val service = fixture.createService()
        val situationInput = "새로운 시작을 앞두고 두렵습니다."

        When("타입이 SITUATION으로 전달된다") {
            val result = service.getBibleMessage("SITUATION", situationInput)

            Then("AI가 생성한 메시지를 반환한다") {
                result shouldBe "두려움보다 하나님의 동행을 붙드십시오. - 여호수아 1:9"
            }

            Then("사용자의 상황을 그대로 AI 프롬프트로 전달한다") {
                verify(exactly = 1) {
                    fixture.requestSpec.user(situationInput)
                }
            }
        }
    }

    Given("AI 응답 본문이 비어 있으면") {
        val fixture = BibleVerseServiceFixture(aiContent = null)
        val service = fixture.createService()

        When("말씀을 요청한다") {
            val result = service.getBibleMessage("SITUATION", "위로가 필요합니다.")

            Then("기본 실패 메시지를 반환한다") {
                result shouldContain "응답"
            }
        }
    }

    Given("AI 호출 중 예외가 발생하면") {
        val builder = mockk<ChatClient.Builder>()
        val chatClient = mockk<ChatClient>()
        val requestSpec = mockk<ChatClient.ChatClientRequestSpec>()
        val systemPrompt = slot<String>()

        every { builder.defaultSystem(capture(systemPrompt)) } returns builder
        every { builder.build() } returns chatClient
        every { chatClient.prompt() } returns requestSpec
        every { requestSpec.user(any<String>()) } throws IllegalStateException("AI gateway timeout")

        val service = BibleVerseService(builder)

        When("말씀을 요청한다") {
            val result = service.getBibleMessage("SITUATION", "마음이 불안합니다.")

            Then("예외 메시지를 포함한 오류 안내를 반환한다") {
                result shouldContain "AI gateway timeout"
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
    private val systemPrompt = slot<String>()

    fun createService(): BibleVerseService {
        every { builder.defaultSystem(capture(systemPrompt)) } returns builder
        every { builder.build() } returns chatClient
        every { chatClient.prompt() } returns requestSpec
        every { requestSpec.user(any<String>()) } returns requestSpec
        every { requestSpec.call() } returns callSpec
        every { callSpec.content() } returns aiContent

        return BibleVerseService(builder)
    }
}
