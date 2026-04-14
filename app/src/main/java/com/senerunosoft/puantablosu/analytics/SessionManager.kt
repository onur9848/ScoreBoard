package com.senerunosoft.puantablosu.analytics

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Manages persistent identity and per-session identifiers required by the analytics spec:
 *  - [identityId]   – generated once on first launch, survives app updates, reset on reinstall.
 *  - [sessionId]    – generated freshly every time [startNewSession] is called (i.e. each app open).
 *  - [isFirstSession] – true only on the very first launch after install.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Stable device/user identifier. Created once, persisted until app is uninstalled. */
    val identityId: String by lazy {
        prefs.getString(KEY_IDENTITY_ID, null) ?: UUID.randomUUID().toString().also { newId ->
            prefs.edit().putString(KEY_IDENTITY_ID, newId).apply()
        }
    }

    /** True only on the first ever session after a fresh install. */
    val isFirstSession: Boolean by lazy {
        val first = !prefs.contains(KEY_HAS_LAUNCHED)
        if (first) {
            prefs.edit().putBoolean(KEY_HAS_LAUNCHED, true).apply()
        }
        first
    }

    /** Current session ID. Replaced each time [startNewSession] is called. */
    var sessionId: String = UUID.randomUUID().toString()
        private set

    /** Call at application start to generate a fresh [sessionId] for this session. */
    fun startNewSession() {
        sessionId = UUID.randomUUID().toString()
    }

    companion object {
        private const val PREFS_NAME = "analytics_session"
        private const val KEY_IDENTITY_ID = "identity_id"
        private const val KEY_HAS_LAUNCHED = "has_launched"
    }
}
