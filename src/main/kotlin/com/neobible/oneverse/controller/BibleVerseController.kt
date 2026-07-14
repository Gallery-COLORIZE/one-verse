package com.neobible.oneverse.controller

import com.neobible.oneverse.service.BibleVerseService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BibleVerseController (
    private val bibleVerseService: BibleVerseService
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
}
