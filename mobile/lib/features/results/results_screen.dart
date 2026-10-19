import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/responsive.dart';
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
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [scheme.primary.withValues(alpha: 0.15), scheme.surface],
          ),
        ),
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
        Text(game.title, style: theme.textTheme.titleLarge?.copyWith(color: theme.colorScheme.primary)),
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
    final scheme = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final (bg, fg) = switch (place) {
      0 => (scheme.primaryContainer, scheme.onPrimaryContainer),
      1 => (scheme.secondaryContainer, scheme.onSecondaryContainer),
      _ => (scheme.tertiaryContainer, scheme.onTertiaryContainer),
    };
    final gap = Standings.gapToLeader(game, leader, standing);
    final subtitle = standing.rank == 1 ? (place == 0 ? 'Birincilik senin!' : 'Liderle aynı puan') : '$gap puan geride';
    return Card(
      key: ValueKey('podium-$place'),
      elevation: place == 0 ? 6 : 3,
      color: bg,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final icon = Icon(standing.rank == 1 ? Icons.emoji_events : Icons.military_tech, color: fg, size: 40);
            final details = Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${standing.rank}. ${standing.player.name}',
                  style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: fg),
                  overflow: TextOverflow.ellipsis,
                ),
                Text(subtitle, style: text.bodyMedium?.copyWith(color: fg.withValues(alpha: 0.85))),
                const SizedBox(height: 4),
                Text(
                  '${standing.total} puan',
                  style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: fg),
                ),
              ],
            );
            // Dar kartlarda (telefonda 2. ve 3. sıra) ikon üste alınır.
            if (constraints.maxWidth < 240) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [icon, const SizedBox(height: 8), details],
              );
            }
            return Row(
              children: [
                icon,
                const SizedBox(width: 16),
                Expanded(child: details),
              ],
            );
          },
        ),
      ),
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
      color: theme.colorScheme.surfaceContainerHighest,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Özet', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
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
                    Text(value, style: const TextStyle(fontWeight: FontWeight.w600)),
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
            child: Text('Diğer Oyuncular', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          ),
          for (final s in standings.skip(3))
            ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 20),
              title: Text('${s.rank}. ${s.player.name}'),
              subtitle: Text('${Standings.gapToLeader(game, standings.first, s)} puan geride'),
              trailing: Text(
                '${s.total}',
                style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.primary),
              ),
            ),
        ],
      ),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty();

  @override
  Widget build(BuildContext context) => const Center(
    child: Padding(
      padding: EdgeInsets.all(32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('Henüz skor girilmemiş', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
          SizedBox(height: 8),
          Text('Oyunu tamamlamak için skor ekleyin.', textAlign: TextAlign.center),
        ],
      ),
    ),
  );
}
