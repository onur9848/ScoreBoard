import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game.dart';
import '../../domain/models/game_type.dart';
import '../../state/providers.dart';

class HistoryScreen extends ConsumerStatefulWidget {
  const HistoryScreen({super.key});

  @override
  ConsumerState<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends ConsumerState<HistoryScreen> {
  GameType? _filter;

  @override
  Widget build(BuildContext context) {
    final games = ref.watch(gamesProvider);
    final columns = switch (WindowSize.of(context)) {
      WindowSize.compact => 1,
      WindowSize.medium => 2,
      WindowSize.expanded => 3,
    };

    return Scaffold(
      appBar: AppBar(title: const Text('Eski Oyunlar')),
      body: SafeArea(
        child: Column(
          children: [
            SizedBox(
              height: 52,
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                children: [_chip('Tümü', null), for (final t in GameType.values) _chip(t.label, t)],
              ),
            ),
            const Divider(height: 1),
            Expanded(
              child: games.when(
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (e, _) => Center(child: Text('Oyunlar yüklenemedi: $e')),
                data: (all) {
                  final filtered = _filter == null ? all : all.where((g) => g.type == _filter).toList();
                  if (filtered.isEmpty) return const _EmptyHistory();
                  return GridView.builder(
                    padding: const EdgeInsets.all(16),
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: columns,
                      mainAxisSpacing: 16,
                      crossAxisSpacing: 16,
                      mainAxisExtent: 168,
                    ),
                    itemCount: filtered.length,
                    itemBuilder: (_, i) => _GameCard(
                      game: filtered[i],
                      onOpen: () => context.pushNamed('board', pathParameters: {'id': filtered[i].id}),
                      onDelete: () => _confirmDelete(filtered[i]),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _chip(String label, GameType? type) => Padding(
    padding: const EdgeInsets.only(right: 8),
    child: FilterChip(label: Text(label), selected: _filter == type, onSelected: (_) => setState(() => _filter = type)),
  );

  Future<void> _confirmDelete(Game game) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Oyunu Sil'),
        content: Text('"${game.title}" silinsin mi? Bu işlem geri alınamaz.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('İptal')),
          TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('Sil')),
        ],
      ),
    );
    if (ok == true) await ref.read(gamesProvider.notifier).delete(game.id);
  }
}

class _GameCard extends StatelessWidget {
  const _GameCard({required this.game, required this.onOpen, required this.onDelete});

  final Game game;
  final VoidCallback onOpen;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final scheme = theme.colorScheme;
    final date = DateFormat('d MMM yyyy, HH:mm', 'tr').format(game.updatedAt.toLocal());
    final (badge, badgeColor) = switch (game.type) {
      GameType.okey => ('O', Palette.tileBlue),
      GameType.yuzBirOkey => ('101', Palette.tileRed),
      GameType.generic => ('#', Palette.tileBlack),
    };
    return Card(
      child: InkWell(
        onTap: onOpen,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(18, 14, 8, 14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  TileBadge(label: badge, color: badgeColor, size: badge.length > 2 ? 36 : 32),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          game.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                        ),
                        Text(
                          '${game.type.label} · $date',
                          style: theme.textTheme.bodySmall?.copyWith(color: scheme.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    tooltip: 'Oyunu Sil',
                    color: scheme.error,
                    onPressed: onDelete,
                    icon: const Icon(Icons.delete_outline),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  _Badge('${game.players.length} oyuncu', scheme.primary),
                  _Badge('${game.handCount} el', scheme.secondary),
                  _Badge(
                    game.isFinished ? 'Bitti' : 'Devam ediyor',
                    game.isFinished ? scheme.outline : scheme.tertiary,
                  ),
                ],
              ),
              const Spacer(),
              Text(
                game.players.map((p) => p.name).join(' · '),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.bodySmall,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  const _Badge(this.label, this.color);

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
    decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(8)),
    child: Text(
      label,
      style: TextStyle(color: color, fontWeight: FontWeight.w500, fontSize: 13),
    ),
  );
}

class _EmptyHistory extends StatelessWidget {
  const _EmptyHistory();

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const TileBadge(icon: Icons.history, color: Palette.tileBlack, size: 56),
            const SizedBox(height: 16),
            Text('Henüz oyun bulunmuyor', style: chalkStyle(32, color: scheme.onSurface, height: 1.1)),
            const SizedBox(height: 8),
            Text(
              "İlk oyununuzu oluşturmak için 'Yeni Oyun' butonuna tıklayın",
              textAlign: TextAlign.center,
              style: TextStyle(color: scheme.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}
