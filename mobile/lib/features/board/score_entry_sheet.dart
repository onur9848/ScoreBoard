import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:go_router/go_router.dart';

import '../../core/chalk.dart';
import '../../core/theme.dart';
import '../../domain/game_engine.dart';
import '../../domain/models/game.dart';
import '../../domain/models/game_type.dart';
import '../../domain/rules/rule_catalog.dart';
import '../rack/rack_models.dart';

const _seatColors = [Palette.tileRed, Palette.tileBlue, Palette.tileYellow, Palette.tileBlack];

class ScoreEntryResult {
  const ScoreEntryResult({required this.scores, required this.sources, required this.rackScans, this.finisherId});

  final Map<String, int> scores;
  final Map<String, ScoreSource> sources;
  final List<RackScanSummary> rackScans;
  final String? finisherId;
}

/// El girişi. [finishRule] verilirse bir oyuncu "bitiren" seçilir ve kuralın
/// değerini alır; diğerleri için puan girilir. [editing] verilirse o elin
/// puanları düzenlenir.
Future<ScoreEntryResult?> showScoreEntrySheet(
  BuildContext context, {
  required Game game,
  RuleDefinition? finishRule,
  Round? editing,
}) => showModalBottomSheet<ScoreEntryResult>(
  context: context,
  isScrollControlled: true,
  useSafeArea: true,
  constraints: const BoxConstraints(maxWidth: 640),
  builder: (_) => _ScoreEntrySheet(game: game, finishRule: finishRule, editing: editing),
);

class _ScoreEntrySheet extends StatefulWidget {
  const _ScoreEntrySheet({required this.game, this.finishRule, this.editing});

  final Game game;
  final RuleDefinition? finishRule;
  final Round? editing;

  @override
  State<_ScoreEntrySheet> createState() => _ScoreEntrySheetState();
}

class _ScoreEntrySheetState extends State<_ScoreEntrySheet> {
  final _formKey = GlobalKey<FormState>();
  late final Map<String, TextEditingController> _controllers = {
    for (final p in widget.game.players)
      p.id: TextEditingController(text: widget.editing == null ? '' : '${widget.editing!.scoreOf(p.id)}'),
  };
  final Map<String, ScoreSource> _sources = {};
  final Map<String, RackScanSummary> _scans = {};
  String? _finisherId;

  Game get _game => widget.game;
  RuleDefinition? get _rule => widget.finishRule;

  int get _finishValue => _game.settings.valueOf(_rule!.key) ?? _rule!.defaultValue;

  int? get _pairedValue {
    final paired = _rule?.pairedKey;
    if (paired == null) return null;
    return _game.settings.valueOf(paired) ?? RuleCatalog.find(_game.type, paired)?.defaultValue;
  }

  bool get _allowNegative => _game.type != GameType.yuzBirOkey || widget.editing != null;

  @override
  void dispose() {
    for (final c in _controllers.values) {
      c.dispose();
    }
    super.dispose();
  }

  void _setValue(String playerId, int value, ScoreSource source) {
    _controllers[playerId]!.text = '$value';
    _sources[playerId] = source;
  }

  Future<void> _openRack(Player player) async {
    final result = await context.pushNamed<RackResult>(
      'rack',
      extra: RackRequest(
        playerName: player.name,
        okeyPenalty: _game.settings.valueOf('penalty') ?? 101,
        noOpenPenalty: _pairedValue ?? _game.settings.valueOf('noOpenPenalty') ?? 202,
      ),
    );
    if (result == null || !mounted) return;
    setState(() {
      _setValue(player.id, result.points, ScoreSource.rackScan);
      _scans[player.id] = RackScanSummary(
        playerId: player.id,
        points: result.points,
        tileCount: result.tileCount,
        recognizedTileCount: result.recognizedTileCount,
        correctedTileCount: result.correctedTileCount,
        usedCamera: result.usedCamera,
      );
    });
  }

  void _submit() {
    if (_rule != null && _finisherId == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Eli bitiren oyuncuyu seçin.')));
      return;
    }
    if (!_formKey.currentState!.validate()) return;
    final scores = <String, int>{};
    for (final p in _game.players) {
      scores[p.id] = p.id == _finisherId ? _finishValue : int.parse(_controllers[p.id]!.text.trim());
    }
    // Rack sonucu sonradan elle değiştirildiyse kaynak manuel sayılır.
    final scans = [
      for (final scan in _scans.values)
        if (scores[scan.playerId] == scan.points) scan,
    ];
    final sources = {
      for (final e in _sources.entries)
        if (e.value != ScoreSource.rackScan || scans.any((s) => s.playerId == e.key)) e.key: e.value,
    };
    Navigator.pop(
      context,
      ScoreEntryResult(scores: scores, sources: sources, rackScans: scans, finisherId: _finisherId),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final scheme = theme.colorScheme;
    final title = widget.editing != null ? 'Eli Düzenle' : (_rule?.label ?? 'Skor Ekle');
    final showRack = _game.type != GameType.generic && widget.editing == null;

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.viewInsetsOf(context).bottom),
      child: Form(
        key: _formKey,
        child: ListView(
          shrinkWrap: true,
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
          children: [
            Text(title, style: chalkStyle(34, color: scheme.onSurface, height: 1.1)),
            Text(_game.type.label, style: theme.textTheme.bodyMedium?.copyWith(color: scheme.onSurfaceVariant)),
            if (_rule != null) ...[
              const SizedBox(height: 16),
              Text('Eli kim bitirdi?', style: theme.textTheme.labelLarge),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  for (final p in _game.players)
                    ChoiceChip(
                      key: ValueKey('finisher-${p.id}'),
                      label: Text(p.name),
                      selected: _finisherId == p.id,
                      onSelected: (_) => setState(() => _finisherId = p.id),
                    ),
                ],
              ),
            ],
            const SizedBox(height: 16),
            for (final (i, p) in _game.players.indexed)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Container(
                  decoration: BoxDecoration(
                    color: scheme.surfaceContainerHigh,
                    borderRadius: BorderRadius.circular(16),
                    border: Border(left: BorderSide(color: _seatColors[i % _seatColors.length], width: 4)),
                  ),
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                  child: p.id == _finisherId
                      ? _FinisherRow(name: p.name, value: _finishValue)
                      : Row(
                          children: [
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(p.name, style: theme.textTheme.titleMedium, overflow: TextOverflow.ellipsis),
                                  if (_pairedValue != null)
                                    Padding(
                                      padding: const EdgeInsets.only(top: 4),
                                      child: ActionChip(
                                        visualDensity: VisualDensity.compact,
                                        label: Text('Açmadı: $_pairedValue'),
                                        onPressed: () =>
                                            setState(() => _setValue(p.id, _pairedValue!, ScoreSource.rule)),
                                      ),
                                    ),
                                ],
                              ),
                            ),
                            if (showRack)
                              IconButton(
                                key: ValueKey('rack-${p.id}'),
                                tooltip: 'Istakadan hesapla',
                                onPressed: () => _openRack(p),
                                icon: Icon(
                                  Icons.photo_camera_outlined,
                                  color: _scans.containsKey(p.id) ? scheme.secondary : scheme.primary,
                                ),
                              ),
                            SizedBox(
                              width: 96,
                              child: TextFormField(
                                key: ValueKey('score-${p.id}'),
                                controller: _controllers[p.id],
                                textAlign: TextAlign.center,
                                keyboardType: TextInputType.numberWithOptions(signed: _allowNegative),
                                inputFormatters: [
                                  FilteringTextInputFormatter.allow(RegExp(_allowNegative ? r'^-?\d*' : r'^\d*')),
                                ],
                                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
                                decoration: const InputDecoration(
                                  hintText: '0',
                                  isDense: true,
                                  errorStyle: TextStyle(height: 0.8, fontSize: 11),
                                ),
                                onChanged: (_) => _sources.remove(p.id),
                                validator: (v) => int.tryParse((v ?? '').trim()) == null ? 'Gerekli' : null,
                              ),
                            ),
                          ],
                        ),
                ),
              ),
            const SizedBox(height: 4),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(onPressed: () => Navigator.pop(context), child: const Text('İptal')),
                const SizedBox(width: 8),
                FilledButton(
                  key: const ValueKey('save-score'),
                  style: FilledButton.styleFrom(minimumSize: const Size(120, 48)),
                  onPressed: _submit,
                  child: const Text('Kaydet'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _FinisherRow extends StatelessWidget {
  const _FinisherRow({required this.name, required this.value});

  final String name;
  final int value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SizedBox(
      height: 48,
      child: Row(
        children: [
          Icon(Icons.flag, color: theme.colorScheme.secondary),
          const SizedBox(width: 8),
          Expanded(child: Text(name, style: theme.textTheme.titleMedium)),
          Text('$value', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(width: 24),
        ],
      ),
    );
  }
}
