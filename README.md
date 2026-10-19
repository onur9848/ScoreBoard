# Puan Tablosu (ScoreBoard)

101 Okey, Okey ve genel oyunlar için puan tablosu. Repo üç parçadan oluşur:

| Klasör | İçerik | Durum |
| --- | --- | --- |
| [`mobile/`](mobile) | Flutter uygulaması (Android, iOS) | Aktif |
| [`backend/`](backend) | .NET 8 Clean Architecture API: oyunları ve oyun günlüğünü toplar | Aktif |
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

## Backend

```bash
cd backend
dotnet test
dotnet run --project src/ScoreBoard.Api   # http://localhost:5210/swagger
```

Ayrıntılar: [backend/README.md](backend/README.md).
