# Google Play Store Pazarlama Görselleri (9:16)

Bu klasör, PuanTablosu için 5 adet yüksek dönüşüm odaklı Google Play ekran görüntüsü taslağını otomatik üretmek için hazırlanmıştır.

## 1) Pazarlama Metinleri (5 Ekran)

1. **Giriş (Hero)**  
   **Başlık:** Oyunun Nabzını Anında Tutun  
   **Alt metin:** PuanTablosu ile tüm oyuncuların skorunu tek ekranda, hızlı ve net şekilde yönetin.

2. **Özellik 1 (Teknik Güç)**  
   **Başlık:** Akıllı Tur Hesaplama ile Hata Payını Sıfırla  
   **Alt metin:** Her tur puanı otomatik toplanır, anlık sıralama ile kazananı tereddütsüz belirleyin.

3. **Özellik 2 (Kullanım Kolaylığı / Hız)**  
   **Başlık:** Saniyeler İçinde Skor Girişi  
   **Alt metin:** Tur aralarında vakit kaybetmeyin: hızlı giriş akışıyla puan ekleyin, oyuna odaklanın.

4. **Özellik 3 (Güven / Benzersiz Yan)**  
   **Başlık:** Oyunlarınız Güvende, Geçmişiniz Hep Yanınızda  
   **Alt metin:** Kayıtlı oyun geçmişi sayesinde kaldığınız yerden devam edin, hiçbir skoru kaybetmeyin.

5. **Kapanış (CTA)**  
   **Başlık:** Kazananı Netleştirin — Şimdi PuanTablosu’nu İndirin  
   **Alt metin:** Arkadaş buluşmalarından turnuvalara kadar her oyunu profesyonelce yönetin.

## 2) Teknik Şablon (Seçenek A: HTML/CSS)

`play-store-screenshots.html` dosyası tek bir şablonda 5 ayrı ekran içerir:

- Boyut: **1080x1920**
- Oran: **9:16**
- Stil: Modern gradient arka plan + üstte büyük metin + ortada telefon çerçevesi
- Kullanım: `?screen=1` ... `?screen=5`

Örnek:

- `file:///path/to/your/project/marketing/play-store-screenshots.html?screen=1`
- `file:///path/to/your/project/marketing/play-store-screenshots.html?screen=2`

## 3) PNG Dışa Aktarma

Chrome ile her ekran için ayrı PNG almak için:

1. Dosyayı tarayıcıda açın (`?screen=1` ile başlayın).
2. Ekran görüntüsünü 1080x1920 çözünürlükte alın.
3. `?screen` değerini 2, 3, 4, 5 olarak değiştirip tekrar alın.

Alternatif (Playwright):

Önkoşul:

```bash
npm install -D playwright
npx playwright install chromium
```

```bash
node -e "const { chromium } = require('playwright'); (async()=>{ const browser = await chromium.launch({headless:true}); const page = await browser.newPage({viewport:{width:1080,height:1920}}); for(let i=1;i<=5;i++){ await page.goto('file:///ABSOLUTE_PATH/marketing/play-store-screenshots.html?screen='+i); await page.screenshot({path:'screen-'+i+'.png'});} await browser.close(); })();"
```

> `ABSOLUTE_PATH` kısmını kendi tam yolunuz ile değiştirin.

## Google Play Uygunluğu

Bu şablon doğrudan **1080x1920 px (9:16)** oranında tasarlandığı için Google Play ekran görüntüsü format beklentisiyle uyumludur.
