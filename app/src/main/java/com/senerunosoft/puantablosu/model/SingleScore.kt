package com.senerunosoft.puantablosu.model

data class SingleScore(
    var playerId: String = "",
    var score: Int = 0
) {
    // Empty constructor for Gson
    constructor() : this("", 0)
}