package com.nxt.callguard.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PhoneNumberUtilsTest {

    @ParameterizedTest(name = "round {index}: normalize(\"{0}\") == \"{1}\"")
    @CsvSource(
        "0901234567, 0901234567",
        "+84901234567, 0901234567",
        "84901234567, 0901234567",
        "+84 90 123 4567, 0901234567",
        "090-123-4567, 0901234567",
        "(84) 901234567, 0901234567",
        "0123456789, 0123456789",
        "+840901234567, 00901234567", // malformed input with extra leading 0 after country code - documents actual (non-ideal) behavior
        "' 0901234567 ', 0901234567",
        "+84.90.123.4567, 0901234567",
        "+8428770, 028770" // short prefix rule (fewer digits than a full number) must still convert via '+'
    )
    fun `normalize handles common VN formats`(input: String, expected: String) {
        assertEquals(expected, PhoneNumberUtils.normalize(input))
    }

    @ParameterizedTest(name = "round {index}: short/garbage input \"{0}\" stays digits-only, no crash")
    @CsvSource(
        "'', ''",
        "abc, ''",
        "+, ''",
        "84, 84",
        "+84, 0",
        "----, ''"
    )
    fun `normalize never throws on edge-case input`(input: String, expected: String) {
        assertEquals(expected, PhoneNumberUtils.normalize(input))
    }

    @ParameterizedTest(name = "round {index}: \"{0}\" and \"{1}\" normalize to the same number")
    @CsvSource(
        "0901234567, +84901234567",
        "0901234567, 84901234567",
        "0931112222, +84 93 111 2222",
        "0981234567, (84)981234567"
    )
    fun `different formats of the same number are equal after normalization`(a: String, b: String) {
        assertEquals(PhoneNumberUtils.normalize(a), PhoneNumberUtils.normalize(b))
    }

    @ParameterizedTest(name = "round {index}: matchesAnyPrefix(\"{0}\") against seeded prefixes = {1}")
    @CsvSource(
        "0901234567, true",
        "+84901234567, true",
        "0909999999, false",
        "0241234567, false"
    )
    fun `matchesAnyPrefix detects numbers under a blocked prefix`(number: String, expected: Boolean) {
        val prefixes = listOf("0901", "0282")
        assertEquals(expected, PhoneNumberUtils.matchesAnyPrefix(number, prefixes))
    }

    @org.junit.jupiter.api.RepeatedTest(50)
    fun `normalize is idempotent across many random-ish inputs`(info: org.junit.jupiter.api.RepetitionInfo) {
        val seedNumber = "0${(900000000 + info.currentRepetition * 137) % 1000000000}"
        val once = PhoneNumberUtils.normalize(seedNumber)
        val twice = PhoneNumberUtils.normalize(once)
        assertEquals(once, twice, "normalize must be stable when re-applied (round ${info.currentRepetition})")
        assertTrue(once.all { it.isDigit() }, "result must be digits-only (round ${info.currentRepetition})")
        assertFalse(once.contains("+"), "result must not contain '+' (round ${info.currentRepetition})")
    }
}
