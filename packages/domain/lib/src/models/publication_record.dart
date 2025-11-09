import 'package:meta/meta.dart';

enum PublicationChannel { epub, mobi, kdp, wattpad, bundle }

@immutable
class PublicationRecord {
  const PublicationRecord({
    required this.id,
    required this.channel,
    required this.filePath,
    required this.notes,
    required this.createdAt,
  });

  final String id;
  final PublicationChannel channel;
  final String filePath;
  final String notes;
  final DateTime createdAt;

  factory PublicationRecord.fromJson(Map<String, dynamic> json) =>
      PublicationRecord(
        id: json['id'] as String? ?? '',
        channel: PublicationChannel.values.firstWhere(
          (value) => value.name == (json['channel'] as String? ?? 'epub'),
          orElse: () => PublicationChannel.epub,
        ),
        filePath: json['filePath'] as String? ?? '',
        notes: json['notes'] as String? ?? '',
        createdAt:
            DateTime.tryParse(json['createdAt'] as String? ?? '') ??
            DateTime.now(),
      );

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id': id,
    'channel': channel.name,
    'filePath': filePath,
    'notes': notes,
    'createdAt': createdAt.toIso8601String(),
  };
}
