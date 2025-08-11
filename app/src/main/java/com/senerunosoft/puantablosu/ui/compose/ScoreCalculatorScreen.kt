package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.senerunosoft.puantablosu.R

// Okey taş renkleri
val okeyColors = listOf(Color.Red, Color.Black, Color.Blue, Color(0xFFFFC107)) // Sarı

@Composable
fun OkeyIstakasi(
    tiles: List<Pair<Int, Int>> = List(30) { i -> ((i % 13) + 1) to (i % 4) }
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.98f)
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreCalculatorScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puan Hesapla") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            OkeyIstakasi()
            Spacer(modifier = Modifier.height(32.dp))
            // Rest of the screen content
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Puan hesaplama özelliği çok yakında!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun OkeyTileSvg(assetName: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val imageLoader = ImageLoader.Builder(ctx)
        .components { add(SvgDecoder.Factory()) }
        .build()

    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(ctx)
                .data("file:///android_asset/$assetName")
                .build(),
            imageLoader
        ),
        contentDescription = null,
        modifier = modifier
    )
}

