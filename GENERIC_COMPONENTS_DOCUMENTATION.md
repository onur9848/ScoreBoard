# Generic Jetpack Compose UI Components

This document provides comprehensive documentation for the generic UI component system implemented for the ScoreBoard project.

## Overview

The generic component system provides reusable, type-safe UI components that can work with any data type `T`. This approach increases flexibility, reusability, and maintains consistency across different screens and projects.

## Component Categories

### 1. Buttons (`GenericButton.kt`)

#### GenericButton<T>
A generic button that can handle any data type with customizable click handling.

```kotlin
GenericButton(
    onClick = { value: String -> 
        // Handle click with typed value
        println("Clicked: $value")
    },
    text = "Click Me",
    value = "button-action",
    modifier = Modifier.fillMaxWidth()
)
```

#### GenericIconButton<T>
A generic icon button with vector icon support.

```kotlin
GenericIconButton(
    onClick = { playerId: String -> 
        // Handle icon click with player ID
        navigateToPlayer(playerId)
    },
    icon = Icons.Default.Person,
    value = "player-123",
    contentDescription = "View player"
)
```

### 2. Text Inputs (`GenericTextField.kt`)

#### GenericTextField<T>
A type-safe text field that can handle various data types with automatic conversion.

```kotlin
// String input
var text by remember { mutableStateOf("") }
GenericTextField(
    value = text,
    onValueChange = { text = it },
    label = "Player Name"
)

// Integer input
var score by remember { mutableStateOf(0) }
GenericTextField(
    value = score,
    onValueChange = { score = it },
    label = "Score",
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
)

// Custom type with converter
data class GameScore(val value: Int)
var gameScore by remember { mutableStateOf(GameScore(0)) }
GenericTextField(
    value = gameScore,
    onValueChange = { gameScore = it },
    label = "Game Score",
    valueToString = { it.value.toString() },
    stringToValue = { GameScore(it.toIntOrNull() ?: 0) }
)
```

### 3. Cards (`GenericCard.kt`)

#### GenericCard<T>
A flexible card component that can display any data type with custom content.

```kotlin
data class Player(val id: String, val name: String, val score: Int)

GenericCard(
    content = { player ->
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Score: ${player.score}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    },
    item = Player("1", "John", 150),
    onClick = { player ->
        // Handle card click with player data
        showPlayerDetails(player)
    }
)
```

### 4. Sliders (`GenericSlider.kt`)

#### GenericSlider
Customizable slider for numeric input.

```kotlin
var volume by remember { mutableStateOf(0.5f) }
GenericSlider(
    value = volume,
    onValueChange = { volume = it },
    minValue = 0f,
    maxValue = 1f,
    steps = 10
)
```

#### GenericRangeSlider
For selecting a range of values.

```kotlin
var scoreRange by remember { mutableStateOf(0f..100f) }
GenericRangeSlider(
    value = scoreRange,
    onValueChange = { scoreRange = it },
    minValue = 0f,
    maxValue = 200f
)
```

### 5. Progress Indicators (`GenericProgressIndicator.kt`)

#### GenericProgressIndicator
Unified progress indicator supporting both linear and circular styles.

```kotlin
// Circular indeterminate
GenericProgressIndicator(
    isIndeterminate = true,
    isCircular = true
)

// Linear with progress
GenericProgressIndicator(
    value = 0.7f,
    isIndeterminate = false,
    isCircular = false
)
```

### 6. List Items (`GenericListItem.kt`)

#### GenericListItem<T>
Type-safe list item component.

```kotlin
data class MenuItem(val title: String, val action: String)

GenericListItem(
    item = MenuItem("Settings", "navigate_settings"),
    content = { menuItem ->
        Text(menuItem.title)
    },
    trailingContent = { menuItem ->
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
)
```

### 7. Dialogs (`GenericDialog.kt`)

#### GenericDialog<T>
Flexible dialog with typed data handling.

```kotlin
data class ConfirmationData(val message: String, val action: String)

GenericDialog(
    openDialog = showDialog,
    onDismissRequest = { showDialog = false },
    title = "Confirm Action",
    content = { data ->
        Text(data.message)
    },
    item = ConfirmationData("Delete player?", "delete_player"),
    confirmButton = { data ->
        TextButton(
            onClick = { 
                executeAction(data.action)
                showDialog = false 
            }
        ) {
            Text("Confirm")
        }
    }
)
```

### 8. Switches & Controls (`GenericSwitch.kt`)

#### GenericSwitch
Boolean state management with Material 3 styling.

```kotlin
var enableSound by remember { mutableStateOf(true) }
GenericSwitch(
    isChecked = enableSound,
    onCheckedChange = { enableSound = it }
)
```

#### GenericCheckbox
Checkbox with customizable colors.

```kotlin
var termsAccepted by remember { mutableStateOf(false) }
GenericCheckbox(
    isChecked = termsAccepted,
    onCheckedChange = { termsAccepted = it }
)
```

### 9. Navigation (`GenericNavigation.kt`)

#### GenericBottomNavigation<T>
Type-safe bottom navigation.

```kotlin
data class NavItem(val route: String, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("home", "Home", Icons.Default.Home),
    NavItem("games", "Games", Icons.Default.SportsEsports),
    NavItem("settings", "Settings", Icons.Default.Settings)
)

var selectedItem by remember { mutableStateOf(navItems.first()) }

GenericBottomNavigation(
    items = navItems,
    selectedItem = selectedItem,
    onItemClick = { selectedItem = it },
    content = { item, isSelected ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(item.icon, contentDescription = item.label)
            if (isSelected) {
                Text(item.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
)
```

### 10. Badges (`GenericBadge.kt`)

#### GenericBadge
Notification badges with count display.

```kotlin
GenericBadge(
    count = unreadMessages,
    content = {
        IconButton(onClick = { openMessages() }) {
            Icon(Icons.Default.Message, contentDescription = "Messages")
        }
    }
)
```

#### GenericCustomBadge<T>
Fully customizable badge with typed data.

```kotlin
data class NotificationBadge(val count: Int, val type: String, val urgent: Boolean)

GenericCustomBadge(
    data = NotificationBadge(5, "message", true),
    content = {
        Button(onClick = { }) { Text("Notifications") }
    },
    badgeContent = { data ->
        Text(
            text = data.count.toString(),
            fontSize = 10.sp,
            fontWeight = if (data.urgent) FontWeight.Bold else FontWeight.Normal
        )
    },
    showBadge = { it.count > 0 },
    containerColor = if (data.urgent) Color.Red else MaterialTheme.colorScheme.primary
)
```

## Design System Integration

### Flat Design Style
```kotlin
import com.senerunosoft.puantablosu.ui.compose.components.styles.DesignSystem

GenericButton(
    // ... other parameters
    colors = DesignSystem.FlatDesign.buttonColors()
)

GenericCard(
    // ... other parameters
    colors = DesignSystem.FlatDesign.cardColors(),
    elevation = DesignSystem.FlatDesign.cardElevation()
)
```

### Neumorphism Style
```kotlin
GenericButton(
    // ... other parameters
    colors = DesignSystem.Neumorphism.buttonColors()
)

GenericCard(
    // ... other parameters
    colors = DesignSystem.Neumorphism.cardColors(),
    elevation = DesignSystem.Neumorphism.cardElevation()
)
```

## Best Practices

1. **Type Safety**: Always specify the generic type explicitly when type inference might be ambiguous.

2. **Custom Converters**: For complex data types, provide custom `valueToString` and `stringToValue` functions for text fields.

3. **Consistent Styling**: Use the DesignSystem object to maintain consistent styling across your app.

4. **Performance**: Remember that generic components are recomposed like any other Compose component, so use `remember` appropriately for complex data.

5. **Preview Functions**: Each component includes preview functions for development and testing.

## Usage Example in ScoreBoard App

```kotlin
// Using generic components in the ScoreBoard app
@Composable
fun ScoreInputScreen(players: List<Player>) {
    Column {
        players.forEach { player ->
            GenericCard(
                content = { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(p.name)
                        GenericTextField(
                            value = p.score,
                            onValueChange = { updatePlayerScore(p.id, it) },
                            label = "Score"
                        )
                    }
                },
                item = player,
                onClick = { selectPlayer(it.id) }
            )
        }
        
        GenericButton(
            onClick = { action: String -> executeGameAction(action) },
            text = "Save Scores",
            value = "save_scores"
        )
    }
}
```

This comprehensive system provides a solid foundation for building consistent, reusable UI components across different projects while maintaining type safety and flexibility.