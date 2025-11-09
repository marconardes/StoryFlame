import 'dart:math' as math;

import 'package:domain/domain.dart';
import 'package:uuid/uuid.dart';

import 'extension_service.dart';

class InsightsService {
  InsightsService({
    DateTime Function()? clock,
    Uuid? uuid,
    ExtensionService? extensionService,
  }) : _clock = clock ?? DateTime.now,
       _uuid = uuid ?? const Uuid(),
       _extensionService = extensionService ?? ExtensionService();

  final DateTime Function() _clock;
  final Uuid _uuid;
  final ExtensionService _extensionService;

  Future<InsightsSnapshot> analyze(Project project) async {
    final generatedAt = _clock();
    final chapters = project.chapters;
    final now = generatedAt;
    var totalWords = 0;
    var totalSentences = 0;
    var totalDialogueWords = 0;
    var totalDescriptionWords = 0;
    final sentiments = <double>[];
    final chapterInsights = <ChapterInsight>[];
    final globalTokens = <String>[];

    final targetPerChapter =
        project.totalGoal > 0 && chapters.isNotEmpty
            ? (project.totalGoal / chapters.length).round()
            : 0;

    for (final chapter in chapters) {
      final content = chapter.content;
      final words =
          chapter.wordCount > 0
              ? chapter.wordCount
              : Chapter.countWords(content);
      final sentences = _splitSentences(content).length;
      final dialogueWords = _dialogueWordCount(content);
      final descriptionWords =
          dialogueWords > words ? 0 : words - dialogueWords;
      final tokens = _tokenize(content);
      final repeatedTerms = _topRepeatedTerms(tokens);
      final mentions = _characterMentions(project.characters, content);
      final sentiment = _sentimentScore(tokens);

      sentiments.add(sentiment);
      globalTokens.addAll(tokens);
      totalWords += words;
      totalSentences += math.max(1, sentences);
      totalDialogueWords += dialogueWords;
      totalDescriptionWords += descriptionWords;

      final goalDelta = targetPerChapter == 0 ? 0 : words - targetPerChapter;
      chapterInsights.add(
        ChapterInsight(
          chapterId: chapter.id,
          chapterTitle: chapter.title,
          wordCount: words,
          dialogueDensity: words == 0 ? 0 : dialogueWords / words,
          repeatedTerms: repeatedTerms,
          characterMentions: mentions,
          goalDelta: goalDelta,
        ),
      );
    }

    final double averageSentenceLength =
        totalSentences == 0 ? 0.0 : totalWords / totalSentences;
    final double dialogueDensity =
        totalWords == 0 ? 0.0 : totalDialogueWords / totalWords;
    final double descriptionRatio =
        totalWords == 0 ? 0.0 : totalDescriptionWords / totalWords;
    final double paceScore = _paceScore(totalWords, chapters.length);
    final double emotionalVariance = _variance(sentiments);

    final styleMetrics = StyleMetrics(
      averageSentenceLength: averageSentenceLength,
      dialogueDensity: dialogueDensity,
      descriptionRatio: descriptionRatio,
      paceScore: paceScore,
      emotionalVariance: emotionalVariance,
    );

    final insights = _buildInsights(
      project: project,
      style: styleMetrics,
      generatedAt: generatedAt,
      globalTokens: globalTokens,
      chapterInsights: chapterInsights,
      now: now,
    );
    final prompts = _buildPrompts(
      project: project,
      generatedAt: generatedAt,
      chapterInsights: chapterInsights,
      repeatedTerms: insights.repeatedTerms,
    );

    final extensions = await _extensionService.loadExtensions();
    final extensionFindings = await _extensionService.runAnalyzers(project);
    final extensionInsights =
        extensionFindings
            .map(
              (finding) => Insight(
                id: _uuid.v4(),
                category: InsightCategory.health,
                severity: finding.severity,
                title: finding.extensionName,
                description:
                    finding.chapterTitle == null
                        ? '${finding.message} (${finding.occurrences} ocorrências)'
                        : '${finding.message} em "${finding.chapterTitle}" '
                            '(${finding.occurrences} ocorrências)',
                contextId: finding.chapterId,
                generatedAt: generatedAt,
              ),
            )
            .toList();

    return InsightsSnapshot(
      projectId: project.id,
      generatedAt: generatedAt,
      style: styleMetrics,
      insights: <Insight>[...insights.items, ...extensionInsights],
      prompts: prompts,
      chapters: chapterInsights,
      extensions: extensions,
      extensionFindings: extensionFindings,
    );
  }

  _InsightBuildResult _buildInsights({
    required Project project,
    required StyleMetrics style,
    required DateTime generatedAt,
    required List<String> globalTokens,
    required List<ChapterInsight> chapterInsights,
    required DateTime now,
  }) {
    final insights = <Insight>[];
    final repeatedTerms = _globalRepeatedTerms(globalTokens);
    final todayWords = project.chapters
        .where((chapter) => _isSameDay(chapter.updatedAt, now))
        .fold<int>(0, (sum, chapter) => sum + chapter.wordCount);

    if (project.dailyGoal > 0 && todayWords < project.dailyGoal) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.goals,
          severity: InsightSeverity.warning,
          title: 'Meta diária em risco',
          description:
              'Foram registradas $todayWords palavras hoje. Restam '
              '${project.dailyGoal - todayWords} para alcançar a meta diária.',
          generatedAt: generatedAt,
        ),
      );
    }

    if (style.dialogueDensity > 0.65) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.pacing,
          severity: InsightSeverity.warning,
          title: 'Muito diálogo contínuo',
          description:
              'O projeto possui ${(style.dialogueDensity * 100).round()}% '
              'das palavras em diálogos. Considere intercalar descrições para '
              'equilibrar o ritmo.',
          generatedAt: generatedAt,
        ),
      );
    } else if (style.dialogueDensity < 0.25 && project.chapters.isNotEmpty) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.pacing,
          severity: InsightSeverity.info,
          title: 'Poucos diálogos detectados',
          description:
              'Os diálogos representam apenas '
              '${(style.dialogueDensity * 100).round()}% do texto. '
              'Inclua falas para aproximar os leitores dos personagens.',
          generatedAt: generatedAt,
        ),
      );
    }

    if (style.emotionalVariance < 0.02 && project.chapters.isNotEmpty) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.emotion,
          severity: InsightSeverity.info,
          title: 'Variedade emocional limitada',
          description:
              'As últimas cenas apresentam pouca oscilação de humor. '
              'Explore conflitos ou resoluções para gerar contraste.',
          generatedAt: generatedAt,
        ),
      );
    }

    if (repeatedTerms.isNotEmpty) {
      final formatted = repeatedTerms
          .take(5)
          .map((term) => '"$term"')
          .join(', ');
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.terminology,
          severity: InsightSeverity.warning,
          title: 'Termos repetidos',
          description:
              'Os termos $formatted aparecem com alta frequência. '
              'Avalie substituir ou variar essas palavras.',
          generatedAt: generatedAt,
        ),
      );
    }

    final underusedCharacters = project.characters
        .where((character) => character.chapterIds.length <= 1)
        .toList(growable: false);
    if (underusedCharacters.isNotEmpty) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.character,
          severity: InsightSeverity.info,
          title: 'Personagens subutilizados',
          description:
              underusedCharacters
                  .map((character) => character.name)
                  .take(4)
                  .join(', ') +
              ' aparecem em poucos capítulos. Planeje cenas extras ou '
                  'considere fundi-los.',
          generatedAt: generatedAt,
        ),
      );
    }

    final chaptersBelowGoal = chapterInsights
        .where((chapter) => chapter.goalDelta < -300)
        .toList(growable: false);
    if (chaptersBelowGoal.isNotEmpty) {
      insights.add(
        Insight(
          id: _uuid.v4(),
          category: InsightCategory.structure,
          severity: InsightSeverity.info,
          title: 'Capítulos curtos detectados',
          description:
              chaptersBelowGoal
                  .map((chapter) => chapter.chapterTitle)
                  .take(3)
                  .join(', ') +
              ' estão abaixo da meta média planejada. '
                  'Avalie expandir seus eventos principais.',
          generatedAt: generatedAt,
        ),
      );
    }

    return _InsightBuildResult(items: insights, repeatedTerms: repeatedTerms);
  }

  List<PromptSuggestion> _buildPrompts({
    required Project project,
    required DateTime generatedAt,
    required List<ChapterInsight> chapterInsights,
    required List<String> repeatedTerms,
  }) {
    final prompts = <PromptSuggestion>[];

    final emptySummaries = project.chapters.where(
      (chapter) => chapter.summary.trim().isEmpty,
    );
    for (final chapter in emptySummaries) {
      prompts.add(
        PromptSuggestion(
          id: _uuid.v4(),
          prompt:
              'Descreva o objetivo dramático de "${chapter.title}" em 2 frases.',
          context: 'Resumo em branco',
          tags: const ['capítulo', 'resumo'],
          score: 0.65,
          relatedChapterId: chapter.id,
          createdAt: generatedAt,
        ),
      );
    }

    for (final term in repeatedTerms.take(3)) {
      prompts.add(
        PromptSuggestion(
          id: _uuid.v4(),
          prompt: 'Liste metáforas alternativas para substituir "$term".',
          context: 'Revisão de terminologia',
          tags: const ['vocabulário', 'edição'],
          score: 0.5,
          createdAt: generatedAt,
        ),
      );
    }

    final idleCharacters = project.characters
        .where((character) => character.chapterIds.isEmpty)
        .toList(growable: false);
    for (final character in idleCharacters) {
      prompts.add(
        PromptSuggestion(
          id: _uuid.v4(),
          prompt:
              'Crie um conflito inesperado para inserir ${character.name} na trama principal.',
          context: 'Personagem ocioso',
          tags: const ['personagem', 'conflito'],
          score: 0.72,
          createdAt: generatedAt,
        ),
      );
    }

    final unattachedWorld = project.worldElements.where(
      (element) => element.chapterIds.isEmpty,
    );
    for (final element in unattachedWorld) {
      prompts.add(
        PromptSuggestion(
          id: _uuid.v4(),
          prompt:
              'Descreva como ${element.name} altera a atmosfera de uma cena chave.',
          context: 'Worldbuilding desconectado',
          tags: const ['mundo', 'ambientação'],
          score: 0.55,
          createdAt: generatedAt,
        ),
      );
    }

    final slowChapters = chapterInsights.where(
      (chapter) => chapter.dialogueDensity < 0.2,
    );
    for (final chapter in slowChapters.take(2)) {
      prompts.add(
        PromptSuggestion(
          id: _uuid.v4(),
          prompt:
              'Adicione um diálogo de tensão para "${chapter.chapterTitle}". '
              'Quem confronta o protagonista e qual segredo surge?',
          context: 'Ritmo lento',
          tags: const ['ritmo', 'diálogo'],
          score: 0.6,
          relatedChapterId: chapter.chapterId,
          createdAt: generatedAt,
        ),
      );
    }

    return prompts;
  }

  static List<String> _splitSentences(String text) {
    return text
        .split(RegExp(r'[.!?]+'))
        .where((part) => part.trim().isNotEmpty)
        .toList();
  }

  static int _dialogueWordCount(String text) {
    final matches = RegExp(r'["“][^"”]+["”]').allMatches(text);
    var count = 0;
    for (final match in matches) {
      count += Chapter.countWords(match.group(0) ?? '');
    }
    return count;
  }

  static List<String> _tokenize(String text) {
    final matches = RegExp(
      r"[A-Za-zÀ-ÖØ-öø-ÿ']+",
    ).allMatches(text.toLowerCase());
    return matches.map((match) => match.group(0) ?? '').toList();
  }

  static List<String> _topRepeatedTerms(List<String> tokens) {
    final counts = <String, int>{};
    for (final token in tokens) {
      if (token.length < 5 || _stopwords.contains(token)) continue;
      counts[token] = (counts[token] ?? 0) + 1;
    }
    final sorted =
        counts.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
    return sorted
        .where((entry) => entry.value >= 3)
        .take(3)
        .map((entry) => '${entry.key} (${entry.value})')
        .toList();
  }

  static List<String> _globalRepeatedTerms(List<String> tokens) {
    final counts = <String, int>{};
    for (final token in tokens) {
      if (token.length < 5 || _stopwords.contains(token)) continue;
      counts[token] = (counts[token] ?? 0) + 1;
    }
    final sorted =
        counts.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
    return sorted
        .where((entry) => entry.value >= 5)
        .map((entry) => entry.key)
        .toList();
  }

  static List<String> _characterMentions(
    List<CharacterSheet> characters,
    String content,
  ) {
    final mentions = <String>[];
    for (final character in characters) {
      final name = character.name.trim();
      if (name.isEmpty) continue;
      final regex = RegExp(
        '\\b${RegExp.escape(name)}\\b',
        caseSensitive: false,
      );
      if (regex.hasMatch(content)) {
        mentions.add(name);
      }
    }
    return mentions;
  }

  static double _sentimentScore(List<String> tokens) {
    if (tokens.isEmpty) return 0;
    var score = 0;
    for (final token in tokens) {
      if (_positiveWords.contains(token)) {
        score += 1;
      } else if (_negativeWords.contains(token)) {
        score -= 1;
      }
    }
    return score / tokens.length;
  }

  static double _paceScore(int totalWords, int chapterCount) {
    if (totalWords == 0 || chapterCount == 0) return 0;
    final average = totalWords / chapterCount;
    return (average / 1500).clamp(0.0, 1.0);
  }

  static double _variance(List<double> values) {
    if (values.isEmpty) return 0;
    final mean = values.reduce((a, b) => a + b) / values.length;
    final sum = values.fold<double>(
      0,
      (acc, value) => acc + math.pow(value - mean, 2).toDouble(),
    );
    return sum / values.length;
  }

  static bool _isSameDay(DateTime a, DateTime b) {
    return a.year == b.year && a.month == b.month && a.day == b.day;
  }

  static const Set<String> _stopwords = {
    'que',
    'para',
    'como',
    'pois',
    'mais',
    'menos',
    'pela',
    'pelas',
    'pelo',
    'pelos',
    'sobre',
    'assim',
    'entre',
    'quando',
    'onde',
    'cada',
    'depois',
    'antes',
    'então',
    'porque',
    'qual',
    'quais',
    'dessa',
    'desse',
    'nesse',
    'nessa',
  };

  static const Set<String> _positiveWords = {
    'esperança',
    'alegria',
    'calmo',
    'feliz',
    'vitória',
    'amor',
    'sucesso',
    'leve',
    'brilho',
    'risada',
  };

  static const Set<String> _negativeWords = {
    'medo',
    'raiva',
    'tristeza',
    'dor',
    'perda',
    'sombrio',
    'ódio',
    'culpa',
    'derrota',
    'choro',
  };
}

class _InsightBuildResult {
  const _InsightBuildResult({required this.items, required this.repeatedTerms});

  final List<Insight> items;
  final List<String> repeatedTerms;
}
