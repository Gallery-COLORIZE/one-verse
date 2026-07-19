package com.neobible.oneverse.controller

import com.neobible.oneverse.dto.BibleVerseDto
import com.neobible.oneverse.service.BibleCardService
import com.neobible.oneverse.service.BibleVerseService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.ui.ExtendedModelMap

class BibleVerseControllerTest : BehaviorSpec({

    Given("the index page is requested") {
        val bibleVerseService = mockk<BibleVerseService>()
        val bibleCardService = mockk<BibleCardService>()
        val controller = BibleVerseController(bibleVerseService, bibleCardService)

        When("index is called") {
            val viewName = controller.index()

            Then("the index view is returned") {
                viewName shouldBe "index"
            }
        }
    }

    Given("a verse message is requested") {
        val bibleVerseService = mockk<BibleVerseService>()
        val bibleCardService = mockk<BibleCardService>()
        val controller = BibleVerseController(bibleVerseService, bibleCardService)
        val model = ExtendedModelMap()
        val type = "SITUATION"
        val situationInput = "Some situation"
        val prevVerse = "Previous verse"
        val bibleVerse = BibleVerseDto(
            customVerse = "Custom verse",
            originalVerse = "Original verse",
            verseSource = "Source"
        )

        every {
            bibleVerseService.getBibleMessage(type, situationInput, prevVerse)
        } returns bibleVerse

        When("the message endpoint is called") {
            val viewName = controller.getMessage(type, situationInput, prevVerse, model)

            Then("the index view and model attributes are returned") {
                viewName shouldBe "index"
                model.asMap()["message"] shouldBe bibleVerse
                model.asMap()["type"] shouldBe type
                model.asMap()["situationInput"] shouldBe situationInput
            }

            Then("the service is called once") {
                verify(exactly = 1) {
                    bibleVerseService.getBibleMessage(type, situationInput, prevVerse)
                }
            }
        }
    }
})
