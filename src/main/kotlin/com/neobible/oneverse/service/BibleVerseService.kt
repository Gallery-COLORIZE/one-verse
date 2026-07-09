package com.neobible.oneverse.service

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class BibleVerseService(
    chatClientBuilder: ChatClient.Builder
) {
    private val log = LoggerFactory.getLogger(BibleVerseService::class.java)

    private val systemPrompt = """
        당신은 복음주의 신학에 기반하여 성경의 메시지를 현대인에게 임팩트 있게 전달하는 사역자이자 음악 감독입니다.
        규칙:
        1. 입력된 상황이나 요청에 가장 적합한 성경 구절 하나를 선택하십시오.
        2. 그 구절의 핵심 의미를 한 줄의 강력한 문장으로 요약하십시오. (성경 원문 그대로 인용하지 말고, 신학적 깊이를 담아 현대적으로 재구성하십시오.)
        3. 요약된 문장 뒤에 반드시 '- 성경구절' 형태로 출처를 기입하십시오.
        4. 예시: 
            - 사용자의 상황: 우울할 때
            - 응답: 내가 너와 끝까지 함께한다. - 이사야 41:10
            - 사용자의 상황: 새로운 시작을 앞두고 있어
            - 응답: 두려워 마라, 내가 너의 길을 열겠다. - 여호수아 1:9
            - 사용자의 상황: 죄책감이 들어
            - 응답: 너의 모든 허물을 내가 덮었다. 자유하라. - 로마서 8:1
    """.trimIndent()

    private val chatClient: ChatClient = chatClientBuilder
        .defaultSystem(systemPrompt)
        .build()

    /**
     * 성경 구절 출력
     */
    fun getBibleMessage(type: String, userInput: String?): String {
        return try {
            val prompt = if (type == "TODAY") {
                "성경에서 지혜와 위로, 삶의 도전이 되는 구절을 하나 골라 정해진 형식으로 출력해줘."
            } else {
                userInput ?: "상황이 입력되지 않았습니다."
            }

            chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: "응답을 생성하지 못했습니다."
        } catch (e: Exception) {
            "말씀을 준비하는 중에 오류가 발생했습니다: ${e.message}"
        }
    }
}