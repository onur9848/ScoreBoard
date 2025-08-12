package com.senerunosoft.puantablosu.model

import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import java.util.UUID

data class Game(
    val gameId: String = UUID.randomUUID().toString(),
    var gameTitle: String = "",
    var playerList: List<Player> = ArrayList(),
    var score: MutableList<Score> = mutableListOf(),
    var gameType: GameType = GameType.GenelOyun,
    var config: IConfig? = null
) {
    // Empty constructor for Gson
    constructor() : this(UUID.randomUUID().toString(), "", ArrayList(), ArrayList(), GameType.GenelOyun, null)
    // Constructor matching the original Java constructor
    constructor(gameTitle: String, playerList: List<Player>, gameType: GameType, config: IConfig?) : this(
        UUID.randomUUID().toString(),
        gameTitle,
        playerList,
        ArrayList(),
        gameType,
        config
    )
}