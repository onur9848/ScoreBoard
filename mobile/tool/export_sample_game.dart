// Backend sözleşme testleri için örnek bir 101 oyununun JSON'unu üretir:
//   dart run tool/export_sample_game.dart > ../backend/tests/ScoreBoard.Tests/Fixtures/sample-game.v2.json
import 'dart:convert';
import 'dart:io';

import 'package:puan_tablosu/domain/game_engine.dart';
import 'package:puan_tablosu/domain/models/game.dart';
import 'package:puan_tablosu/domain/models/game_type.dart';

void main() {
  var tick = 0;
  var ids = 0;
  final start = DateTime.utc(2026, 10, 18, 19);
  final engine = GameEngine(
    clock: () => start.add(Duration(minutes: 4 * tick++)),
    newId: () => 'id-${(++ids).toString().padLeft(3, '0')}',
  );

  var game = engine.createGame(
    title: 'Cuma Masası',
    type: GameType.yuzBirOkey,
    playerNames: ['xyz', 'abc'],
    ruleValues: {'penalty': 150},
  );
  final [xyz, abc] = game.players;
  game = engine.addPenalty(game, 'penalty', abc.id);
  game = engine.addHand(
    game,
    {xyz.id: 40, abc.id: 12},
    sources: {abc.id: ScoreSource.rackScan},
    rackScans: [
      RackScanSummary(
        playerId: abc.id,
        points: 12,
        tileCount: 3,
        recognizedTileCount: 3,
        correctedTileCount: 1,
        usedCamera: true,
      ),
    ],
  );
  final second = game.rounds.last.id;
  game = engine.editRound(game, second, {xyz.id: 44, abc.id: 12});
  game = engine.addFinish(game, 'handFinish', xyz.id, {abc.id: 404}, sources: {abc.id: ScoreSource.rule});
  game = engine.finish(game);
  stdout.writeln(const JsonEncoder.withIndent('  ').convert(game.toJson()));
}
