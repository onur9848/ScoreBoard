package com.senerunosoft.puantablosu.ui.compose.components.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system configuration for different UI styles
 * Supports Flat Design and Neumorphism styles as requested in the issue
 */
object DesignSystem {
    
    /**
     * Flat Design style configuration
     * Clean, minimalist approach with flat colors and simple shapes
     */
    object FlatDesign {
        val shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(16.dp)
        )
        
        val elevations = object {
            val none = 0.dp
            val small = 2.dp
            val medium = 4.dp
            val large = 8.dp
        }
        
        @Composable
        fun buttonColors(
            containerColor: Color = MaterialTheme.colorScheme.primary,
            contentColor: Color = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor: Color = MaterialTheme.colorScheme.outline,
            disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ) = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
        
        @Composable
        fun cardColors(
            containerColor: Color = MaterialTheme.colorScheme.surface,
            contentColor: Color = MaterialTheme.colorScheme.onSurface
        ) = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
        
        @Composable
        fun cardElevation(
            defaultElevation: androidx.compose.ui.unit.Dp = elevations.small
        ) = CardDefaults.cardElevation(
            defaultElevation = defaultElevation
        )
    }
    
    /**
     * Neumorphism style configuration
     * Soft, tactile design with subtle shadows and highlights
     */
    object Neumorphism {
        val shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(32.dp)
        )
        
        val elevations = object {
            val none = 0.dp
            val small = 8.dp
            val medium = 16.dp
            val large = 24.dp
        }
        
        @Composable
        fun buttonColors(
            containerColor: Color = MaterialTheme.colorScheme.surface,
            contentColor: Color = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ) = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
        
        @Composable
        fun cardColors(
            containerColor: Color = MaterialTheme.colorScheme.surface,
            contentColor: Color = MaterialTheme.colorScheme.onSurface
        ) = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
        
        @Composable
        fun cardElevation(
            defaultElevation: androidx.compose.ui.unit.Dp = elevations.medium
        ) = CardDefaults.cardElevation(
            defaultElevation = defaultElevation
        )
        
        // Neumorphism specific colors for shadows and highlights
        @Composable
        fun shadowColor(): Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        
        @Composable
        fun highlightColor(): Color = Color.White.copy(alpha = 0.7f)
    }
    
    /**
     * Style configuration enum for easy switching between designs
     */
    enum class Style {
        FLAT_DESIGN,
        NEUMORPHISM
    }
}