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
/// Taşlar fotoğraftan okunur ya da alttaki taş klavyesiyle tek dokunuşla
/// eklenir; puan dökümü en üstte anlık güncellenir.
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

  /// Taş klavyesinin eklediği sıra.
  int _activeRow = 0;
  TileColor _padColor = TileColor.red;

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

  void _addNumber(int number) => _rack.add(_activeRow, Tile(id: _rack.newTileId(), color: _padColor, number: number));

  void _addFakeOkey() => _rack.add(_activeRow, Tile.fakeOkey(id: _rack.newTileId()));

  void _undo() {
    final row = _rack.rows[_activeRow];
    if (row.isNotEmpty) _rack.remove(_activeRow, row.length - 1);
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
    final expanded = WindowSize.of(context).isExpanded;
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
          builder: (context, _) {
            final scoreBoard = _ScoreBoard(score: _rack.score, tileCount: _rack.tileCount, options: _rack.options);
            final scan = canScan ? _ScanButtons(scanning: _scanning, onScan: _scan) : const _ManualHint();
            final rack = _RackBoard(
              controller: _rack,
              activeRow: _activeRow,
              onRowTap: (row) => setState(() => _activeRow = row),
              onTileTap: _editTile,
            );
            final pad = _TilePad(
              color: _padColor,
              activeRow: _activeRow,
              canUndo: _rack.rows[_activeRow].isNotEmpty,
              onColor: (c) => setState(() => _padColor = c),
              onRow: (row) => setState(() => _activeRow = row),
              onNumber: _addNumber,
              onFakeOkey: _addFakeOkey,
              onUndo: _undo,
            );
            final options = _OptionsCard(controller: _rack, onPickIndicator: _pickIndicator);
            const gap = SizedBox(height: 14);

            final Widget content;
            if (expanded) {
              content = Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: ListView(
                      padding: const EdgeInsets.fromLTRB(20, 12, 10, 20),
                      children: [scan, gap, rack, gap, pad],
                    ),
                  ),
                  SizedBox(
                    width: 380,
                    child: ListView(
                      padding: const EdgeInsets.fromLTRB(10, 12, 20, 20),
                      children: [
                        scoreBoard,
                        gap,
                        options,
                        if (playerName != null) ...[gap, _UseButton(total: _rack.score.total, onUse: _useScore)],
                      ],
                    ),
                  ),
                ],
              );
            } else {
              content = Column(
                children: [
                  Expanded(
                    child: ListView(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      children: [
                        ResponsiveCenter(
                          maxWidth: 760,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [scoreBoard, gap, scan, gap, rack, gap, pad, gap, options],
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (playerName != null)
                    Material(
                      color: Theme.of(context).colorScheme.surfaceContainerHigh,
                      child: ResponsiveCenter(
                        maxWidth: 760,
                        padding: const EdgeInsets.fromLTRB(16, 10, 16, 12),
                        child: _UseButton(total: _rack.score.total, onUse: _useScore),
                      ),
                    ),
                ],
              );
            }
            return content;
          },
        ),
      ),
    );
  }
}

/// Toplam ve nasıl hesaplandığı, tahtaya yazılmış gibi.
class _ScoreBoard extends StatelessWidget {
  const _ScoreBoard({required this.score, required this.tileCount, required this.options});

  final RackScore score;
  final int tileCount;
  final RackScoreOptions options;

  @override
  Widget build(BuildContext context) {
    final lines = <(String, String)>[
      if (score.usedNoOpenPenalty)
        ('Açmama cezası', '${options.noOpenPenalty}')
      else ...[
        ('$tileCount taş', '${score.tileSum}'),
        if (score.okeyCount > 0) ('${score.okeyCount} okey', '+${score.okeyPoints}'),
      ],
      if (score.multiplier > 1) ('Çift gitti', '×${score.multiplier}'),
    ];
    return Chalkboard(
      padding: const EdgeInsets.fromLTRB(18, 14, 18, 14),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('El puanı', style: Theme.of(context).textTheme.labelLarge?.copyWith(color: Palette.chalkDim)),
              Text('${score.total}', key: const ValueKey('rack-total'), style: displayStyle(44, height: 1.1)),
            ],
          ),
          const SizedBox(width: 16),
          Container(width: 1.5, height: 64, color: Palette.chalk.withValues(alpha: 0.25)),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                for (final (label, value) in lines)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 2),
                    child: Row(
                      children: [
                        Expanded(
                          child: Text(label, style: const TextStyle(color: Palette.chalkDim, fontSize: 14)),
                        ),
                        Text(value, style: displayStyle(16, color: Palette.chalk)),
                      ],
                    ),
                  ),
              ],
            ),
          ),
        ],
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
          padding: EdgeInsets.all(18),
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
          flex: 3,
          child: FilledButton.icon(
            key: const ValueKey('scan-camera'),
            style: FilledButton.styleFrom(backgroundColor: Palette.wood, foregroundColor: Colors.white),
            onPressed: () => onScan(ImageSource.camera),
            icon: const Icon(Icons.photo_camera),
            label: const Text('Istakayı Çek'),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          flex: 2,
          child: OutlinedButton.icon(
            onPressed: () => onScan(ImageSource.gallery),
            icon: const Icon(Icons.photo_library_outlined),
            label: const Text('Galeri'),
          ),
        ),
      ],
    );
  }
}

class _ManualHint extends StatelessWidget {
  const _ManualHint();

  @override
  Widget build(BuildContext context) => Row(
    children: [
      Icon(Icons.info_outline, size: 18, color: Theme.of(context).colorScheme.onSurfaceVariant),
      const SizedBox(width: 8),
      Expanded(
        child: Text(
          'Fotoğrafla okuma telefonda çalışır. Taşları aşağıdaki klavyeyle ekleyin.',
          style: TextStyle(color: Theme.of(context).colorScheme.onSurfaceVariant, fontSize: 13),
        ),
      ),
    ],
  );
}

/// Ahşap ıstaka: iki sıra, sürükle-bırak ile sıralanır. Seçili sıra taş
/// klavyesinin eklediği sıradır.
class _RackBoard extends StatelessWidget {
  const _RackBoard({
    required this.controller,
    required this.activeRow,
    required this.onRowTap,
    required this.onTileTap,
  });

  final RackController controller;
  final int activeRow;
  final void Function(int row) onRowTap;
  final void Function(int row, int index) onTileTap;

  @override
  Widget build(BuildContext context) {
    final indicator = controller.options.indicator;
    return LayoutBuilder(
      builder: (context, constraints) {
        // Geniş ekranda 15 taş sığar; telefonda taşlar okunur boyutta kalır,
        // sığmayanlar yana kaydırılır.
        final tileWidth = ((constraints.maxWidth - 36) / 15 - 4).clamp(32.0, 50.0);
        final rowHeight = tileWidth * 1.42 + 14;
        return Container(
          padding: const EdgeInsets.fromLTRB(8, 8, 8, 12),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            gradient: const LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [Palette.woodLight, Palette.wood, Palette.woodDark],
            ),
            border: Border.all(color: Palette.woodDeep.withValues(alpha: 0.6)),
            boxShadow: const [BoxShadow(color: Color(0x40000000), blurRadius: 10, offset: Offset(0, 5))],
          ),
          child: Column(
            children: [
              for (var row = 0; row < RackController.rowCount; row++) ...[
                if (row > 0) const SizedBox(height: 8),
                _RackRow(
                  key: ValueKey('rack-row-$row'),
                  height: rowHeight,
                  active: row == activeRow,
                  label: row == 0 ? 'Üst sıra' : 'Alt sıra',
                  count: controller.rows[row].length,
                  onTap: () => onRowTap(row),
                  child: ReorderableListView.builder(
                    scrollDirection: Axis.horizontal,
                    buildDefaultDragHandles: false,
                    padding: const EdgeInsets.symmetric(horizontal: 4),
                    proxyDecorator: (child, _, _) => Material(color: Colors.transparent, child: child),
                    itemCount: controller.rows[row].length,
                    onReorderItem: (a, b) => controller.reorder(row, a, b),
                    itemBuilder: (context, index) {
                      final slot = controller.rows[row][index];
                      return ReorderableDelayedDragStartListener(
                        key: ValueKey(slot.tile.id),
                        index: index,
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 2, vertical: 5),
                          child: TileView(
                            tile: slot.tile,
                            width: tileWidth,
                            isOkey: RackScoring.isOkey(slot.tile, indicator),
                            // Elle girişte her taş "düzeltme" sayılır; işaret yalnız okunan elde anlamlı.
                            highlight: controller.usedCamera && slot.isCorrection,
                            onTap: () => onTileTap(row, index),
                          ),
                        ),
                      );
                    },
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

class _RackRow extends StatelessWidget {
  const _RackRow({
    super.key,
    required this.height,
    required this.active,
    required this.label,
    required this.count,
    required this.onTap,
    required this.child,
  });

  final double height;
  final bool active;
  final String label;
  final int count;
  final VoidCallback onTap;
  final Widget child;

  @override
  Widget build(BuildContext context) => GestureDetector(
    behavior: HitTestBehavior.opaque,
    onTap: onTap,
    child: Semantics(
      selected: active,
      label: '$label, $count taş',
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        height: height,
        decoration: BoxDecoration(
          color: Palette.woodDeep.withValues(alpha: active ? 0.35 : 0.2),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: active ? Palette.chalkYellow : Colors.transparent, width: 2),
        ),
        child: count == 0
            ? Center(
                child: Text(
                  active ? '$label · klavyeden taş ekleyin' : '$label · seçmek için dokunun',
                  style: const TextStyle(color: Color(0xFFFFE6CC), fontWeight: FontWeight.w600),
                ),
              )
            : child,
      ),
    ),
  );
}

/// Tek dokunuşla taş ekleme: renk seç, sayıya dokun.
class _TilePad extends StatelessWidget {
  const _TilePad({
    required this.color,
    required this.activeRow,
    required this.canUndo,
    required this.onColor,
    required this.onRow,
    required this.onNumber,
    required this.onFakeOkey,
    required this.onUndo,
  });

  final TileColor color;
  final int activeRow;
  final bool canUndo;
  final ValueChanged<TileColor> onColor;
  final ValueChanged<int> onRow;
  final ValueChanged<int> onNumber;
  final VoidCallback onFakeOkey;
  final VoidCallback onUndo;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(14, 12, 14, 14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text('Taş ekle', style: displayStyle(18, color: theme.colorScheme.onSurface)),
                ),
                SegmentedButton<int>(
                  key: const ValueKey('pad-row'),
                  showSelectedIcon: false,
                  style: const ButtonStyle(visualDensity: VisualDensity.compact),
                  segments: const [
                    ButtonSegment(value: 0, label: Text('Üst')),
                    ButtonSegment(value: 1, label: Text('Alt')),
                  ],
                  selected: {activeRow},
                  onSelectionChanged: (s) => onRow(s.single),
                ),
                const SizedBox(width: 4),
                IconButton(
                  key: const ValueKey('pad-undo'),
                  tooltip: 'Son taşı sil',
                  onPressed: canUndo ? onUndo : null,
                  icon: const Icon(Icons.backspace_outlined),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                for (final (i, c) in TileColor.values.indexed) ...[
                  if (i > 0) const SizedBox(width: 8),
                  Expanded(
                    child: _ColorButton(color: c, selected: c == color, onTap: () => onColor(c)),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 12),
            LayoutBuilder(
              builder: (context, constraints) {
                const columns = 7;
                const spacing = 6.0;
                final width = (constraints.maxWidth - spacing * (columns - 1)) / columns;
                return Wrap(
                  spacing: spacing,
                  runSpacing: spacing,
                  children: [
                    for (var n = 1; n <= 13; n++)
                      _PadKey(
                        key: ValueKey('pad-number-$n'),
                        width: width,
                        label: '$n',
                        semantics: '${color.label} $n',
                        color: AppTheme.tileColor(color),
                        onTap: () => onNumber(n),
                      ),
                    _PadKey(
                      key: const ValueKey('pad-fake'),
                      width: width,
                      icon: Icons.star_rounded,
                      semantics: 'Sahte okey',
                      color: const Color(0xFF2E7D32),
                      onTap: onFakeOkey,
                    ),
                  ],
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _ColorButton extends StatelessWidget {
  const _ColorButton({required this.color, required this.selected, required this.onTap});

  final TileColor color;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final c = AppTheme.tileColor(color);
    return Semantics(
      button: true,
      selected: selected,
      child: Material(
        color: selected ? c : c.withValues(alpha: 0.1),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: BorderSide(color: c, width: 1.5),
        ),
        child: InkWell(
          key: ValueKey('pad-color-${color.name}'),
          borderRadius: BorderRadius.circular(12),
          onTap: onTap,
          child: SizedBox(
            height: 40,
            child: Center(
              child: FittedBox(
                fit: BoxFit.scaleDown,
                child: Text(
                  color.label,
                  style: TextStyle(color: selected ? Colors.white : c, fontWeight: FontWeight.w800, fontSize: 14),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _PadKey extends StatelessWidget {
  const _PadKey({
    super.key,
    required this.width,
    required this.color,
    required this.semantics,
    required this.onTap,
    this.label,
    this.icon,
  });

  final double width;
  final Color color;
  final String semantics;
  final VoidCallback onTap;
  final String? label;
  final IconData? icon;

  @override
  Widget build(BuildContext context) => Semantics(
    button: true,
    label: semantics,
    excludeSemantics: true,
    child: Material(
      color: Palette.tileFace,
      elevation: 1.5,
      shadowColor: const Color(0x66000000),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: const BorderSide(color: Color(0xFFE2D6BE)),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(10),
        onTap: onTap,
        child: SizedBox(
          width: width,
          height: (width * 1.15).clamp(44, 64),
          child: Center(
            child: label != null
                ? Text(
                    label!,
                    style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: (width * 0.42).clamp(16, 24)),
                  )
                : Icon(icon, color: color, size: (width * 0.5).clamp(20, 28)),
          ),
        ),
      ),
    ),
  );
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
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('El ayarları', style: displayStyle(18, color: theme.colorScheme.onSurface)),
            const SizedBox(height: 12),
            InkWell(
              borderRadius: BorderRadius.circular(12),
              onTap: onPickIndicator,
              child: Row(
                children: [
                  TileView(key: const ValueKey('indicator-tile'), tile: indicator, width: 36, onTap: onPickIndicator),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Gösterge', style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                        Text(
                          okey == null
                              ? 'Okeyi işaretlemek için göstergeyi seçin.'
                              : 'Okey: ${okey.color.label} ${okey.number}',
                          style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                  Icon(Icons.chevron_right, color: theme.colorScheme.onSurfaceVariant),
                ],
              ),
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

class _UseButton extends StatelessWidget {
  const _UseButton({required this.total, required this.onUse});

  final int total;
  final VoidCallback onUse;

  @override
  Widget build(BuildContext context) => FilledButton.icon(
    key: const ValueKey('rack-use'),
    onPressed: onUse,
    icon: const Icon(Icons.check),
    label: Text('Puanı Kullan · $total'),
  );
}
