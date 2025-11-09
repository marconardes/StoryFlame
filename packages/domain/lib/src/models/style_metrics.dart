import 'package:meta/meta.dart';

@immutable
class StyleMetrics {
  const StyleMetrics({
    required this.averageSentenceLength,
    required this.dialogueDensity,
    required this.descriptionRatio,
    required this.paceScore,
    required this.emotionalVariance,
  });

  final double averageSentenceLength;
  final double dialogueDensity;
  final double descriptionRatio;
  final double paceScore;
  final double emotionalVariance;

  factory StyleMetrics.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      return const StyleMetrics.zero();
    }
    return StyleMetrics(
      averageSentenceLength:
          (json['averageSentenceLength'] as num?)?.toDouble() ?? 0,
      dialogueDensity: (json['dialogueDensity'] as num?)?.toDouble() ?? 0,
      descriptionRatio: (json['descriptionRatio'] as num?)?.toDouble() ?? 0,
      paceScore: (json['paceScore'] as num?)?.toDouble() ?? 0,
      emotionalVariance: (json['emotionalVariance'] as num?)?.toDouble() ?? 0,
    );
  }

  const StyleMetrics.zero()
    : averageSentenceLength = 0,
      dialogueDensity = 0,
      descriptionRatio = 0,
      paceScore = 0,
      emotionalVariance = 0;

  Map<String, dynamic> toJson() => <String, dynamic>{
    'averageSentenceLength': averageSentenceLength,
    'dialogueDensity': dialogueDensity,
    'descriptionRatio': descriptionRatio,
    'paceScore': paceScore,
    'emotionalVariance': emotionalVariance,
  };

  StyleMetrics copyWith({
    double? averageSentenceLength,
    double? dialogueDensity,
    double? descriptionRatio,
    double? paceScore,
    double? emotionalVariance,
  }) {
    return StyleMetrics(
      averageSentenceLength:
          averageSentenceLength ?? this.averageSentenceLength,
      dialogueDensity: dialogueDensity ?? this.dialogueDensity,
      descriptionRatio: descriptionRatio ?? this.descriptionRatio,
      paceScore: paceScore ?? this.paceScore,
      emotionalVariance: emotionalVariance ?? this.emotionalVariance,
    );
  }
}
