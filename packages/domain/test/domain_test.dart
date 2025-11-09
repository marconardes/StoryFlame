import 'package:domain/domain.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Project.fromJson builds chapters correctly', () {
    final project = Project.fromJson({
      'id': 'test',
      'title': 'Projeto Teste',
      'description': 'Descricao',
      'updatedAt': '2024-05-28T12:00:00Z',
      'chapters': [
        {
          'id': 'chapter-1',
          'title': 'Capitulo 1',
          'summary': 'Resumo',
          'content': 'Conteudo de teste',
        },
      ],
    });

    expect(project.chapters, hasLength(1));
    expect(project.chapters.first.wordCount, greaterThan(0));
    expect(project.title, 'Projeto Teste');
  });

  test('Project serialization keeps narrative elements', () {
    final project = Project(
      id: 'p',
      title: 'Demo',
      description: 'Desc',
      updatedAt: DateTime.utc(2024, 5, 28),
      chapters: const [],
      characters: [
        CharacterSheet(
          id: 'c',
          name: 'Lyra',
          alias: 'A emissária',
          description: 'Diplomata',
          history: 'Treinada em Safir',
          traits: const ['Paciente'],
          relationships: const ['Mentora: Solenne'],
          notes: const ['Carrega diário'],
          chapterIds: const ['chapter-1'],
          updatedAt: DateTime.utc(2024, 5, 28, 12),
        ),
      ],
      glossary: [
        GlossaryEntry(
          id: 'g',
          term: 'Aurora',
          definition: 'Vírus sintético',
          category: 'Tecnologia',
          notes: 'Usado como ameaça',
          chapterIds: const ['chapter-1'],
          updatedAt: DateTime.utc(2024, 5, 28, 13),
        ),
      ],
      timeline: [
        TimelineEvent(
          id: 'e',
          title: 'Convocação',
          description: 'Lyra recebe ordem',
          date: DateTime.utc(2024, 5, 29),
          chapterId: 'chapter-1',
          order: 0,
          tags: const ['Conflito'],
        ),
      ],
      worldElements: [
        WorldElement(
          id: 'w',
          name: 'Edria',
          description: 'Cidade-cúpula',
          lore: 'Construída por exilados',
          chapterIds: const ['chapter-1'],
          type: WorldElementType.location,
          updatedAt: DateTime.utc(2024, 5, 28, 14),
        ),
      ],
      creativeIdeas: [
        CreativeIdea(
          id: 'idea-1',
          title: 'Conselho secreto',
          description: 'Lyra flagra o antagonista quebrando o protocolo.',
          tags: const ['suspense'],
          status: CreativeIdeaStatus.idea,
          updatedAt: DateTime.utc(2024, 5, 28, 15),
        ),
      ],
      templates: [
        NarrativeTemplate(
          id: 'tpl-1',
          name: 'Três atos',
          description: 'Estrutura clássica',
          steps: const ['Preparação', 'Confronto', 'Resolução'],
          completedSteps: const ['Preparação'],
          updatedAt: DateTime.utc(2024, 5, 28, 15, 10),
        ),
      ],
    );

    final json = project.toJson();
    final roundTrip = Project.fromJson(json);

    expect(roundTrip.characters.single.name, 'Lyra');
    expect(roundTrip.glossary.single.term, 'Aurora');
    expect(roundTrip.timeline.single.title, 'Convocação');
    expect(roundTrip.worldElements.single.type, WorldElementType.location);
    expect(roundTrip.creativeIdeas.single.title, 'Conselho secreto');
    expect(roundTrip.templates.single.steps.length, 3);
    expect(roundTrip.publicationChecklist.betaRead, isFalse);
    expect(roundTrip.publicationHistory, isEmpty);
  });
}
