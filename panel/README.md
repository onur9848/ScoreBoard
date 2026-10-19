# Puan Tablosu – Panel

React + Vite + TypeScript. Backend'deki oyunları gösterir:

- **Özet:** oyun ve cihaz sayısı, oynanan el, ortalama süre, günlük oyunlar, oyun türleri,
  kural kullanımı (elden bitme, ceza…), değiştirilen ayarlar, ıstaka okuma ve düzeltme oranı,
  puanların kaynağı, masadaki oyuncu sayısı. Dönem ve oyun türüne göre süzülür.
- **Oyunlar:** başlık ya da oyuncu adına göre arama, tür ve durum filtresi.
- **Oyun ayrıntısı:** oyunun tek paragraflık özeti ("101 Okey seçildi. Ayarlar değiştirildi:
  Ceza Puanı 101 → 150. 2 kişi (xyz, abc) ile oyun kuruldu. 2 el oynandı, xyz kazandı…"),
  uygulamadaki gibi skor tablosu, olay günlüğü, kurallar ve cihaz.

Giriş için backend'deki `Auth:PanelKey` kullanılır; anahtar tarayıcıda saklanır.

```bash
npm ci
npm run dev        # http://localhost:5173, /api istekleri http://localhost:5210'a gider
npm test
npm run build
```

Backend başka bir adresteyse `VITE_API_BASE_URL` verilir ve o adres backend'in
`Cors:AllowedOrigins` listesine eklenir. Docker'da panel API'yi kendi adresinden
(`/api`) çağırır; kök dizindeki `docker-compose.yml`'a bakın.
