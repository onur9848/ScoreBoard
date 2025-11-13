package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.senerunosoft.puantablosu.R

/**
 * Reusable Top App Bar component for BoardScreen
 * Separates UI concerns following Modern Android Architecture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardTopBar(
    gameTitle: String,
    onBackClick: () -> Unit,
    onCalculateClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = gameTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            IconButton(onClick = onCalculateClick) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = stringResource(R.string.error_incomplete_score),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = stringResource(R.string.close_dialog_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
