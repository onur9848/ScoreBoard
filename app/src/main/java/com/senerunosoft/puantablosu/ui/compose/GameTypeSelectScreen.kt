package com.senerunosoft.puantablosu.ui.compose

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameTypeSelectScreen(
    onGameTypeSelected: (GameType, config: IConfig?) -> Unit, // config: IConfig? olarak kullanılır
    modifier: Modifier = Modifier,
    handleSystemBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onInteraction: (String, Map<String, Any>?) -> Unit = { _, _ -> }
) {
    var selectedType by remember { mutableStateOf<GameType?>(null) }
    var yuzBirConfig by remember { mutableStateOf(YuzBirOkeyConfig()) }
    var okeyConfig by remember { mutableStateOf(OkeyConfig()) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var editingValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("scoreboard_rules", Context.MODE_PRIVATE) }

    BackHandler(enabled = handleSystemBack) {
        onInteraction("system_back_pressed", null)
        onNavigateBack()
    }

    // Load saved rule values from SharedPreferences on first composition
    LaunchedEffect(selectedType) {
        if (selectedType == GameType.YuzBirOkey) {
            val savedRules = yuzBirConfig.rules.map { rule ->
                val savedValue = prefs.getString("yuzbir_${rule.key}", rule.value) ?: rule.value
                rule.copy(value = savedValue)
            }
            yuzBirConfig = yuzBirConfig.copy(rules = savedRules)
        } else if (selectedType == GameType.Okey) {
            val savedRules = okeyConfig.rules.map { rule ->
                val savedValue = prefs.getString("okey_${rule.key}", rule.value) ?: rule.value
                rule.copy(value = savedValue)
            }
            okeyConfig = okeyConfig.copy(rules = savedRules)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = yuzBirConfig.isPartnered,
                                onClick = {
                                    yuzBirConfig = yuzBirConfig.copy(isPartnered = true)
                                    onInteraction("yuzbir_mode_selected", mapOf("isPartnered" to true))
                                },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !yuzBirConfig.isPartnered,
                                onClick = {
                                    yuzBirConfig = yuzBirConfig.copy(isPartnered = false)
                                    onInteraction("yuzbir_mode_selected", mapOf("isPartnered" to false))
                                },
                                label = { Text("Tekli") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Puan kuralları liste şeklinde, tıklanınca düzenlenebilir
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "Puan Kuralları",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        val scoreFields = yuzBirConfig.rules // RuleConfig listesi
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column {
                                scoreFields.forEach { rule ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            rule.label + ":",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f).padding(end = 16.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(0.5f)
                                        ) {
                                            Text(
                                                rule.value,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .clickable {
                                                        editingField = rule.key
                                                        editingValue = rule.value
                                                        onInteraction("rule_edit_opened", mapOf("ruleKey" to rule.key, "source" to "value_text"))
                                                    })
                                            IconButton(onClick = {
                                                editingField = rule.key
                                                editingValue = rule.value
                                                onInteraction("rule_edit_opened", mapOf("ruleKey" to rule.key, "source" to "edit_icon"))
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Edit dialog
                        if (editingField != null) {
                            val rule = yuzBirConfig.rules.find { it.key == editingField }
                            if (rule != null) {
                                AlertDialog(
                                    onDismissRequest = {
                                        onInteraction("rule_edit_dismissed", mapOf("ruleKey" to rule.key))
                                        editingField = null
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            // Save to config and SharedPreferences
                                            val updatedRules = yuzBirConfig.rules.map {
                                                if (it.key == editingField) it.copy(value = editingValue) else it
                                            }
                                            yuzBirConfig = yuzBirConfig.copy(rules = updatedRules)
                                            prefs.edit().putString("yuzbir_${editingField}", editingValue).apply()
                                            onInteraction("rule_edit_saved", mapOf("ruleKey" to rule.key, "value" to editingValue))
                                            editingField = null
                                        }) { Text("Kaydet") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            onInteraction("rule_edit_cancelled", mapOf("ruleKey" to rule.key))
                                            editingField = null
                                        }) { Text("İptal") }
                                    },
                                    title = { Text("${rule.label} Düzenle") },
                                    text = {
                                        OutlinedTextField(
                                            value = editingValue,
                                            onValueChange = { editingValue = it },
                                            label = { Text("Değer") },
                                            singleLine = true
                                        )
                                    }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = okeyConfig.isPartnered,
                                onClick = {
                                    okeyConfig = okeyConfig.copy(isPartnered = true)
                                    onInteraction("okey_mode_selected", mapOf("isPartnered" to true))
                                },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !okeyConfig.isPartnered,
                                onClick = {
                                    okeyConfig = okeyConfig.copy(isPartnered = false)
                                    onInteraction("okey_mode_selected", mapOf("isPartnered" to false))
                                },
                                label = { Text("Tekli") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            else -> {}
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
