package com.senerunosoft.puantablosu.model.enums

enum class RuleType {
    GameConfig,           // Genel oyun ayarı (örn: toplam el sayısı, ortak kurallar)
    PlayerPenaltyScore,   // Oyuncuya özel ceza/puan (örn: penalty)
    FinishScore           // Bitirme ve açmama gibi birden fazla oyuncuyu ilgilendiren skorlar (örn: normalFinish + noOpenPenalty birlikte)
}
