package com.senerunosoft.puantablosu.navigation

import androidx.navigation.NavHostController

/**
 * Centralized navigation handler
 * Separates navigation logic from UI components
 * Single source of truth for navigation
 */
class NavigationHandler(private val navController: NavHostController) {
    
    fun navigateTo(destination: NavigationDestination) {
        navController.navigate(destination.route)
    }
    
    fun navigateToWithPopUp(
        destination: NavigationDestination,
        popUpTo: NavigationDestination,
        inclusive: Boolean = false
    ) {
        navController.navigate(destination.route) {
            popUpTo(popUpTo.route) {
                this.inclusive = inclusive
            }
        }
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
    
    fun navigateBackTo(destination: NavigationDestination, inclusive: Boolean = false) {
        navController.popBackStack(destination.route, inclusive)
    }
    
    fun getCurrentRoute(): String? {
        return navController.currentBackStackEntry?.destination?.route
    }
}
