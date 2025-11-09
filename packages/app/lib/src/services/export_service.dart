import 'dart:io';

import 'package:domain/domain.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:path_provider/path_provider.dart';
import 'package:pdf/widgets.dart' as pw;

class ExportService {
  _PdfFonts? _fonts;

  Future<File> exportTxt(Project project) async {
    final buffer =
        StringBuffer()
          ..writeln(project.title)
          ..writeln(project.description)
          ..writeln('=============================');
    for (final chapter in project.chapters) {
      buffer
        ..writeln('# ${chapter.title}')
        ..writeln(chapter.summary)
        ..writeln()
        ..writeln(chapter.content)
        ..writeln('---');
    }
    final file = await _createFile(project, 'txt');
    await file.writeAsString(buffer.toString());
    return file;
  }

  Future<File> exportPdf(Project project) async {
    final fonts = await _loadFonts();
    final doc = pw.Document();
    doc.addPage(
      pw.MultiPage(
        pageTheme: pw.PageTheme(
          theme: pw.ThemeData.withFont(
            base: fonts.regular,
            bold: fonts.bold,
            italic: fonts.italic,
          ),
        ),
        build:
            (context) => [
              pw.Text(
                project.title,
                style: pw.TextStyle(fontSize: 22, font: fonts.bold),
              ),
              if (project.description.isNotEmpty)
                pw.Padding(
                  padding: const pw.EdgeInsets.only(top: 4),
                  child: pw.Text(project.description),
                ),
              pw.SizedBox(height: 16),
              ..._chapterSections(project, fonts),
            ],
      ),
    );
    final file = await _createFile(project, 'pdf');
    await file.writeAsBytes(await doc.save());
    return file;
  }

  Future<File> _createFile(Project project, String extension) async {
    final directory = await getApplicationDocumentsDirectory();
    final sanitized = project.title.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');
    return File('${directory.path}/storyflame_${sanitized}_export.$extension');
  }

  Future<_PdfFonts> _loadFonts() async {
    final cached = _fonts;
    if (cached != null) return cached;
    final regularData = await rootBundle.load('assets/fonts/Roboto-Regular.ttf');
    final mediumData = await rootBundle.load('assets/fonts/Roboto-Medium.ttf');
    final italicData = await rootBundle.load('assets/fonts/Roboto-Italic.ttf');
    final fonts = _PdfFonts(
      regular: pw.Font.ttf(regularData),
      bold: pw.Font.ttf(mediumData),
      italic: pw.Font.ttf(italicData),
    );
    _fonts = fonts;
    return fonts;
  }

  List<pw.Widget> _chapterSections(Project project, _PdfFonts fonts) {
    return project.chapters
        .expand((chapter) sync* {
          yield pw.Header(
            level: 1,
            child: pw.Text(
              chapter.title,
              style: pw.TextStyle(fontSize: 18, font: fonts.bold),
            ),
          );
          if (chapter.summary.isNotEmpty) {
            yield pw.Paragraph(
              text: chapter.summary,
              style: pw.TextStyle(font: fonts.italic),
            );
          }
          for (final paragraph in _splitParagraphs(chapter.content)) {
            yield pw.Paragraph(text: paragraph);
          }
          yield pw.Divider();
        })
        .toList(growable: false);
  }

  Iterable<String> _splitParagraphs(String content) sync* {
    final parts = content.split(RegExp(r'\n{2,}'));
    for (final part in parts) {
      final trimmed = part.trim();
      if (trimmed.isEmpty) continue;
      yield trimmed;
    }
  }
}

class _PdfFonts {
  const _PdfFonts({
    required this.regular,
    required this.bold,
    required this.italic,
  });

  final pw.Font regular;
  final pw.Font bold;
  final pw.Font italic;
}
