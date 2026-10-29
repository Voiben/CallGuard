package com.nxt.callguard.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.nxt.callguard.core.CallDecision
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end test of the REAL Android pieces (Room + android.content.Context
 * + the repository that CallScreeningService/SmsReceiver call into) running
 * under Robolectric - no emulator needed, but this exercises actual Room
 * queries and actual asset loading, not just the pure :core logic.
 *
 * Uses a plain stock [Application] (not [com.nxt.callguard.CallGuardApp])
 * so each test gets its own isolated in-memory-backed database instead of
 * racing against CallGuardApp.onCreate()'s own background seeding coroutine,
 * which runs on a real thread pool and is not scoped to a single test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class CallGuardRepositoryTest {

    private val repository: CallGuardRepository by lazy {
        val context: Application = ApplicationProvider.getApplicationContext()
        CallGuardRepository(context, AppDatabase.createInMemoryForTesting(context))
    }

    @Test
    fun `a manually added number is blocked end-to-end`() = runTest {
        val repo = repository
        assertEquals(false, repo.isBlocked("0901234567"))

        repo.addToBlocklist("0901234567", "test")

        assertEquals(true, repo.isBlocked("0901234567"))
        assertEquals("different format of the same number must also be blocked", true, repo.isBlocked("+84901234567"))
    }

    @Test
    fun `removing a number allows it again`() = runTest {
        val repo = repository
        repo.addToBlocklist("0909999999", "test")
        assertEquals(true, repo.isBlocked("0909999999"))

        val entry = BlockedNumber("0909999999", "test", BlockSource.MANUAL)
        repo.removeFromBlocklist(entry)

        assertEquals(false, repo.isBlocked("0909999999"))
    }

    @Test
    fun `a number reported twice auto-escalates to blocked, once does not`() = runTest {
        val repo = repository
        assertEquals(false, repo.isBlocked("0912345678"))

        repo.reportNumber("0912345678", SpamCategory.LUA_DAO_TAI_CHINH, "lần 1")
        assertEquals("one report must not be enough to auto-block", false, repo.isBlocked("0912345678"))

        repo.reportNumber("0912345678", SpamCategory.LUA_DAO_TAI_CHINH, "lần 2")
        assertEquals("second report must auto-escalate to blocked", true, repo.isBlocked("0912345678"))
    }

    @Test
    fun `seeding loads the bundled sample prefix file from real Android assets`() = runTest {
        val repo = repository
        repo.seedIfEmpty()

        val seeded = repo.decide("+84287700000")
        assertEquals(
            "a number under the seeded sample prefix must be blocked after seedIfEmpty()",
            CallDecision.BLOCK,
            seeded.decision
        )
    }

    @Test
    fun `spam sms keywords load from real Android assets and are usable`() {
        val repo = repository
        val keywords = repo.loadSpamKeywords()
        assertTrue("expected at least one seeded keyword", keywords.isNotEmpty())
        assertTrue(
            "expected a known seed keyword to be present",
            keywords.any { it.contains("trúng thưởng") }
        )
    }

    @Test
    fun `activity log records block decisions and feeds the today counter`() = runTest {
        val repo = repository
        repo.addToBlocklist("0933334444", "test")

        repeat(3) {
            repo.logActivity("0933334444", ActivityType.CALL, ActivityAction.BLOCKED, "round $it")
        }
        repo.logActivity("0999999999", ActivityType.CALL, ActivityAction.ALLOWED, "not blocked")

        assertEquals(3, repo.blockedTodayCount())
    }
}
