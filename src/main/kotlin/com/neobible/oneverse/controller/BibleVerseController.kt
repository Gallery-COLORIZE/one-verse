package com.neobible.oneverse.controller

import com.neobible.oneverse.dto.BibleVerseDto
import com.neobible.oneverse.service.BibleCardService
import com.neobible.oneverse.service.BibleVerseService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BibleVerseController (
    private val bibleVerseService: BibleVerseService,
    private val bibleCardService: BibleCardService
){

    @GetMapping("/")
    fun index() : String {
        return "index"
    }

    @PostMapping("/")
    fun getMessage(
        @RequestParam type: String,
        @RequestParam(required = false) situationInput: String?,
        @RequestParam(required = false) prevVerse: String?,
        model: Model
    ): String {

        val result = bibleVerseService.getBibleMessage(type, situationInput, prevVerse)


        model.addAttribute("message", result)
        model.addAttribute("type", type)
        model.addAttribute("situationInput", situationInput)
        return "index"
    }


    @PostMapping("/card")
    fun getCard(
        @RequestParam customVerse: String,
        @RequestParam originalVerse: String,
        @RequestParam verseSource: String,
        model: Model
    ): String {

        val verse = BibleVerseDto(
            customVerse = customVerse,
            originalVerse = originalVerse,
            verseSource = verseSource
        )

        val image = bibleCardService.generateVerseCardImageBase64(verse)
        model.addAttribute("message", verse)
        model.addAttribute("verseCard", image)
        return "index"
    }
}
