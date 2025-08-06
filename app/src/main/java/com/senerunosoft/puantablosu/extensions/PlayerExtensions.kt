package com.senerunosoft.puantablosu.extensions

import com.senerunosoft.puantablosu.model.Player

/**
 * Extension functions for Player model.
 * Follows Open/Closed Principle (OCP) - extends functionality without modifying existing code.
 */

/**
 * Extension function to get a display name for the player.
 * Useful for UI formatting.
 */
fun Player.getDisplayName(): String = 
    if (name.isBlank()) "Player ${id.take(6)}" else name

/**
 * Extension function to validate player name length.
 */
fun Player.hasValidName(): Boolean = 
    name.isNotBlank() && name.length >= 2 && name.length <= 20

/**
 * Extension function to get initials from player name.
 */
fun Player.getInitials(): String {
    return if (name.isBlank()) {
        "P"
    } else {
        name.split(' ')
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { name.first().uppercaseChar().toString() }
    }
}

/**
 * Extension function to check if player name contains only valid characters.
 */
fun Player.hasValidCharacters(): Boolean = 
    name.matches(Regex("^[a-zA-Z0-9\\s]+$"))

/**
 * Extension function to format player name for display.
 */
fun Player.formatForDisplay(): String = 
    name.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }