import '../models/game.dart';
import '../models/game_type.dart';

class Standing {
  const Standing({required this.player, required this.total, required this.rank});

  final Player player;
  final int total;

  /// 1'den başlar; eşit puanlı oyuncular aynı sırayı paylaşır.
  final int rank;
}

abstract final class Standings {
  static Map<String, int> totals(Game game) => {
    for (final p in game.players) p.id: game.rounds.fold<int>(0, (sum, r) => sum + r.scoreOf(p.id)),
  };

  /// Kazanma koşuluna göre sıralanmış liste (lider ilk sırada).
  static List<Standing> compute(Game game) {
    final totals = Standings.totals(game);
    final lowerWins = game.settings.winCondition == WinCondition.lowestTotal;
    final players = [...game.players]
      ..sort((a, b) {
        final cmp = lowerWins ? totals[a.id]!.compareTo(totals[b.id]!) : totals[b.id]!.compareTo(totals[a.id]!);
        return cmp != 0 ? cmp : a.seat.compareTo(b.seat);
      });

    final result = <Standing>[];
    for (var i = 0; i < players.length; i++) {
      final total = totals[players[i].id]!;
      final rank = i > 0 && result[i - 1].total == total ? result[i - 1].rank : i + 1;
      result.add(Standing(player: players[i], total: total, rank: rank));
    }
    return result;
  }

  static List<String> leaders(Game game) {
    final standings = compute(game);
    if (standings.isEmpty || game.rounds.isEmpty) return const [];
    return [
      for (final s in standings)
        if (s.rank == 1) s.player.id,
    ];
  }

  /// Liderle arasındaki fark; pozitif değer "geride" anlamına gelir.
  static int gapToLeader(Game game, Standing leader, Standing other) =>
      game.settings.winCondition == WinCondition.lowestTotal ? other.total - leader.total : leader.total - other.total;
}
