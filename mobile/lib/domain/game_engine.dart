import 'package:uuid/uuid.dart';

import 'models/game.dart';
import 'models/game_event.dart';
import 'models/game_type.dart';
import 'rules/rule_catalog.dart';
import 'scoring/standings.dart';

typedef Clock = DateTime Function();

/// Istaka ekranından bir oyuncunun puanına aktarılan sonuç.
class RackScanSummary {
  const RackScanSummary({
    required this.playerId,
    required this.points,
    required this.tileCount,
    required this.recognizedTileCount,
    required this.correctedTileCount,
    required this.usedCamera,
  });

  final String playerId;
  final int points;
  final int tileCount;

  /// Kameradan otomatik okunan taş sayısı.
  final int recognizedTileCount;

  /// Kullanıcının düzelttiği, eklediği ya da sildiği taş sayısı.
  final int correctedTileCount;
  final bool usedCamera;

  Map<String, Object?> toEventData() => {
    'player_id': playerId,
    'points': points,
    'tile_count': tileCount,
    'recognized_tile_count': recognizedTileCount,
    'corrected_tile_count': correctedTileCount,
    'used_camera': usedCamera,
  };
}

/// Oyun üzerindeki tüm değişiklikler buradan geçer; her değişiklik
/// [Game.events] günlüğüne bir kayıt düşer. Saf Dart, UI'dan bağımsızdır.
class GameEngine {
  GameEngine({Clock? clock, String Function()? newId})
    : _clock = clock ?? DateTime.now,
      _newId = newId ?? const Uuid().v4;

  final Clock _clock;
  final String Function() _newId;

  Game createGame({
    required String title,
    required GameType type,
    required List<String> playerNames,
    bool isPartnered = false,
    Map<String, int>? ruleValues,
    WinCondition? winCondition,
    bool isQuickGame = false,
  }) {
    final now = _clock();
    final defaults = RuleCatalog.defaultValues(type);
    final values = {...defaults, ...?ruleValues}..removeWhere((k, _) => !defaults.containsKey(k));
    final customized = {
      for (final e in values.entries)
        if (defaults[e.key] != e.value) e.key,
    };
    final players = [
      for (var i = 0; i < playerNames.length; i++) Player(id: _newId(), name: playerNames[i].trim(), seat: i),
    ];
    final settings = GameSettings(
      isPartnered: RuleCatalog.supportsPartnerMode(type) && isPartnered,
      winCondition: winCondition ?? RuleCatalog.defaultWinCondition(type),
      ruleValues: values,
      customizedRuleKeys: customized,
    );
    return Game(
      id: _newId(),
      title: title.trim(),
      type: type,
      settings: settings,
      players: players,
      rounds: const [],
      createdAt: now,
      updatedAt: now,
      isQuickGame: isQuickGame,
      events: [
        _event(GameEventType.gameCreated, now, {
          'game_type': type.wireName,
          'is_partnered': settings.isPartnered,
          'is_quick_game': isQuickGame,
          'win_condition': settings.winCondition.wireName,
          'player_count': players.length,
          'players': [
            for (final p in players) {'id': p.id, 'name': p.name, 'seat': p.seat},
          ],
          'rule_values': values,
          'customized_rules': [
            for (final key in customized) {'key': key, 'default': defaults[key], 'value': values[key]},
          ],
        }),
      ],
    );
  }

  /// Herkese puan yazılan normal el.
  Game addHand(
    Game game,
    Map<String, int> scores, {
    Map<String, ScoreSource> sources = const {},
    List<RackScanSummary> rackScans = const [],
  }) {
    _requireAllPlayers(game, scores);
    final now = _clock();
    final round = Round(
      id: _newId(),
      kind: RoundKind.hand,
      scores: Map.unmodifiable(scores),
      sources: Map.unmodifiable(sources),
      createdAt: now,
    );
    return _appendRound(game, round, now, rackScans: rackScans);
  }

  /// Tek bir oyuncuya ceza kuralı uygular (örn. 101 ceza).
  Game addPenalty(Game game, String ruleKey, String playerId) {
    final rule = _rule(game, ruleKey, RuleCategory.penalty);
    _requirePlayer(game, playerId);
    final now = _clock();
    final value = game.settings.valueOf(ruleKey) ?? rule.defaultValue;
    final round = Round(
      id: _newId(),
      kind: RoundKind.penalty,
      ruleKey: ruleKey,
      actorPlayerId: playerId,
      scores: {for (final p in game.players) p.id: p.id == playerId ? value : 0},
      sources: {playerId: ScoreSource.rule},
      createdAt: now,
    );
    return _appendRound(game, round, now, appliedRule: rule, value: value);
  }

  /// Bir oyuncunun bitirdiği el. Bitiren kuralın değerini alır; diğer
  /// oyuncuların puanı [otherScores] ile verilir.
  Game addFinish(
    Game game,
    String ruleKey,
    String finisherId,
    Map<String, int> otherScores, {
    Map<String, ScoreSource> sources = const {},
    List<RackScanSummary> rackScans = const [],
  }) {
    final rule = _rule(game, ruleKey, RuleCategory.finish);
    _requirePlayer(game, finisherId);
    final value = game.settings.valueOf(ruleKey) ?? rule.defaultValue;
    final scores = {...otherScores, finisherId: value};
    _requireAllPlayers(game, scores);
    final now = _clock();
    final round = Round(
      id: _newId(),
      kind: RoundKind.finish,
      ruleKey: ruleKey,
      actorPlayerId: finisherId,
      scores: Map.unmodifiable(scores),
      sources: Map.unmodifiable({...sources, finisherId: ScoreSource.rule}),
      createdAt: now,
    );
    return _appendRound(game, round, now, appliedRule: rule, value: value, rackScans: rackScans);
  }

  Game editRound(Game game, String roundId, Map<String, int> scores) {
    _requireAllPlayers(game, scores);
    final index = game.rounds.indexWhere((r) => r.id == roundId);
    if (index < 0) throw ArgumentError.value(roundId, 'roundId', 'Böyle bir el yok');
    final old = game.rounds[index];
    final now = _clock();
    final changedSources = {
      for (final id in scores.keys) id: scores[id] == old.scoreOf(id) ? old.sourceOf(id) : ScoreSource.manual,
    };
    final rounds = [...game.rounds]..[index] = old.copyWith(scores: scores, sources: changedSources);
    return _touch(
      game.copyWith(rounds: rounds),
      now,
      _event(GameEventType.roundEdited, now, {'round_id': roundId, 'previous_scores': old.scores, 'scores': scores}),
    );
  }

  Game deleteRound(Game game, String roundId) {
    final old = game.rounds.where((r) => r.id == roundId).firstOrNull;
    if (old == null) return game;
    final now = _clock();
    return _touch(
      game.copyWith(rounds: game.rounds.where((r) => r.id != roundId).toList()),
      now,
      _event(GameEventType.roundDeleted, now, {
        'round_id': roundId,
        'kind': old.kind.wireName,
        'rule_key': old.ruleKey,
        'scores': old.scores,
      }),
    );
  }

  /// Oyunu bitirir ve kazananları kaydeder.
  Game finish(Game game) {
    final now = _clock();
    final winners = Standings.leaders(game);
    final totals = Standings.totals(game);
    return _touch(
      game.copyWith(status: GameStatus.finished, finishedAt: now, winnerPlayerIds: winners),
      now,
      _event(GameEventType.gameFinished, now, {
        'winner_player_ids': winners,
        'winner_names': [for (final id in winners) game.playerById(id)?.name],
        'hand_count': game.handCount,
        'penalty_count': game.rounds.where((r) => r.kind == RoundKind.penalty).length,
        'duration_seconds': now.difference(game.createdAt).inSeconds,
        'totals': totals,
      }),
    );
  }

  Game reopen(Game game) {
    if (!game.isFinished) return game;
    final now = _clock();
    return _touch(
      game.copyWith(status: GameStatus.active, clearFinishedAt: true, winnerPlayerIds: const []),
      now,
      _event(GameEventType.gameReopened, now, const {}),
    );
  }

  /// Geçmişten bir oyun yeniden açıldığında.
  Game markResumed(Game game) {
    final now = _clock();
    return _touch(game, now, _event(GameEventType.gameResumed, now, {'hand_count': game.handCount}));
  }

  Game _appendRound(
    Game game,
    Round round,
    DateTime now, {
    RuleDefinition? appliedRule,
    int? value,
    List<RackScanSummary> rackScans = const [],
  }) {
    final handNumber = round.countsAsHand ? game.handCount + 1 : null;
    final events = <GameEvent>[
      _event(GameEventType.roundAdded, now, {
        'round_id': round.id,
        'kind': round.kind.wireName,
        'hand_number': handNumber,
        'rule_key': round.ruleKey,
        'actor_player_id': round.actorPlayerId,
        'scores': round.scores,
        'sources': {for (final id in round.scores.keys) id: round.sourceOf(id).wireName},
      }),
      if (appliedRule != null)
        _event(GameEventType.ruleApplied, now, {
          'round_id': round.id,
          'rule_key': appliedRule.key,
          'rule_label': appliedRule.label,
          'category': appliedRule.category.wireName,
          'player_id': round.actorPlayerId,
          'value': value,
        }),
      for (final scan in rackScans)
        _event(GameEventType.rackScanUsed, now, {'round_id': round.id, ...scan.toEventData()}),
    ];
    return game.copyWith(rounds: [...game.rounds, round], events: [...game.events, ...events], updatedAt: now);
  }

  Game _touch(Game game, DateTime now, GameEvent event) =>
      game.copyWith(events: [...game.events, event], updatedAt: now);

  GameEvent _event(GameEventType type, DateTime at, Map<String, Object?> data) =>
      GameEvent(id: _newId(), type: type, occurredAt: at, data: data);

  RuleDefinition _rule(Game game, String key, RuleCategory expected) {
    final rule = RuleCatalog.find(game.type, key);
    if (rule == null || rule.category != expected) {
      throw ArgumentError.value(key, 'ruleKey', '${game.type.label} için geçerli değil');
    }
    return rule;
  }

  void _requirePlayer(Game game, String playerId) {
    if (game.playerById(playerId) == null) {
      throw ArgumentError.value(playerId, 'playerId', 'Oyuncu bu masada değil');
    }
  }

  void _requireAllPlayers(Game game, Map<String, int> scores) {
    final ids = {for (final p in game.players) p.id};
    if (scores.length != ids.length || !scores.keys.every(ids.contains)) {
      throw ArgumentError('Tüm oyuncular için puan girilmelidir');
    }
  }
}
