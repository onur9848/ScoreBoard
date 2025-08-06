# PuanTablosu

PuanTablosu, oyunlar ve oyuncular için puan takibi yapmanızı sağlayan, MVVM mimarisiyle geliştirilmiş bir Android uygulamasıdır.

## Uygulama Tanıtımı
PuanTablosu ile farklı oyunlar oluşturabilir, oyuncu ekleyebilir ve her oyuncunun puanlarını kolayca takip edebilirsiniz. Son oynanan oyunları ve puan tablolarını hızlıca görüntüleyebilir, kullanıcı dostu arayüzüyle puan ekleme ve düzenleme işlemlerini rahatça gerçekleştirebilirsiniz.

## Özellikler
- Oyun ve oyuncu oluşturma, düzenleme
- Oyunculara puan ekleme ve güncelleme
- Son oyunları ve puan tablolarını görüntüleme
- Farklı ekran boyutlarına uyumlu, duyarlı tasarım
- Diyaloglar ile kolay veri girişi
- Fragment tabanlı gezinme

## Kurulum
1. Depoyu klonlayın:
   ```sh
git clone https://github.com/yourusername/PuanTablosu.git
```
2. Android Studio ile projeyi açın.
3. Uygulamayı cihazınızda veya emülatörde çalıştırın.

## Proje Yapısı
- `model/` - Veri modelleri (Game, Player, Score, vb.)
- `service/` - Oyun mantığı servisleri
- `ui/` - Arayüz bileşenleri ve Fragment'lar
- `viewmodel/` - MVVM için ViewModel'ler
- `res/layout/` - XML arayüz dosyaları
- `res/drawable/` - İkonlar ve görseller
- `res/values/` - Renkler, boyutlar, metinler, temalar
- `res/navigation/` - Gezinme grafiği

## Güncelleme Planı
Mevcut hata ve iyileştirme önerileri için [update-plan.md](./update-plan.md) dosyasını inceleyebilirsiniz.

## Lisans
MIT
