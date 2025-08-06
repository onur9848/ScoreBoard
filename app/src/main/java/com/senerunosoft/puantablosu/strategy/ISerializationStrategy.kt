package com.senerunosoft.puantablosu.strategy

import com.senerunosoft.puantablosu.model.Game

/**
 * Strategy interface for different serialization methods.
 * Follows Open/Closed Principle (OCP) - allows adding new serialization strategies.
 * Follows Strategy Pattern - enables different serialization algorithms.
 */
interface ISerializationStrategy {
    /**
     * Serializes a game to string representation.
     * @param game The game to serialize
     * @return Serialized string or null if failed
     */
    fun serialize(game: Game?): String?
    
    /**
     * Deserializes a game from string representation.
     * @param data The serialized data string
     * @return Deserialized game or null if failed
     */
    fun deserialize(data: String): Game?
    
    /**
     * Validates if the data string is valid for this serialization format.
     * @param data The data string to validate
     * @return true if valid, false otherwise
     */
    fun validate(data: String): Boolean
    
    /**
     * Gets the name of this serialization strategy.
     * @return Human-readable name of the strategy
     */
    fun getStrategyName(): String
    
    /**
     * Gets the file extension associated with this format.
     * @return File extension (e.g., "json", "xml")
     */
    fun getFileExtension(): String
}