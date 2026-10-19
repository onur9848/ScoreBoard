import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../domain/models/game.dart';
import '../domain/models/game_type.dart';
import 'legacy_game_parser.dart';

abstract interface class GameRepository {
  Future<List<Game>> loadAll();
  Future<Game?> load(String gameId);
  Future<void> save(Game game);
  Future<void> delete(String gameId);
}

/// Oyunları cihazda JSON olarak saklar. Her oyun ayrı anahtardadır; bir
/// kaydın bozulması diğerlerini etkilemez.
class SharedPrefsGameRepository implements GameRepository {
  SharedPrefsGameRepository(this._prefs, {this._legacySource});

  static const _indexKey = 'games.index';
  static const _gamePrefix = 'games.item.';
  static const _legacyMigratedKey = 'games.legacy_migrated';

  final SharedPreferences _prefs;
  final LegacyGameSource? _legacySource;

  List<String> get _ids => _prefs.getStringList(_indexKey) ?? const [];

  @override
  Future<List<Game>> loadAll() async {
    await _migrateLegacyOnce();
    final games = <Game>[];
    for (final id in _ids) {
      final raw = _prefs.getString('$_gamePrefix$id');
      if (raw == null) continue;
      try {
        games.add(Game.fromJson(jsonDecode(raw) as Map<String, dynamic>));
      } catch (e, st) {
        debugPrint('Oyun okunamadı ($id): $e\n$st');
      }
    }
    games.sort((a, b) => b.updatedAt.compareTo(a.updatedAt));
    return games;
  }

  @override
  Future<Game?> load(String gameId) async {
    final raw = _prefs.getString('$_gamePrefix$gameId');
    if (raw == null) return null;
    try {
      return Game.fromJson(jsonDecode(raw) as Map<String, dynamic>);
    } catch (e) {
      debugPrint('Oyun okunamadı ($gameId): $e');
      return null;
    }
  }

  @override
  Future<void> save(Game game) async {
    await _prefs.setString('$_gamePrefix${game.id}', jsonEncode(game.toJson()));
    if (!_ids.contains(game.id)) {
      await _prefs.setStringList(_indexKey, [..._ids, game.id]);
    }
  }

  @override
  Future<void> delete(String gameId) async {
    await _prefs.remove('$_gamePrefix$gameId');
    await _prefs.setStringList(_indexKey, _ids.where((id) => id != gameId).toList());
  }

  Future<void> _migrateLegacyOnce() async {
    final source = _legacySource;
    if (source == null || (_prefs.getBool(_legacyMigratedKey) ?? false)) return;
    try {
      final legacy = await source.readGames();
      for (final raw in legacy) {
        final game = LegacyGameParser.tryParse(raw);
        if (game != null && !_ids.contains(game.id)) await save(game);
      }
      await _prefs.setBool(_legacyMigratedKey, true);
    } catch (e) {
      // Bir sonraki açılışta yeniden denenir.
      debugPrint('Eski oyunlar taşınamadı: $e');
    }
  }
}

/// Eski Kotlin uygulamasının SharedPreferences dosyalarını okur.
abstract interface class LegacyGameSource {
  Future<List<String>> readGames();
  Future<Map<String, String>> readRuleValues();
}

class MethodChannelLegacyGameSource implements LegacyGameSource {
  const MethodChannelLegacyGameSource();

  static const _channel = MethodChannel('puan_tablosu/legacy');

  @override
  Future<List<String>> readGames() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return const [];
    final result = await _channel.invokeListMethod<String>('readGames');
    return result ?? const [];
  }

  @override
  Future<Map<String, String>> readRuleValues() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) return const {};
    final result = await _channel.invokeMapMethod<String, String>('readRuleValues');
    return result ?? const {};
  }
}

/// Kullanıcının oyun türü başına son kullandığı kural değerleri ve eşli/tekli
/// tercihi. Yeni oyunda varsayılan olarak önerilir.
class RulePreferences {
  RulePreferences(this._prefs);

  final SharedPreferences _prefs;

  String _key(GameType type, String rule) => 'rules.${type.wireName}.$rule';

  Map<String, int> load(GameType type, Map<String, int> defaults) => {
    for (final e in defaults.entries) e.key: _prefs.getInt(_key(type, e.key)) ?? e.value,
  };

  Future<void> save(GameType type, Map<String, int> values) async {
    for (final e in values.entries) {
      await _prefs.setInt(_key(type, e.key), e.value);
    }
  }

  bool? loadPartnered(GameType type) => _prefs.getBool('rules.${type.wireName}.partnered');

  Future<void> savePartnered(GameType type, bool value) => _prefs.setBool('rules.${type.wireName}.partnered', value);

  /// Eski uygulamadaki `scoreboard_rules` değerlerini bir kez içeri alır.
  Future<void> importLegacy(LegacyGameSource source) async {
    const flag = 'rules.legacy_imported';
    if (_prefs.getBool(flag) ?? false) return;
    try {
      final values = await source.readRuleValues();
      for (final e in values.entries) {
        final parsed = int.tryParse(e.value.trim());
        if (parsed == null) continue;
        if (e.key.startsWith('yuzbir_')) {
          await _prefs.setInt(_key(GameType.yuzBirOkey, e.key.substring(7)), parsed);
        }
      }
      await _prefs.setBool(flag, true);
    } catch (e) {
      debugPrint('Eski kural ayarları taşınamadı: $e');
    }
  }
}
