import 'package:flutter/material.dart';

import '../../core/theme.dart';
import '../../domain/tiles/tile.dart';

enum TileEditAction { save, delete, moveRow }

class TileEditResult {
  const TileEditResult(this.action, [this.tile]);

  final TileEditAction action;
  final Tile? tile;
}

/// Taş seçici: renk + sayı ya da sahte okey.
Future<TileEditResult?> showTileEditor(
  BuildContext context, {
  required String id,
  Tile? initial,
  String title = 'Taş',
  bool allowFakeOkey = true,
  bool allowDelete = false,
  bool allowMove = false,
}) => showModalBottomSheet<TileEditResult>(
  context: context,
  isScrollControlled: true,
  useSafeArea: true,
  constraints: const BoxConstraints(maxWidth: 560),
  builder: (_) => _TileEditor(
    id: id,
    initial: initial,
    title: title,
    allowFakeOkey: allowFakeOkey,
    allowDelete: allowDelete,
    allowMove: allowMove,
  ),
);

class _TileEditor extends StatefulWidget {
  const _TileEditor({
    required this.id,
    required this.initial,
    required this.title,
    required this.allowFakeOkey,
    required this.allowDelete,
    required this.allowMove,
  });

  final String id;
  final Tile? initial;
  final String title;
  final bool allowFakeOkey;
  final bool allowDelete;
  final bool allowMove;

  @override
  State<_TileEditor> createState() => _TileEditorState();
}

class _TileEditorState extends State<_TileEditor> {
  late TileColor _color = widget.initial?.color ?? TileColor.red;
  late bool _fake = widget.initial?.isFakeOkey ?? false;

  void _pick(int number) =>
      Navigator.pop(context, TileEditResult(TileEditAction.save, Tile(id: widget.id, color: _color, number: number)));

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ListView(
      shrinkWrap: true,
      padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
      children: [
        Text(widget.title, style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
        const SizedBox(height: 16),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            for (final c in TileColor.values)
              _ColorDot(
                color: c,
                selected: !_fake && c == _color,
                onTap: () => setState(() {
                  _color = c;
                  _fake = false;
                }),
              ),
          ],
        ),
        const SizedBox(height: 16),
        GridView.count(
          crossAxisCount: 7,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          mainAxisSpacing: 8,
          crossAxisSpacing: 8,
          childAspectRatio: 0.8,
          children: [
            for (var n = 1; n <= 13; n++)
              Material(
                color: const Color(0xFFFFF8E7),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                  side: BorderSide(
                    color: !_fake && widget.initial?.number == n && widget.initial?.color == _color
                        ? theme.colorScheme.secondary
                        : Colors.brown.shade200,
                    width: 2,
                  ),
                ),
                child: InkWell(
                  key: ValueKey('tile-number-$n'),
                  borderRadius: BorderRadius.circular(8),
                  onTap: () => _pick(n),
                  child: Center(
                    child: Text(
                      '$n',
                      style: TextStyle(color: AppTheme.tileColor(_color), fontWeight: FontWeight.w800, fontSize: 20),
                    ),
                  ),
                ),
              ),
            if (widget.allowFakeOkey)
              Material(
                color: const Color(0xFFFFF8E7),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                  side: BorderSide(color: _fake ? theme.colorScheme.secondary : Colors.brown.shade200, width: 2),
                ),
                child: InkWell(
                  key: const ValueKey('tile-fake-okey'),
                  borderRadius: BorderRadius.circular(8),
                  onTap: () =>
                      Navigator.pop(context, TileEditResult(TileEditAction.save, Tile.fakeOkey(id: widget.id))),
                  child: const Tooltip(
                    message: 'Sahte okey',
                    child: Icon(Icons.star_rounded, color: Color(0xFF2E7D32)),
                  ),
                ),
              ),
          ],
        ),
        if (widget.allowDelete || widget.allowMove) ...[
          const SizedBox(height: 16),
          Row(
            children: [
              if (widget.allowMove)
                TextButton.icon(
                  onPressed: () => Navigator.pop(context, const TileEditResult(TileEditAction.moveRow)),
                  icon: const Icon(Icons.swap_vert),
                  label: const Text('Diğer sıraya taşı'),
                ),
              const Spacer(),
              if (widget.allowDelete)
                TextButton.icon(
                  style: TextButton.styleFrom(foregroundColor: theme.colorScheme.error),
                  onPressed: () => Navigator.pop(context, const TileEditResult(TileEditAction.delete)),
                  icon: const Icon(Icons.delete_outline),
                  label: const Text('Sil'),
                ),
            ],
          ),
        ],
      ],
    );
  }
}

class _ColorDot extends StatelessWidget {
  const _ColorDot({required this.color, required this.selected, required this.onTap});

  final TileColor color;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Semantics(
    button: true,
    selected: selected,
    label: color.label,
    child: InkWell(
      key: ValueKey('tile-color-${color.name}'),
      customBorder: const CircleBorder(),
      onTap: onTap,
      child: Column(
        children: [
          AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppTheme.tileColor(color),
              shape: BoxShape.circle,
              border: Border.all(
                color: selected ? Theme.of(context).colorScheme.secondary : Colors.transparent,
                width: 3,
              ),
            ),
            child: selected ? const Icon(Icons.check, color: Colors.white) : null,
          ),
          const SizedBox(height: 4),
          Text(color.label, style: Theme.of(context).textTheme.labelSmall),
        ],
      ),
    ),
  );
}
