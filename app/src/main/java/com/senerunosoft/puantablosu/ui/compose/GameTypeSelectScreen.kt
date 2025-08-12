package com.senerunosoft.puantablosu.ui.compose

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.model.enums.GameType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameTypeSelectScreen(
    onGameTypeSelected: (GameType, config: IConfig?) -> Unit, // config: IConfig? olarak kullanılır
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<GameType?>(null) }
    var yuzBirConfig by remember { mutableStateOf(YuzBirOkeyConfig()) }
    var okeyConfig by remember { mutableStateOf(OkeyConfig()) }
    val inputFieldTextStyle = MaterialTheme.typography.labelMedium
    var editingField by remember { mutableStateOf<String?>(null) }

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
                    onClick = { selectedType = type },
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
                                onClick = { yuzBirConfig = yuzBirConfig.copy(isPartnered = true) },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !yuzBirConfig.isPartnered,
                                onClick = { yuzBirConfig = yuzBirConfig.copy(isPartnered = false) },
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
                        val scoreFields = listOf(
                            Triple("Ceza Puanı", yuzBirConfig.penalty, "penalty"),
                            Triple("Normal Bitiş", yuzBirConfig.normalFinish, "normalFinish"),
                            Triple("Açmama Cezası", yuzBirConfig.noOpenPenalty, "noOpenPenalty"),
                            Triple("Elden Bitme", yuzBirConfig.handFinish, "handFinish"),
                            Triple("Elden Bitme Açmama Cezası", yuzBirConfig.handNoOpenPenalty, "handNoOpenPenalty"),
                            Triple("Okeyle Elden Bitme", yuzBirConfig.handOkeyFinish, "handOkeyFinish"),
                            Triple("Okeyle Elden Bitme Açamama Cezası", yuzBirConfig.handOkeyNoOpenPenalty, "handOkeyNoOpenPenalty")
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                scoreFields.forEachIndexed { idx, (label, value, key) ->
                                    val isEditing = editingField == key
                                    val rowBg = if (isEditing) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                    else if (idx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surface
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(rowBg)
                                            .clip(MaterialTheme.shapes.medium)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            label,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            textAlign = TextAlign.End,
                                            color = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isEditing) {
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .align(Alignment.CenterVertically),
                                            ) {
                                                OutlinedTextField(
                                                    value = value,
                                                    onValueChange = {
                                                        if (it.all { c -> c == '-' || c.isDigit() }) {
                                                            yuzBirConfig = when (key) {
                                                                "penalty" -> yuzBirConfig.copy(penalty = it)
                                                                "normalFinish" -> yuzBirConfig.copy(normalFinish = it)
                                                                "noOpenPenalty" -> yuzBirConfig.copy(noOpenPenalty = it)
                                                                "handFinish" -> yuzBirConfig.copy(handFinish = it)
                                                                "handNoOpenPenalty" -> yuzBirConfig.copy(handNoOpenPenalty = it)
                                                                "handOkeyFinish" -> yuzBirConfig.copy(handOkeyFinish = it)
                                                                "handOkeyNoOpenPenalty" -> yuzBirConfig.copy(handOkeyNoOpenPenalty = it)
                                                                else -> yuzBirConfig
                                                            }
                                                        }
                                                    },
                                                    singleLine = true,
                                                    textStyle = inputFieldTextStyle.copy(
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.End
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(0.3f)
                                                        .height(48.dp)
                                                        .padding(
                                                            horizontal = 0.dp,
                                                            vertical = 0.dp
                                                        )
                                                        .align(Alignment.CenterVertically),
                                                    maxLines = 1,
                                                    shape = MaterialTheme.shapes.small,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    placeholder = {
                                                        Text(
                                                            value,
                                                            textAlign = TextAlign.End,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                    },

                                                )
                                                IconButton(
                                                    onClick = { editingField = null },
                                                    modifier = Modifier
                                                        .weight(0.2f)
                                                        .size(32.dp)
                                                        .align(Alignment.CenterVertically)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Kaydet")
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Text(
                                                    value,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.CenterEnd)
                                                        .clickable { editingField = key },
                                                    textAlign = TextAlign.End,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
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
                                onClick = { okeyConfig = okeyConfig.copy(isPartnered = true) },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !okeyConfig.isPartnered,
                                onClick = { okeyConfig = okeyConfig.copy(isPartnered = false) },
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
