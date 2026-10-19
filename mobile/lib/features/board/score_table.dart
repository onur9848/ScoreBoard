import 'package:flutter/material.dart';

import '../../core/chalk.dart';
import '../../core/theme.dart';
import '../../domain/models/game.dart';
import '../../domain/rules/rule_catalog.dart';
import '../../domain/scoring/standings.dart';

/// Logodaki gibi kara tahtaya tebeşirle yazılmış puan tablosu: başlıkta
/// oyuncular, her satırda bir el; altta sabit toplam satırı.
class ScoreTable extends StatelessWidget {
  const ScoreTable({super.key, required this.game, required this.onRoundLongPress});

  final Game game;
  final void Function(Round round) onRoundLongPress;

  static const _turnColumnWidth = 52.0;

  @override
  Widget build(BuildContext context) {
    final totals = Standings.totals(game);
    final leaders = Standings.leaders(game).toSet();
    final many = game.players.length > 4;

    var hand = 0;
    final labels = [
      for (final r in game.rounds)
        if (r.countsAsHand) '${++hand}' else '–',
    ];

    return Chalkboard(
      padding: const EdgeInsets.fromLTRB(4, 8, 4, 6),
      child: Column(
        children: [
          _Row(
            leading: Text('#', style: displayStyle(24, color: Palette.chalkPink, height: 1)),
            cells: [for (final p in game.players) _PlayerName(name: p.name, size: many ? 14 : 16)],
          ),
          const _ChalkRule(),
          Expanded(
            child: game.rounds.isEmpty
                ? const _EmptyRounds()
                : ListView.separated(
                    padding: const EdgeInsets.symmetric(vertical: 2),
                    itemCount: game.rounds.length,
                    separatorBuilder: (_, _) => const Padding(
                      padding: EdgeInsets.symmetric(horizontal: 8),
                      child: ChalkLine(thickness: 1, opacity: 0.14),
                    ),
                    itemBuilder: (context, index) {
                      final round = game.rounds[index];
                      final rule = round.ruleKey == null ? null : RuleCatalog.find(game.type, round.ruleKey!);
                      return InkWell(
                        key: ValueKey('round-${round.id}'),
                        onLongPress: () => onRoundLongPress(round),
                        splashColor: Palette.chalk.withValues(alpha: 0.08),
                        child: _Row(
                          leading: Tooltip(
                            message: rule?.label ?? 'El',
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(labels[index], style: displayStyle(17, color: Palette.chalkDim, height: 1)),
                                if (rule != null)
                                  Icon(
                                    rule.category == RuleCategory.penalty ? Icons.gavel : Icons.flag,
                                    size: 12,
                                    color: Palette.chalkPink,
                                  ),
                              ],
                            ),
                          ),
                          cells: [
                            for (final p in game.players)
                              _ScoreCell(value: round.scoreOf(p.id), circled: round.actorPlayerId == p.id),
                          ],
                        ),
                      );
                    },
                  ),
          ),
          const _ChalkRule(double: true),
          _Row(
            leading: Text(
              'Σ',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w600,
                color: Palette.chalkPink.withValues(alpha: 0.95),
              ),
            ),
            cells: [
              for (final p in game.players)
                Row(
                  key: ValueKey('total-${p.id}'),
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (leaders.contains(p.id))
                      const Padding(
                        padding: EdgeInsets.only(right: 2),
                        child: Icon(Icons.emoji_events, size: 16, color: Palette.chalkYellow),
                      ),
                    Flexible(
                      child: Text(
                        '${totals[p.id]}',
                        overflow: TextOverflow.ellipsis,
                        style: displayStyle(
                          22,
                          color: leaders.contains(p.id) ? Palette.chalkYellow : Palette.chalk,
                          height: 1,
                        ),
                      ),
                    ),
                  ],
                ),
            ],
          ),
        ],
      ),
    );
  }
}

class _Row extends StatelessWidget {
  const _Row({required this.leading, required this.cells});

  final Widget leading;
  final List<Widget> cells;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 6),
    child: IntrinsicHeight(
      child: Row(
        children: [
          SizedBox(
            width: ScoreTable._turnColumnWidth,
            child: Center(child: leading),
          ),
          for (final cell in cells) ...[
            const _ChalkColumnLine(),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 2),
                child: Center(child: cell),
              ),
            ),
          ],
        ],
      ),
    ),
  );
}

/// Tek kelimelik uzun isimler harf harf bölünmesin diye küçültülür.
class _PlayerName extends StatelessWidget {
  const _PlayerName({required this.name, required this.size});

  final String name;
  final double size;

  @override
  Widget build(BuildContext context) {
    final text = Text(
      name,
      maxLines: 2,
      textAlign: TextAlign.center,
      overflow: TextOverflow.ellipsis,
      style: displayStyle(size, height: 1),
    );
    return name.trim().contains(' ') ? text : FittedBox(fit: BoxFit.scaleDown, child: text);
  }
}

class _ChalkColumnLine extends StatelessWidget {
  const _ChalkColumnLine();

  @override
  Widget build(BuildContext context) => Container(
    width: 1.5,
    margin: const EdgeInsets.symmetric(vertical: 2),
    color: Palette.chalk.withValues(alpha: 0.28),
  );
}

class _ChalkRule extends StatelessWidget {
  const _ChalkRule({this.double = false});

  final bool double;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
    child: Column(
      children: [
        const ChalkLine(thickness: 2.5, opacity: 0.7),
        if (double) ...[const SizedBox(height: 3), const ChalkLine(thickness: 1.5, opacity: 0.5)],
      ],
    ),
  );
}

class _ScoreCell extends StatelessWidget {
  const _ScoreCell({required this.value, required this.circled});

  final int value;

  /// Cezayı yiyen ya da eli bitiren oyuncu tebeşirle çevrelenir.
  final bool circled;

  @override
  Widget build(BuildContext context) => Container(
    constraints: const BoxConstraints(minHeight: 34, minWidth: 44),
    padding: const EdgeInsets.symmetric(horizontal: 8),
    decoration: circled
        ? BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: Palette.chalkYellow.withValues(alpha: 0.85), width: 1.8),
          )
        : null,
    child: Center(
      widthFactor: 1,
      heightFactor: 1,
      child: Text(
        '$value',
        textAlign: TextAlign.center,
        style: displayStyle(21, color: value < 0 ? Palette.chalkPink : Palette.chalk, height: 1),
      ),
    ),
  );
}

class _EmptyRounds extends StatelessWidget {
  const _EmptyRounds();

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('Tahta boş', style: displayStyle(24)),
          const SizedBox(height: 6),
          const Text(
            'İlk eli yazmak için + düğmesine dokunun.',
            textAlign: TextAlign.center,
            style: TextStyle(color: Palette.chalkDim),
          ),
        ],
      ),
    ),
  );
}
