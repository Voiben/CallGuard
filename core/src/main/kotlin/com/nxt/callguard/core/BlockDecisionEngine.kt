package com.nxt.callguard.core

enum class CallDecision { BLOCK, ALLOW }

data class CallVerdict(val decision: CallDecision, val reason: String)

/**
 * Pure decision logic for whether an incoming call number should be blocked.
 * No Android/telecom dependency - exists so it can be unit-tested on the
 * plain JVM and reused identically by the real CallScreeningService.
 */
object BlockDecisionEngine {

    fun decide(
        rawNumber: String,
        exactBlockedNumbers: Set<String>,
        blockedPrefixes: Collection<String>
    ): CallVerdict {
        val normalized = PhoneNumberUtils.normalize(rawNumber)

        if (normalized.isBlank()) {
            return CallVerdict(CallDecision.ALLOW, "Số rỗng/không xác định - cho qua để tránh chặn nhầm")
        }

        if (exactBlockedNumbers.contains(normalized)) {
            return CallVerdict(CallDecision.BLOCK, "Số nằm trong danh sách chặn")
        }

        if (PhoneNumberUtils.matchesAnyPrefix(normalized, blockedPrefixes)) {
            return CallVerdict(CallDecision.BLOCK, "Khớp đầu số bị chặn")
        }

        return CallVerdict(CallDecision.ALLOW, "Không khớp danh sách/đầu số chặn")
    }
}

/**
 * Pure SMS content scanner - flags a message when it contains any of the
 * configured spam keywords (case-insensitive, substring match).
 */
object SpamKeywordMatcher {
    fun findMatch(body: String, keywords: Collection<String>): String? {
        val lower = body.lowercase()
        return keywords.firstOrNull { lower.contains(it.lowercase()) }
    }
}

/**
 * Pure escalation rule for community reports: a number is auto-escalated to
 * the blocklist once it has been reported at least [threshold] times.
 */
object ReportEscalation {
    const val DEFAULT_THRESHOLD = 2

    fun shouldEscalate(reportCountForNumber: Int, threshold: Int = DEFAULT_THRESHOLD): Boolean =
        reportCountForNumber >= threshold
}
