import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../domain/tiles/rack_scoring.dart';
import '../../domain/tiles/tile.dart';
import '../../state/providers.dart';
import 'rack_controller.dart';
import 'rack_models.dart';
import 'tile_editor_sheet.dart';
import 'tile_view.dart';

/// Istakadaki taşları okur, sanal ıstakaya dizer ve eldeki puanı hesaplar.
class RackScreen extends ConsumerStatefulWidget {
  const RackScreen({super.key, this.request});

  final RackRequest? request;

  @override
  ConsumerState<RackScreen> createState() => _RackScreenState();
}

class _RackScreenState extends ConsumerState<RackScreen> {
  late final RackController _rack = RackController(
    options: RackScoreOptions(
      okeyPenalty: widget.request?.okeyPenalty ?? 101,
      noOpenPenalty: widget.request?.noOpenPenalty ?? 202,
    ),
  );
  bool _scanning = false;

  @override
  void dispose() {
    _rack.dispose();
    super.dispose();
  }

  Future<void> _scan(ImageSource source) async {
    final recognizer = ref.read(tileRecognizerProvider);
    final analytics = ref.read(analyticsProvider);
    final messenger = ScaffoldMessenger.of(context);
    final photo = await ImagePicker().pickImage(source: source, imageQuality: 92, maxWidth: 2400);
    if (photo == null) return;
    setState(() => _scanning = true);
    try {
      final rows = await recognizer.recognize(photo.path);
      _rack.loadDetections(rows);
      final count = _rack.tileCount;
      analytics.event('rack_scan_completed', {'tile_count': count, 'source': source.name});
      messenger.showSnackBar(
        SnackBar(
          content: Text(
            count == 0
                ? 'Taş bulunamadı. Istakaya yakından ve düz bir açıyla çekmeyi deneyin.'
                : '$count taş okundu. Hatalı olanlara dokunarak düzeltin.',
          ),
        ),
      );
    } catch (e) {
      messenger.showSnackBar(SnackBar(content: Text('Fotoğraf okunamadı: $e')));
    } finally {
      if (mounted) setState(() => _scanning = false);
    }
  }

  Future<void> _editTile(int row, int index) async {
    final slot = _rack.rows[row][index];
    final result = await showTileEditor(
      context,
      id: slot.tile.id,
      initial: slot.tile,
      title: 'Taşı düzelt',
      allowDelete: true,
      allowMove: true,
    );
    switch (result?.action) {
      case TileEditAction.save:
        _rack.replace(row, index, result!.tile!);
      case TileEditAction.delete:
        _rack.remove(row, index);
      case TileEditAction.moveRow:
        _rack.moveToOtherRow(row, index);
      case null:
        break;
    }
  }

  Future<void> _addTile(int row) async {
    final result = await showTileEditor(context, id: _rack.newTileId(), title: 'Taş ekle');
    if (result?.action == TileEditAction.save) _rack.add(row, result!.tile!);
  }

  Future<void> _pickIndicator() async {
    final current = _rack.options.indicator;
    final result = await showTileEditor(
      context,
      id: 'indicator',
      initial: current,
      title: 'Gösterge taşı',
      allowFakeOkey: false,
      allowDelete: current != null,
    );
    switch (result?.action) {
      case TileEditAction.save:
        _rack.setOptions(_rack.options.copyWith(indicator: result!.tile));
      case TileEditAction.delete:
        _rack.setOptions(_rack.options.copyWith(clearIndicator: true));
      default:
        break;
    }
  }

  void _useScore() {
    Navigator.pop(
      context,
      RackResult(
        points: _rack.score.total,
        tileCount: _rack.tileCount,
        recognizedTileCount: _rack.recognizedTileCount,
        correctedTileCount: _rack.correctedTileCount,
        usedCamera: _rack.usedCamera,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final canScan = ref.watch(tileRecognizerProvider).isSupported;
    final playerName = widget.request?.playerName;
    return Scaffold(
      appBar: AppBar(
        title: Text(playerName == null ? 'Puan Hesapla' : '$playerName · Istaka'),
        actions: [
          ListenableBuilder(
            listenable: _rack,
            builder: (_, _) => IconButton(
              tooltip: 'Istakayı temizle',
              onPressed: _rack.tileCount == 0 ? null : _rack.clear,
              icon: const Icon(Icons.delete_sweep_outlined),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: ListenableBuilder(
          listenable: _rack,
          builder: (context, _) => Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  children: [
                    ResponsiveCenter(
                      maxWidth: 900,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          if (canScan) _ScanButtons(scanning: _scanning, onScan: _scan) else const _ManualHint(),
                          const SizedBox(height: 16),
                          _RackBoard(controller: _rack, onTileTap: _editTile, onAdd: _addTile),
                          const SizedBox(height: 16),
                          _OptionsCard(controller: _rack, onPickIndicator: _pickIndicator),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              _ScoreBar(score: _rack.score, tileCount: _rack.tileCount, onUse: playerName == null ? null : _useScore),
            ],
          ),
        ),
      ),
    );
  }
}

class _ScanButtons extends StatelessWidget {
  const _ScanButtons({required this.scanning, required this.onScan});

  final bool scanning;
  final void Function(ImageSource) onScan;

  @override
  Widget build(BuildContext context) {
    if (scanning) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(20),
          child: Row(
            children: [
              SizedBox.square(dimension: 24, child: CircularProgressIndicator(strokeWidth: 3)),
              SizedBox(width: 16),
              Text('Taşlar okunuyor…'),
            ],
          ),
        ),
      );
    }
    return Row(
      children: [
        Expanded(
          child: FilledButton.icon(
            key: const ValueKey('scan-camera'),
            onPressed: () => onScan(ImageSource.camera),
            icon: const Icon(Icons.photo_camera),
            label: const Text('Istakayı Çek'),
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: OutlinedButton.icon(
            onPressed: () => onScan(ImageSource.gallery),
            icon: const Icon(Icons.photo_library_outlined),
            label: const Text('Galeriden'),
          ),
        ),
      ],
    );
  }
}

class _ManualHint extends StatelessWidget {
  const _ManualHint();

  @override
  Widget build(BuildContext context) => const Card(
    child: Padding(
      padding: EdgeInsets.all(16),
      child: Row(
        children: [
          TileBadge(icon: Icons.photo_camera_outlined, color: Palette.tileBlue, size: 32),
          SizedBox(width: 14),
          Expanded(child: Text('Kamera ile okuma telefonda çalışır. Burada taşları + ile ekleyebilirsiniz.')),
        ],
      ),
    ),
  );
}

/// Ahşap ıstaka görünümü; iki sıra, sürükle-bırak ile sıralanır.
class _RackBoard extends StatelessWidget {
  const _RackBoard({required this.controller, required this.onTileTap, required this.onAdd});

  final RackController controller;
  final void Function(int row, int index) onTileTap;
  final void Function(int row) onAdd;

  @override
  Widget build(BuildContext context) {
    final indicator = controller.options.indicator;
    return LayoutBuilder(
      builder: (context, constraints) {
        // 15 taş + ekle yuvası bir sıraya sığacak şekilde ölçeklenir.
        final tileWidth = ((constraints.maxWidth - 32) / 16 - 4).clamp(28.0, 52.0);
        final rowHeight = tileWidth * 1.42 + 12;
        return Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(16),
            gradient: const LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [Palette.wood, Palette.woodDark],
            ),
            border: Border.all(color: Palette.woodDeep.withValues(alpha: 0.6)),
            boxShadow: const [BoxShadow(color: Color(0x40000000), blurRadius: 8, offset: Offset(0, 4))],
          ),
          child: Column(
            children: [
              for (var row = 0; row < RackController.rowCount; row++) ...[
                if (row > 0)
                  Container(height: 4, margin: const EdgeInsets.symmetric(vertical: 6), color: Palette.woodDeep),
                SizedBox(
                  height: rowHeight,
                  child: Row(
                    children: [
                      Expanded(
                        child: ReorderableListView.builder(
                          key: ValueKey('rack-row-$row'),
                          scrollDirection: Axis.horizontal,
                          buildDefaultDragHandles: false,
                          proxyDecorator: (child, _, _) => Material(color: Colors.transparent, child: child),
                          itemCount: controller.rows[row].length,
                          onReorderItem: (a, b) => controller.reorder(row, a, b),
                          itemBuilder: (context, index) {
                            final slot = controller.rows[row][index];
                            return ReorderableDelayedDragStartListener(
                              key: ValueKey(slot.tile.id),
                              index: index,
                              child: Padding(
                                padding: const EdgeInsets.symmetric(horizontal: 2, vertical: 6),
                                child: TileView(
                                  tile: slot.tile,
                                  width: tileWidth,
                                  isOkey: RackScoring.isOkey(slot.tile, indicator),
                                  highlight: slot.isCorrection,
                                  onTap: () => onTileTap(row, index),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.only(left: 4),
                        child: TileView(
                          key: ValueKey('rack-add-$row'),
                          tile: null,
                          width: tileWidth,
                          onTap: () => onAdd(row),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
        );
      },
    );
  }
}

class _OptionsCard extends StatelessWidget {
  const _OptionsCard({required this.controller, required this.onPickIndicator});

  final RackController controller;
  final VoidCallback onPickIndicator;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final o = controller.options;
    final indicator = o.indicator;
    final okey = indicator == null ? null : okeyFromIndicator(indicator);
    void set(RackScoreOptions next) => controller.setOptions(next);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                TileView(key: const ValueKey('indicator-tile'), tile: indicator, width: 40, onTap: onPickIndicator),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Gösterge', style: chalkStyle(26, color: theme.colorScheme.onSurface, height: 1.1)),
                      Text(
                        okey == null
                            ? 'Okeyi işaretlemek için göstergeyi seçin.'
                            : 'Okey: ${okey.color.label} ${okey.number}',
                        style: theme.textTheme.bodySmall,
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const Divider(height: 24),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Elini açtı'),
              subtitle: Text(o.opened ? 'Taşların toplamı yazılır' : 'Açmama cezası (${o.noOpenPenalty}) yazılır'),
              value: o.opened,
              onChanged: (v) => set(o.copyWith(opened: v)),
            ),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Çift gitti'),
              subtitle: const Text('Ceza iki katı yazılır'),
              value: o.doubled,
              onChanged: (v) => set(o.copyWith(doubled: v)),
            ),
            const SizedBox(height: 8),
            Text('Elde kalan okey', style: theme.textTheme.labelLarge),
            const SizedBox(height: 6),
            SegmentedButton<OkeyInHandRule>(
              segments: [
                ButtonSegment(value: OkeyInHandRule.penalty, label: Text('Ceza (${o.okeyPenalty})')),
                const ButtonSegment(value: OkeyInHandRule.faceValue, label: Text('Taş değeri')),
              ],
              selected: {o.okeyInHand},
              onSelectionChanged: (s) => set(o.copyWith(okeyInHand: s.single)),
            ),
          ],
        ),
      ),
    );
  }
}

class _ScoreBar extends StatelessWidget {
  const _ScoreBar({required this.score, required this.tileCount, required this.onUse});

  final RackScore score;
  final int tileCount;
  final VoidCallback? onUse;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final details = score.usedNoOpenPenalty
        ? 'Açmama cezası'
        : [
            '$tileCount taş',
            if (score.okeyCount > 0) '${score.okeyCount} okey (+${score.okeyPoints})',
            if (score.multiplier > 1) 'x${score.multiplier}',
          ].join(' · ');
    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: Palette.boardGradient,
        border: Border(top: BorderSide(color: Palette.wood, width: 5)),
      ),
      child: ResponsiveCenter(
        maxWidth: 900,
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('${score.total}', key: const ValueKey('rack-total'), style: chalkStyle(44, height: 1.05)),
                  Text(details, style: theme.textTheme.bodySmall?.copyWith(color: Palette.chalkDim)),
                ],
              ),
            ),
            if (onUse != null)
              FilledButton(
                key: const ValueKey('rack-use'),
                style: FilledButton.styleFrom(
                  minimumSize: const Size(140, 52),
                  backgroundColor: Palette.wood,
                  foregroundColor: Colors.white,
                ),
                onPressed: onUse,
                child: const Text('Puanı Kullan'),
              ),
          ],
        ),
      ),
    );
  }
}
