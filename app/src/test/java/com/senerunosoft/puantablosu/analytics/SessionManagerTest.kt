package com.senerunosoft.puantablosu.analytics

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SessionManager].
 * Validates identity/session lifecycle as specified in the analytics requirements.
 */
class SessionManagerTest {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var context: Context

    @Before
    fun setUp() {
        editor = mockk(relaxed = true)
        sharedPrefs = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit
    }

    // ------------------------------------------------------------------ identityId

    @Test
    fun `identityId is generated and persisted on first access when absent`() {
        every { sharedPrefs.getString("identity_id", null) } returns null

        val manager = SessionManager(context)
        val id = manager.identityId

        assertNotNull(id)
        assertTrue(id.isNotBlank())
        verify { editor.putString("identity_id", id) }
        verify { editor.apply() }
    }

    @Test
    fun `identityId returns stored value when present`() {
        val storedId = "stored-identity-id"
        every { sharedPrefs.getString("identity_id", null) } returns storedId

        val manager = SessionManager(context)

        assertEquals(storedId, manager.identityId)
    }

    @Test
    fun `identityId is stable across multiple accesses`() {
        every { sharedPrefs.getString("identity_id", null) } returns null
        val capturedId = slot<String>()
        every { editor.putString("identity_id", capture(capturedId)) } returns editor

        val manager = SessionManager(context)
        val first = manager.identityId
        val second = manager.identityId

        assertEquals(first, second)
    }

    // ------------------------------------------------------------------ isFirstSession

    @Test
    fun `isFirstSession is true when has_launched key is absent`() {
        every { sharedPrefs.contains("has_launched") } returns false

        val manager = SessionManager(context)

        assertTrue(manager.isFirstSession)
        verify { editor.putBoolean("has_launched", true) }
    }

    @Test
    fun `isFirstSession is false when has_launched key is present`() {
        every { sharedPrefs.contains("has_launched") } returns true

        val manager = SessionManager(context)

        assertFalse(manager.isFirstSession)
    }

    // ------------------------------------------------------------------ sessionId

    @Test
    fun `sessionId is non-blank after construction`() {
        val manager = SessionManager(context)

        assertTrue(manager.sessionId.isNotBlank())
    }

    @Test
    fun `startNewSession generates a different sessionId`() {
        val manager = SessionManager(context)
        val first = manager.sessionId

        manager.startNewSession()
        val second = manager.sessionId

        assertNotEquals(first, second)
    }

    @Test
    fun `sessionId stays the same between startNewSession calls`() {
        val manager = SessionManager(context)
        manager.startNewSession()
        val id = manager.sessionId

        // Access multiple times without calling startNewSession
        assertEquals(id, manager.sessionId)
        assertEquals(id, manager.sessionId)
    }
}
