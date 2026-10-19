# Puan Tablosu (ScoreBoard)

101 Okey, Okey ve genel oyunlar için puan tablosu. Repo üç parçadan oluşur:

| Klasör | İçerik | Durum |
| --- | --- | --- |
| [`mobile/`](mobile) | Flutter uygulaması (Android, iOS) | Aktif |
| [`backend/`](backend) | .NET 8 Clean Architecture API: oyunları ve oyun günlüğünü toplar | Aktif |
| [`panel/`](panel) | Oyunları ve gerçek kullanımı izleme paneli (React) | Aktif |

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

## Panel

```bash
cd panel
npm ci
npm run dev   # http://localhost:5173 (backend çalışıyor olmalı)
```

Ayrıntılar: [panel/README.md](panel/README.md).

## Hepsi birlikte

```bash
SCOREBOARD_INGEST_KEY=<en az 16 karakter> SCOREBOARD_PANEL_KEY=<farklı, en az 16 karakter> docker compose up --build
```

Panel http://localhost:8080, API http://localhost:5210 adresinde açılır. Uygulama
`--dart-define=API_BASE_URL=http://<sunucu>:5210/ --dart-define=API_KEY=<ingest anahtarı>`
ile derlendiğinde oyunlar panele düşer.
