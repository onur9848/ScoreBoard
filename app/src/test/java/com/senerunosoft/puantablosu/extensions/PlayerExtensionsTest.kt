package com.senerunosoft.puantablosu.extensions

import com.senerunosoft.puantablosu.model.Player
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Player extension functions
 * Tests utility functions that extend Player model functionality
 */
class PlayerExtensionsTest {

    // ==================== getDisplayName Tests ====================

    @Test
    fun `getDisplayName returns player name when not blank`() {
        val player = Player("Alice")
        
        assertEquals("Alice", player.getDisplayName())
    }

    @Test
    fun `getDisplayName returns shortened ID when name is blank`() {
        val player = Player("")
        
        val displayName = player.getDisplayName()
        assertTrue(displayName.startsWith("Player "))
        assertTrue(displayName.length > 7) // "Player " + at least 1 char
    }

    @Test
    fun `getDisplayName handles whitespace only name`() {
        val player = Player("   ")
        
        val displayName = player.getDisplayName()
        assertTrue(displayName.startsWith("Player "))
    }

    // ==================== hasValidName Tests ====================

    @Test
    fun `hasValidName returns true for valid name`() {
        val player = Player("Alice")
        
        assertTrue(player.hasValidName())
    }

    @Test
    fun `hasValidName returns false for blank name`() {
        val player = Player("")
        
        assertFalse(player.hasValidName())
    }

    @Test
    fun `hasValidName returns false for name with only whitespace`() {
        val player = Player("   ")
        
        assertFalse(player.hasValidName())
    }

    @Test
    fun `hasValidName returns false for single character name`() {
        val player = Player("A")
        
        assertFalse(player.hasValidName())
    }

    @Test
    fun `hasValidName returns true for 2 character name`() {
        val player = Player("Al")
        
        assertTrue(player.hasValidName())
    }

    @Test
    fun `hasValidName returns true for 20 character name`() {
        val player = Player("A".repeat(20))
        
        assertTrue(player.hasValidName())
    }

    @Test
    fun `hasValidName returns false for name longer than 20 characters`() {
        val player = Player("A".repeat(21))
        
        assertFalse(player.hasValidName())
    }

    // ==================== getInitials Tests ====================

    @Test
    fun `getInitials returns first letter for single word`() {
        val player = Player("Alice")
        
        assertEquals("A", player.getInitials())
    }

    @Test
    fun `getInitials returns first letters for two words`() {
        val player = Player("Alice Bob")
        
        assertEquals("AB", player.getInitials())
    }

    @Test
    fun `getInitials returns first two letters for multiple words`() {
        val player = Player("Alice Bob Charlie")
        
        assertEquals("AB", player.getInitials())
    }

    @Test
    fun `getInitials returns P for blank name`() {
        val player = Player("")
        
        assertEquals("P", player.getInitials())
    }

    @Test
    fun `getInitials converts to uppercase`() {
        val player = Player("alice bob")
        
        assertEquals("AB", player.getInitials())
    }

    @Test
    fun `getInitials handles single letter name`() {
        val player = Player("A")
        
        assertEquals("A", player.getInitials())
    }

    @Test
    fun `getInitials handles name with numbers`() {
        val player = Player("Player1 Team2")
        
        assertEquals("PT", player.getInitials())
    }

    // ==================== hasValidCharacters Tests ====================

    @Test
    fun `hasValidCharacters returns true for alphabetic name`() {
        val player = Player("Alice")
        
        assertTrue(player.hasValidCharacters())
    }

    @Test
    fun `hasValidCharacters returns true for name with spaces`() {
        val player = Player("Alice Bob")
        
        assertTrue(player.hasValidCharacters())
    }

    @Test
    fun `hasValidCharacters returns true for name with numbers`() {
        val player = Player("Player123")
        
        assertTrue(player.hasValidCharacters())
    }

    @Test
    fun `hasValidCharacters returns false for name with special characters`() {
        val player = Player("Alice@Bob")
        
        assertFalse(player.hasValidCharacters())
    }

    @Test
    fun `hasValidCharacters returns false for name with punctuation`() {
        val player = Player("Alice, Bob")
        
        assertFalse(player.hasValidCharacters())
    }

    @Test
    fun `hasValidCharacters returns false for name with symbols`() {
        val player = Player("Alice#Bob$")
        
        assertFalse(player.hasValidCharacters())
    }

    // ==================== formatForDisplay Tests ====================

    @Test
    fun `formatForDisplay capitalizes first letter`() {
        val player = Player("alice")
        
        assertEquals("Alice", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay capitalizes each word`() {
        val player = Player("alice bob charlie")
        
        assertEquals("Alice Bob Charlie", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay handles already capitalized name`() {
        val player = Player("Alice Bob")
        
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay handles mixed case`() {
        val player = Player("aLiCe bOb")
        
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay removes extra spaces`() {
        val player = Player("Alice    Bob")
        
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay trims leading and trailing spaces`() {
        val player = Player("  Alice Bob  ")
        
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay handles single word`() {
        val player = Player("alice")
        
        assertEquals("Alice", player.formatForDisplay())
    }

    @Test
    fun `formatForDisplay handles all caps name`() {
        val player = Player("ALICE BOB")
        
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `multiple extensions work together`() {
        val player = Player("alice bob")
        
        assertTrue(player.hasValidName())
        assertTrue(player.hasValidCharacters())
        assertEquals("AB", player.getInitials())
        assertEquals("Alice Bob", player.formatForDisplay())
    }

    @Test
    fun `extensions handle unicode names correctly`() {
        val player = Player("José María")
        
        // Should fail hasValidCharacters due to accent marks
        assertFalse(player.hasValidCharacters())
        // But other functions should still work
        assertTrue(player.hasValidName())
    }
}
