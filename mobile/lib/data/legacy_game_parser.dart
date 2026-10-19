import 'dart:convert';

import '../domain/models/game.dart';
import '../domain/models/game_event.dart';
import '../domain/models/game_type.dart';
import '../domain/rules/rule_catalog.dart';

/// Eski Kotlin uygulamasının Gson ile kaydettiği oyunları yeni modele çevirir.
///
/// Eski uygulama `config` alanını kaydedebiliyor ama geri okuyamıyordu
/// (Gson `IConfig` arayüzünü oluşturamıyordu), bu yüzden 101 ve Okey oyunları
/// geçmişte görünmüyordu. Burada config doğrudan JSON'dan okunur.
abstract final class LegacyGameParser {
  static Game? tryParse(String raw) {
    try {
      final json = jsonDecode(raw);
      if (json is! Map<String, dynamic>) return null;
      return parse(json);
    } catch (_) {
      return null;
    }
  }

  static Game parse(Map<String, dynamic> json) {
    final type = GameType.fromLegacy(json['gameType'] as String?);
    final updatedAt = DateTime.fromMillisecondsSinceEpoch(
      (json['lastModified'] as num?)?.toInt() ?? DateTime.now().millisecondsSinceEpoch,
    );

    final legacyPlayers = (json['playerList'] as List? ?? const []).cast<Map<String, dynamic>>();
    final players = [
      for (var i = 0; i < legacyPlayers.length; i++)
        Player(
          id: legacyPlayers[i]['id'] as String,
          name: legacyPlayers[i]['name'] as String? ?? 'Oyuncu ${i + 1}',
          seat: i,
        ),
    ];

    final config = json['config'] as Map<String, dynamic>?;
    final defaults = RuleCatalog.defaultValues(type);
    final ruleValues = {...defaults};
    for (final rule in (config?['rules'] as List? ?? const []).cast<Map<String, dynamic>>()) {
      final key = rule['key'] as String?;
      final value = int.tryParse('${rule['value']}'.trim());
      if (key != null && value != null && defaults.containsKey(key)) ruleValues[key] = value;
    }
    final settings = GameSettings(
      isPartnered: config?['isPartnered'] as bool? ?? false,
      winCondition: RuleCatalog.defaultWinCondition(type),
      ruleValues: ruleValues,
      customizedRuleKeys: {
        for (final e in ruleValues.entries)
          if (defaults[e.key] != e.value) e.key,
      },
    );

    final legacyScores = (json['score'] as List? ?? const []).cast<Map<String, dynamic>>()
      ..sort((a, b) => ((a['scoreOrder'] as num?) ?? 0).compareTo((b['scoreOrder'] as num?) ?? 0));
    final penaltyValue = ruleValues['penalty'];
    final rounds = <Round>[];
    for (var i = 0; i < legacyScores.length; i++) {
      final map = (legacyScores[i]['scoreMap'] as Map? ?? const {}).cast<String, dynamic>();
      final scores = {for (final p in players) p.id: (map[p.id] as num?)?.toInt() ?? 0};
      final nonZero = scores.entries.where((e) => e.value != 0).toList();
      final isPenalty = penaltyValue != null && nonZero.length == 1 && nonZero.single.value == penaltyValue;
      rounds.add(
        Round(
          id: '${json['gameId']}-legacy-$i',
          kind: isPenalty ? RoundKind.penalty : RoundKind.hand,
          ruleKey: isPenalty ? 'penalty' : null,
          actorPlayerId: isPenalty ? nonZero.single.key : null,
          scores: scores,
          createdAt: updatedAt,
        ),
      );
    }

    final id = json['gameId'] as String;
    return Game(
      id: id,
      title: (json['gameTitle'] as String?)?.trim().isNotEmpty == true ? json['gameTitle'] as String : 'Eski Oyun',
      type: type,
      settings: settings,
      players: players,
      rounds: rounds,
      createdAt: updatedAt,
      updatedAt: updatedAt,
      events: [
        GameEvent(
          id: '$id-legacy-import',
          type: GameEventType.gameCreated,
          occurredAt: updatedAt,
          data: {
            'game_type': type.wireName,
            'is_partnered': settings.isPartnered,
            'player_count': players.length,
            'players': [
              for (final p in players) {'id': p.id, 'name': p.name, 'seat': p.seat},
            ],
            'rule_values': ruleValues,
            'imported_from_legacy': true,
            'legacy_round_count': rounds.length,
          },
        ),
      ],
    );
  }
}
