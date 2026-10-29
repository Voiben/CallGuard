package com.nxt.callguard.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReportEscalationTest {

    @ParameterizedTest(name = "round {index}: {0} report(s) -> escalate={1}")
    @CsvSource(
        "0, false",
        "1, false",
        "2, true",
        "3, true",
        "10, true"
    )
    fun `default threshold escalates at 2 reports`(reportCount: Int, expected: Boolean) {
        assertEquals(expected, ReportEscalation.shouldEscalate(reportCount))
    }

    @ParameterizedTest(name = "round {index}: threshold={1}, count={0} -> escalate={2}")
    @CsvSource(
        "5, 5, true",
        "4, 5, false",
        "1, 1, true",
        "0, 1, false"
    )
    fun `custom thresholds are respected`(reportCount: Int, threshold: Int, expected: Boolean) {
        assertEquals(expected, ReportEscalation.shouldEscalate(reportCount, threshold))
    }
}
