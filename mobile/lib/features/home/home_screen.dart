import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game.dart';
import '../../domain/models/game_type.dart';
import '../../domain/scoring/standings.dart';
import '../../state/providers.dart';
import 'quick_game_names.dart';

const _tileColors = [Palette.tileRed, Palette.tileBlue, Palette.tileYellow, Palette.tileBlack];

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final lastGame = ref.watch(lastActiveGameProvider);
    final analytics = ref.read(analyticsProvider);
    final wide = !WindowSize.of(context).isCompact;

    final actions = [
      _ActionCard(
        title: 'Hızlı Oyun',
        subtitle: 'Rastgele masa',
        icon: Icons.bolt,
        iconColor: Palette.tileRed,
        wood: true,
        onTap: () {
          analytics.event('quick_game_tap');
          _showQuickGameDialog(context, ref);
        },
      ),
      _ActionCard(
        title: 'Yeni Oyun',
        subtitle: 'Kuralları belirle',
        icon: Icons.add,
        iconColor: Palette.tileBlue,
        wood: false,
        onTap: () {
          analytics.event('new_game_tap');
          context.pushNamed('setup');
        },
      ),
    ];

    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(vertical: 16),
          children: [
            ResponsiveCenter(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const _Header(),
                  const SizedBox(height: 28),
                  if (lastGame != null) ...[
                    _ContinueCard(
                      game: lastGame,
                      onTap: () {
                        analytics.event('continue_last_game_tap');
                        context.pushNamed('board', pathParameters: {'id': lastGame.id});
                      },
                    ),
                    const SizedBox(height: 20),
                  ],
                  Row(
                    children: [
                      Expanded(child: actions[0]),
                      const SizedBox(width: 16),
                      Expanded(child: actions[1]),
                    ],
                  ),
                  const SizedBox(height: 20),
                  if (wide)
                    Row(
                      children: [
                        Expanded(child: _historyCard(context, analytics.event)),
                        const SizedBox(width: 16),
                        Expanded(child: _rackCard(context, analytics.event)),
                      ],
                    )
                  else ...[
                    _historyCard(context, analytics.event),
                    const SizedBox(height: 12),
                    _rackCard(context, analytics.event),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _historyCard(BuildContext context, void Function(String) track) => _WideCard(
    icon: Icons.history,
    iconColor: Palette.tileBlack,
    title: 'Geçmiş Oyunlar',
    subtitle: 'Kayıtlı masaları aç',
    onTap: () {
      track('latest_games_tap');
      context.pushNamed('history');
    },
  );

  Widget _rackCard(BuildContext context, void Function(String) track) => _WideCard(
    icon: Icons.calculate,
    iconColor: Palette.tileYellow,
    title: 'Puan Hesapla',
    subtitle: 'Istakayı okut, eli say',
    onTap: () {
      track('rack_calculator_tap');
      context.pushNamed('rack');
    },
  );

  Future<void> _showQuickGameDialog(BuildContext context, WidgetRef ref) async {
    final count = await showDialog<int>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Kaç Kişi Oynayacaksınız?', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            for (final (i, n) in const [2, 3, 4, 5].indexed)
              Semantics(
                button: true,
                label: '$n kişi',
                child: InkWell(
                  borderRadius: BorderRadius.circular(10),
                  onTap: () => Navigator.pop(context, n),
                  child: TileBadge(label: '$n', size: 48, color: _tileColors[i % _tileColors.length]),
                ),
              ),
          ],
        ),
        actions: [TextButton(onPressed: () => Navigator.pop(context), child: const Text('İptal Et'))],
      ),
    );
    if (count == null || !context.mounted) return;

    final random = Random();
    final game = ref
        .read(gameEngineProvider)
        .createGame(
          title: QuickGameNames.titles[random.nextInt(QuickGameNames.titles.length)],
          type: GameType.generic,
          playerNames: ([...QuickGameNames.players]..shuffle(random)).take(count).toList(),
          isQuickGame: true,
        );
    await ref.read(gamesProvider.notifier).save(game);
    ref.read(analyticsProvider).event('quick_game_created', {'player_count': count});
    if (context.mounted) unawaited(context.pushNamed('board', pathParameters: {'id': game.id}));
  }
}

class _Header extends StatelessWidget {
  const _Header();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(26),
          child: Image.asset('assets/images/logo.png', height: 112, semanticLabel: 'Puan Tablosu'),
        ),
        const SizedBox(height: 12),
        Text('Masa Hazır!', style: chalkStyle(44, color: theme.colorScheme.onSurface, height: 1.1)),
        Text(
          'Bugün şansın bol olsun',
          style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant),
        ),
      ],
    );
  }
}

/// Yarım kalan oyun, tahtada küçük bir puan özetiyle.
class _ContinueCard extends StatelessWidget {
  const _ContinueCard({required this.game, required this.onTap});

  final Game game;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final standings = Standings.compute(game);
    final leader = game.rounds.isEmpty || standings.isEmpty ? null : standings.first;
    return Chalkboard(
      onTap: onTap,
      padding: const EdgeInsets.fromLTRB(18, 16, 18, 16),
      child: Row(
        children: [
          const TileBadge(icon: Icons.play_arrow_rounded, size: 48),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                FittedBox(
                  fit: BoxFit.scaleDown,
                  alignment: Alignment.centerLeft,
                  child: Text('Kaldığın yerden devam et', style: chalkStyle(26, height: 1.1)),
                ),
                const SizedBox(height: 2),
                Text(
                  '${game.title} · ${game.type.label} · ${game.handCount}. el',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: text.bodyMedium?.copyWith(color: Palette.chalkDim),
                ),
                if (leader != null) ...[
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.emoji_events, size: 16, color: Palette.chalkYellow),
                      const SizedBox(width: 6),
                      Flexible(
                        child: Text(
                          '${leader.player.name} önde · ${leader.total}',
                          overflow: TextOverflow.ellipsis,
                          style: chalkStyle(20, color: Palette.chalkYellow, height: 1),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: Palette.chalkDim),
        ],
      ),
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.iconColor,
    required this.wood,
    required this.onTap,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final Color iconColor;

  /// Ahşap ya da tahta yeşili zemin.
  final bool wood;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final content = ConstrainedBox(
      constraints: const BoxConstraints(minHeight: 116),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TileBadge(icon: icon, color: iconColor, size: 38),
          const SizedBox(height: 14),
          Text(title, style: chalkStyle(28, height: 1)),
          const SizedBox(height: 2),
          Text(subtitle, style: text.bodySmall?.copyWith(color: wood ? const Color(0xFFFFE6CC) : Palette.chalkDim)),
        ],
      ),
    );
    return wood
        ? WoodCard(onTap: onTap, child: content)
        : Chalkboard(onTap: onTap, frame: 5, padding: const EdgeInsets.all(11), child: content);
  }
}

class _WideCard extends StatelessWidget {
  const _WideCard({
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final Color iconColor;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Card(
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Row(
            children: [
              TileBadge(icon: icon, color: iconColor, size: 34),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                    Text(subtitle, style: text.bodySmall?.copyWith(color: scheme.onSurfaceVariant)),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: scheme.onSurfaceVariant),
            ],
          ),
        ),
      ),
    );
  }
}
