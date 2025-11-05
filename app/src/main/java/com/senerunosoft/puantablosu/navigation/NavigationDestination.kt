package com.senerunosoft.puantablosu.navigation

/**
 * Type-safe navigation destinations
 * Sealed class ensures compile-time safety
 */
sealed class NavigationDestination(val route: String) {
    object Home : NavigationDestination("home")
    object SelectGameType : NavigationDestination("select_game_type")
    object NewGame : NavigationDestination("new_game")
    object LatestGames : NavigationDestination("latest_games")
    object Board : NavigationDestination("board")
    object ScoreBoard : NavigationDestination("score_board_screen")
    
    companion object {
        fun fromRoute(route: String?): NavigationDestination? {
            return when (route) {
                Home.route -> Home
                SelectGameType.route -> SelectGameType
                NewGame.route -> NewGame
                LatestGames.route -> LatestGames
                Board.route -> Board
                ScoreBoard.route -> ScoreBoard
                else -> null
            }
        }
    }
}
