import 'package:collection/collection.dart';

import 'game_event.dart';
import 'game_type.dart';

/// Bu dosyadaki JSON alan adları backend sözleşmesidir (snake_case).
/// Alan eklemek serbesttir; ad değiştirmek backend ile birlikte yapılmalıdır.
const int gameSchemaVersion = 2;

class Player {
  const Player({required this.id, required this.name, required this.seat});

  final String id;
  final String name;

  /// Masadaki sıra (0'dan başlar). Tablodaki sütun sırası da budur.
  final int seat;

  Player copyWith({String? name, int? seat}) => Player(id: id, name: name ?? this.name, seat: seat ?? this.seat);

  Map<String, dynamic> toJson() => {'id': id, 'name': name, 'seat': seat};

  factory Player.fromJson(Map<String, dynamic> json) =>
      Player(id: json['id'] as String, name: json['name'] as String? ?? '', seat: (json['seat'] as num?)?.toInt() ?? 0);
}

enum RoundKind {
  /// Normal el: herkese puan yazılır.
  hand('hand'),

  /// Tek bir oyuncuya ceza; el sayısını artırmaz.
  penalty('penalty'),

  /// Bir oyuncunun bitirdiği el (101'de normal bitiş, elden bitme vb.).
  finish('finish');

  const RoundKind(this.wireName);

  final String wireName;

  static RoundKind fromWire(String? value) =>
      RoundKind.values.firstWhere((k) => k.wireName == value, orElse: () => RoundKind.hand);
}

/// Puanın nereden geldiği. Panelde özellik kullanımını ölçmek için tutulur.
enum ScoreSource {
  manual('manual'),
  rule('rule'),
  rackScan('rack_scan');

  const ScoreSource(this.wireName);

  final String wireName;

  static ScoreSource fromWire(String? value) =>
      ScoreSource.values.firstWhere((s) => s.wireName == value, orElse: () => ScoreSource.manual);
}

class Round {
  const Round({
    required this.id,
    required this.kind,
    required this.scores,
    required this.createdAt,
    this.ruleKey,
    this.actorPlayerId,
    this.sources = const {},
  });

  final String id;
  final RoundKind kind;

  /// Oyuncu id -> puan. Masadaki her oyuncu için bir değer bulunur.
  final Map<String, int> scores;
  final DateTime createdAt;

  /// Kural ile eklendiyse kuralın anahtarı (örn. `handFinish`).
  final String? ruleKey;

  /// Cezayı alan ya da eli bitiren oyuncu.
  final String? actorPlayerId;

  /// Oyuncu id -> puanın kaynağı. Boşsa hepsi [ScoreSource.manual] sayılır.
  final Map<String, ScoreSource> sources;

  /// Penalty satırları el sayısına dahil edilmez.
  bool get countsAsHand => kind != RoundKind.penalty;

  int scoreOf(String playerId) => scores[playerId] ?? 0;

  ScoreSource sourceOf(String playerId) => sources[playerId] ?? ScoreSource.manual;

  Round copyWith({RoundKind? kind, Map<String, int>? scores, Map<String, ScoreSource>? sources}) => Round(
    id: id,
    kind: kind ?? this.kind,
    scores: scores ?? this.scores,
    createdAt: createdAt,
    ruleKey: ruleKey,
    actorPlayerId: actorPlayerId,
    sources: sources ?? this.sources,
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    'kind': kind.wireName,
    'rule_key': ruleKey,
    'actor_player_id': actorPlayerId,
    'created_at': createdAt.toUtc().toIso8601String(),
    'entries': [
      for (final entry in scores.entries)
        {'player_id': entry.key, 'points': entry.value, 'source': sourceOf(entry.key).wireName},
    ],
  };

  factory Round.fromJson(Map<String, dynamic> json) {
    final entries = (json['entries'] as List? ?? const []).cast<Map<String, dynamic>>();
    return Round(
      id: json['id'] as String,
      kind: RoundKind.fromWire(json['kind'] as String?),
      ruleKey: json['rule_key'] as String?,
      actorPlayerId: json['actor_player_id'] as String?,
      createdAt: DateTime.parse(json['created_at'] as String),
      scores: {for (final e in entries) e['player_id'] as String: (e['points'] as num).toInt()},
      sources: {for (final e in entries) e['player_id'] as String: ScoreSource.fromWire(e['source'] as String?)},
    );
  }
}

class GameSettings {
  const GameSettings({
    required this.isPartnered,
    required this.winCondition,
    this.ruleValues = const {},
    this.customizedRuleKeys = const {},
  });

  final bool isPartnered;
  final WinCondition winCondition;

  /// Kural anahtarı -> bu oyunda kullanılan değer.
  final Map<String, int> ruleValues;

  /// Varsayılandan farklı girilen kurallar; panelde "ayarları değiştirdi"
  /// bilgisini göstermek için.
  final Set<String> customizedRuleKeys;

  int? valueOf(String key) => ruleValues[key];

  GameSettings copyWith({
    bool? isPartnered,
    WinCondition? winCondition,
    Map<String, int>? ruleValues,
    Set<String>? customizedRuleKeys,
  }) => GameSettings(
    isPartnered: isPartnered ?? this.isPartnered,
    winCondition: winCondition ?? this.winCondition,
    ruleValues: ruleValues ?? this.ruleValues,
    customizedRuleKeys: customizedRuleKeys ?? this.customizedRuleKeys,
  );

  Map<String, dynamic> toJson() => {
    'is_partnered': isPartnered,
    'win_condition': winCondition.wireName,
    'rules': [
      for (final entry in ruleValues.entries)
        {'key': entry.key, 'value': entry.value, 'customized': customizedRuleKeys.contains(entry.key)},
    ],
  };

  factory GameSettings.fromJson(Map<String, dynamic> json) {
    final rules = (json['rules'] as List? ?? const []).cast<Map<String, dynamic>>();
    return GameSettings(
      isPartnered: json['is_partnered'] as bool? ?? false,
      winCondition: WinCondition.fromWire(json['win_condition'] as String?),
      ruleValues: {for (final r in rules) r['key'] as String: (r['value'] as num).toInt()},
      customizedRuleKeys: {
        for (final r in rules)
          if (r['customized'] == true) r['key'] as String,
      },
    );
  }
}

enum GameStatus {
  active('active'),
  finished('finished');

  const GameStatus(this.wireName);

  final String wireName;

  static GameStatus fromWire(String? value) =>
      GameStatus.values.firstWhere((s) => s.wireName == value, orElse: () => GameStatus.active);
}

/// Bir masanın oyun türü seçiminden bitişine kadar tüm hali.
class Game {
  const Game({
    required this.id,
    required this.title,
    required this.type,
    required this.settings,
    required this.players,
    required this.rounds,
    required this.events,
    required this.createdAt,
    required this.updatedAt,
    this.status = GameStatus.active,
    this.finishedAt,
    this.winnerPlayerIds = const [],
    this.isQuickGame = false,
  });

  final String id;
  final String title;
  final GameType type;
  final GameSettings settings;
  final List<Player> players;
  final List<Round> rounds;

  /// Oyun içinde olan her şeyin sıralı günlüğü. Backend'e bu şekilde gider.
  final List<GameEvent> events;
  final DateTime createdAt;
  final DateTime updatedAt;
  final GameStatus status;
  final DateTime? finishedAt;
  final List<String> winnerPlayerIds;
  final bool isQuickGame;

  bool get isFinished => status == GameStatus.finished;

  int get handCount => rounds.where((r) => r.countsAsHand).length;

  Player? playerById(String id) => players.firstWhereOrNull((p) => p.id == id);

  Game copyWith({
    String? title,
    GameSettings? settings,
    List<Player>? players,
    List<Round>? rounds,
    List<GameEvent>? events,
    DateTime? updatedAt,
    GameStatus? status,
    DateTime? finishedAt,
    bool clearFinishedAt = false,
    List<String>? winnerPlayerIds,
  }) => Game(
    id: id,
    title: title ?? this.title,
    type: type,
    settings: settings ?? this.settings,
    players: players ?? this.players,
    rounds: rounds ?? this.rounds,
    events: events ?? this.events,
    createdAt: createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
    status: status ?? this.status,
    finishedAt: clearFinishedAt ? null : (finishedAt ?? this.finishedAt),
    winnerPlayerIds: winnerPlayerIds ?? this.winnerPlayerIds,
    isQuickGame: isQuickGame,
  );

  Map<String, dynamic> toJson() => {
    'schema_version': gameSchemaVersion,
    'id': id,
    'title': title,
    'game_type': type.wireName,
    'status': status.wireName,
    'is_quick_game': isQuickGame,
    'created_at': createdAt.toUtc().toIso8601String(),
    'updated_at': updatedAt.toUtc().toIso8601String(),
    'finished_at': finishedAt?.toUtc().toIso8601String(),
    'winner_player_ids': winnerPlayerIds,
    'settings': settings.toJson(),
    'players': [for (final p in players) p.toJson()],
    'rounds': [for (final r in rounds) r.toJson()],
    'events': [for (final e in events) e.toJson()],
  };

  factory Game.fromJson(Map<String, dynamic> json) {
    final players = [
      for (final p in (json['players'] as List? ?? const []).cast<Map<String, dynamic>>()) Player.fromJson(p),
    ]..sort((a, b) => a.seat.compareTo(b.seat));
    return Game(
      id: json['id'] as String,
      title: json['title'] as String? ?? '',
      type: GameType.fromWire(json['game_type'] as String?),
      status: GameStatus.fromWire(json['status'] as String?),
      isQuickGame: json['is_quick_game'] as bool? ?? false,
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
      finishedAt: json['finished_at'] == null ? null : DateTime.parse(json['finished_at'] as String),
      winnerPlayerIds: (json['winner_player_ids'] as List? ?? const []).cast<String>(),
      settings: GameSettings.fromJson(json['settings'] as Map<String, dynamic>? ?? const {}),
      players: players,
      rounds: [for (final r in (json['rounds'] as List? ?? const []).cast<Map<String, dynamic>>()) Round.fromJson(r)],
      events: [
        for (final e in (json['events'] as List? ?? const []).cast<Map<String, dynamic>>()) GameEvent.fromJson(e),
      ],
    );
  }
}
