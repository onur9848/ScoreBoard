import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game.dart';
import '../../domain/rules/rule_catalog.dart';
import '../../domain/scoring/standings.dart';
import '../../state/providers.dart';
import 'score_entry_sheet.dart';
import 'score_table.dart';

class BoardScreen extends ConsumerStatefulWidget {
  const BoardScreen({super.key, required this.gameId});

  final String gameId;

  @override
  ConsumerState<BoardScreen> createState() => _BoardScreenState();
}

class _BoardScreenState extends ConsumerState<BoardScreen> {
  bool _resumeRecorded = false;

  GamesNotifier get _games => ref.read(gamesProvider.notifier);

  @override
  Widget build(BuildContext context) {
    final async = ref.watch(gamesProvider);
    final game = ref.watch(gameProvider(widget.gameId));

    if (game == null) {
      return Scaffold(
        appBar: AppBar(),
        body: Center(child: async.isLoading ? const CircularProgressIndicator() : const Text('Oyun bulunamadı.')),
      );
    }
    _recordResumeOnce(game);

    final rules = [
      for (final r in RuleCatalog.forType(game.type))
        if (r.category != RuleCategory.finishPenalty) r,
    ];
    final expanded = WindowSize.of(context).isExpanded;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) async {
        if (didPop) return;
        if (await _confirmExit(context) && context.mounted) _leave(context);
      },
      child: Scaffold(
        appBar: AppBar(
          leading: IconButton(
            tooltip: 'Geri',
            icon: const Icon(Icons.arrow_back_ios_new),
            onPressed: () async {
              if (await _confirmExit(context) && context.mounted) _leave(context);
            },
          ),
          title: Column(
            children: [
              Text(game.title, overflow: TextOverflow.ellipsis),
              Text(
                '${game.type.label} · ${game.handCount} el${game.isFinished ? ' · Bitti' : ''}',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
          ),
          actions: [
            IconButton(
              tooltip: 'Sonuçlar',
              icon: const Icon(Icons.leaderboard),
              onPressed: () => context.pushNamed('results', pathParameters: {'id': game.id}),
            ),
            PopupMenuButton<String>(
              onSelected: (value) => switch (value) {
                'finish' => _finish(game),
                'reopen' => _games.apply(game.id, (e, g) => e.reopen(g)),
                _ => null,
              },
              itemBuilder: (_) => [
                if (!game.isFinished)
                  const PopupMenuItem(value: 'finish', child: Text('Oyunu bitir'))
                else
                  const PopupMenuItem(value: 'reopen', child: Text('Oyuna devam et')),
              ],
            ),
          ],
        ),
        floatingActionButton: game.isFinished
            ? null
            : FloatingActionButton(
                key: const ValueKey('add-score'),
                tooltip: 'Puan Ekle',
                onPressed: () => _addHand(game),
                child: const Icon(Icons.add),
              ),
        bottomNavigationBar: rules.isEmpty || game.isFinished || expanded
            ? null
            : _RuleBar(rules: rules, onRule: (r) => _applyRule(game, r)),
        body: SafeArea(
          child: Padding(
            // Telefonda toplam satırı FAB'ın altında kalmasın.
            padding: EdgeInsets.fromLTRB(8, 8, 8, game.isFinished || expanded ? 8 : 80),
            child: expanded
                ? Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Expanded(
                        flex: 3,
                        child: ScoreTable(game: game, onRoundLongPress: (r) => _roundActions(game, r)),
                      ),
                      const SizedBox(width: 12),
                      SizedBox(
                        width: 320,
                        child: _SidePanel(
                          game: game,
                          rules: game.isFinished ? const [] : rules,
                          onRule: (r) => _applyRule(game, r),
                        ),
                      ),
                    ],
                  )
                : ScoreTable(game: game, onRoundLongPress: (r) => _roundActions(game, r)),
          ),
        ),
      ),
    );
  }

  void _recordResumeOnce(Game game) {
    if (_resumeRecorded) return;
    _resumeRecorded = true;
    // Yeni oluşturulan oyun için değil, sonradan açılan oyun için kaydedilir.
    if (game.rounds.isNotEmpty && !game.isFinished) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _games.apply(game.id, (e, g) => e.markResumed(g));
      });
    }
  }

  void _leave(BuildContext context) {
    if (context.canPop()) {
      context.pop();
    } else {
      context.goNamed('home');
    }
  }

  Future<bool> _confirmExit(BuildContext context) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Oyundan çıkmak istediğinize emin misiniz?'),
        content: const Text('Skorlar kaydedildi, geçmişten devam edebilirsiniz.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Hayır')),
          TextButton(onPressed: () => Navigator.pop(context, true), child: const Text('Evet')),
        ],
      ),
    );
    return result ?? false;
  }

  Future<void> _addHand(Game game) async {
    final result = await showScoreEntrySheet(context, game: game);
    if (result == null) return;
    await _games.apply(
      game.id,
      (e, g) => e.addHand(g, result.scores, sources: result.sources, rackScans: result.rackScans),
    );
    ref.read(analyticsProvider).event('round_score_saved', {'player_count': result.scores.length});
  }

  Future<void> _applyRule(Game game, RuleDefinition rule) async {
    ref.read(analyticsProvider).event('rule_action_opened', {'rule_key': rule.key});
    if (rule.category == RuleCategory.penalty) {
      final playerId = await _pickPlayer(game, rule);
      if (playerId == null) return;
      await _games.apply(game.id, (e, g) => e.addPenalty(g, rule.key, playerId));
      return;
    }
    final result = await showScoreEntrySheet(context, game: game, finishRule: rule);
    if (result == null || result.finisherId == null) return;
    await _games.apply(
      game.id,
      (e, g) => e.addFinish(
        g,
        rule.key,
        result.finisherId!,
        Map.of(result.scores)..remove(result.finisherId),
        sources: result.sources,
        rackScans: result.rackScans,
      ),
    );
  }

  Future<String?> _pickPlayer(Game game, RuleDefinition rule) {
    final value = game.settings.valueOf(rule.key) ?? rule.defaultValue;
    return showDialog<String>(
      context: context,
      builder: (context) => SimpleDialog(
        title: Text('${rule.label} ($value) · Oyuncu Seç'),
        children: [
          for (final p in game.players)
            SimpleDialogOption(
              key: ValueKey('penalty-${p.id}'),
              onPressed: () => Navigator.pop(context, p.id),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Text(p.name, style: Theme.of(context).textTheme.titleMedium),
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _roundActions(Game game, Round round) async {
    if (game.isFinished) return;
    final action = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.edit),
              title: const Text('Puanları düzenle'),
              onTap: () => Navigator.pop(context, 'edit'),
            ),
            ListTile(
              leading: Icon(Icons.delete_outline, color: Theme.of(context).colorScheme.error),
              title: const Text('Satırı sil'),
              onTap: () => Navigator.pop(context, 'delete'),
            ),
          ],
        ),
      ),
    );
    if (!mounted) return;
    switch (action) {
      case 'edit':
        final result = await showScoreEntrySheet(context, game: game, editing: round);
        if (result != null) await _games.apply(game.id, (e, g) => e.editRound(g, round.id, result.scores));
      case 'delete':
        await _games.apply(game.id, (e, g) => e.deleteRound(g, round.id));
    }
  }

  Future<void> _finish(Game game) async {
    final leaders = Standings.leaders(game).map((id) => game.playerById(id)?.name).join(', ');
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Oyunu bitir'),
        content: Text(game.rounds.isEmpty ? 'Henüz el oynanmadı. Yine de bitirilsin mi?' : 'Kazanan: $leaders'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('İptal')),
          FilledButton(
            style: FilledButton.styleFrom(minimumSize: const Size(96, 40)),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Bitir'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    await _games.apply(game.id, (e, g) => e.finish(g));
    if (mounted) unawaited(context.pushNamed('results', pathParameters: {'id': game.id}));
  }
}

class _RuleBar extends StatelessWidget {
  const _RuleBar({required this.rules, required this.onRule});

  final List<RuleDefinition> rules;
  final void Function(RuleDefinition) onRule;

  @override
  Widget build(BuildContext context) => Material(
    color: Theme.of(context).colorScheme.surfaceContainerHigh,
    child: SafeArea(
      top: false,
      child: SizedBox(
        height: 60,
        child: ListView(
          key: const ValueKey('rule-bar'),
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
          children: [
            for (final rule in rules)
              Padding(
                padding: const EdgeInsets.only(right: 8),
                child: _RuleChip(rule: rule, onPressed: () => onRule(rule)),
              ),
          ],
        ),
      ),
    ),
  );
}

class _RuleChip extends StatelessWidget {
  const _RuleChip({required this.rule, required this.onPressed});

  final RuleDefinition rule;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) => ActionChip(
    key: ValueKey('rule-chip-${rule.key}'),
    avatar: Icon(
      rule.category == RuleCategory.penalty ? Icons.gavel : Icons.flag,
      size: 18,
      color: rule.category == RuleCategory.penalty ? Palette.tileRed : Theme.of(context).colorScheme.secondary,
    ),
    label: Text(rule.label),
    onPressed: onPressed,
  );
}

/// Geniş ekranlarda tablonun yanında canlı sıralama ve kural düğmeleri.
class _SidePanel extends StatelessWidget {
  const _SidePanel({required this.game, required this.rules, required this.onRule});

  final Game game;
  final List<RuleDefinition> rules;
  final void Function(RuleDefinition) onRule;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final standings = Standings.compute(game);
    return Card(
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text('Sıralama', style: chalkStyle(30, color: theme.colorScheme.onSurface, height: 1.1)),
          Text(game.settings.winCondition.label, style: theme.textTheme.bodySmall),
          const SizedBox(height: 8),
          for (final s in standings)
            ListTile(
              dense: true,
              contentPadding: EdgeInsets.zero,
              leading: TileBadge(
                label: '${s.rank}',
                size: 28,
                color: s.rank == 1 ? Palette.tileRed : (s.rank == 2 ? Palette.tileBlue : Palette.tileBlack),
              ),
              title: Text(
                s.player.name,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(fontWeight: s.rank == 1 ? FontWeight.w700 : FontWeight.w500),
              ),
              trailing: Text('${s.total}', style: chalkStyle(28, color: theme.colorScheme.onSurface, height: 1)),
            ),
          if (rules.isNotEmpty) ...[
            const Divider(height: 24),
            Text('Kurallar', style: chalkStyle(30, color: theme.colorScheme.onSurface, height: 1.1)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [for (final r in rules) _RuleChip(rule: r, onPressed: () => onRule(r))],
            ),
          ],
        ],
      ),
    );
  }
}
