import 'package:flutter/material.dart';

import '../../domain/models/game.dart';
import '../../domain/rules/rule_catalog.dart';
import '../../domain/scoring/standings.dart';

/// Eski BoardScreen tablosunun Flutter karşılığı: başlıkta oyuncular, her
/// satırda bir el; altta sabit toplam satırı.
class ScoreTable extends StatelessWidget {
  const ScoreTable({super.key, required this.game, required this.onRoundLongPress});

  final Game game;
  final void Function(Round round) onRoundLongPress;

  static const _turnColumnWidth = 56.0;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final totals = Standings.totals(game);
    final leaders = Standings.leaders(game).toSet();

    var hand = 0;
    final labels = [
      for (final r in game.rounds)
        if (r.countsAsHand) '#${++hand}' else '–',
    ];

    return Card(
      elevation: 6,
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          children: [
            _Row(
              background: scheme.primaryContainer,
              radius: 16,
              leading: Text('#', style: _bold(context, scheme.onPrimaryContainer)),
              cells: [
                for (final (i, p) in game.players.indexed)
                  _Cell(
                    color: _columnColor(scheme, i, strong: true),
                    child: Text(
                      p.name,
                      maxLines: 2,
                      textAlign: TextAlign.center,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context).textTheme.labelLarge
                          ?.copyWith(fontWeight: FontWeight.bold, color: scheme.onPrimaryContainer, height: 1.15),
                    ),
                  ),
              ],
            ),
            Divider(color: scheme.outline, thickness: 2, height: 4),
            Expanded(
              child: game.rounds.isEmpty
                  ? _EmptyRounds(scheme: scheme)
                  : ListView.separated(
                      padding: const EdgeInsets.symmetric(vertical: 4),
                      itemCount: game.rounds.length,
                      separatorBuilder: (_, _) => Divider(height: 1, color: scheme.outline.withValues(alpha: 0.3)),
                      itemBuilder: (context, index) {
                        final round = game.rounds[index];
                        final rule = round.ruleKey == null ? null : RuleCatalog.find(game.type, round.ruleKey!);
                        return InkWell(
                          key: ValueKey('round-${round.id}'),
                          onLongPress: () => onRoundLongPress(round),
                          child: _Row(
                            background: index.isEven ? scheme.surface : scheme.surfaceContainerHighest,
                            leading: Tooltip(
                              message: rule?.label ?? 'El',
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text(labels[index], style: _bold(context, scheme.primary)),
                                  if (rule != null)
                                    Icon(
                                      rule.category == RuleCategory.penalty ? Icons.gavel : Icons.flag,
                                      size: 12,
                                      color: scheme.secondary,
                                    ),
                                ],
                              ),
                            ),
                            cells: [
                              for (final (i, p) in game.players.indexed)
                                _Cell(
                                  color: _columnColor(scheme, i, strong: false),
                                  outlined: round.actorPlayerId == p.id,
                                  child: Text(
                                    '${round.scoreOf(p.id)}',
                                    textAlign: TextAlign.center,
                                    style: Theme.of(context).textTheme.bodyLarge,
                                  ),
                                ),
                            ],
                          ),
                        );
                      },
                    ),
            ),
            Divider(color: scheme.outline, thickness: 2, height: 4),
            _Row(
              background: scheme.secondaryContainer.withValues(alpha: 0.6),
              radius: 12,
              leading: Text('Σ', style: _bold(context, scheme.onSecondaryContainer)),
              cells: [
                for (final p in game.players)
                  _Cell(
                    key: ValueKey('total-${p.id}'),
                    color: Colors.transparent,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        if (leaders.contains(p.id))
                          Padding(
                            padding: const EdgeInsets.only(right: 2),
                            child: Icon(Icons.emoji_events, size: 14, color: scheme.secondary),
                          ),
                        Flexible(
                          child: Text(
                            '${totals[p.id]}',
                            style: _bold(context, scheme.onSecondaryContainer),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  static Color _columnColor(ColorScheme scheme, int index, {required bool strong}) => index.isEven
      ? scheme.secondaryContainer.withValues(alpha: strong ? 0.85 : 0.5)
      : scheme.primaryContainer.withValues(alpha: strong ? 0.7 : 0.3);

  static TextStyle? _bold(BuildContext context, Color color) =>
      Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold, color: color);
}

class _Row extends StatelessWidget {
  const _Row({required this.leading, required this.cells, required this.background, this.radius = 0});

  final Widget leading;
  final List<Widget> cells;
  final Color background;
  final double radius;

  @override
  Widget build(BuildContext context) => Container(
    decoration: BoxDecoration(color: background, borderRadius: BorderRadius.circular(radius)),
    padding: const EdgeInsets.symmetric(vertical: 8),
    child: Row(
      children: [
        SizedBox(
          width: ScoreTable._turnColumnWidth,
          child: Center(child: leading),
        ),
        for (final cell in cells)
          Expanded(
            child: Padding(padding: const EdgeInsets.symmetric(horizontal: 2), child: cell),
          ),
      ],
    ),
  );
}

class _Cell extends StatelessWidget {
  const _Cell({super.key, required this.child, required this.color, this.outlined = false});

  final Widget child;
  final Color color;
  final bool outlined;

  @override
  Widget build(BuildContext context) => Container(
    constraints: const BoxConstraints(minHeight: 36),
    alignment: Alignment.center,
    padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 4),
    decoration: BoxDecoration(
      color: color,
      borderRadius: BorderRadius.circular(8),
      border: outlined ? Border.all(color: Theme.of(context).colorScheme.secondary, width: 1.5) : null,
    ),
    child: child,
  );
}

class _EmptyRounds extends StatelessWidget {
  const _EmptyRounds({required this.scheme});

  final ColorScheme scheme;

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.edit_note, size: 48, color: scheme.primary.withValues(alpha: 0.6)),
          const SizedBox(height: 8),
          Text(
            'İlk eli eklemek için + düğmesine dokunun.',
            textAlign: TextAlign.center,
            style: TextStyle(color: scheme.onSurfaceVariant),
          ),
        ],
      ),
    ),
  );
}
