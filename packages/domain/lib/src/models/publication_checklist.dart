import 'package:meta/meta.dart';

@immutable
class PublicationChecklist {
  const PublicationChecklist({
    required this.betaRead,
    required this.coverReady,
    required this.isbnRegistered,
    required this.ebookExported,
    required this.kdpPackageReady,
  });

  final bool betaRead;
  final bool coverReady;
  final bool isbnRegistered;
  final bool ebookExported;
  final bool kdpPackageReady;

  factory PublicationChecklist.fromJson(Map<String, dynamic>? json) {
    if (json == null) {
      return const PublicationChecklist(
        betaRead: false,
        coverReady: false,
        isbnRegistered: false,
        ebookExported: false,
        kdpPackageReady: false,
      );
    }
    return PublicationChecklist(
      betaRead: json['betaRead'] as bool? ?? false,
      coverReady: json['coverReady'] as bool? ?? false,
      isbnRegistered: json['isbnRegistered'] as bool? ?? false,
      ebookExported: json['ebookExported'] as bool? ?? false,
      kdpPackageReady: json['kdpPackageReady'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
    'betaRead': betaRead,
    'coverReady': coverReady,
    'isbnRegistered': isbnRegistered,
    'ebookExported': ebookExported,
    'kdpPackageReady': kdpPackageReady,
  };

  PublicationChecklist copyWith({
    bool? betaRead,
    bool? coverReady,
    bool? isbnRegistered,
    bool? ebookExported,
    bool? kdpPackageReady,
  }) {
    return PublicationChecklist(
      betaRead: betaRead ?? this.betaRead,
      coverReady: coverReady ?? this.coverReady,
      isbnRegistered: isbnRegistered ?? this.isbnRegistered,
      ebookExported: ebookExported ?? this.ebookExported,
      kdpPackageReady: kdpPackageReady ?? this.kdpPackageReady,
    );
  }
}
