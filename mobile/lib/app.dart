import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'core/theme.dart';
import 'features/board/board_screen.dart';
import 'features/history/history_screen.dart';
import 'features/home/home_screen.dart';
import 'features/rack/rack_models.dart';
import 'features/rack/rack_screen.dart';
import 'features/results/results_screen.dart';
import 'features/setup/game_type_screen.dart';
import 'features/setup/players_screen.dart';
import 'state/providers.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final analytics = ref.read(analyticsProvider);
  return GoRouter(
    observers: [_AnalyticsObserver((name) => analytics.screen(name))],
    routes: [
      GoRoute(
        path: '/',
        name: 'home',
        builder: (_, _) => const HomeScreen(),
        routes: [
          GoRoute(path: 'setup', name: 'setup', builder: (_, _) => const GameTypeScreen()),
          GoRoute(
            path: 'setup/players',
            name: 'players',
            builder: (_, state) => PlayersScreen(setup: state.extra! as GameSetup),
          ),
          GoRoute(path: 'history', name: 'history', builder: (_, _) => const HistoryScreen()),
          GoRoute(
            path: 'rack',
            name: 'rack',
            builder: (_, state) => RackScreen(request: state.extra as RackRequest?),
          ),
          GoRoute(
            path: 'game/:id',
            name: 'board',
            builder: (_, state) => BoardScreen(gameId: state.pathParameters['id']!),
            routes: [
              GoRoute(
                path: 'results',
                name: 'results',
                builder: (_, state) => ResultsScreen(gameId: state.pathParameters['id']!),
              ),
            ],
          ),
        ],
      ),
    ],
  );
});

class PuanTablosuApp extends ConsumerWidget {
  const PuanTablosuApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) => MaterialApp.router(
    title: 'Puan Tablosu',
    debugShowCheckedModeBanner: false,
    theme: AppTheme.light(),
    darkTheme: AppTheme.dark(),
    locale: const Locale('tr', 'TR'),
    supportedLocales: const [Locale('tr', 'TR')],
    localizationsDelegates: GlobalMaterialLocalizations.delegates,
    routerConfig: ref.watch(routerProvider),
  );
}

class _AnalyticsObserver extends NavigatorObserver {
  _AnalyticsObserver(this.onScreen);

  final void Function(String name) onScreen;

  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    final name = route.settings.name;
    if (name != null) onScreen(name);
  }
}
