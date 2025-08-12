package com.senerunosoft.puantablosu.service.impl

import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.interfaces.IGameManager
import com.senerunosoft.puantablosu.service.interfaces.IGameSerializer
import com.senerunosoft.puantablosu.service.interfaces.IPlayerManager
import com.senerunosoft.puantablosu.service.interfaces.IScoreCalculator
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.model.config.IConfig

/**
 * Composite service that implements the original IGameService interface.
 * Delegates to focused services following SOLID principles.
 * Follows Dependency Inversion Principle (DIP) - depends on abstractions.
 * Maintains backward compatibility while improving architecture.
 */
class CompositeGameService(
    private val gameManager: IGameManager,
    private val playerManager: IPlayerManager,
    private val scoreCalculator: IScoreCalculator,
    private val gameSerializer: IGameSerializer
) : IGameService {

    override fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game? {
        return gameManager.createGame(gameTitle, gameType, config)
    }

    override fun addPlayer(game: Game?, playerName: String) {
        playerManager.addPlayer(game, playerName)
    }

    override fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean {
        return scoreCalculator.addScore(game, scoreList)
    }

    override fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int {
        return scoreCalculator.getPlayerRoundScore(game, playerId, round)
    }

    override fun getCalculatedScore(game: Game): List<SingleScore> {
        return scoreCalculator.getCalculatedScore(game)
    }
    
    override fun serializeGame(game: Game?): String? {
        return gameSerializer.serializeGame(game)
    }
    
    override fun deserializeGame(gameString: String): Game? {
        return gameSerializer.deserializeGame(gameString)
    }
}