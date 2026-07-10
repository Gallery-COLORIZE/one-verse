package com.neobible.oneverse.controller

import com.neobible.oneverse.dto.BibleVerseDto
import com.neobible.oneverse.service.BibleVerseService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.ui.ExtendedModelMap

class BibleVerseControllerTest : BehaviorSpec({

    Given("첫 화면 요청") {
        val bibleVerseService = mockk<BibleVerseService>()
        val controller = BibleVerseController(bibleVerseService)

        When("index 요청") {
            val viewName = controller.index()

            Then("index 화면 반환") {
                viewName shouldBe "index"
            }
        }
    }

    Given("상황별 말씀 요청") {
        val bibleVerseService = mockk<BibleVerseService>()
        val controller = BibleVerseController(bibleVerseService)
        val model = ExtendedModelMap()
        val type = "SITUATION"
        val situationInput = "기분이 안좋아"
        val bibleVerse = BibleVerseDto(
            customVerse = "커스텀말씀",
            originalVerse = "원본말씀",
            verseSource = "원본구절"
        )

        every {
            bibleVerseService.getBibleMessage(type, situationInput)
        } returns bibleVerse

        When("메시지 생성") {
            val viewName = controller.getMessage(type, situationInput, model)

            Then("index 화면에 말씀 DTO와 선택 타입을 담아 반환") {
                viewName shouldBe "index"
                model.asMap()["message"] shouldBe bibleVerse
                model.asMap()["type"] shouldBe type
            }

            Then("서비스에 사용자 입력을 전달") {
                verify(exactly = 1) {
                    bibleVerseService.getBibleMessage(type, situationInput)
                }
            }
        }
    }
})
