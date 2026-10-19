import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:puan_tablosu/data/game_repository.dart';
import 'package:puan_tablosu/data/legacy_game_parser.dart';
import 'package:puan_tablosu/domain/game_engine.dart';
import 'package:puan_tablosu/domain/models/game.dart';
import 'package:puan_tablosu/domain/models/game_type.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Eski Kotlin uygulamasının Gson çıktısı (YuzBirOkeyConfig ile).
const legacyYuzBir = '''
{"gameId":"g-101","gameTitle":"Kahvehane Derbisi",
 "playerList":[{"id":"p1","name":"Ali"},{"id":"p2","name":"Veli"}],
 "score":[{"scoreOrder":2,"scoreMap":{"p1":0,"p2":150}},
          {"scoreOrder":1,"scoreMap":{"p1":40,"p2":12}},
          {"scoreOrder":3,"scoreMap":{"p1":-101,"p2":202}}],
 "gameType":"YuzBirOkey",
 "config":{"isPartnered":false,"rules":[
   {"key":"penalty","label":"Ceza Puanı","types":["PlayerPenaltyScore"],"value":"150"},
   {"key":"normalFinish","label":"Normal Bitiş","types":["FinishScore"],"value":"-101","pairedKey":"noOpenPenalty"},
   {"key":"noOpenPenalty","label":"Açmama Cezası","types":["FinishScore"],"value":"abc"}]},
 "lastModified":1760900000000}
''';

class FakeLegacySource implements LegacyGameSource {
  FakeLegacySource(this.games, {this.fail = false});

  final List<String> games;
  bool fail;
  int reads = 0;

  @override
  Future<List<String>> readGames() async {
    reads++;
    if (fail) throw StateError('channel');
    return games;
  }

  @override
  Future<Map<String, String>> readRuleValues() async => {'yuzbir_penalty': '120', 'yuzbir_handFinish': 'x'};
}

void main() {
  group('LegacyGameParser', () {
    test('101 oyununu kurallarıyla birlikte çevirir', () {
      final game = LegacyGameParser.tryParse(legacyYuzBir)!;
      expect(game.id, 'g-101');
      expect(game.type, GameType.yuzBirOkey);
      expect(game.settings.isPartnered, isFalse);
      expect(game.settings.winCondition, WinCondition.lowestTotal);
      expect(game.settings.valueOf('penalty'), 150);
      // Geçersiz değer varsayılana döner.
      expect(game.settings.valueOf('noOpenPenalty'), 202);
      expect(game.settings.customizedRuleKeys, {'penalty'});
      expect(game.players.map((p) => p.name), ['Ali', 'Veli']);
      // scoreOrder'a göre sıralanır; tek oyuncuya ceza değeri penalty sayılır.
      expect(game.rounds.map((r) => r.kind), [RoundKind.hand, RoundKind.penalty, RoundKind.hand]);
      expect(game.rounds[1].actorPlayerId, 'p2');
      expect(game.handCount, 2);
      expect(game.events.single.data['imported_from_legacy'], isTrue);
      expect(game.updatedAt, DateTime.fromMillisecondsSinceEpoch(1760900000000));
    });

    test('config olmadan genel oyunu çevirir, bozuk veriyi atlar', () {
      final game = LegacyGameParser.tryParse(
        '{"gameId":"g","gameTitle":"","playerList":[{"id":"a","name":"A"}],"score":[],"gameType":"GenelOyun"}',
      )!;
      expect(game.type, GameType.generic);
      expect(game.title, 'Eski Oyun');
      expect(LegacyGameParser.tryParse('not json'), isNull);
      expect(LegacyGameParser.tryParse('[1,2]'), isNull);
    });
  });

  group('SharedPrefsGameRepository', () {
    setUp(() => SharedPreferences.setMockInitialValues({}));

    test('kaydeder, günceller, siler', () async {
      final prefs = await SharedPreferences.getInstance();
      final repo = SharedPrefsGameRepository(prefs);
      final engine = GameEngine();
      var game = engine.createGame(title: 'A', type: GameType.generic, playerNames: ['x', 'y']);
      await repo.save(game);
      game = engine.addHand(game, {for (final p in game.players) p.id: 5});
      await repo.save(game);

      final loaded = await repo.loadAll();
      expect(loaded.single.rounds.length, 1);

      await repo.delete(game.id);
      expect(await repo.loadAll(), isEmpty);
    });

    test('bozuk kayıt diğerlerini engellemez', () async {
      final prefs = await SharedPreferences.getInstance();
      final repo = SharedPrefsGameRepository(prefs);
      final game = GameEngine().createGame(title: 'A', type: GameType.generic, playerNames: ['x']);
      await repo.save(game);
      await prefs.setStringList('games.index', ['broken', game.id]);
      await prefs.setString('games.item.broken', '{"id":1}');
      expect((await repo.loadAll()).map((g) => g.id), [game.id]);
    });

    test('eski oyunları bir kez taşır; hata olursa sonra yeniden dener', () async {
      final prefs = await SharedPreferences.getInstance();
      final source = FakeLegacySource([legacyYuzBir, 'bozuk'], fail: true);
      final repo = SharedPrefsGameRepository(prefs, legacySource: source);

      expect(await repo.loadAll(), isEmpty);
      source.fail = false;
      final games = await repo.loadAll();
      expect(games.single.id, 'g-101');
      await repo.loadAll();
      expect(source.reads, 2);
      expect(jsonDecode(prefs.getString('games.item.g-101')!)['game_type'], 'yuzbir_okey');
    });
  });

  test('RulePreferences eski kural değerlerini içeri alır', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final rules = RulePreferences(prefs);
    await rules.importLegacy(FakeLegacySource(const []));
    final values = rules.load(GameType.yuzBirOkey, {'penalty': 101, 'handFinish': -202});
    expect(values, {'penalty': 120, 'handFinish': -202});
  });
}
