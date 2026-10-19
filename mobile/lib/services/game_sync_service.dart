import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

import '../domain/models/game.dart';

/// Backend adresi ve anahtarı derleme sırasında verilir:
/// `flutter run --dart-define=API_BASE_URL=https://... --dart-define=API_KEY=...`
/// İkisi de verilmezse senkronizasyon kapalıdır ve uygulama yalnızca cihazda çalışır.
class SyncConfig {
  const SyncConfig({this.baseUrl, this.apiKey = ''});

  factory SyncConfig.fromEnvironment() {
    const url = String.fromEnvironment('API_BASE_URL');
    const key = String.fromEnvironment('API_KEY');
    return SyncConfig(baseUrl: url.isEmpty ? null : Uri.tryParse(url), apiKey: key);
  }

  final Uri? baseUrl;
  final String apiKey;

  bool get isEnabled => baseUrl != null && apiKey.isNotEmpty;
}

typedef GameLookup = Future<Game?> Function(String gameId);

/// Oyunları backend'e gönderir. Her kayıttan sonra oyun kuyruğa girer; ağ yoksa
/// kuyruk cihazda kalır ve sonraki kayıtta, uygulama açılışında ya da artan
/// aralıklarla yeniden denenir. Backend aynı oyunun tekrar gönderilmesini tolere eder.
class GameSyncService {
  GameSyncService({
    required this._prefs,
    required this._config,
    required this._lookup,
    http.Client? client,
    String? platform,
    this._appVersion,
    this.retryDelays = const [Duration(seconds: 30), Duration(minutes: 2), Duration(minutes: 10)],
  }) : _client = client ?? http.Client(),
       _platform = platform ?? (kIsWeb ? 'web' : defaultTargetPlatform.name);

  static const _deviceIdKey = 'sync.device_id';
  static const _pendingKey = 'sync.pending';
  static const _syncedKey = 'sync.synced';
  static const _timeout = Duration(seconds: 20);

  final SharedPreferences _prefs;
  final SyncConfig _config;
  final GameLookup _lookup;
  final http.Client _client;
  final String _platform;
  final Future<String?> Function()? _appVersion;

  /// Başarısız denemeden sonra bekleme süreleri; son değer tekrar eder.
  final List<Duration> retryDelays;

  Future<void>? _running;
  bool _again = false;
  Timer? _retry;
  int _failures = 0;
  String? _version;

  /// Uygulamanın bu kurulumu için bir kez üretilen kimlik. Kişisel bilgi içermez.
  String get deviceId {
    final existing = _prefs.getString(_deviceIdKey);
    if (existing != null) return existing;
    final id = const Uuid().v4();
    unawaited(_prefs.setString(_deviceIdKey, id));
    return id;
  }

  List<String> get pending => _prefs.getStringList(_pendingKey) ?? const [];

  Map<String, String> get _synced {
    final raw = _prefs.getString(_syncedKey);
    if (raw == null) return {};
    try {
      return (jsonDecode(raw) as Map<String, dynamic>).cast<String, String>();
    } catch (_) {
      return {};
    }
  }

  /// Oyunu kuyruğa ekler ve göndermeyi dener.
  Future<void> enqueue(String gameId) async {
    if (!pending.contains(gameId)) await _prefs.setStringList(_pendingKey, [...pending, gameId]);
    await flush();
  }

  /// Açılışta: son gönderilen halinden sonra değişmiş oyunları kuyruğa ekler.
  Future<void> enqueueChanged(Iterable<Game> games) async {
    final synced = _synced;
    final changed = [
      for (final game in games)
        if (synced[game.id] != _stamp(game)) game.id,
    ];
    final queue = {...pending, ...changed}.toList();
    await _prefs.setStringList(_pendingKey, queue);
    await flush();
  }

  /// Cihazdan silinen oyun artık gönderilmez. Backend'deki kayıt kullanım verisi olarak kalır.
  Future<void> forget(String gameId) async {
    await _prefs.setStringList(_pendingKey, pending.where((id) => id != gameId).toList());
    final synced = _synced..remove(gameId);
    await _prefs.setString(_syncedKey, jsonEncode(synced));
  }

  /// Kuyruğu gönderir. Aynı anda tek bir gönderim çalışır; arada gelen istek
  /// mevcut gönderim bitince bir tur daha çalıştırır.
  Future<void> flush() {
    if (!_config.isEnabled) return Future.value();
    if (_running != null) {
      _again = true;
      return _running!;
    }
    return _running = _drain().whenComplete(() => _running = null);
  }

  Future<void> _drain() async {
    do {
      _again = false;
      _retry?.cancel();
      for (final id in List.of(pending)) {
        final outcome = await _upload(id);
        if (outcome == _Outcome.retryLater) {
          _scheduleRetry();
          return;
        }
        await _prefs.setStringList(_pendingKey, pending.where((p) => p != id).toList());
      }
      _failures = 0;
    } while (_again);
  }

  Future<_Outcome> _upload(String id) async {
    final game = await _lookup(id);
    if (game == null) return _Outcome.dropped;
    final url = _config.baseUrl!.resolve('api/v1/sessions/${Uri.encodeComponent(id)}');
    try {
      final response = await _client
          .put(
            url,
            headers: {
              'Content-Type': 'application/json',
              'X-Api-Key': _config.apiKey,
              'X-Device-Id': deviceId,
              'X-Platform': _platform,
              'X-App-Version': ?await _appVersionOnce(),
            },
            body: jsonEncode(game.toJson()),
          )
          .timeout(_timeout);
      final status = response.statusCode;
      if (status >= 200 && status < 300) {
        await _prefs.setString(_syncedKey, jsonEncode(_synced..[id] = _stamp(game)));
        return _Outcome.sent;
      }
      // Geçersiz veri ya da başka cihazın oyunu: tekrar denemek düzeltmez.
      if (status == 400 || status == 409 || status == 413 || status == 422) {
        debugPrint('Oyun backend tarafından reddedildi ($id): $status ${response.body}');
        return _Outcome.dropped;
      }
      debugPrint('Oyun gönderilemedi ($id): $status');
      return _Outcome.retryLater;
    } catch (e) {
      debugPrint('Oyun gönderilemedi ($id): $e');
      return _Outcome.retryLater;
    }
  }

  Future<String?> _appVersionOnce() async => _version ??= await _appVersion?.call();

  void _scheduleRetry() {
    if (retryDelays.isEmpty) return;
    final delay = retryDelays[min(_failures, retryDelays.length - 1)];
    _failures++;
    _retry?.cancel();
    _retry = Timer(delay, () => unawaited(flush()));
  }

  static String _stamp(Game game) => '${game.updatedAt.toUtc().toIso8601String()}#${game.events.length}';

  void dispose() {
    _retry?.cancel();
    _client.close();
  }
}

enum _Outcome { sent, dropped, retryLater }
