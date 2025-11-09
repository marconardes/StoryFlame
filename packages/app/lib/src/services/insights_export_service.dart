import 'dart:io';

import 'package:domain/domain.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';

class InsightsExportService {
  Future<File> export(Project project, InsightsSnapshot snapshot) async {
    final buffer = StringBuffer()
      ..writeln('StoryFlame – Insights do projeto')
      ..writeln('Projeto: ${project.title}')
      ..writeln(
        'Gerado em: ${DateFormat('yyyy-MM-dd HH:mm').format(snapshot.generatedAt.toLocal())}',
      )
      ..writeln();

    buffer
      ..writeln('== Métricas de estilo ==')
      ..writeln(
        '- Palavras por sentença: '
        '${snapshot.style.averageSentenceLength.toStringAsFixed(1)}',
      )
      ..writeln(
        '- Densidade de diálogos: '
        '${(snapshot.style.dialogueDensity * 100).round()}%',
      )
      ..writeln(
        '- Razão de descrição: '
        '${(snapshot.style.descriptionRatio * 100).round()}%',
      )
      ..writeln('- Ritmo (0-1): ${snapshot.style.paceScore.toStringAsFixed(2)}')
      ..writeln(
        '- Variação emocional: '
        '${snapshot.style.emotionalVariance.toStringAsFixed(3)}',
      )
      ..writeln();

    buffer.writeln('== Insights ==');
    if (snapshot.insights.isEmpty) {
      buffer.writeln('Nenhum insight disponível.');
    } else {
      for (var i = 0; i < snapshot.insights.length; i++) {
        final insight = snapshot.insights[i];
        buffer.writeln(
          '${i + 1}. [${insight.category.label} | ${insight.severity.label}] '
          '${insight.title}',
        );
        buffer.writeln('   ${insight.description}');
      }
    }
    buffer.writeln();

    buffer.writeln('== Sugestões de prompts ==');
    if (snapshot.prompts.isEmpty) {
      buffer.writeln('Sem prompts no momento.');
    } else {
      for (final prompt in snapshot.prompts) {
        buffer
          ..writeln('- ${prompt.prompt}')
          ..writeln('  Contexto: ${prompt.context}')
          ..writeln('  Tags: ${prompt.tags.join(', ')}');
      }
    }
    buffer.writeln();

    buffer.writeln('== Extensões instaladas ==');
    if (snapshot.extensions.isEmpty) {
      buffer.writeln('Nenhuma extensão instalada.');
    } else {
      for (final extension in snapshot.extensions) {
        buffer.writeln(
          '- ${extension.name} (v${extension.version}) · '
          '${extension.author} · Regras: ${extension.rules.length}',
        );
      }
    }
    buffer.writeln();

    buffer.writeln('== Achados das extensões ==');
    if (snapshot.extensionFindings.isEmpty) {
      buffer.writeln('Nenhuma ocorrência reportada.');
    } else {
      for (final finding in snapshot.extensionFindings) {
        buffer.writeln(
          '- [${finding.extensionName}] ${finding.message} '
          '(${finding.occurrences} ocorrência(s))'
          '${finding.chapterTitle == null ? '' : ' · Capítulo: ${finding.chapterTitle}'}',
        );
      }
    }
    buffer.writeln();

    buffer.writeln('== Radar por capítulo ==');
    if (snapshot.chapters.isEmpty) {
      buffer.writeln('Nenhum capítulo analisado.');
    } else {
      for (final entry in snapshot.chapters) {
        buffer
          ..writeln('* ${entry.chapterTitle}')
          ..writeln('  - Palavras: ${entry.wordCount}')
          ..writeln(
            '  - Densidade de diálogos: '
            '${(entry.dialogueDensity * 100).round()}%',
          );
        if (entry.repeatedTerms.isNotEmpty) {
          buffer.writeln(
            '  - Termos frequentes: ${entry.repeatedTerms.join(', ')}',
          );
        }
        if (entry.characterMentions.isNotEmpty) {
          buffer.writeln(
            '  - Personagens: ${entry.characterMentions.join(', ')}',
          );
        }
        buffer
          ..writeln('  - Delta meta: ${entry.goalDelta}')
          ..writeln();
      }
    }

    final directory = await getApplicationDocumentsDirectory();
    final sanitized = project.title.replaceAll(RegExp(r'[^a-zA-Z0-9_-]'), '_');
    final file = File('${directory.path}/storyflame_${sanitized}_insights.txt');
    await file.writeAsString(buffer.toString());
    return file;
  }
}
