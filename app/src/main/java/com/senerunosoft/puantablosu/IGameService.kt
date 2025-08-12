package com.senerunosoft.puantablosu

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType

interface IGameService {
    fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game?
    fun addPlayer(game: Game?, playerName: String)
    fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean
    fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int
    fun getCalculatedScore(game: Game): List<SingleScore>
    fun serializeGame(game: Game?): String?
    fun deserializeGame(gameString: String): Game?
}