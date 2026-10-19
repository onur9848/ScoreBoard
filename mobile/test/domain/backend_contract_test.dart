import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:puan_tablosu/domain/models/game.dart';

/// Backend testlerinin kullandığı örnek oyun bu uygulamanın ürettiği biçimle aynı kalmalı.
/// Dosya `dart run tool/export_sample_game.dart` ile yeniden üretilir.
void main() {
  test('backend örnek oyunu uygulamanın JSON biçimiyle birebir aynı', () {
    final file = File('../backend/tests/ScoreBoard.Tests/Fixtures/sample-game.v2.json');
    final json = jsonDecode(file.readAsStringSync()) as Map<String, dynamic>;
    final game = Game.fromJson(json);
    expect(game.toJson(), json);
    expect(json['schema_version'], gameSchemaVersion);
  });
}
