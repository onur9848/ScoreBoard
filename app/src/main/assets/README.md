
# Okey UI Asset Pack (v1)

Bu paket, Jetpack Compose ile **gerçekçi** Okey istakası ve taşları görünümü oluşturmanız için gerekli temel SVG varlıklarını içerir.

## İçindekiler
- `tile_template.svg` — Numara ve renkleri düzenleyebileceğiniz, gölgeli/oval kenarlı **taş şablonu**.
- `fake_joker.svg` — Sahte okey yıldızlı taş.
- `indicator_tile.svg` — *Gösterge* / özel işaretleme için örnek taş.
- `rack.svg` — Ahşap desenli, olukları olan **istaka**.

> Not: SVG'ler vektöreldir; istediğiniz ölçüde ölçekleyebilirsiniz.

## Renkler ve Yazı Tipi
Taş üzerindeki sayı ve pip'ler için bu sınıflar mevcut: `.red`, `.blue`, `.black`, `.yellow`.
Varsayılan font **Inter / DIN ailesi** benzeri kalın bir grotesk yazı tipidir. Uygulamada kendi fontunuzu eklemeniz tavsiye edilir (ör: `DIN Condensed Bold`).

## Düzenleme
- `tile_template.svg` içinde `<text id="num">5</text>` alanını değiştirerek numarayı belirleyin.
- Aynı `<text>` elementinin `class` değerini `.red/.blue/.black/.yellow` ile değiştirerek rengi ayarlayın.
- Alt kısımdaki iki küçük daire **pip** olarak durur; renkleri aynı sınıfı paylaşır.

## Compose'da Kullanım (Coil + SVG)
**build.gradle(:app)** ekleyin:
```gradle
implementation("io.coil-kt:coil-compose:2.6.0")
implementation("io.coil-kt:coil-svg:2.6.0")
```

Örnek kullanım:
```kotlin
@Composable
fun OkeyTileSvg(
    assetName: String, // "tile_template.svg" gibi
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val imageLoader = ImageLoader.Builder(ctx)
        .components { add(SvgDecoder.Factory()) }
        .build()

    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(ctx)
                .data("file:///android_asset/$assetName")
                .build(),
            imageLoader = imageLoader
        ),
        contentDescription = null,
        modifier = modifier
    )
}
```

## Gerçekçi İstaka + Perspektif
İstakayı hafif perspektif için Compose tarafında `graphicsLayer` ile döndürebilirsiniz:
```kotlin
Box(
  modifier = Modifier
    .graphicsLayer {
       rotationX = 8f
       shadowElevation = 12f
       clip = false
    }
)
```

## Tamamen Kodla (SVG'siz) Çizmek İsterseniz
- `Canvas` üzerinde `RoundRect`, `Brush.verticalGradient` ile gövde; iç oyuk için ikinci bir `RoundRect` ve `BlendMode.Multiply` ile **inner shadow** efekti verebilirsiniz.
- Performans için taş composable'ını `@Stable` veri modeline bağlayın ve `remember` ile cache'leyin.

## Ölçüler (Öneri)
- Taş: 160×220 px @1x (uygulamada 32×44 dp iyi bir başlangıç)
- Kenar yarıçapı: 16 px
- Taşlar arası boşluk: 4–6 dp
- İstaka yüksekliği: ~72–88 dp

## Lisans
Bu varlıkları projende serbestçe kullanabilirsin (MIT).

İyi oyunlar! 🎲
