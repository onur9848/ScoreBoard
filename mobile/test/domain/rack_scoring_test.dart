import 'package:flutter_test/flutter_test.dart';
import 'package:puan_tablosu/domain/tiles/rack_scoring.dart';
import 'package:puan_tablosu/domain/tiles/tile.dart';

Tile t(TileColor color, int number) => Tile(id: '$color$number', color: color, number: number);

void main() {
  const red = TileColor.red;
  const blue = TileColor.blue;
  const black = TileColor.black;

  test('göstergenin bir üstü okeydir, 13ten sonra 1 gelir', () {
    expect(okeyFromIndicator(t(red, 5)), (color: red, number: 6));
    expect(okeyFromIndicator(t(blue, 13)), (color: blue, number: 1));
  });

  test('açılmış elde taşlar toplanır', () {
    final score = RackScoring.score([t(red, 3), t(blue, 12), t(black, 7)], const RackScoreOptions());
    expect(score.total, 22);
    expect(score.okeyCount, 0);
  });

  test('elde kalan okey ceza olarak yazılır, sahte okey okeyin değerini alır', () {
    final indicator = t(red, 5);
    final tiles = [t(red, 6), const Tile.fakeOkey(id: 'f'), t(black, 2)];
    final penalty = RackScoring.score(tiles, RackScoreOptions(indicator: indicator, okeyPenalty: 101));
    expect(penalty.okeyCount, 1);
    expect(penalty.tileSum, 6 + 2);
    expect(penalty.total, 101 + 8);

    final face = RackScoring.score(tiles, RackScoreOptions(indicator: indicator, okeyInHand: OkeyInHandRule.faceValue));
    expect(face.total, 6 + 6 + 2);
  });

  test('gösterge yoksa sahte okey 0 sayılır', () {
    final score = RackScoring.score([const Tile.fakeOkey(id: 'f'), t(red, 9)], const RackScoreOptions());
    expect(score.total, 9);
  });

  test('açmadıysa ceza, çift gittiyse iki katı yazılır', () {
    final tiles = [t(red, 13), t(red, 12)];
    expect(RackScoring.score(tiles, const RackScoreOptions(opened: false, noOpenPenalty: 202)).total, 202);
    expect(RackScoring.score(tiles, const RackScoreOptions(opened: false, doubled: true)).total, 404);
    expect(RackScoring.score(tiles, const RackScoreOptions(doubled: true)).total, 50);
  });
}
