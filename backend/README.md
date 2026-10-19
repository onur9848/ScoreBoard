# Puan Tablosu – Backend

.NET 8, Clean Architecture. Mobil uygulamanın gönderdiği oyunları saklar ve panele
okuma API'si sunar.

```
src/
  ScoreBoard.Domain          Oyun oturumu (GameSession), oyuncular, kurallar, eller, olay günlüğü, ıstaka okumaları, cihaz
  ScoreBoard.Application     Kullanım senaryoları: oyunu kaydet, listele, ayrıntı, kullanım istatistikleri
  ScoreBoard.Infrastructure  EF Core (PostgreSQL; geliştirme ve testlerde SQLite), migration'lar
  ScoreBoard.Api             Minimal API, anahtar doğrulama, CORS, Swagger
tests/ScoreBoard.Tests       Uçtan uca API testleri + domain testleri
```

## Çalıştırma

```bash
cd backend
dotnet run --project src/ScoreBoard.Api      # Development: SQLite dosyası, Swagger http://localhost:5210/swagger
dotnet test                                  # SQLite ile
SCOREBOARD_TEST_POSTGRES="Host=localhost;Username=scoreboard;Password=scoreboard" dotnet test   # PostgreSQL ile
```

Docker ile API + PostgreSQL + panel için kök dizindeki `docker-compose.yml` kullanılır.

## Ayarlar

| Anahtar | Açıklama |
| --- | --- |
| `Database:Provider` | `Postgres` (varsayılan) ya da `Sqlite` |
| `ConnectionStrings:ScoreBoard` | Bağlantı cümlesi |
| `Database:MigrateOnStartup` | Açılışta migration uygula (varsayılan `true`) |
| `Auth:IngestKey` | Uygulamanın oyun gönderirken kullandığı anahtar (`X-Api-Key`), en az 16 karakter |
| `Auth:PanelKey` | Panelin okuma anahtarı (`X-Panel-Key`), en az 16 karakter, ingest anahtarından farklı |
| `Cors:AllowedOrigins` | Panelin adresi |
| `Swagger:Enabled` | Development dışında Swagger'ı açar |

Ortam değişkeni olarak `Auth__PanelKey` biçiminde verilir. Anahtarlar boşsa uygulama açılmaz.

## API

| İstek | Anahtar | Açıklama |
| --- | --- | --- |
| `PUT /api/v1/sessions/{id}` | `X-Api-Key` + `X-Device-Id` | Oyunun tam halini kaydeder. Tekrar göndermek güvenlidir; daha eski bir kopya kaydı değiştirmez (`created` / `updated` / `unchanged`). Başka cihazın oyunu `409`. |
| `GET /api/v1/sessions` | `X-Panel-Key` | `gameType`, `status`, `deviceId`, `from`, `to`, `q` (başlık ya da oyuncu adı), `page`, `pageSize` |
| `GET /api/v1/sessions/{id}` | `X-Panel-Key` | Oyuncular (toplam, sıra, kazanan), kurallar, eller, olay günlüğü, cihaz |
| `GET /api/v1/stats/overview` | `X-Panel-Key` | Oyun türü dağılımı, kural kullanımı, değiştirilen ayarlar, ıstaka okuma ve düzeltme oranı, puan kaynakları, oyuncu sayıları, günlük oyunlar (`from`, `to`, `gameType`, `days`, `utcOffsetMinutes`) |
| `GET /api/v1/auth/check` | `X-Panel-Key` | Panel girişi |
| `GET /health` | – | Sağlık kontrolü |

İstek ve yanıtlar snake_case JSON'dur. Gövde şeması mobil uygulamadaki `Game.toJson()`
ile aynıdır (`schema_version: 2`). Örnek: `tests/ScoreBoard.Tests/Fixtures/sample-game.v2.json`;
bu dosya `mobile/tool/export_sample_game.dart` ile uygulamanın kendi oyun motorundan üretilir
ve iki tarafın testleri de onu okur.

Oyun her güncellemede bütün olarak yeniden yazılır. Bilinmeyen olay türleri saklanır, böylece
uygulamanın yeni sürümleri backend'den önce yayınlanabilir.

## Migration

```bash
dotnet tool restore
dotnet ef migrations add <Ad> -p src/ScoreBoard.Infrastructure -s src/ScoreBoard.Infrastructure -o Persistence/Migrations
```

Migration'lar PostgreSQL içindir. SQLite şeması modelden doğrudan oluşturulur.
