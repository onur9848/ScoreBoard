package com.senerunosoft.puantablosu.service.impl

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.service.interfaces.IGameSerializer

/**
 * JSON-based implementation of game serialization operations.
 * Follows Single Responsibility Principle (SRP) - only handles serialization/persistence.
 * Follows Open/Closed Principle (OCP) - can be extended with other serialization formats.
 */
class JsonGameSerializerService : IGameSerializer {
    
    companion object {
        private const val TAG = "JsonGameSerializerService"
    }
    
    private val gson = Gson()
    
    override fun serializeGame(game: Game?): String? {
        if (game == null) {
            Log.w(TAG, "serializeGame: Game is null")
            return null
        }
        
        return try {
            val serialized = gson.toJson(game)
            Log.d(TAG, "serializeGame: Game serialized successfully")
            serialized
        } catch (e: Exception) {
            Log.e(TAG, "serializeGame: Error serializing game", e)
            null
        }
    }
    
    override fun deserializeGame(gameString: String): Game? {
        if (!validateSerializedGame(gameString)) {
            Log.w(TAG, "deserializeGame: Invalid game string")
            return null
        }
        
        return try {
            val game = gson.fromJson(gameString, Game::class.java)
            Log.d(TAG, "deserializeGame: Game deserialized successfully")
            game
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "deserializeGame: JSON syntax error", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "deserializeGame: Error deserializing game", e)
            null
        }
    }
    
    override fun validateSerializedGame(gameString: String): Boolean {
        if (gameString.isBlank()) {
            return false
        }
        
        return try {
            // Basic JSON validation - try to parse as JSON object
            gson.fromJson(gameString, Any::class.java)
            
            // Additional validation - check if it contains expected game fields
            gameString.contains("gameId") && 
            gameString.contains("gameTitle") && 
            gameString.contains("playerList")
        } catch (e: JsonSyntaxException) {
            Log.w(TAG, "validateSerializedGame: Invalid JSON format")
            false
        } catch (e: Exception) {
            Log.w(TAG, "validateSerializedGame: Validation error", e)
            false
        }
    }
}