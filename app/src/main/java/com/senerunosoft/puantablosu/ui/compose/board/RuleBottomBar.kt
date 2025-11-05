package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.config.RuleConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.model.enums.RuleType

/**
 * Bottom bar component displaying game rules as navigation items
 * Handles rule-based score input for different game types
 */
@Composable
fun RuleBottomBar(
    rules: List<RuleConfig>,
    gameType: GameType,
    onRuleClick: (RuleConfig, RuleConfig?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(bottom = 4.dp)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            rules.forEach { rule ->
                val showButton = when (rule.types.first()) {
                    RuleType.PlayerPenaltyScore -> true
                    RuleType.FinishScore -> rule.pairedKey != null
                    else -> false
                }
                
                if (showButton) {
                    val icon = when (rule.types.first()) {
                        RuleType.PlayerPenaltyScore -> Icons.Default.Face
                        RuleType.FinishScore -> Icons.Default.Calculate
                        else -> Icons.Default.Edit
                    }
                    
                    val pairedRule = if (rule.types.first() == RuleType.FinishScore && rule.pairedKey != null) {
                        rules.find { it.key == rule.pairedKey }
                    } else {
                        null
                    }
                    
                    NavigationBarItem(
                        selected = false,
                        onClick = { onRuleClick(rule, pairedRule) },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = rule.label,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                rule.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    }
}
