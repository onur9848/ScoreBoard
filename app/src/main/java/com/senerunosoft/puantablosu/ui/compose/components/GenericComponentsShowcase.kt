package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.ui.compose.components.styles.DesignSystem

/**
 * Showcase screen demonstrating all generic components
 * This serves as both documentation and testing for all components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericComponentsShowcase(
    style: DesignSystem.Style = DesignSystem.Style.FLAT_DESIGN
) {
    val sampleData = listOf(
        ComponentDemo("Buttons", Icons.Default.TouchApp),
        ComponentDemo("Text Fields", Icons.Default.Edit),
        ComponentDemo("Cards", Icons.Default.CreditCard),
        ComponentDemo("Sliders", Icons.Default.Tune),
        ComponentDemo("Progress", Icons.Default.Schedule),
        ComponentDemo("Lists", Icons.Default.List),
        ComponentDemo("Dialogs", Icons.Default.Chat),
        ComponentDemo("Switches", Icons.Default.ToggleOn),
        ComponentDemo("Navigation", Icons.Default.Navigation),
        ComponentDemo("Badges", Icons.Default.Notifications)
    )
    
    var selectedComponent by remember { mutableStateOf(sampleData.first()) }
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Generic Components Showcase",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Style: ${style.name.replace('_', ' ')}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ComponentSection(
                    title = "Buttons",
                    style = style
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenericButton(
                            onClick = { data: String -> },
                            text = "Primary",
                            value = "primary-action",
                            colors = when (style) {
                                DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.buttonColors()
                                DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.buttonColors()
                            }
                        )
                        
                        GenericIconButton(
                            onClick = { data: Int -> },
                            icon = Icons.Default.Favorite,
                            value = 42,
                            contentDescription = "Favorite"
                        )
                    }
                }
            }
            
            item {
                ComponentSection(
                    title = "Text Fields",
                    style = style
                ) {
                    var textValue by remember { mutableStateOf("Sample text") }
                    var numberValue by remember { mutableStateOf(42) }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenericTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            label = "Text Input"
                        )
                        
                        GenericTextField(
                            value = numberValue,
                            onValueChange = { numberValue = it },
                            label = "Number Input"
                        )
                    }
                }
            }
            
            item {
                ComponentSection(
                    title = "Cards",
                    style = style
                ) {
                    GenericCard(
                        content = { data ->
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = data.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = data.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        item = SampleCardData("Card Title", "Card Description", 123),
                        colors = when (style) {
                            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors()
                            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors()
                        },
                        elevation = when (style) {
                            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardElevation()
                            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardElevation()
                        }
                    )
                }
            }
            
            item {
                ComponentSection(
                    title = "Controls",
                    style = style
                ) {
                    var sliderValue by remember { mutableStateOf(0.5f) }
                    var switchState by remember { mutableStateOf(true) }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenericSlider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            minValue = 0f,
                            maxValue = 1f
                        )
                        
                        GenericSwitch(
                            isChecked = switchState,
                            onCheckedChange = { switchState = it }
                        )
                    }
                }
            }
            
            item {
                ComponentSection(
                    title = "Progress Indicators",
                    style = style
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GenericProgressIndicator(
                            value = 0.7f,
                            isIndeterminate = false,
                            isCircular = true
                        )
                        
                        GenericProgressIndicator(
                            value = 0.4f,
                            isIndeterminate = false,
                            isCircular = false
                        )
                    }
                }
            }
            
            item {
                ComponentSection(
                    title = "Badges",
                    style = style
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GenericBadge(
                            count = 5,
                            content = {
                                Button(onClick = {}) { Text("Messages") }
                            }
                        )
                        
                        GenericDotBadge(
                            show = true,
                            content = {
                                Button(onClick = {}) { Text("Online") }
                            }
                        )
                    }
                }
            }
            
            item {
                ComponentSection(
                    title = "Dialog Demo",
                    style = style
                ) {
                    Button(
                        onClick = { showDialog = true }
                    ) {
                        Text("Show Dialog")
                    }
                }
            }
        }
    }
    
    // Demo dialog
    GenericDialog(
        openDialog = showDialog,
        onDismissRequest = { showDialog = false },
        title = "Sample Dialog",
        content = { message ->
            Text(text = message)
        },
        item = "This is a demonstration of the generic dialog component.",
        confirmButton = { _ ->
            TextButton(onClick = { showDialog = false }) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun ComponentSection(
    title: String,
    style: DesignSystem.Style,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = when (style) {
            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors()
            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors()
        },
        elevation = when (style) {
            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardElevation()
            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardElevation()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

data class ComponentDemo(
    val name: String,
    val icon: ImageVector
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericComponentsShowcaseFlatPreview() {
    ScoreBoardTheme {
        GenericComponentsShowcase(DesignSystem.Style.FLAT_DESIGN)
    }
}

@Preview(showBackground = true)
@Composable
fun GenericComponentsShowcaseNeumorphismPreview() {
    ScoreBoardTheme {
        GenericComponentsShowcase(DesignSystem.Style.NEUMORPHISM)
    }
}