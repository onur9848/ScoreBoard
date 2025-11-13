# PuanTablosu (ScoreBoard)

Modern Jetpack Compose ve Kotlin ile geliştirilmiş Android puan takip uygulaması.

## 🚀 Modern Mimari

PuanTablosu, güncel Android geliştirme standartlarını benimser:
- **100% Kotlin** - Modern, güvenli ve özlü kod
- **Jetpack Compose** - Deklaratif UI framework
- **Material Design 3** - Güncel tasarım sistemi
- **StateFlow** - Reaktif durum yönetimi
- **MVVM Mimarisi** - Temiz kod ayrımı

## ✨ Özellikler

- **Oyun Yönetimi**: Yeni oyunlar oluşturun ve oyuncuları ekleyin
- **Puan Takibi**: Gerçek zamanlı puan güncellemeleri
- **Oyun Geçmişi**: Geçmiş oyunları görüntüleyin ve devam ettirin
- **Material Design 3**: Koyu/Açık tema desteği
- **Duyarlı Tasarım**: Farklı ekran boyutlarına uyum
- **Erişilebilirlik**: Ekran okuyucu desteği

## 🛠️ Teknoloji Yığını

### Core
- **Kotlin** - Ana programlama dili
- **Jetpack Compose** - UI framework
- **Material 3** - Tasarım sistemi
- **StateFlow** - Durum yönetimi

### Architecture
- **MVVM** - Model-View-ViewModel pattern
- **Single Activity** - Compose navigation
- **Repository Pattern** - Veri erişim katmanı

### Dependencies
- `androidx.compose.*` - Compose UI toolkit
- `androidx.navigation:navigation-compose` - Navigation
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel
- `com.google.code.gson` - JSON serialization

## 📱 Kurulum

### Gereksinimler
- Android SDK 26+ (Android 8.0+)
- Android Studio Arctic Fox veya üstü
- Kotlin support

### Adımlar
1. Depoyu klonlayın:
   ```bash
   git clone https://github.com/onur9848/ScoreBoard.git
   ```

2. Android Studio ile projeyi açın

3. Gradle sync yapın

4. Uygulamayı çalıştırın

## 🏗️ Proje Yapısı

```
app/src/main/java/com/senerunosoft/puantablosu/
├── MainActivity.kt                    # Ana activity
├── model/                            # Veri modelleri
│   ├── Game.kt                      # Oyun data class
│   ├── Player.kt                    # Oyuncu data class
│   └── Score.kt                     # Puan data class
├── service/                         # İş mantığı
│   ├── IGameService.kt             # Service interface
│   └── GameService.kt              # Service implementation
├── ui/compose/                      # Compose UI
│   ├── HomeScreen.kt               # Ana ekran
│   ├── NewGameScreen.kt            # Yeni oyun ekranı
│   ├── BoardScreen.kt              # Puan tablosu
│   ├── LatestGamesScreen.kt        # Oyun geçmişi
│   ├── AddScoreDialog.kt           # Puan ekleme diyalogu
│   ├── ScoreBoardNavigation.kt     # Navigation setup
│   └── theme/                      # Material 3 tema
└── viewmodel/
    └── GameViewModel.kt            # ViewModel (StateFlow)
```

## 🎯 Kullanım

### Yeni Oyun Oluşturma
1. Ana ekranda "Yeni Skor Tablosu" tıklayın
2. Oyun başlığını girin
3. Oyuncuları ekleyin
4. "Başla" ile oyunu başlatın

### Puan Ekleme
1. Oyun ekranında "Skor Ekle" tıklayın
2. Her oyuncu için puanları girin
3. "Kaydet" ile puanları onaylayın

### Geçmiş Oyunları Görüntüleme
1. Ana ekranda "Eski Oyunlar" tıklayın
2. Devam etmek istediğiniz oyunu seçin

## 🔄 Modernizasyon Durumu

Bu proje tam modernizasyon sürecini tamamlamıştır:

✅ **Java → Kotlin migration** (100% complete)  
✅ **Fragment → Compose migration** (100% complete)  
✅ **LiveData → StateFlow migration** (100% complete)  
✅ **XML layouts removal** (100% complete)  
✅ **ViewBinding removal** (100% complete)  

Detaylar için [MIGRATION_STATUS.md](./MIGRATION_STATUS.md) dosyasına bakın.

## 🧪 Test Coverage

Proje kapsamlı test altyapısına sahiptir:

✅ **157+ Total Tests**
- 130+ Unit Tests (Business logic, ViewModels, Services)
- 15+ UI Tests (Compose screens, Navigation)
- 12+ Integration Tests (End-to-end flows)

**Test Coverage**: 60%+ overall

### Hızlı Test Komutları
```bash
# Tüm unit testleri çalıştır
./gradlew test

# UI testleri çalıştır (emulator gerekli)
./gradlew connectedAndroidTest

# Coverage raporu oluştur
./gradlew koverHtmlReport
```

Detaylar için:
- [TEST_DOCUMENTATION.md](./TEST_DOCUMENTATION.md) - Kapsamlı test dokümantasyonu
- [TESTING_QUICK_START.md](./TESTING_QUICK_START.md) - Hızlı başlangıç rehberi

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Branch'i push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

MIT License - Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 🎨 Ekran Görüntüleri

Modern Material Design 3 arayüzü ile:
- Koyu/Açık tema desteği
- Smooth animasyonlar
- Duyarlı tasarım
- Erişilebilir kontroller

---

**PuanTablosu** - Modern Android geliştirme standartlarının uygulandığı örnek proje.
