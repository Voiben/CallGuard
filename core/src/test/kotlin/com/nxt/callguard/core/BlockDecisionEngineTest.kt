package com.nxt.callguard.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class BlockDecisionEngineTest {

    private val exactBlocked = setOf("0901234567", "0987654321", "0281112222")
    private val blockedPrefixes = listOf("0282", "02838", "0399")

    data class Case(val number: String, val expected: CallDecision, val label: String)

    companion object {
        @JvmStatic
        fun cases(): Stream<Case> = Stream.of(
            Case("0901234567", CallDecision.BLOCK, "exact match, local format"),
            Case("+84901234567", CallDecision.BLOCK, "exact match, +84 format"),
            Case("84901234567", CallDecision.BLOCK, "exact match, 84 format (no plus)"),
            Case("0987654321", CallDecision.BLOCK, "second exact-blocked number"),
            Case("0282999888", CallDecision.BLOCK, "prefix match 0282"),
            Case("+84282999888", CallDecision.BLOCK, "prefix match 0282, +84 format"),
            Case("0283899999", CallDecision.BLOCK, "prefix match 02838 (more specific prefix)"),
            Case("0399111222", CallDecision.BLOCK, "prefix match 0399"),
            Case("0909999999", CallDecision.ALLOW, "unrelated number, should pass"),
            Case("0241234567", CallDecision.ALLOW, "landline not in any list"),
            Case("", CallDecision.ALLOW, "empty number must never crash/incorrectly block"),
            Case("0281112222", CallDecision.BLOCK, "third exact-blocked number, different prefix family"),
            Case("0280000000", CallDecision.ALLOW, "near-miss: shares digits with blocked number but different prefix family"),
        )
    }

    @ParameterizedTest(name = "round {index}: {0}.label -> {0}.expected")
    @MethodSource("cases")
    fun `decide blocks or allows as expected`(case: Case) {
        val verdict = BlockDecisionEngine.decide(case.number, exactBlocked, blockedPrefixes)
        assertEquals(case.expected, verdict.decision, "Case [${case.label}] number=${case.number} -> got ${verdict.decision} (${verdict.reason})")
    }

    @Test
    fun `a number removed from the blocklist is allowed again`() {
        val afterRemoval = exactBlocked - "0901234567"
        val verdict = BlockDecisionEngine.decide("0901234567", afterRemoval, blockedPrefixes)
        assertEquals(CallDecision.ALLOW, verdict.decision)
    }

    @Test
    fun `every known-bad number across 200 generated variants is still caught`() {
        val variants = (0 until 200).map { i ->
            when (i % 4) {
                0 -> "0901234567"
                1 -> "+84901234567"
                2 -> "84901234567"
                else -> "090 123 4567".let { it } // spaced variant, still maps to the same number
            }
        }
        variants.forEachIndexed { index, number ->
            val verdict = BlockDecisionEngine.decide(number, exactBlocked, blockedPrefixes)
            assertEquals(CallDecision.BLOCK, verdict.decision, "round $index failed for variant \"$number\"")
        }
    }
}
