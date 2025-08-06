package com.senerunosoft.puantablosu.model

data class SingleScore(
    var playerId: String = "",
    var score: Int = 0
) {
    // Empty constructor for Gson
    constructor() : this("", 0)

    // Getter methods to match original Java API (for backward compatibility)
    fun playerId(): String = playerId
    fun setPlayerId(playerId: String) {
        this.playerId = playerId
    }
    fun getScore(): Int = score
    fun setScore(score: Int) {
        this.score = score
    }
}