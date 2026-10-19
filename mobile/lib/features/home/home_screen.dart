import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game_type.dart';
import '../../state/providers.dart';
import 'quick_game_names.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final scheme = Theme.of(context).colorScheme;
    final lastGame = ref.watch(lastActiveGameProvider);
    final analytics = ref.read(analyticsProvider);
    final wide = !WindowSize.of(context).isCompact;

    final actions = [
      _ActionCard(
        title: 'Hızlı Oyun',
        subtitle: 'Rastgele masa',
        icon: Icons.flash_on,
        background: scheme.secondaryContainer,
        iconBackground: scheme.secondary,
        iconColor: scheme.onSecondary,
        onTap: () {
          analytics.event('quick_game_tap');
          _showQuickGameDialog(context, ref);
        },
      ),
      _ActionCard(
        title: 'Yeni Oyun',
        subtitle: 'Kuralları belirle',
        icon: Icons.add,
        background: scheme.primaryContainer,
        iconBackground: scheme.primary,
        iconColor: scheme.onPrimary,
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
                  const SizedBox(height: 24),
                  if (lastGame != null) ...[
                    _ContinueCard(
                      title: lastGame.title,
                      subtitle: '${lastGame.type.label} · ${lastGame.handCount}. el',
                      onTap: () {
                        analytics.event('continue_last_game_tap');
                        context.pushNamed('board', pathParameters: {'id': lastGame.id});
                      },
                    ),
                    const SizedBox(height: 24),
                  ],
                  Row(
                    children: [
                      Expanded(child: actions[0]),
                      const SizedBox(width: 16),
                      Expanded(child: actions[1]),
                    ],
                  ),
                  const SizedBox(height: 24),
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
                    const SizedBox(height: 24),
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
    title: 'Geçmiş Oyunlar',
    subtitle: 'Kayıtlı masaları aç',
    onTap: () {
      track('latest_games_tap');
      context.pushNamed('history');
    },
  );

  Widget _rackCard(BuildContext context, void Function(String) track) => _WideCard(
    icon: Icons.calculate,
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
            for (final n in const [2, 3, 4, 5])
              SizedBox.square(
                dimension: 56,
                child: FilledButton.tonal(
                  style: FilledButton.styleFrom(padding: EdgeInsets.zero, minimumSize: const Size.square(56)),
                  onPressed: () => Navigator.pop(context, n),
                  child: Text('$n', style: Theme.of(context).textTheme.titleLarge),
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
        Image.asset('assets/images/game.png', height: 120, semanticLabel: 'Puan Tablosu'),
        const SizedBox(height: 16),
        Text('Masa Hazır!', style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold)),
        Text(
          'Bugün şansın bol olsun',
          style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant),
        ),
      ],
    );
  }
}

class _ContinueCard extends StatelessWidget {
  const _ContinueCard({required this.title, required this.subtitle, required this.onTap});

  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Card(
      elevation: 8,
      shape: RoundedCornerShape.card,
      color: scheme.tertiaryContainer,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 22),
          child: Row(
            children: [
              _IconTile(icon: Icons.play_arrow, background: scheme.tertiary, color: scheme.onTertiary, size: 56),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Kaldığın Yerden Devam Et',
                      style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: scheme.onTertiaryContainer),
                    ),
                    Text(
                      '$title · $subtitle',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: text.bodyMedium?.copyWith(color: scheme.onTertiaryContainer.withValues(alpha: 0.8)),
                    ),
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

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.background,
    required this.iconBackground,
    required this.iconColor,
    required this.onTap,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final Color background;
  final Color iconBackground;
  final Color iconColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final foreground = Theme.of(context).colorScheme.onSurface;
    return Card(
      elevation: 6,
      shape: RoundedCornerShape.card,
      color: background,
      child: InkWell(
        onTap: onTap,
        child: ConstrainedBox(
          constraints: const BoxConstraints(minHeight: 130),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _IconTile(icon: icon, background: iconBackground, color: iconColor, size: 40),
                const SizedBox(height: 18),
                Text(
                  title,
                  style: text.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: foreground),
                ),
                Text(subtitle, style: text.bodySmall?.copyWith(color: foreground.withValues(alpha: 0.7))),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _WideCard extends StatelessWidget {
  const _WideCard({required this.icon, required this.title, required this.subtitle, required this.onTap});

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Card(
      elevation: 4,
      shape: RoundedCornerShape.card,
      color: scheme.surfaceContainerHighest,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 18),
          child: Row(
            children: [
              Icon(icon, color: scheme.primary),
              const SizedBox(width: 12),
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

class _IconTile extends StatelessWidget {
  const _IconTile({required this.icon, required this.background, required this.color, required this.size});

  final IconData icon;
  final Color background;
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) => Container(
    width: size,
    height: size,
    decoration: BoxDecoration(color: background, borderRadius: BorderRadius.circular(size * 0.3)),
    child: Icon(icon, color: color, size: size * 0.55),
  );
}
