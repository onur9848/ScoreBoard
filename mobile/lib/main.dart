import 'dart:async';

import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app.dart';
import 'services/analytics_service.dart';
import 'state/providers.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('tr');
  final prefs = await SharedPreferences.getInstance();
  final analytics = await _initFirebase();

  runApp(
    ProviderScope(
      overrides: [sharedPreferencesProvider.overrideWithValue(prefs), analyticsProvider.overrideWithValue(analytics)],
      child: const PuanTablosuApp(),
    ),
  );
}

/// Firebase yalnızca yapılandırma dosyası (google-services.json /
/// GoogleService-Info.plist) varsa açılır; yoksa uygulama analitiksiz çalışır.
Future<AnalyticsService> _initFirebase() async {
  if (kIsWeb) return const DebugAnalyticsService();
  try {
    await Firebase.initializeApp();
    FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;
    PlatformDispatcher.instance.onError = (error, stack) {
      FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
      return true;
    };
    return FirebaseAnalyticsService(FirebaseAnalytics.instance);
  } catch (e) {
    debugPrint('Firebase başlatılamadı, analitik kapalı: $e');
    return const DebugAnalyticsService();
  }
}
