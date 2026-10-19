import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/foundation.dart';

/// Ürün analitiği (Firebase). Oyunun kendi takibi [Game.events] ile yapılır;
/// bu servis yalnızca ekran ve etkileşim istatistikleri içindir.
abstract interface class AnalyticsService {
  void screen(String name);
  void event(String name, [Map<String, Object>? params]);
}

class FirebaseAnalyticsService implements AnalyticsService {
  FirebaseAnalyticsService(this._analytics);

  final FirebaseAnalytics _analytics;

  @override
  void screen(String name) => _analytics.logScreenView(screenName: name).ignore();

  @override
  void event(String name, [Map<String, Object>? params]) =>
      _analytics.logEvent(name: name, parameters: params).ignore();
}

/// Firebase yapılandırılmadığında (geliştirme, web, testler) kullanılır.
class DebugAnalyticsService implements AnalyticsService {
  const DebugAnalyticsService();

  @override
  void screen(String name) {
    if (kDebugMode) debugPrint('[analytics] screen $name');
  }

  @override
  void event(String name, [Map<String, Object>? params]) {
    if (kDebugMode) debugPrint('[analytics] $name ${params ?? ''}');
  }
}
