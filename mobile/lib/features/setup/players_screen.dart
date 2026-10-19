import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/chalk.dart';
import '../../core/responsive.dart';
import '../../core/theme.dart';
import '../../state/providers.dart';
import 'game_type_screen.dart';

const _seatColors = [Palette.tileRed, Palette.tileBlue, Palette.tileYellow, Palette.tileBlack];

class PlayersScreen extends ConsumerStatefulWidget {
  const PlayersScreen({super.key, required this.setup});

  final GameSetup setup;

  static const maxPlayers = 6;

  @override
  ConsumerState<PlayersScreen> createState() => _PlayersScreenState();
}

class _PlayersScreenState extends ConsumerState<PlayersScreen> {
  final _formKey = GlobalKey<FormState>();
  final _title = TextEditingController();
  late final List<TextEditingController> _players = List.generate(
    widget.setup.isPartnered ? 2 : 4,
    (_) => TextEditingController(),
  );

  String get _seatLabel => widget.setup.isPartnered ? 'Takım' : 'Oyuncu';

  @override
  void dispose() {
    _title.dispose();
    for (final c in _players) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _start() async {
    if (!_formKey.currentState!.validate()) return;
    final setup = widget.setup;
    final game = ref
        .read(gameEngineProvider)
        .createGame(
          title: _title.text,
          type: setup.type,
          playerNames: [for (final c in _players) c.text],
          isPartnered: setup.isPartnered,
          ruleValues: setup.ruleValues,
          winCondition: setup.winCondition,
        );
    await ref.read(gamesProvider.notifier).save(game);
    ref.read(analyticsProvider).event('game_created', {
      'game_type': setup.type.wireName,
      'player_count': game.players.length,
      'customized_rule_count': game.settings.customizedRuleKeys.length,
    });
    if (mounted) context.goNamed('board', pathParameters: {'id': game.id});
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(widget.setup.type.label)),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  children: [
                    ResponsiveCenter(
                      child: Card(
                        child: Padding(
                          padding: const EdgeInsets.all(20),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              TextFormField(
                                key: const ValueKey('game-title'),
                                controller: _title,
                                textCapitalization: TextCapitalization.sentences,
                                textInputAction: TextInputAction.next,
                                decoration: const InputDecoration(labelText: 'Oyun Başlığı'),
                                validator: (v) => (v ?? '').trim().isEmpty ? 'Oyun başlığı boş bırakılamaz.' : null,
                              ),
                              const SizedBox(height: 20),
                              Text(
                                widget.setup.isPartnered ? 'Takımlar' : 'Oyuncular',
                                style: displayStyle(20, color: theme.colorScheme.onSurface, height: 1.1),
                              ),
                              const SizedBox(height: 8),
                              for (var i = 0; i < _players.length; i++)
                                Padding(
                                  padding: const EdgeInsets.only(bottom: 8),
                                  child: Row(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Padding(
                                        padding: const EdgeInsets.only(top: 6, right: 12),
                                        child: TileBadge(
                                          label: '${i + 1}',
                                          size: 36,
                                          color: _seatColors[i % _seatColors.length],
                                        ),
                                      ),
                                      Expanded(
                                        child: TextFormField(
                                          key: ValueKey('player-$i'),
                                          controller: _players[i],
                                          textCapitalization: TextCapitalization.words,
                                          textInputAction: i == _players.length - 1
                                              ? TextInputAction.done
                                              : TextInputAction.next,
                                          decoration: InputDecoration(labelText: '$_seatLabel ${i + 1}'),
                                          validator: (v) => (v ?? '').trim().isEmpty ? 'İsim boş bırakılamaz.' : null,
                                        ),
                                      ),
                                      IconButton(
                                        tooltip: 'Sil',
                                        padding: const EdgeInsets.only(top: 8),
                                        color: theme.colorScheme.error,
                                        onPressed: _players.length > 2
                                            ? () => setState(() => _players.removeAt(i).dispose())
                                            : null,
                                        icon: const Icon(Icons.delete_outline),
                                      ),
                                    ],
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              ResponsiveCenter(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
                child: Column(
                  children: [
                    OutlinedButton.icon(
                      onPressed: _players.length < PlayersScreen.maxPlayers
                          ? () => setState(() => _players.add(TextEditingController()))
                          : null,
                      icon: const Icon(Icons.add),
                      label: Text('$_seatLabel Ekle'),
                    ),
                    const SizedBox(height: 12),
                    FilledButton(onPressed: _start, child: const Text('Başla')),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
