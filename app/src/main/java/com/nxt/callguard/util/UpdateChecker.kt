package com.nxt.callguard.util

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val versionName: String, val downloadUrl: String, val releasePageUrl: String)

/**
 * Checks GitHub Releases for a newer CallGuard build. No backend of our own -
 * github.com/Voiben/CallGuard/releases/latest is the single source of truth
 * for "what is the current version".
 */
object UpdateChecker {
    private const val LATEST_RELEASE_API = "https://api.github.com/repos/Voiben/CallGuard/releases/latest"

    /** Null if up to date, on an unreleased/newer build, or the check failed (e.g. offline). */
    fun checkForUpdate(currentVersionName: String): UpdateInfo? {
        return try {
            val connection = URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode != 200) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val latestVersion = json.getString("tag_name").removePrefix("v")
            val releasePageUrl = json.getString("html_url")

            if (!isNewer(latestVersion, currentVersionName)) return null

            val assets = json.getJSONArray("assets")
            val apkAsset = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") }
                ?: return UpdateInfo(latestVersion, releasePageUrl, releasePageUrl)

            UpdateInfo(latestVersion, apkAsset.getString("browser_download_url"), releasePageUrl)
        } catch (e: Exception) {
            null
        }
    }

    /** Simple dotted-numeric version comparison (1.10.0 > 1.9.0), ignoring any non-numeric suffix. */
    private fun isNewer(candidate: String, current: String): Boolean {
        val c = candidate.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val cur = current.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(c.size, cur.size)) {
            val a = c.getOrElse(i) { 0 }
            val b = cur.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}
