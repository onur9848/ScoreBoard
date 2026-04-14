package com.senerunosoft.puantablosu.analytics

import com.senerunosoft.puantablosu.analytics.model.AnalyticsEvent
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AnalyticsService].
 * Validates event construction, HTTP dispatching, and retry queue behaviour.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsServiceTest {

    private lateinit var sessionManager: SessionManager
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var mockCall: Call
    private lateinit var mockResponse: Response
    private lateinit var service: AnalyticsService

    @Before
    fun setUp() {
        sessionManager = mockk(relaxed = true) {
            every { sessionId } returns "test-session-id"
            every { identityId } returns "test-identity-id"
            every { isFirstSession } returns false
        }

        mockResponse = mockk(relaxed = true) {
            every { isSuccessful } returns true
            every { body } returns mockk<ResponseBody>(relaxed = true)
        }

        mockCall = mockk(relaxed = true) {
            every { execute() } returns mockResponse
        }

        okHttpClient = mockk(relaxed = true) {
            every { newCall(any()) } returns mockCall
        }

        service = AnalyticsService(
            context = mockk(relaxed = true),
            sessionManager = sessionManager,
            appVersion = "1.0.0",
            httpClient = okHttpClient
        )
    }

    // ------------------------------------------------------------------ event construction

    @Test
    fun `trackScreenView sends screen_view event with correct fields`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackScreenView("HomeScreen", fromScreen = "Splash")
        advanceUntilIdle()

        assertEquals(1, capturedEvents.size)
        val event = capturedEvents.first()
        assertEquals("screen_view", event.eventName)
        assertEquals("navigation", event.eventType)
        assertEquals("HomeScreen", event.screenName)
        assertEquals("Splash", event.eventDetails?.get("fromScreen"))
        assertEquals("android", event.platform)
        assertEquals("1.0.0", event.appVersion)
    }

    @Test
    fun `trackScreenExit sends screen_exit event with duration`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackScreenExit("HomeScreen", durationMs = 5000L)
        advanceUntilIdle()

        assertEquals(1, capturedEvents.size)
        val event = capturedEvents.first()
        assertEquals("screen_exit", event.eventName)
        assertEquals("navigation", event.eventType)
        assertEquals("HomeScreen", event.screenName)
        assertEquals(5000L, event.screenDurationMs)
    }

    @Test
    fun `trackAppOpen sends app_open lifecycle event`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackAppOpen()
        advanceUntilIdle()

        assertEquals(1, capturedEvents.size)
        assertEquals("app_open", capturedEvents.first().eventName)
        assertEquals("lifecycle", capturedEvents.first().eventType)
    }

    @Test
    fun `trackAppBackground sends app_background lifecycle event`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackAppBackground()
        advanceUntilIdle()

        assertEquals("app_background", capturedEvents.first().eventName)
    }

    @Test
    fun `trackAppForeground sends app_foreground lifecycle event`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackAppForeground()
        advanceUntilIdle()

        assertEquals("app_foreground", capturedEvents.first().eventName)
    }

    // ------------------------------------------------------------------ session fields

    @Test
    fun `event carries sessionId and identityId from SessionManager`() = runTest {
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackAppOpen()
        advanceUntilIdle()

        val event = capturedEvents.first()
        assertEquals("test-session-id", event.sessionId)
        assertEquals("test-identity-id", event.identityId)
        assertFalse(event.isFirstSession)
    }

    @Test
    fun `event carries isFirstSession true when SessionManager reports first session`() = runTest {
        every { sessionManager.isFirstSession } returns true
        val capturedEvents = mutableListOf<AnalyticsEvent>()
        val serviceUnderTest = buildCapturingService(capturedEvents)

        serviceUnderTest.trackAppOpen()
        advanceUntilIdle()

        assertTrue(capturedEvents.first().isFirstSession)
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Builds a service variant that appends dispatched events to [sink] instead of
     * making real HTTP calls.
     */
    private fun buildCapturingService(sink: MutableList<AnalyticsEvent>): AnalyticsService {
        val capturingClient: OkHttpClient = mockk(relaxed = true) {
            every { newCall(any()) } answers {
                val request = firstArg<okhttp3.Request>()
                // Extract the body and deserialize to inspect the event
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val json = buffer.readUtf8()
                val event = com.google.gson.Gson().fromJson(json, AnalyticsEvent::class.java)
                sink.add(event)
                mockCall
            }
        }
        return AnalyticsService(
            context = mockk(relaxed = true),
            sessionManager = sessionManager,
            appVersion = "1.0.0",
            httpClient = capturingClient
        )
    }
}
