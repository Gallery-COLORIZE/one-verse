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
        당신은 복음주의 신학에 기반하여 성경의 메시지를 현대인에게 임팩트 있게 전달하는 사역자입니다.
        규칙:
        1. 입력된 상황이나 요청에 가장 적합한 성경 구절을 선택하십시오.
        2. 구절의 핵심을 복음주의 신학에 의거해 현대적으로 재구성하십시오.
        3. [문체 지침 - 핵심] 문체를 구성할 때는 Ernest Hemingway(45%), Albert Camus(45%), Friedrich Nietzsche(10%)의 비율을 혼합하십시오.
            3.1 헤밍웨이, 카뮈, 니체 특유의 거친 문학적 은유는 풍부하게 살리십시오.
            3.1.2 헤밍웨이의 Iceberg Theory를 문맥에 맞게 적용하십시오. 만약 문맥에 맞지 않다면 적용하지 마십시오.
            3.2 단, 친절하게 설명하거나 설득하려 하지 마십시오. ('그러면 비로소', '그렇지 않으면', '결국 ~할 뿐' 같은 접속사와 부연 설명을 철저히 배제할 것)
            3.3 철학적 무게감을 잃지 않으면서도, 3~4문장 분량의 단호하고 무게감 있는 단문으로 끊어 치십시오.
            3.4 이 말씀은 성경과 기독교를 기반으로 합니다. '신'과 같은 범 종교적인 단어 대신 '여호와', '하나님', '나(화자를 여호와로 가정)' 과 같은 단어를 사용하십시오.
        4. 데이터 양식은 
        <data>
        <custom-verse>
            커스텀 말씀
        </custom-verse>
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
    """.trimIndent()

    private val chatClient: ChatClient = chatClientBuilder
        .defaultSystem(systemPrompt)
        .build()

    /**
     * 성경 구절 출력
     */
    fun getBibleMessage(type: String, userInput: String?, prevVerse: String? = null): BibleVerseDto {
        return try {
            val basePrompt = if (type == "TODAY") {
                "성경에서 삶의 지혜와 위로, 감사가 되는 구절을 하나 골라 정해진 형식으로 출력해줘."
            } else {
                userInput ?: "상황이 입력되지 않았습니다."
            }
            val prompt = buildPromptExcludingPreviousVerse(basePrompt, prevVerse)

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
            BibleVerseDto(
                customVerse = "",
                originalVerse = "말씀을 준비하는 중에 오류가 발생했습니다: ${e.message}",
                verseSource = "Error"
            )
        }
    }

    private fun buildPromptExcludingPreviousVerse(basePrompt: String, prevVerse: String?): String {
        val verse = prevVerse?.trim()
        if (verse.isNullOrBlank()) {
            return basePrompt
        }

        return """
            $basePrompt

            이전에 추천한 말씀은 제외하고 다른 성경 구절을 선택하십시오.
            제외할 이전 말씀:
            - 출처: $verse
        """.trimIndent()
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
