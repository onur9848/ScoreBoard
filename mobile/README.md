# Puan Tablosu – Flutter

## Yapı

```
lib/
  domain/            Saf Dart iş kuralları (UI'dan bağımsız, testli)
    models/          Game, Player, Round, GameSettings, GameEvent
    rules/           Oyun türüne göre kurallar (101 ceza, bitiş, açmama…)
    scoring/         Sıralama ve kazanan (101'de en düşük puan kazanır)
    tiles/           Istaka puanlaması, okey/gösterge, taş rengi ve sıra tespiti
    game_engine.dart Oyundaki her değişikliği yapar ve olay günlüğüne yazar
  data/              Cihazda saklama + eski Kotlin kayıtlarını taşıma
  services/          Firebase analitik, ML Kit ile ıstaka okuma
  state/             Riverpod sağlayıcıları
  features/          Ekranlar: home, setup, board, results, history, rack
```

## Veri modeli ve oyun günlüğü

Her `Game` backend'e gidecek biçimde JSON'a çevrilir (snake_case alanlar,
`schema_version`). Puanlar `rounds` altında el el tutulur; her elin türü
(`hand`, `penalty`, `finish`), uygulanan kural (`rule_key`), bitiren/ceza alan
oyuncu ve her puanın kaynağı (`manual`, `rule`, `rack_scan`) bellidir.

`events` listesi oyunun baştan sona günlüğüdür. Analitikten bağımsızdır ve panelde
gerçek kullanımı göstermek için tasarlandı:

| Olay | Ne zaman | Önemli alanlar |
| --- | --- | --- |
| `game_created` | Oyun kurulunca | oyun türü, eşli/tekli, oyuncular, kural değerleri, varsayılandan değiştirilen kurallar |
| `round_added` | Her el / ceza / bitiş | el numarası, tür, kural, puanlar, puan kaynakları |
| `rule_applied` | Kural düğmesiyle eklenen satır | kural (örn. `handFinish`), değer, oyuncu |
| `rack_scan_used` | Istakadan hesaplanan puan kullanılınca | taş sayısı, okunan/düzeltilen taş sayısı, kamera kullanıldı mı |
| `round_edited`, `round_deleted` | Satır düzeltme/silme | eski ve yeni puanlar |
| `game_finished`, `game_reopened`, `game_resumed` | Bitiş ve yeniden açma | kazananlar, el ve ceza sayısı, süre, toplamlar |

## Istaka okuma

`Puan Hesapla` ekranı ve skor girişindeki kamera düğmesi aynı ekranı açar:

1. Fotoğraf çekilir ya da galeriden seçilir.
2. Rakamlar cihaz üzerinde ML Kit metin tanıma ile okunur (internet gerekmez).
3. Her rakamın rengi, rakam kutusundaki piksellerden çıkarılır (kırmızı, sarı, mavi, siyah).
4. Taşlar dikey konumlarına göre iki sıralı sanal ıstakaya dizilir.
5. Hatalı taşlara dokunularak renk/sayı düzeltilir, silinir ya da diğer sıraya taşınır;
   uzun basıp sürükleyerek sıra değiştirilir. Eksik taşlar `+` ile eklenir.
6. Gösterge seçilince okey işaretlenir; sahte okey okeyin değerini alır.
   "Elini açtı", "Çift gitti" ve "Elde kalan okey" seçenekleri puanı belirler.

Okuma yalnızca Android ve iOS'ta çalışır; web'de taşlar elle eklenir.

## Firebase

`android/app/google-services.json` ve `ios/Runner/GoogleService-Info.plist`
repoda tutulmaz. Dosyalar varsa Analytics ve Crashlytics açılır; yoksa uygulama
analitiksiz çalışır. CI, `GOOGLE_SERVICES_JSON` secret'ı varsa dosyayı yazar.

## Play Store imzası

`android/key.properties` (repoda tutulmaz) varsa release derlemesi onunla imzalanır:

```
storeFile=/yol/upload-keystore.jks
storePassword=...
keyAlias=upload
keyPassword=...
```
