package com.senerunosoft.puantablosu.service.interfaces

import com.senerunosoft.puantablosu.model.Game

/**
 * Interface for game serialization and persistence operations.
 * Follows Interface Segregation Principle (ISP) - focused on persistence operations only.
 * Follows Open/Closed Principle (OCP) - allows different serialization strategies.
 */
interface IGameSerializer {
    /**
     * Serializes a game to a string representation.
     * @param game The game to serialize
     * @return Serialized game string or null if serialization fails
     */
    fun serializeGame(game: Game?): String?
    
    /**
     * Deserializes a game from string representation.
     * @param gameString The serialized game string
     * @return Deserialized game or null if deserialization fails
     */
    fun deserializeGame(gameString: String): Game?
    
    /**
     * Validates if a serialized game string is valid.
     * @param gameString The serialized game string to validate
     * @return true if valid, false otherwise
     */
    fun validateSerializedGame(gameString: String): Boolean
}