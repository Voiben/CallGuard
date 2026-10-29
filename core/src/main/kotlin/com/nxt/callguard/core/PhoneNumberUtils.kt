package com.nxt.callguard.core

object PhoneNumberUtils {
    /**
     * Normalizes a raw phone number to a comparable digits-only form.
     * Converts the Vietnam country code (+84 / 84) prefix to the local
     * leading-zero form so "+84901234567" and "0901234567" match.
     */
    fun normalize(raw: String): String {
        val hasExplicitCountryCode = raw.contains('+')
        val digits = raw.filter { it.isDigit() || it == '+' }
        val stripped = digits.trimStart('+')
        // An explicit "+" always means "84" is a country code, even for a short
        // prefix like "+8428770" (used for prefix-based blocking rules). Without
        // a "+", only treat a leading "84" as a country code once the string is
        // long enough to plausibly be a full number - otherwise a short local
        // number that happens to start with "84" would be mis-converted.
        val looksLikeCountryCode = stripped.startsWith("84") && (hasExplicitCountryCode || stripped.length > 9)
        return if (looksLikeCountryCode) "0" + stripped.removePrefix("84") else stripped
    }

    /** True if [number] starts with any prefix in [prefixes] after normalization. */
    fun matchesAnyPrefix(number: String, prefixes: Collection<String>): Boolean {
        val normalized = normalize(number)
        return prefixes.any { normalized.startsWith(normalize(it)) }
    }
}
