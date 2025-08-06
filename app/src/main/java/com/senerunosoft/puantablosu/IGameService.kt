package com.senerunosoft.puantablosu

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore

interface IGameService {
    fun createGame(gameTitle: String): Game?
    fun addPlayer(game: Game?, playerName: String)
    fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean
    fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int
    fun getCalculatedScore(game: Game): List<SingleScore>
    fun serializeGame(game: Game?): String?
    fun deserializeGame(gameString: String): Game?
}