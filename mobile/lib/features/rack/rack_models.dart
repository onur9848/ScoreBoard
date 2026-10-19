/// Istaka ekranına, bir oyuncunun elini saymak için verilen bağlam.
class RackRequest {
  const RackRequest({this.playerName, this.okeyPenalty = 101, this.noOpenPenalty = 202});

  /// Doluysa ekran "puanı kullan" düğmesiyle sonucu geri döndürür.
  final String? playerName;
  final int okeyPenalty;
  final int noOpenPenalty;
}

class RackResult {
  const RackResult({
    required this.points,
    required this.tileCount,
    required this.recognizedTileCount,
    required this.correctedTileCount,
    required this.usedCamera,
  });

  final int points;
  final int tileCount;
  final int recognizedTileCount;
  final int correctedTileCount;
  final bool usedCamera;
}
