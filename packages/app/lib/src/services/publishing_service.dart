import 'dart:convert';
import 'dart:io';

import 'package:archive/archive.dart';
import 'package:domain/domain.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import 'export_service.dart';

class PublishingService {
  PublishingService({ExportService? exportService})
    : _exportService = exportService ?? ExportService();

  final ExportService _exportService;

  Future<File> exportEpub(Project project) async {
    final docsDir = await getApplicationDocumentsDirectory();
    final sanitized = _sanitize(project.title);
    final targetFile = File(p.join(docsDir.path, '$sanitized.epub'));

    final archive = Archive();
    final mimeBytes = utf8.encode('application/epub+zip');
    archive.addFile(
      ArchiveFile.noCompress('mimetype', mimeBytes.length, mimeBytes),
    );

    final containerBytes = utf8.encode(_containerXml());
    archive.addFile(
      ArchiveFile(
        'META-INF/container.xml',
        containerBytes.length,
        containerBytes,
      ),
    );

    final chapters = <String>[];
    for (var i = 0; i < project.chapters.length; i++) {
      final chapter = project.chapters[i];
      final filename = 'OEBPS/chapter_$i.xhtml';
      chapters.add(filename);
      final content = _chapterXhtml(chapter.title, chapter.content);
      final bytes = utf8.encode(content);
      archive.addFile(ArchiveFile(filename, bytes.length, bytes));
    }

    final nav = _navDocument(project.title, chapters, project.chapters);
    final navBytes = utf8.encode(nav);
    archive.addFile(ArchiveFile('OEBPS/nav.xhtml', navBytes.length, navBytes));

    final opf = _contentOpf(project.title, chapters);
    final opfBytes = utf8.encode(opf);
    archive.addFile(
      ArchiveFile('OEBPS/content.opf', opfBytes.length, opfBytes),
    );

    final bytes = ZipEncoder().encode(archive);
    if (bytes == null) {
      throw StateError('Falha ao gerar arquivo EPUB');
    }
    await targetFile.writeAsBytes(bytes);
    return targetFile;
  }

  Future<File> exportBundle(Project project) async {
    final docsDir = await getApplicationDocumentsDirectory();
    final sanitized = _sanitize(project.title);
    final bundlePath = p.join(docsDir.path, '${sanitized}_bundle.zip');
    final archive = Archive();

    final pdfFile = await _exportService.exportPdf(project);
    final txtFile = await _exportService.exportTxt(project);
    final jsonFile = File(p.join(docsDir.path, '$sanitized.json'))
      ..writeAsStringSync(jsonEncode(project.toJson()));

    for (final file in [pdfFile, txtFile, jsonFile]) {
      final data = await file.readAsBytes();
      archive.addFile(ArchiveFile(p.basename(file.path), data.length, data));
    }

    final bytes = ZipEncoder().encode(archive);
    if (bytes == null) {
      throw StateError('Falha ao gerar pacote de publicação');
    }
    final targetFile = File(bundlePath);
    await targetFile.writeAsBytes(bytes);
    return targetFile;
  }

  Future<File> createKdpPackage(
    Project project,
    PublicationMetadata metadata,
  ) async {
    final docsDir = await getApplicationDocumentsDirectory();
    final sanitized = _sanitize(project.title);
    final file = File(p.join(docsDir.path, '${sanitized}_kdp.json'));
    final payload = {
      'title': project.title,
      'synopsis': metadata.synopsis,
      'categories': metadata.categories,
      'keywords': metadata.keywords,
      'audience': metadata.audience,
      'chapters': project.chapters.map((c) => c.title).toList(),
    };
    await file.writeAsString(
      const JsonEncoder.withIndent('  ').convert(payload),
    );
    return file;
  }

  String _sanitize(String value) =>
      value.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');

  String _containerXml() => '''<?xml version="1.0"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml" />
  </rootfiles>
</container>
''';

  String _chapterXhtml(String title, String rawContent) {
    final body = rawContent
        .split(RegExp(r'\n{2,}'))
        .map((paragraph) => '<p>${_escape(paragraph)}</p>')
        .join('\n');
    return '''<?xml version="1.0" encoding="utf-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>${_escape(title)}</title>
  </head>
  <body>
    <h1>${_escape(title)}</h1>
    $body
  </body>
</html>
''';
  }

  String _contentOpf(String title, List<String> chapterFiles) {
    final manifestItems = <String>[
      '<item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>',
      for (var i = 0; i < chapterFiles.length; i++)
        '<item id="ch$i" href="chapter_$i.xhtml" media-type="application/xhtml+xml"/>',
    ].join('\n    ');
    final spineItems = [
      for (var i = 0; i < chapterFiles.length; i++) '<itemref idref="ch$i"/>',
    ].join('\n      ');
    return '''<?xml version="1.0" encoding="utf-8"?>
<package version="3.0" unique-identifier="pub-id" xmlns="http://www.idpf.org/2007/opf">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:title>${_escape(title)}</dc:title>
    <dc:language>pt-BR</dc:language>
  </metadata>
  <manifest>
    $manifestItems
  </manifest>
  <spine>
      $spineItems
  </spine>
</package>
''';
  }

  String _navDocument(
    String title,
    List<String> chapterFiles,
    List<Chapter> chapters,
  ) {
    final navLi = <String>[];
    for (var i = 0; i < chapterFiles.length; i++) {
      navLi.add(
        '<li><a href="chapter_$i.xhtml">${_escape(chapters[i].title)}</a></li>',
      );
    }
    final navList = navLi.join('\n        ');
    return '''<?xml version="1.0" encoding="utf-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>${_escape(title)} - Sumário</title>
  </head>
  <body>
    <nav epub:type="toc" id="toc">
      <h1>Sumário</h1>
      <ol>
        $navList
      </ol>
    </nav>
  </body>
</html>
''';
  }

  String _escape(String input) => htmlEscape.convert(input);
}

class PublicationMetadata {
  const PublicationMetadata({
    required this.synopsis,
    required this.categories,
    required this.keywords,
    required this.audience,
  });

  final String synopsis;
  final List<String> categories;
  final List<String> keywords;
  final String audience;
}
