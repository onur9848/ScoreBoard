package com.senerunosoft.puantablosu.analytics

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.senerunosoft.puantablosu.analytics.model.AnalyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Production analytics service.
 *
 * Design:
 *  - Fire-and-forget: callers are never blocked.
 *  - Offline resilience: failed events are held in an in-memory queue and
 *    retried with exponential back-off.
 *  - Privacy: never place PII in [AnalyticsEvent.eventDetails].
 */
class AnalyticsService(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val sessionManager: SessionManager,
    private val appVersion: String,
    private val httpClient: OkHttpClient = OkHttpClient()
) : IAnalyticsService, java.io.Closeable {

    private val gson = Gson()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private val retryQueue = ConcurrentLinkedQueue<AnalyticsEvent>()

    init {
        startRetryWorker()
    }

    // -----------------------------------------------------------------  public API

    override fun sendEvent(event: AnalyticsEvent) {
        scope.launch { dispatch(event) }
    }

    override fun trackScreenView(screenName: String, fromScreen: String?) {
        val details = buildMap<String, Any> {
            fromScreen?.let { put("fromScreen", it) }
        }
        sendEvent(buildEvent("screen_view", "navigation", screenName, eventDetails = details.ifEmpty { null }))
    }

    override fun trackScreenExit(screenName: String, durationMs: Long) {
        sendEvent(buildEvent("screen_exit", "navigation", screenName, screenDurationMs = durationMs))
    }

    override fun trackAppOpen() {
        sendEvent(buildEvent("app_open", "lifecycle"))
    }

    override fun trackAppBackground() {
        sendEvent(buildEvent("app_background", "lifecycle"))
    }

    override fun trackAppForeground() {
        sendEvent(buildEvent("app_foreground", "lifecycle"))
    }

    override fun trackInteraction(
        screenName: String,
        actionName: String,
        eventDetails: Map<String, Any>?
    ) {
        val details = buildMap<String, Any> {
            put("action", actionName)
            eventDetails?.forEach { (key, value) -> put(key, value) }
        }
        sendEvent(
            buildEvent(
                eventName = "interaction",
                eventType = "interaction",
                screenName = screenName,
                eventDetails = details
            )
        )
    }

    override fun trackGameplayAction(
        screenName: String,
        actionName: String,
        eventDetails: Map<String, Any>?
    ) {
        val details = buildMap<String, Any> {
            put("action", actionName)
            eventDetails?.forEach { (key, value) -> put(key, value) }
        }
        sendEvent(
            buildEvent(
                eventName = "gameplay_action",
                eventType = "gameplay",
                screenName = screenName,
                eventDetails = details
            )
        )
    }

    // -----------------------------------------------------------------  internals

    private fun buildEvent(
        eventName: String,
        eventType: String,
        screenName: String? = null,
        screenDurationMs: Long? = null,
        eventDetails: Map<String, Any>? = null
    ) = AnalyticsEvent(
        eventName = eventName,
        eventType = eventType,
        screenName = screenName,
        screenDurationMs = screenDurationMs,
        sessionId = sessionManager.sessionId,
        identityId = sessionManager.identityId,
        isFirstSession = sessionManager.isFirstSession,
        occurredAtUtc = Instant.now().toString(),
        appVersion = appVersion,
        osVersion = Build.VERSION.RELEASE,
        eventDetails = eventDetails
    )

    private suspend fun dispatch(event: AnalyticsEvent, attempt: Int = 0) {
        val success = post(event)
        if (!success) {
            if (attempt < MAX_RETRIES) {
                delay(backOffMs(attempt))
                dispatch(event, attempt + 1)
            } else {
                retryQueue.offer(event)
            }
        }
    }

    private fun post(event: AnalyticsEvent): Boolean {
        return try {
            val json = gson.toJson(event)
            val body = json.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$BASE_URL$ENDPOINT?app=$APP_QUERY_PARAM")
                .post(body)
                .build()
            httpClient.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private fun startRetryWorker() {
        scope.launch {
            while (true) {
                delay(RETRY_WORKER_INTERVAL_MS)
                flushRetryQueue()
            }
        }
    }

    private suspend fun flushRetryQueue() {
        // Drain a fixed snapshot so that any events re-queued during this flush
        // are deferred to the next flush cycle rather than retried immediately.
        val snapshot = mutableListOf<AnalyticsEvent>()
        var event = retryQueue.poll()
        while (event != null) {
            snapshot.add(event)
            event = retryQueue.poll()
        }
        snapshot.forEach { dispatch(it) }
    }

    private fun backOffMs(attempt: Int): Long = minOf(INITIAL_BACK_OFF_MS * (1L shl attempt), MAX_BACK_OFF_MS)

    /** Cancels the background retry worker and releases resources. */
    override fun close() {
        job.cancel()
    }

    companion object {
        private const val BASE_URL = "https://portfoy-history.senerunosoft.com"
        private const val ENDPOINT = "/api/mobile-analytics/save-event"
        private const val APP_QUERY_PARAM = "PuanTablosu"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACK_OFF_MS = 2_000L
        private const val MAX_BACK_OFF_MS = 30_000L
        private const val RETRY_WORKER_INTERVAL_MS = 60_000L
    }
}
