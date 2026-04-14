package com.senerunosoft.puantablosu.analytics

import com.senerunosoft.puantablosu.analytics.model.AnalyticsEvent

/**
 * Contract for the analytics service.
 * All methods are fire-and-forget; callers do not wait for network results.
 */
interface IAnalyticsService {
    /** Send an arbitrary [AnalyticsEvent]. */
    fun sendEvent(event: AnalyticsEvent)

    /** Convenience: track that a screen became visible. */
    fun trackScreenView(screenName: String, fromScreen: String? = null)

    /** Convenience: track that the user left a screen after [durationMs] milliseconds. */
    fun trackScreenExit(screenName: String, durationMs: Long)

    /** Convenience: track the app-open lifecycle event. */
    fun trackAppOpen()

    /** Convenience: track the app going to the background. */
    fun trackAppBackground()

    /** Convenience: track the app returning to the foreground (after being backgrounded). */
    fun trackAppForeground()
}
