/// Oyun günlüğündeki olay türleri. [wireName] backend ve panel ile ortaktır.
enum GameEventType {
  gameCreated('game_created'),
  roundAdded('round_added'),
  roundEdited('round_edited'),
  roundDeleted('round_deleted'),
  ruleApplied('rule_applied'),
  rackScanUsed('rack_scan_used'),
  gameFinished('game_finished'),
  gameReopened('game_reopened'),
  gameResumed('game_resumed');

  const GameEventType(this.wireName);

  final String wireName;

  static GameEventType? fromWire(String? value) {
    for (final t in GameEventType.values) {
      if (t.wireName == value) return t;
    }
    return null;
  }
}

/// Analytics'ten bağımsız, oyunun kendisine ait olay kaydı.
class GameEvent {
  const GameEvent({required this.id, required this.type, required this.occurredAt, this.data = const {}});

  final String id;

  /// Bilinmeyen (ileride eklenmiş) türler okunurken atılmaz; [rawType] korunur.
  final GameEventType? type;
  final DateTime occurredAt;
  final Map<String, Object?> data;

  String get rawType => (data['_raw_type'] as String?) ?? type!.wireName;

  Map<String, dynamic> toJson() => {
    'id': id,
    'type': type?.wireName ?? rawType,
    'occurred_at': occurredAt.toUtc().toIso8601String(),
    'data': Map.of(data)..remove('_raw_type'),
  };

  factory GameEvent.fromJson(Map<String, dynamic> json) {
    final raw = json['type'] as String?;
    final type = GameEventType.fromWire(raw);
    final data = Map<String, Object?>.from(json['data'] as Map? ?? const {});
    if (type == null) data['_raw_type'] = raw;
    return GameEvent(
      id: json['id'] as String,
      type: type,
      occurredAt: DateTime.parse(json['occurred_at'] as String),
      data: data,
    );
  }
}
