# Puan Tablosu (ScoreBoard)

101 Okey, Okey ve genel oyunlar için puan tablosu. Repo üç parçadan oluşur:

| Klasör | İçerik | Durum |
| --- | --- | --- |
| [`mobile/`](mobile) | Flutter uygulaması (Android, iOS) | Aktif |
| `backend/` | .NET Clean Architecture API: oyun oturumlarını toplar | Sonraki aşama |
| `panel/` | Oyun oturumlarını izleme paneli (React) | Sonraki aşama |

Eski Kotlin/Compose uygulaması `1.3.2` sürümüyle git geçmişinde duruyor. Flutter
uygulaması aynı paket adını (`com.senerunosoft.puantablosu`) kullanır ve ilk
açılışta eski uygulamanın kayıtlı oyunlarını ve kural ayarlarını içeri alır.

## Mobil

```bash
cd mobile
flutter pub get
flutter test
flutter run
```

Ayrıntılar: [mobile/README.md](mobile/README.md).
