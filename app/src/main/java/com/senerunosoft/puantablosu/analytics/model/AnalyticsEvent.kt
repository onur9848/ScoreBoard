package com.senerunosoft.puantablosu.analytics.model

/**
 * Generic analytics event model matching the backend API contract.
 * Keep [eventDetails] free of PII (phone, email, TCKN, etc.).
 */
data class AnalyticsEvent(
    val eventName: String,
    val eventType: String,
    val screenName: String? = null,
    val screenDurationMs: Long? = null,
    val sessionId: String,
    val identityId: String,
    val isFirstSession: Boolean,
    val occurredAtUtc: String,
    val platform: String = "android",
    val appVersion: String,
    val osVersion: String,
    val eventDetails: Map<String, Any>? = null
)
