package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OkeyIstakasiDynamic(
    tiles: List<OkeyTileModel> = List(30) { i ->
        OkeyTileModel(
            value = (i % 13) + 1,
            color = when (i % 4) {
                0 -> TileColor.RED
                1 -> TileColor.BLACK
                2 -> TileColor.BLUE
                else -> TileColor.YELLOW
            },
            isFakeJoker = false,
            isSelected = (i % 7 == 0)
        )
    }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.98f)
            .height(160.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RackRow(tiles.take(15))
        RackRow(tiles.drop(15).take(15))
    }
}

@Composable
private fun RackRow(rowTiles: List<OkeyTileModel>) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.98f)
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            rowTiles.forEach { tile ->
                OkeyTile(tile, modifier = Modifier.size(44.dp))
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

// add preview function
@Preview(showBackground = true)
@Composable
fun OkeyIstakasiDynamicPreview() {
    OkeyIstakasiDynamic(
        tiles = List(30) { i ->
            OkeyTileModel(
                value = (i % 13) + 1,
                color = when (i % 4) {
                    0 -> TileColor.RED
                    1 -> TileColor.BLACK
                    2 -> TileColor.BLUE
                    else -> TileColor.YELLOW
                },
                isFakeJoker = false,
                isSelected = (i % 7 == 0)
            )
        }
    )
}
