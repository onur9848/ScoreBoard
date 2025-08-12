package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.enums.GameType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameTypeSelectScreen(
    onGameTypeSelected: (GameType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<GameType?>(null) }
    // 101 Okey ayarları için state
    var yuzBirIsPartnered by remember { mutableStateOf(true) }
    var yuzBirPenalty by remember { mutableStateOf("101") }
    var yuzBirNormalFinish by remember { mutableStateOf("-101") }
    var yuzBirNoOpenPenalty by remember { mutableStateOf("202") }
    var yuzBirHandFinish by remember { mutableStateOf("-202") }
    var yuzBirHandNoOpenPenalty by remember { mutableStateOf("404") }
    var yuzBirHandOkeyFinish by remember { mutableStateOf("-404") }
    var yuzBirHandOkeyNoOpenPenalty by remember { mutableStateOf("808") }
    // Okey ayarları için state
    var okeyIsPartnered by remember { mutableStateOf(true) }
    val inputFieldTextStyle = MaterialTheme.typography.labelMedium

    // 101 Okey puan ayarları için edit mod state'leri
    var editingField by remember { mutableStateOf<String?>(null) }

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
                                selected = yuzBirIsPartnered,
                                onClick = { yuzBirIsPartnered = true },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !yuzBirIsPartnered,
                                onClick = { yuzBirIsPartnered = false },
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
                            Triple("Ceza Puanı", yuzBirPenalty, "penalty"),
                            Triple("Normal Bitiş", yuzBirNormalFinish, "normalFinish"),
                            Triple("Açmama Cezası", yuzBirNoOpenPenalty, "noOpenPenalty"),
                            Triple("Elden Bitme", yuzBirHandFinish, "handFinish"),
                            Triple("Elden Bitme Açmama Cezası", yuzBirHandNoOpenPenalty, "handNoOpenPenalty"),
                            Triple("Okeyle Elden Bitme", yuzBirHandOkeyFinish, "handOkeyFinish"),
                            Triple(
                                "Okeyle Elden Bitme Açamama Cezası",
                                yuzBirHandOkeyNoOpenPenalty,
                                "handOkeyNoOpenPenalty"
                            )
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
                                                    .padding(start = 32.dp)
                                                    .align(Alignment.CenterVertically),
                                            ) {
                                                OutlinedTextField(
                                                    value = value,
                                                    onValueChange = {
                                                        if (it.all { c -> c == '-' || c.isDigit() }) {
                                                            when (key) {
                                                                "penalty" -> yuzBirPenalty = it
                                                                "normalFinish" -> yuzBirNormalFinish = it
                                                                "noOpenPenalty" -> yuzBirNoOpenPenalty = it
                                                                "handFinish" -> yuzBirHandFinish = it
                                                                "handNoOpenPenalty" -> yuzBirHandNoOpenPenalty = it
                                                                "handOkeyFinish" -> yuzBirHandOkeyFinish = it
                                                                "handOkeyNoOpenPenalty" -> yuzBirHandOkeyNoOpenPenalty = it
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
                                selected = okeyIsPartnered,
                                onClick = { okeyIsPartnered = true },
                                label = { Text("Eşli") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !okeyIsPartnered,
                                onClick = { okeyIsPartnered = false },
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
            onClick = { selectedType?.let { onGameTypeSelected(it) } },
            enabled = selectedType != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Devam Et")
        }
    }
}
