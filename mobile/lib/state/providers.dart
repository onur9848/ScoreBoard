import 'package:collection/collection.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/game_repository.dart';
import '../domain/game_engine.dart';
import '../domain/models/game.dart';
import '../services/analytics_service.dart';
import '../services/tile_recognizer.dart';

/// `main.dart` içinde gerçek örneklerle değiştirilir.
final sharedPreferencesProvider = Provider<SharedPreferences>(
  (ref) => throw UnimplementedError('sharedPreferencesProvider override edilmeli'),
);

final analyticsProvider = Provider<AnalyticsService>((ref) => const DebugAnalyticsService());

final legacyGameSourceProvider = Provider<LegacyGameSource?>((ref) => const MethodChannelLegacyGameSource());

final gameRepositoryProvider = Provider<GameRepository>(
  (ref) => SharedPrefsGameRepository(
    ref.watch(sharedPreferencesProvider),
    legacySource: ref.watch(legacyGameSourceProvider),
  ),
);

final rulePreferencesProvider = Provider<RulePreferences>(
  (ref) => RulePreferences(ref.watch(sharedPreferencesProvider)),
);

final gameEngineProvider = Provider<GameEngine>((ref) => GameEngine());

final tileRecognizerProvider = Provider<TileRecognizer>((ref) => MlKitTileRecognizer());

final gamesProvider = AsyncNotifierProvider<GamesNotifier, List<Game>>(GamesNotifier.new);

final gameProvider = Provider.family<Game?, String>(
  (ref, id) => ref.watch(gamesProvider).value?.firstWhereOrNull((g) => g.id == id),
);

/// En son değiştirilen, bitmemiş oyun ("Kaldığın yerden devam et").
final lastActiveGameProvider = Provider<Game?>(
  (ref) => ref.watch(gamesProvider).value?.firstWhereOrNull((g) => !g.isFinished),
);

class GamesNotifier extends AsyncNotifier<List<Game>> {
  GameRepository get _repo => ref.read(gameRepositoryProvider);

  @override
  Future<List<Game>> build() async {
    final legacy = ref.read(legacyGameSourceProvider);
    if (legacy != null) await ref.read(rulePreferencesProvider).importLegacy(legacy);
    return _repo.loadAll();
  }

  List<Game> get _current => state.value ?? const [];

  Future<void> save(Game game) async {
    final others = _current.where((g) => g.id != game.id);
    state = AsyncData([game, ...others]);
    await _repo.save(game);
  }

  /// Oyuna bir değişiklik uygular ve kaydeder. Oyun yoksa `null` döner.
  Future<Game?> apply(String gameId, Game Function(GameEngine engine, Game game) change) async {
    final game = _current.firstWhereOrNull((g) => g.id == gameId);
    if (game == null) return null;
    final updated = change(ref.read(gameEngineProvider), game);
    if (identical(updated, game)) return game;
    await save(updated);
    return updated;
  }

  Future<void> delete(String gameId) async {
    state = AsyncData(_current.where((g) => g.id != gameId).toList());
    await _repo.delete(gameId);
  }
}
