import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/responsive.dart';
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
                        Wrap(
                          alignment: WrapAlignment.center,
                          spacing: 8,
                          runSpacing: 8,
                          children: [
                            for (final t in GameType.values)
                              ChoiceChip(
                                key: ValueKey('type-${t.wireName}'),
                                label: Text(t.label),
                                selected: t == type,
                                onSelected: (_) => _select(t),
                              ),
                          ],
                        ),
                        const SizedBox(height: 20),
                        if (type == null)
                          Padding(
                            padding: const EdgeInsets.all(32),
                            child: Text(
                              'Masadaki oyunu seçin; kuralları bir sonraki oyun için hatırlarız.',
                              textAlign: TextAlign.center,
                              style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                            ),
                          )
                        else
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
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('${type.label} Ayarları', style: theme.textTheme.titleMedium),
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
              Text(
                'Puan Kuralları',
                textAlign: TextAlign.center,
                style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.primary),
              ),
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
