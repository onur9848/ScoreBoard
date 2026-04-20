package com.senerunosoft.puantablosu.ui.compose

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameTypeSelectScreen(
    onGameTypeSelected: (GameType, config: IConfig?) -> Unit, // config: IConfig? olarak kullanılır
    modifier: Modifier = Modifier,
    initialSelectedType: GameType? = null,
    handleSystemBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onInteraction: (String, Map<String, Any>?) -> Unit = { _, _ -> }
) {
    var selectedType by remember { mutableStateOf<GameType?>(initialSelectedType) }
    var yuzBirConfig by remember { mutableStateOf(YuzBirOkeyConfig()) }
    var okeyConfig by remember { mutableStateOf(OkeyConfig()) }

    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val prefs = remember { if (isPreview) null else context.getSharedPreferences("scoreboard_rules", Context.MODE_PRIVATE) }

    BackHandler(enabled = handleSystemBack) {
        onInteraction("system_back_pressed", null)
        onNavigateBack()
    }

    // Load saved rule values from SharedPreferences on first composition
    LaunchedEffect(selectedType) {
        if (selectedType == GameType.YuzBirOkey) {
            val savedRules = yuzBirConfig.rules.map { rule ->
                val savedValue = prefs?.getString("yuzbir_${rule.key}", rule.value) ?: rule.value
                rule.copy(value = savedValue)
            }
            yuzBirConfig = yuzBirConfig.copy(rules = savedRules)
        } else if (selectedType == GameType.Okey) {
            val savedRules = okeyConfig.rules.map { rule ->
                val savedValue = prefs?.getString("okey_${rule.key}", rule.value) ?: rule.value
                rule.copy(value = savedValue)
            }
            okeyConfig = okeyConfig.copy(rules = savedRules)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oyun Türü Seç",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        // Oyun tipi seçim butonları (minimal, chip tarzı)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            GameType.entries.forEach { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedType = type
                        onInteraction("game_type_selected", mapOf("gameType" to type.name))
                    },
                    label = {
                        Text(
                            when (type) {
                                GameType.Okey -> "Okey"
                                GameType.YuzBirOkey -> "101 Okey"
                                GameType.GenelOyun -> "Genel Oyun"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        // Seçilen oyun tipine göre ayarlar
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedType) {
                GameType.YuzBirOkey -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("101 Okey Ayarları", style = MaterialTheme.typography.titleMedium)
                        // Eşli/Tekli
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            SegmentedButton(
                                selected = yuzBirConfig.isPartnered,
                                onClick = {
                                    yuzBirConfig = yuzBirConfig.copy(isPartnered = true)
                                    onInteraction("yuzbir_mode_selected", mapOf("isPartnered" to true))
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Eşli") }
                            SegmentedButton(
                                selected = !yuzBirConfig.isPartnered,
                                onClick = {
                                    yuzBirConfig = yuzBirConfig.copy(isPartnered = false)
                                    onInteraction("yuzbir_mode_selected", mapOf("isPartnered" to false))
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Tekli") }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Puan Kuralları",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        val scoreFields = yuzBirConfig.rules
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            scoreFields.forEach { rule ->
                                OutlinedTextField(
                                    value = rule.value,
                                    onValueChange = { newValue ->
                                        val updatedRules = yuzBirConfig.rules.map {
                                            if (it.key == rule.key) it.copy(value = newValue) else it
                                        }
                                        yuzBirConfig = yuzBirConfig.copy(rules = updatedRules)
                                        prefs?.edit()?.putString("yuzbir_${rule.key}", newValue)?.apply()
                                        onInteraction("rule_edit_inline", mapOf("ruleKey" to rule.key, "value" to newValue))
                                    },
                                    label = { Text(rule.label) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                        }
                    }
                }
            }

            GameType.Okey -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Okey Ayarları", style = MaterialTheme.typography.titleMedium)
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            SegmentedButton(
                                selected = okeyConfig.isPartnered,
                                onClick = {
                                    okeyConfig = okeyConfig.copy(isPartnered = true)
                                    onInteraction("okey_mode_selected", mapOf("isPartnered" to true))
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Eşli") }
                            SegmentedButton(
                                selected = !okeyConfig.isPartnered,
                                onClick = {
                                    okeyConfig = okeyConfig.copy(isPartnered = false)
                                    onInteraction("okey_mode_selected", mapOf("isPartnered" to false))
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Tekli") }
                        }
                    }
                }
            }

            else -> {}
        }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                selectedType?.let {
                    val config = when (it) {
                        GameType.YuzBirOkey -> yuzBirConfig
                        GameType.Okey -> okeyConfig
                        else -> null
                    }
                    onInteraction(
                        "continue_tapped",
                        mapOf(
                            "gameType" to it.name,
                            "hasConfig" to (config != null)
                        )
                    )
                    onGameTypeSelected(it, config)
                }
            },
            enabled = selectedType != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Devam Et")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameTypeSelectScreenPreview() {
    com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme {
        GameTypeSelectScreen(
            initialSelectedType = com.senerunosoft.puantablosu.model.enums.GameType.YuzBirOkey,
            onGameTypeSelected = { _, _ -> }
        )
    }
}
