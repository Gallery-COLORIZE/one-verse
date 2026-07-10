package com.neobible.oneverse.service

import com.neobible.oneverse.dto.BibleVerseDto
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
        5. 데이터 양식은 
        <data>
        <costom-verse>
            커스텀 말씀
        </costom-verse>
        <original-verse>
            원본 말씀
        </original-verse>
        <verse-source>
            구절
        </verse-source>
        </data>
        형식을 따르시오.
        4. 예시: 
            - 사용자의 상황: 우울할 때
            - 응답: 
            <data>
                <custom-verse>
                    내가 너와 끝까지 함께한다.
                </custom-verse>
                <original-verse>
                    두려워 말라 내가 너와 함께 함이니라 놀라지 말라 나는 네 하나님이 됨이니라 내가 너를 굳세게 하리라 참으로 너를 도와 주리라 참으로 나의 의로운 오른손으로 너를 붙들리라
                </original-verse>
                <verse-source>
                    이사야 41:10
                </verse-source>
            </data>

            - 사용자의 상황: 새로운 시작을 앞두고 있어
            
            - 응답:
            <data>
                <custom-verse>
                    두려워 마라, 내가 너의 길을 열겠다.
                </custom-verse>
                <original-verse>
                    내가 네게 명령한 것이 아니냐 강하고 담대하라 두려워하지 말며 놀라지 말라 네가 어디로 가든지 네 하나님 여호와가 너와 함께 하느니라 하시니라
                </original-verse>
                <verse-source>
                    여호수아 1:9
                </verse-source>
            </data>
            - 사용자의 상황: 죄책감이 들어
            - 응답:
            <data>
                <custom-verse>
                    너의 모든 허물을 내가 덮었다. 자유하라.
                </custom-verse>
                <original-verse>
                    그러므로 이제 그리스도 예수 안에 있는 자에게는 결코 정죄함이 없나니 이는 그리스도 예수 안에 있는 생명의 성령의 법이 죄와 사망의 법에서 1)너를 해방하였음이라
                </original-verse>
                <verse-source>
                    로마서 8:1-2
                </verse-source>
            </data>
    """.trimIndent()

    private val chatClient: ChatClient = chatClientBuilder
        .defaultSystem(systemPrompt)
        .build()

    /**
     * 성경 구절 출력
     */
    fun getBibleMessage(type: String, userInput: String?): BibleVerseDto {
        return try {
            val prompt = if (type == "TODAY") {
                "성경에서 지혜와 위로, 삶의 도전이 되는 구절을 하나 골라 정해진 형식으로 출력해줘."
            } else {
                userInput ?: "상황이 입력되지 않았습니다."
            }

            val xmlResult = chatClient.prompt()
                .user(prompt)
                .call()
                .content()

            log.info("verse: $xmlResult")

            if (xmlResult.isNullOrBlank()) {
                return BibleVerseDto(
                        customVerse = "",
                        originalVerse = "응답을 생성하지 못했습니다.",
                        verseSource = "Error"
                    )
            }

            parseVerseXml(xmlResult)
        } catch (e: Exception) {
            // 3. 예외 발생 시 에러 메시지를 담은 기본 DTO 반환
            BibleVerseDto(
                customVerse = "",
                originalVerse = "말씀을 준비하는 중에 오류가 발생했습니다: ${e.message}",
                verseSource = "Error"
            )
        }
    }

    fun parseVerseXml(xmlString: String): BibleVerseDto {
        // xml 데이터 추출
        fun extractTagContent(tag: String): String {
            val regex = "<$tag>\\s*([\\s\\S]*?)\\s*</$tag>".toRegex()
            return regex.find(xmlString)?.groups?.get(1)?.value?.trim() ?: ""
        }

        return BibleVerseDto(
            customVerse = extractTagContent("custom-verse"),
            originalVerse = extractTagContent("original-verse"),
            verseSource = extractTagContent("verse-source")
        )
    }
}