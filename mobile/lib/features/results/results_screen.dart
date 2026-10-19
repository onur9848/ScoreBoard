import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game.dart';
import '../../domain/scoring/standings.dart';
import '../../state/providers.dart';

class ResultsScreen extends ConsumerWidget {
  const ResultsScreen({super.key, required this.gameId});

  final String gameId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final game = ref.watch(gameProvider(gameId));
    final scheme = Theme.of(context).colorScheme;
    if (game == null) return const Scaffold(body: Center(child: Text('Oyun bulunamadı.')));

    final standings = Standings.compute(game);
    final hasScores = game.rounds.isNotEmpty;

    return Scaffold(
      appBar: AppBar(title: const Text('Oyun Sonuçları')),
      body: DecoratedBox(
        decoration: BoxDecoration(color: scheme.surface),
        child: SafeArea(
          child: !hasScores
              ? const _Empty()
              : ListView(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  children: [
                    ResponsiveCenter(
                      padding: const EdgeInsets.symmetric(horizontal: 24),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          _Header(game: game),
                          const SizedBox(height: 24),
                          _Podium(game: game, standings: standings),
                          const SizedBox(height: 24),
                          _Summary(game: game, standings: standings),
                          if (standings.length > 3) ...[
                            const SizedBox(height: 24),
                            _Remaining(game: game, standings: standings),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.game});

  final Game game;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(game.title, style: chalkStyle(36, color: theme.colorScheme.onSurface, height: 1.1)),
        const SizedBox(height: 4),
        Text(
          'Toplam ${game.handCount} el ${game.isFinished ? 'oynandı · Oyun bitti' : 'tamamlandı · Devam ediyor'}',
          style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
        ),
        Text(game.settings.winCondition.label, style: theme.textTheme.bodySmall),
      ],
    );
  }
}

class _Podium extends StatelessWidget {
  const _Podium({required this.game, required this.standings});

  final Game game;
  final List<Standing> standings;

  @override
  Widget build(BuildContext context) {
    final cards = [
      for (final (i, s) in standings.take(3).indexed)
        _PodiumCard(game: game, standing: s, leader: standings.first, place: i),
    ];
    final wide = !WindowSize.of(context).isCompact;
    if (cards.length == 1 || !wide && cards.length > 1) {
      return Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          cards.first,
          if (cards.length > 1) ...[
            const SizedBox(height: 16),
            Row(
              children: [
                for (final (i, c) in cards.skip(1).indexed) ...[
                  if (i > 0) const SizedBox(width: 16),
                  Expanded(child: c),
                ],
              ],
            ),
          ],
        ],
      );
    }
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        for (final (i, c) in cards.indexed) ...[if (i > 0) const SizedBox(width: 16), Expanded(child: c)],
      ],
    );
  }
}

class _PodiumCard extends StatelessWidget {
  const _PodiumCard({required this.game, required this.standing, required this.leader, required this.place});

  final Game game;
  final Standing standing;
  final Standing leader;
  final int place;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;
    final gap = Standings.gapToLeader(game, leader, standing);
    final subtitle = standing.rank == 1 ? (place == 0 ? 'Birincilik senin!' : 'Liderle aynı puan') : '$gap puan geride';
    // 1. tahta, 2. ahşap, 3. kâğıt.
    final (fg, dim, accent) = switch (place) {
      0 => (Palette.chalk, Palette.chalkDim, Palette.chalkYellow),
      1 => (Colors.white, const Color(0xFFFFE6CC), Colors.white),
      _ => (scheme.onSurface, scheme.onSurfaceVariant, scheme.secondary),
    };
    final badgeColor = switch (place) {
      0 => Palette.tileRed,
      1 => Palette.tileBlue,
      _ => Palette.tileBlack,
    };
    final content = LayoutBuilder(
      builder: (context, constraints) {
        final badge = place == 0
            ? const Icon(Icons.emoji_events, color: Palette.chalkYellow, size: 52)
            : TileBadge(label: '${standing.rank}', color: badgeColor, size: 34);
        final details = Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              '${standing.rank}. ${standing.player.name}',
              style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: fg),
              overflow: TextOverflow.ellipsis,
            ),
            Text(subtitle, style: text.bodyMedium?.copyWith(color: dim)),
            const SizedBox(height: 2),
            Text('${standing.total} puan', style: chalkStyle(place == 0 ? 38 : 30, color: accent, height: 1.1)),
          ],
        );
        // Dar kartlarda (telefonda 2. ve 3. sıra) rozet üste alınır.
        if (constraints.maxWidth < 240) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [badge, const SizedBox(height: 8), details],
          );
        }
        return Row(
          children: [
            badge,
            const SizedBox(width: 16),
            Expanded(child: details),
          ],
        );
      },
    );
    const padding = EdgeInsets.all(18);
    return KeyedSubtree(
      key: ValueKey('podium-$place'),
      child: switch (place) {
        0 => Chalkboard(padding: const EdgeInsets.all(20), child: content),
        1 => WoodCard(padding: padding, child: content),
        _ => Card(
          child: Padding(padding: padding, child: content),
        ),
      },
    );
  }
}

class _Summary extends StatelessWidget {
  const _Summary({required this.game, required this.standings});

  final Game game;
  final List<Standing> standings;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final leaderGap = standings.length > 1 ? Standings.gapToLeader(game, standings[0], standings[1]) : null;
    final total = standings.fold<int>(0, (s, e) => s + e.total);
    final perHand = game.handCount == 0 ? 0 : total / game.handCount;
    final penalties = game.rounds.where((r) => r.kind == RoundKind.penalty).length;
    final rows = <(String, String)>[
      ('El Sayısı', '${game.handCount}'),
      ('Lider Avantajı', leaderGap == null ? '-' : (leaderGap == 0 ? 'Berabere' : '+$leaderGap')),
      ('Toplam Puan', '$total'),
      ('Ortalama (El Başına)', perHand.toStringAsFixed(1)),
      if (penalties > 0) ('Ceza Sayısı', '$penalties'),
    ];
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Özet', style: chalkStyle(30, color: theme.colorScheme.onSurface, height: 1.1)),
            const SizedBox(height: 8),
            for (final (label, value) in rows)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(label, style: TextStyle(color: theme.colorScheme.onSurfaceVariant)),
                    ),
                    Text(value, style: const TextStyle(fontWeight: FontWeight.w700)),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _Remaining extends StatelessWidget {
  const _Remaining({required this.game, required this.standings});

  final Game game;
  final List<Standing> standings;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 4),
            child: Text('Diğer Oyuncular', style: chalkStyle(28, color: theme.colorScheme.onSurface, height: 1.1)),
          ),
          for (final s in standings.skip(3))
            ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 20),
              title: Text('${s.rank}. ${s.player.name}'),
              subtitle: Text('${Standings.gapToLeader(game, standings.first, s)} puan geride'),
              trailing: Text('${s.total}', style: chalkStyle(28, color: theme.colorScheme.secondary, height: 1)),
            ),
        ],
      ),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty();

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Henüz skor girilmemiş',
            style: chalkStyle(32, color: Theme.of(context).colorScheme.onSurface, height: 1.1),
          ),
          const SizedBox(height: 8),
          const Text('Oyunu tamamlamak için skor ekleyin.', textAlign: TextAlign.center),
        ],
      ),
    ),
  );
}
