package com.senerunosoft.puantablosu.model

import java.util.UUID

data class Game(
    val gameId: String = UUID.randomUUID().toString(),
    var gameTitle: String = "",
    var playerList: List<Player> = ArrayList(),
    var score: List<Score> = ArrayList()
) {
    // Empty constructor for Gson
    constructor() : this(UUID.randomUUID().toString(), "", ArrayList(), ArrayList())
    
    // Constructor matching the original Java constructor
    constructor(gameTitle: String, playerList: List<Player>) : this(
        UUID.randomUUID().toString(),
        gameTitle,
        playerList,
        ArrayList()
    )
    
    // Getter and setter methods to match original Java API (for backward compatibility)
    fun getGameId(): String = gameId
    
    fun setGameId(gameId: String) {
        // Note: This is immutable in Kotlin data class, but kept for compatibility
        // In practice, create a new instance: copy(gameId = gameId)
    }
    
    fun getGameTitle(): String = gameTitle
    
    fun setGameTitle(gameTitle: String) {
        this.gameTitle = gameTitle
    }
    
    fun getPlayerList(): List<Player> = playerList
    
    fun setPlayerList(playerList: List<Player>) {
        this.playerList = playerList
    }
    
    fun getScore(): List<Score> = score
    
    fun setScore(score: List<Score>) {
        this.score = score
    }
}