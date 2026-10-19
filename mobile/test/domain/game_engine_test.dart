import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:puan_tablosu/domain/game_engine.dart';
import 'package:puan_tablosu/domain/models/game.dart';
import 'package:puan_tablosu/domain/models/game_event.dart';
import 'package:puan_tablosu/domain/models/game_type.dart';
import 'package:puan_tablosu/domain/scoring/standings.dart';

void main() {
  late GameEngine engine;
  late DateTime now;
  var id = 0;

  setUp(() {
    id = 0;
    now = DateTime.utc(2026, 10, 19, 20);
    engine = GameEngine(clock: () => now, newId: () => 'id${id++}');
  });

  Game yuzBir({Map<String, int>? rules}) => engine.createGame(
    title: 'Cuma Masası',
    type: GameType.yuzBirOkey,
    playerNames: [' xyz ', 'abc'],
    isPartnered: true,
    ruleValues: rules,
  );

  group('createGame', () {
    test('kural değerlerini, değiştirilenleri ve oyuncuları kaydeder', () {
      final game = yuzBir(rules: {'penalty': 150, 'unknown': 5});

      expect(game.players.map((p) => p.name), ['xyz', 'abc']);
      expect(game.players.map((p) => p.seat), [0, 1]);
      expect(game.settings.isPartnered, isTrue);
      expect(game.settings.winCondition, WinCondition.lowestTotal);
      expect(game.settings.valueOf('penalty'), 150);
      expect(game.settings.valueOf('handFinish'), -202);
      expect(game.settings.ruleValues.containsKey('unknown'), isFalse);
      expect(game.settings.customizedRuleKeys, {'penalty'});

      final created = game.events.single;
      expect(created.type, GameEventType.gameCreated);
      expect(created.data['customized_rules'], [
        {'key': 'penalty', 'default': 101, 'value': 150},
      ]);
      expect(created.data['player_count'], 2);
    });

    test('genel oyunda eşli mod ve kural yoktur', () {
      final game = engine.createGame(
        title: 'Batak',
        type: GameType.generic,
        playerNames: ['a', 'b', 'c'],
        isPartnered: true,
      );
      expect(game.settings.isPartnered, isFalse);
      expect(game.settings.ruleValues, isEmpty);
      expect(game.settings.winCondition, WinCondition.highestTotal);
    });
  });

  group('eller', () {
    test('normal el, ceza ve bitiş eklenir; ceza el sayısını artırmaz', () {
      var game = yuzBir();
      final [xyz, abc] = game.players;

      game = engine.addHand(game, {xyz.id: 40, abc.id: 12});
      game = engine.addPenalty(game, 'penalty', abc.id);
      game = engine.addFinish(game, 'handFinish', xyz.id, {abc.id: 404});

      expect(game.rounds.map((r) => r.kind), [RoundKind.hand, RoundKind.penalty, RoundKind.finish]);
      expect(game.handCount, 2);
      expect(game.rounds[1].scores, {xyz.id: 0, abc.id: 101});
      expect(game.rounds[2].scores, {xyz.id: -202, abc.id: 404});
      expect(game.rounds[2].sourceOf(xyz.id), ScoreSource.rule);
      expect(Standings.totals(game), {xyz.id: -162, abc.id: 517});

      final types = game.events.map((e) => e.type).toList();
      expect(types, [
        GameEventType.gameCreated,
        GameEventType.roundAdded,
        GameEventType.roundAdded,
        GameEventType.ruleApplied,
        GameEventType.roundAdded,
        GameEventType.ruleApplied,
      ]);
      final finishRule = game.events.last;
      expect(finishRule.data['rule_key'], 'handFinish');
      expect(finishRule.data['value'], -202);
      expect(game.events[4].data['hand_number'], 2);
      expect(game.events[2].data['hand_number'], isNull);
    });

    test('ıstaka taraması olay olarak kaydedilir', () {
      var game = yuzBir();
      final [xyz, abc] = game.players;
      game = engine.addHand(
        game,
        {xyz.id: 37, abc.id: 5},
        sources: {xyz.id: ScoreSource.rackScan},
        rackScans: const [
          RackScanSummary(
            playerId: 'id0',
            points: 37,
            tileCount: 9,
            recognizedTileCount: 8,
            correctedTileCount: 2,
            usedCamera: true,
          ),
        ],
      );
      final scan = game.events.last;
      expect(scan.type, GameEventType.rackScanUsed);
      expect(scan.data['corrected_tile_count'], 2);
      expect(scan.data['round_id'], game.rounds.single.id);
      expect(game.rounds.single.sourceOf(xyz.id), ScoreSource.rackScan);
      expect(game.rounds.single.sourceOf(abc.id), ScoreSource.manual);
    });

    test('eksik oyuncu ya da geçersiz kural reddedilir', () {
      final game = yuzBir();
      final [xyz, abc] = game.players;
      expect(() => engine.addHand(game, {xyz.id: 1}), throwsArgumentError);
      expect(() => engine.addPenalty(game, 'normalFinish', xyz.id), throwsArgumentError);
      expect(() => engine.addFinish(game, 'penalty', xyz.id, {abc.id: 1}), throwsArgumentError);
      expect(() => engine.addPenalty(game, 'penalty', 'yok'), throwsArgumentError);
    });

    test('düzenleme ve silme günlüğe yazılır, değişen puanın kaynağı manuel olur', () {
      var game = yuzBir();
      final [xyz, abc] = game.players;
      game = engine.addFinish(game, 'normalFinish', xyz.id, {abc.id: 202});
      final roundId = game.rounds.single.id;

      game = engine.editRound(game, roundId, {xyz.id: -101, abc.id: 150});
      expect(game.rounds.single.scores[abc.id], 150);
      expect(game.rounds.single.sourceOf(xyz.id), ScoreSource.rule);
      expect(game.rounds.single.sourceOf(abc.id), ScoreSource.manual);
      expect(game.events.last.type, GameEventType.roundEdited);

      game = engine.deleteRound(game, roundId);
      expect(game.rounds, isEmpty);
      expect(game.events.last.data['rule_key'], 'normalFinish');
    });
  });

  group('bitiş', () {
    test('101de en düşük puan kazanır ve süre kaydedilir', () {
      var game = yuzBir();
      final [xyz, abc] = game.players;
      game = engine.addHand(game, {xyz.id: 80, abc.id: 30});
      now = now.add(const Duration(minutes: 45));
      game = engine.finish(game);

      expect(game.isFinished, isTrue);
      expect(game.winnerPlayerIds, [abc.id]);
      final event = game.events.last;
      expect(event.type, GameEventType.gameFinished);
      expect(event.data['winner_names'], ['abc']);
      expect(event.data['duration_seconds'], 45 * 60);
      expect(event.data['hand_count'], 1);

      game = engine.reopen(game);
      expect(game.isFinished, isFalse);
      expect(game.winnerPlayerIds, isEmpty);
      expect(game.finishedAt, isNull);
    });

    test('beraberlikte birden fazla kazanan olur', () {
      var game = engine.createGame(title: 't', type: GameType.generic, playerNames: ['a', 'b', 'c']);
      final [a, b, c] = game.players;
      game = engine.addHand(game, {a.id: 10, b.id: 10, c.id: 3});
      expect(Standings.leaders(game), [a.id, b.id]);
      final standings = Standings.compute(game);
      expect(standings.map((s) => s.rank), [1, 1, 3]);
    });
  });

  test('JSON gidiş-dönüşü oyunu kayıpsız saklar', () {
    var game = yuzBir(rules: {'penalty': 120});
    final [xyz, abc] = game.players;
    game = engine.addHand(game, {xyz.id: 3, abc.id: 4}, sources: {xyz.id: ScoreSource.rackScan});
    game = engine.addPenalty(game, 'penalty', xyz.id);
    game = engine.finish(game);

    final encoded = jsonEncode(game.toJson());
    final decoded = Game.fromJson(jsonDecode(encoded) as Map<String, dynamic>);

    expect(jsonEncode(decoded.toJson()), encoded);
    expect(decoded.settings.customizedRuleKeys, {'penalty'});
    expect(decoded.rounds.first.sourceOf(xyz.id), ScoreSource.rackScan);
    expect(decoded.winnerPlayerIds, game.winnerPlayerIds);
    expect(decoded.toJson()['schema_version'], gameSchemaVersion);
  });

  test('bilinmeyen olay türleri okunurken korunur', () {
    final event = GameEvent.fromJson({
      'id': 'e1',
      'type': 'future_event',
      'occurred_at': '2026-10-19T20:00:00.000Z',
      'data': {'x': 1},
    });
    expect(event.type, isNull);
    expect(event.toJson(), {
      'id': 'e1',
      'type': 'future_event',
      'occurred_at': '2026-10-19T20:00:00.000Z',
      'data': {'x': 1},
    });
  });
}
