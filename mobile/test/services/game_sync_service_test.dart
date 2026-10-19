import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:puan_tablosu/data/game_repository.dart';
import 'package:puan_tablosu/domain/game_engine.dart';
import 'package:puan_tablosu/domain/models/game.dart';
import 'package:puan_tablosu/domain/models/game_type.dart';
import 'package:puan_tablosu/services/game_sync_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  late SharedPreferences prefs;
  late SharedPrefsGameRepository repo;
  late List<http.Request> requests;
  late int Function(http.Request) respond;
  final engine = GameEngine();

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    prefs = await SharedPreferences.getInstance();
    repo = SharedPrefsGameRepository(prefs);
    requests = [];
    respond = (_) => 201;
  });

  GameSyncService service({SyncConfig? config}) => GameSyncService(
    prefs: prefs,
    config: config ?? SyncConfig(baseUrl: Uri.parse('https://api.example.com/'), apiKey: 'ingest-key'),
    lookup: repo.load,
    platform: 'android',
    appVersion: () async => '2.0.0+9',
    retryDelays: const [],
    client: MockClient((request) async {
      requests.add(request);
      return http.Response('{}', respond(request));
    }),
  );

  Future<Game> saved(String title) async {
    final game = engine.createGame(title: title, type: GameType.yuzBirOkey, playerNames: ['xyz', 'abc']);
    await repo.save(game);
    return game;
  }

  test('oyunu anahtarlar ve cihaz bilgisiyle gönderir', () async {
    final sync = service();
    final game = await saved('Masa');
    await sync.enqueue(game.id);

    final request = requests.single;
    expect(request.method, 'PUT');
    expect(request.url.toString(), 'https://api.example.com/api/v1/sessions/${game.id}');
    expect(request.headers['X-Api-Key'], 'ingest-key');
    expect(request.headers['X-Device-Id'], sync.deviceId);
    expect(request.headers['X-Platform'], 'android');
    expect(request.headers['X-App-Version'], '2.0.0+9');
    expect(jsonDecode(request.body), game.toJson());
    expect(sync.pending, isEmpty);
    // Cihaz kimliği kalıcıdır.
    expect(service().deviceId, sync.deviceId);
  });

  test('ağ hatasında kuyrukta kalır, sonra gönderilir', () async {
    final sync = service();
    final game = await saved('Masa');
    respond = (_) => 503;
    await sync.enqueue(game.id);
    expect(sync.pending, [game.id]);

    respond = (_) => 200;
    await sync.flush();
    expect(sync.pending, isEmpty);
    expect(requests.length, 2);
  });

  test('reddedilen oyun tekrar denenmez, sıradaki gönderilir', () async {
    final sync = service();
    final bad = await saved('Kötü');
    final good = await saved('İyi');
    respond = (r) => r.url.path.endsWith(bad.id) ? 400 : 201;
    await prefs.setStringList('sync.pending', [bad.id, good.id]);
    await sync.flush();
    expect(sync.pending, isEmpty);
    expect(requests.map((r) => r.url.pathSegments.last), [bad.id, good.id]);
  });

  test('açılışta yalnızca değişen oyunlar gönderilir', () async {
    final sync = service();
    var game = await saved('Masa');
    final other = await saved('Diğer');
    await sync.enqueueChanged([game, other]);
    expect(requests.length, 2);

    game = engine.addHand(game, {for (final p in game.players) p.id: 5});
    await repo.save(game);
    await sync.enqueueChanged([game, other]);
    expect(requests.length, 3);
    expect(requests.last.url.pathSegments.last, game.id);
  });

  test('silinen oyun kuyruktan çıkar; ayar yoksa hiçbir şey gönderilmez', () async {
    final game = await saved('Masa');
    respond = (_) => 503;
    final sync = service();
    await sync.enqueue(game.id);
    await sync.forget(game.id);
    expect(sync.pending, isEmpty);

    final disabled = service(config: const SyncConfig());
    await disabled.enqueue(game.id);
    expect(requests.length, 1);
  });
}
