package com.senerunosoft.puantablosu.model

data class SingleScore(
    var playerId: String = "",
    var score: Int = 0
) {
    // Empty constructor for Gson
    constructor() : this("", 0)
    
    // Constructor matching the original Java constructor
    constructor(playerId: String, score: Int) : this() {
        this.playerId = playerId
        this.score = score
    }
    
    // Getter methods to match original Java API (for backward compatibility)
    fun playerId(): String = playerId
    
    fun playerId(playerId: String) {
        this.playerId = playerId
    }
    
    fun getScore(): Int = score
    
    fun setScore(score: Int) {
        this.score = score
    }
}