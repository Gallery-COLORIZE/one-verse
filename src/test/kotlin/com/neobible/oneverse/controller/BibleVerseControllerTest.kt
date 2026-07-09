package com.neobible.oneverse.controller

import com.neobible.oneverse.service.BibleVerseService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.ui.ExtendedModelMap

class BibleVerseControllerTest : BehaviorSpec({

    Given("사용자가 첫 화면에 접근하면") {
        val bibleVerseService = mockk<BibleVerseService>()
        val controller = BibleVerseController(bibleVerseService)

        When("index를 요청한다") {
            val viewName = controller.index()

            Then("index 화면을 반환한다") {
                viewName shouldBe "index"
            }
        }
    }

    Given("사용자가 상황을 입력하고 말씀을 요청하면") {
        val bibleVerseService = mockk<BibleVerseService>()
        val controller = BibleVerseController(bibleVerseService)
        val model = ExtendedModelMap()
        val type = "SITUATION"
        val situationInput = "새로운 시작을 앞두고 두렵습니다."
        val aiMessage = "두려움보다 하나님의 동행을 붙드십시오. - 여호수아 1:9"

        every {
            bibleVerseService.getBibleMessage(type, situationInput)
        } returns aiMessage

        When("메시지를 생성한다") {
            val viewName = controller.getMessage(type, situationInput, model)

            Then("index 화면에 AI 메시지와 선택 타입을 담아 반환한다") {
                viewName shouldBe "index"
                model.asMap()["message"] shouldBe aiMessage
                model.asMap()["type"] shouldBe type
            }

            Then("서비스에 사용자의 입력을 그대로 전달한다") {
                verify(exactly = 1) {
                    bibleVerseService.getBibleMessage(type, situationInput)
                }
            }
        }
    }
})
