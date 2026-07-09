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
        return "index";
    }

    @PostMapping("/")
    fun getMessage(
        @RequestParam type: String, // "TODAY" 또는 "SITUATION"
        @RequestParam(required = false) situationInput: String?,
        model: Model
    ): String {



        // AI 서비스 계층 호출
        val result = bibleVerseService.getBibleMessage(type, situationInput)

        // 1. 메시지 모델에 추가
        model.addAttribute("message", result)

        // 입력값 유지를 위해
        model.addAttribute("type", type)

        return "index"
    }
}