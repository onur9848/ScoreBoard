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
}