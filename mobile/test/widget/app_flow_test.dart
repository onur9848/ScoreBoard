import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:puan_tablosu/app.dart';
import 'package:puan_tablosu/domain/models/game.dart';
import 'package:puan_tablosu/domain/models/game_event.dart';
import 'package:puan_tablosu/domain/tiles/tile.dart';
import 'package:puan_tablosu/domain/tiles/tile_detection.dart';
import 'package:puan_tablosu/services/tile_recognizer.dart';
import 'package:puan_tablosu/state/providers.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FakeRecognizer implements TileRecognizer {
  @override
  bool get isSupported => false;

  @override
  Future<List<List<DetectedTile>>> recognize(String imagePath) async => const [];
}

Future<ProviderContainer> pumpApp(WidgetTester tester, {Size size = const Size(400, 860)}) async {
  tester.view.physicalSize = size * 2;
  tester.view.devicePixelRatio = 2;
  addTearDown(tester.view.reset);
  SharedPreferences.setMockInitialValues({});
  final prefs = await SharedPreferences.getInstance();
  final container = ProviderContainer(
    overrides: [
      sharedPreferencesProvider.overrideWithValue(prefs),
      legacyGameSourceProvider.overrideWithValue(null),
      tileRecognizerProvider.overrideWithValue(FakeRecognizer()),
    ],
  );
  addTearDown(container.dispose);
  await tester.pumpWidget(UncontrolledProviderScope(container: container, child: const PuanTablosuApp()));
  await tester.pumpAndSettle();
  return container;
}

Future<void> createYuzBirGame(WidgetTester tester) async {
  await tester.tap(find.text('Yeni Oyun'));
  await tester.pumpAndSettle();
  await tester.tap(find.byKey(const ValueKey('type-yuzbir_okey')));
  await tester.pumpAndSettle();
  // Ceza puanını değiştir; "ayarları değiştirdi" bilgisi kaydedilmeli.
  await tester.enterText(find.byKey(const ValueKey('rule-penalty')), '150');
  await tester.tap(find.text('Devam Et'));
  await tester.pumpAndSettle();

  await tester.enterText(find.byKey(const ValueKey('game-title')), 'Cuma Masası');
  await tester.enterText(find.byKey(const ValueKey('player-0')), 'xyz');
  await tester.enterText(find.byKey(const ValueKey('player-1')), 'abc');
  await tester.tap(find.text('Başla'));
  await tester.pumpAndSettle();
}

void main() {
  setUpAll(() => initializeDateFormatting('tr'));

  testWidgets('101 oyunu kurulur, ceza ve el eklenir, oyun bitirilir', (tester) async {
    final container = await pumpApp(tester);
    expect(find.text('Masa Hazır!'), findsOneWidget);

    await createYuzBirGame(tester);
    expect(find.text('Cuma Masası'), findsOneWidget);

    Game game() => container.read(gamesProvider).value!.single;
    final [xyz, abc] = game().players;
    expect(game().settings.customizedRuleKeys, {'penalty'});

    // Ceza: abc'ye 150.
    await tester.tap(find.byKey(const ValueKey('rule-chip-penalty')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(ValueKey('penalty-${abc.id}')));
    await tester.pumpAndSettle();
    expect(find.byKey(ValueKey('total-${abc.id}')), findsOneWidget);
    expect(find.descendant(of: find.byKey(ValueKey('total-${abc.id}')), matching: find.text('150')), findsOneWidget);

    // Normal el.
    await tester.tap(find.byKey(const ValueKey('add-score')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('save-score')));
    await tester.pumpAndSettle();
    expect(find.text('Gerekli'), findsNWidgets(2));
    await tester.enterText(find.byKey(ValueKey('score-${xyz.id}')), '40');
    await tester.enterText(find.byKey(ValueKey('score-${abc.id}')), '12');
    await tester.tap(find.byKey(const ValueKey('save-score')));
    await tester.pumpAndSettle();

    // Elden bitme: xyz bitirir, abc açmadı.
    await tester.scrollUntilVisible(
      find.byKey(const ValueKey('rule-chip-handFinish')),
      200,
      scrollable: find.descendant(of: find.byKey(const ValueKey('rule-bar')), matching: find.byType(Scrollable)),
    );
    await tester.tap(find.byKey(const ValueKey('rule-chip-handFinish')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(ValueKey('finisher-${xyz.id}')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Açmadı: 404'));
    await tester.tap(find.byKey(const ValueKey('save-score')));
    await tester.pumpAndSettle();

    expect(game().rounds.map((r) => r.kind), [RoundKind.penalty, RoundKind.hand, RoundKind.finish]);
    expect(game().rounds.last.scores, {xyz.id: -202, abc.id: 404});
    expect(game().rounds.last.sourceOf(abc.id), ScoreSource.rule);
    expect(game().handCount, 2);

    // Oyunu bitir: 101'de en düşük puan (xyz: -162) kazanır.
    await tester.tap(find.byType(PopupMenuButton<String>));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Oyunu bitir'));
    await tester.pumpAndSettle();
    expect(find.text('Kazanan: xyz'), findsOneWidget);
    await tester.tap(find.text('Bitir'));
    await tester.pumpAndSettle();

    expect(find.text('Oyun Sonuçları'), findsOneWidget);
    expect(find.text('1. xyz'), findsOneWidget);
    expect(game().winnerPlayerIds, [xyz.id]);
    expect(game().events.last.type, GameEventType.gameFinished);
  });

  testWidgets('satır uzun basılarak silinir', (tester) async {
    final container = await pumpApp(tester);
    await createYuzBirGame(tester);
    final abc = container.read(gamesProvider).value!.single.players[1];

    await tester.tap(find.byKey(const ValueKey('rule-chip-penalty')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(ValueKey('penalty-${abc.id}')));
    await tester.pumpAndSettle();

    final round = container.read(gamesProvider).value!.single.rounds.single;
    await tester.longPress(find.byKey(ValueKey('round-${round.id}')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Satırı sil'));
    await tester.pumpAndSettle();
    expect(container.read(gamesProvider).value!.single.rounds, isEmpty);
  });

  testWidgets('ıstakadan hesaplanan puan skor girişine aktarılır', (tester) async {
    final container = await pumpApp(tester);
    await createYuzBirGame(tester);
    final xyz = container.read(gamesProvider).value!.single.players[0];

    await tester.tap(find.byKey(const ValueKey('add-score')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(ValueKey('rack-${xyz.id}')));
    await tester.pumpAndSettle();
    expect(find.text('xyz · Istaka'), findsOneWidget);

    Future<void> addTile(TileColor color, int number) async {
      await tester.ensureVisible(find.byKey(ValueKey('pad-color-${color.name}')));
      await tester.tap(find.byKey(ValueKey('pad-color-${color.name}')));
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(ValueKey('pad-number-$number')));
      await tester.pumpAndSettle();
    }

    await addTile(TileColor.red, 12);
    await addTile(TileColor.blue, 6);
    expect(tester.widget<Text>(find.byKey(const ValueKey('rack-total'))).data, '18');

    // Gösterge mavi 5 -> mavi 6 okeydir ve 150 ceza yazılır (ayardaki ceza puanı).
    await tester.ensureVisible(find.byKey(const ValueKey('indicator-tile')));
    await tester.tap(find.byKey(const ValueKey('indicator-tile')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('tile-color-blue')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('tile-number-5')));
    await tester.pumpAndSettle();
    expect(find.text('Okey: Mavi 6'), findsOneWidget);
    expect(tester.widget<Text>(find.byKey(const ValueKey('rack-total'))).data, '162');

    await tester.tap(find.byKey(const ValueKey('rack-use')));
    await tester.pumpAndSettle();
    expect(find.widgetWithText(TextFormField, '162'), findsOneWidget);
  });

  testWidgets('hızlı oyun geniş ekranda yan panelle açılır', (tester) async {
    final container = await pumpApp(tester, size: const Size(1280, 800));
    await tester.tap(find.text('Hızlı Oyun'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('3'));
    await tester.pumpAndSettle();

    final game = container.read(gamesProvider).value!.single;
    expect(game.isQuickGame, isTrue);
    expect(game.players.length, 3);
    expect(find.text('Sıralama'), findsOneWidget);
  });
}
