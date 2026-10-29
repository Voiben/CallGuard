package com.nxt.callguard.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SpamKeywordMatcherTest {

    private val keywords = listOf("trúng thưởng", "vay tiền nhanh", "chuyển khoản gấp", "link xác thực")

    @ParameterizedTest(name = "round {index}: body=\"{0}\" -> matched=\"{1}\"")
    @CsvSource(
        delimiter = '|',
        value = [
            "Chúc mừng bạn đã TRÚNG THƯỞNG xe máy|trúng thưởng",
            "Ban can VAY TIEN NHANH khong the hien bang tieng viet co dau|",
            "Vui lòng chuyển khoản gấp trước 17h|chuyển khoản gấp",
            "Nhấn vào Link Xác Thực để mở lại tài khoản|link xác thực",
            "Chào bạn, mai họp lúc 9h nhé|",
            "|"
        ]
    )
    fun `findMatch is case-insensitive and substring based`(body: String?, expectedMatch: String?) {
        val result = SpamKeywordMatcher.findMatch(body ?: "", keywords)
        if (expectedMatch.isNullOrBlank()) {
            assertNull(result, "expected no match for body=\"$body\" but got \"$result\"")
        } else {
            assertEquals(expectedMatch, result)
        }
    }

    @org.junit.jupiter.api.RepeatedTest(30)
    fun `clean messages never false-positive across many rounds`(info: org.junit.jupiter.api.RepetitionInfo) {
        val cleanBodies = listOf(
            "Con da den truong an toan",
            "Hop luc ${info.currentRepetition} gio sang mai",
            "Giao hang thanh cong don hang #${info.currentRepetition}",
            "Nhac nho lich hop du an tuan nay"
        )
        val body = cleanBodies[info.currentRepetition % cleanBodies.size]
        assertNull(SpamKeywordMatcher.findMatch(body, keywords), "round ${info.currentRepetition}: false positive on \"$body\"")
    }
}
