import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/models/game_type.dart';
import '../../domain/rules/rule_catalog.dart';
import '../../state/providers.dart';

/// Oyun türü ve kurallar seçildikten sonra oyuncu ekranına taşınan ayarlar.
class GameSetup {
  const GameSetup({
    required this.type,
    required this.isPartnered,
    required this.ruleValues,
    required this.winCondition,
  });

  final GameType type;
  final bool isPartnered;
  final Map<String, int> ruleValues;
  final WinCondition winCondition;
}

class GameTypeScreen extends ConsumerStatefulWidget {
  const GameTypeScreen({super.key});

  @override
  ConsumerState<GameTypeScreen> createState() => _GameTypeScreenState();
}

class _GameTypeScreenState extends ConsumerState<GameTypeScreen> {
  GameType? _type;
  bool _partnered = true;
  WinCondition _winCondition = WinCondition.highestTotal;
  final Map<String, TextEditingController> _ruleControllers = {};

  @override
  void dispose() {
    for (final c in _ruleControllers.values) {
      c.dispose();
    }
    super.dispose();
  }

  void _select(GameType type) {
    final prefs = ref.read(rulePreferencesProvider);
    final values = prefs.load(type, RuleCatalog.defaultValues(type));
    for (final c in _ruleControllers.values) {
      c.dispose();
    }
    _ruleControllers
      ..clear()
      ..addAll({for (final e in values.entries) e.key: TextEditingController(text: '${e.value}')});
    setState(() {
      _type = type;
      _partnered = prefs.loadPartnered(type) ?? true;
      _winCondition = RuleCatalog.defaultWinCondition(type);
    });
    ref.read(analyticsProvider).event('game_type_selected', {'game_type': type.wireName});
  }

  Map<String, int>? _parsedRules() {
    final values = <String, int>{};
    for (final e in _ruleControllers.entries) {
      final v = int.tryParse(e.value.text.trim());
      if (v == null) return null;
      values[e.key] = v;
    }
    return values;
  }

  void _continue() {
    final type = _type!;
    final rules = _parsedRules();
    if (rules == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Kural değerleri sayı olmalıdır.')));
      return;
    }
    final prefs = ref.read(rulePreferencesProvider);
    prefs.save(type, rules);
    if (RuleCatalog.supportsPartnerMode(type)) prefs.savePartnered(type, _partnered);
    context.pushNamed(
      'players',
      extra: GameSetup(
        type: type,
        isPartnered: RuleCatalog.supportsPartnerMode(type) && _partnered,
        ruleValues: rules,
        winCondition: _winCondition,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final type = _type;
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Oyun Türü Seç')),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: ListView(
                padding: const EdgeInsets.symmetric(vertical: 8),
                children: [
                  ResponsiveCenter(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Text(
                          'Masada ne oynuyorsunuz?',
                          textAlign: TextAlign.center,
                          style: displayStyle(23, color: theme.colorScheme.onSurface, height: 1.2),
                        ),
                        Text(
                          'Kuralları bir sonraki oyun için hatırlarız.',
                          textAlign: TextAlign.center,
                          style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                        ),
                        const SizedBox(height: 16),
                        Row(
                          children: [
                            for (final (i, t) in GameType.values.indexed) ...[
                              if (i > 0) const SizedBox(width: 10),
                              Expanded(
                                child: _TypeCard(
                                  key: ValueKey('type-${t.wireName}'),
                                  type: t,
                                  selected: t == type,
                                  onTap: () => _select(t),
                                ),
                              ),
                            ],
                          ],
                        ),
                        const SizedBox(height: 16),
                        if (type != null)
                          _SettingsCard(
                            type: type,
                            partnered: _partnered,
                            onPartneredChanged: (v) => setState(() => _partnered = v),
                            winCondition: _winCondition,
                            onWinConditionChanged: (v) => setState(() => _winCondition = v),
                            ruleControllers: _ruleControllers,
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            ResponsiveCenter(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
              child: FilledButton(onPressed: type == null ? null : _continue, child: const Text('Devam Et')),
            ),
          ],
        ),
      ),
    );
  }
}

/// Oyun türü seçimi: okey taşı rozeti ve kısa açıklama; seçili olan
/// kara tahtaya döner.
class _TypeCard extends StatelessWidget {
  const _TypeCard({super.key, required this.type, required this.selected, required this.onTap});

  final GameType type;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    final (badge, color, hint) = switch (type) {
      GameType.okey => ('O', Palette.tileBlue, 'Klasik okey'),
      GameType.yuzBirOkey => ('101', Palette.tileRed, 'Cezalar, bitişler'),
      GameType.generic => ('#', Palette.tileBlack, 'Her oyun için'),
    };
    final content = SizedBox(
      height: 132,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          TileBadge(label: badge, color: color, size: badge.length > 2 ? 46 : 40),
          const SizedBox(height: 10),
          FittedBox(
            fit: BoxFit.scaleDown,
            child: Text(
              type.label,
              style: displayStyle(19, color: selected ? Palette.chalk : scheme.onSurface, height: 1),
            ),
          ),
          const SizedBox(height: 2),
          Text(
            hint,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(fontSize: 12, color: selected ? Palette.chalkDim : scheme.onSurfaceVariant),
          ),
        ],
      ),
    );
    return Semantics(
      selected: selected,
      button: true,
      child: AnimatedSwitcher(
        duration: const Duration(milliseconds: 180),
        child: selected
            ? Chalkboard(
                key: const ValueKey('on'),
                frame: 5,
                radius: 18,
                padding: const EdgeInsets.symmetric(horizontal: 6),
                onTap: onTap,
                child: content,
              )
            : Card(
                key: const ValueKey('off'),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                  side: BorderSide(color: scheme.outlineVariant, width: 1.5),
                ),
                child: InkWell(
                  onTap: onTap,
                  child: Padding(padding: const EdgeInsets.all(11), child: content),
                ),
              ),
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  const _SettingsCard({
    required this.type,
    required this.partnered,
    required this.onPartneredChanged,
    required this.winCondition,
    required this.onWinConditionChanged,
    required this.ruleControllers,
  });

  final GameType type;
  final bool partnered;
  final ValueChanged<bool> onPartneredChanged;
  final WinCondition winCondition;
  final ValueChanged<WinCondition> onWinConditionChanged;
  final Map<String, TextEditingController> ruleControllers;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final rules = RuleCatalog.forType(type);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('${type.label} Ayarları', style: displayStyle(20, color: theme.colorScheme.onSurface, height: 1.1)),
            const SizedBox(height: 12),
            if (RuleCatalog.supportsPartnerMode(type)) ...[
              SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(value: true, label: Text('Eşli')),
                  ButtonSegment(value: false, label: Text('Tekli')),
                ],
                selected: {partnered},
                onSelectionChanged: (s) => onPartneredChanged(s.single),
              ),
              const SizedBox(height: 12),
            ],
            Text('Kazanan', style: theme.textTheme.labelLarge),
            const SizedBox(height: 6),
            SegmentedButton<WinCondition>(
              segments: const [
                ButtonSegment(value: WinCondition.lowestTotal, label: Text('En düşük puan')),
                ButtonSegment(value: WinCondition.highestTotal, label: Text('En yüksek puan')),
              ],
              selected: {winCondition},
              onSelectionChanged: (s) => onWinConditionChanged(s.single),
            ),
            if (rules.isNotEmpty) ...[
              const Divider(height: 32),
              Text('Puan Kuralları', style: displayStyle(19, color: theme.colorScheme.secondary, height: 1.1)),
              const SizedBox(height: 12),
              LayoutBuilder(
                builder: (context, constraints) {
                  final twoColumns = constraints.maxWidth >= 520;
                  final width = twoColumns ? (constraints.maxWidth - 12) / 2 : constraints.maxWidth;
                  return Wrap(
                    spacing: 12,
                    runSpacing: 12,
                    children: [
                      for (final rule in rules)
                        SizedBox(
                          width: width,
                          child: TextField(
                            key: ValueKey('rule-${rule.key}'),
                            controller: ruleControllers[rule.key],
                            keyboardType: const TextInputType.numberWithOptions(signed: true),
                            inputFormatters: [FilteringTextInputFormatter.allow(RegExp(r'^-?\d*'))],
                            decoration: InputDecoration(
                              labelText: rule.label,
                              helperText: 'Varsayılan: ${rule.defaultValue}',
                            ),
                          ),
                        ),
                    ],
                  );
                },
              ),
            ],
          ],
        ),
      ),
    );
  }
}
