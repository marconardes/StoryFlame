import 'dart:io';

import 'package:domain/domain.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';

import '../project_store.dart';
import '../services/export_service.dart';
import '../services/publishing_service.dart';
import '../services/insights_export_service.dart';
import 'chapter_editor_page.dart';
import 'scrivener_layout_page.dart';

class ProjectDetailsPage extends StatefulWidget {
  const ProjectDetailsPage({super.key, required this.projectId});

  static const routeName = '/project-details';
  final String projectId;

  @override
  State<ProjectDetailsPage> createState() => _ProjectDetailsPageState();
}

class _ProjectDetailsPageState extends State<ProjectDetailsPage>
    with SingleTickerProviderStateMixin {
  final ExportService _exportService = ExportService();
  final Uuid _uuid = const Uuid();
  final PublishingService _publishingService = PublishingService();
  final InsightsExportService _insightsExportService = InsightsExportService();

  @override
  Widget build(BuildContext context) {
    final store = context.watch<ProjectStore>();
    Project? maybeProject;
    try {
      maybeProject = store.projects.firstWhere((p) => p.id == widget.projectId);
    } catch (_) {
      maybeProject = null;
    }

    if (maybeProject == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Projeto não encontrado')),
        body: const Center(
          child: Text('Este projeto não está mais disponível.'),
        ),
      );
    }

    final Project project = maybeProject;
    final currentProject = project;
    final totalWords = store.projectWordCount(currentProject);
    final todayWords = store.wordsWrittenToday(currentProject);

    return DefaultTabController(
      length: 11,
      child: Scaffold(
        appBar: AppBar(
          title: Text(currentProject.title),
          actions: [
            IconButton(
              tooltip: 'Exportar TXT',
              icon: const Icon(Icons.description_outlined),
              onPressed: () => _exportProject(project, ExportType.txt),
            ),
            IconButton(
              tooltip: 'Exportar PDF',
              icon: const Icon(Icons.picture_as_pdf),
              onPressed: () => _exportProject(project, ExportType.pdf),
            ),
          ],
          bottom: const TabBar(
            isScrollable: true,
            tabs: [
              Tab(text: 'Capítulos'),
              Tab(text: 'Corkboard'),
              Tab(text: 'Personagens'),
              Tab(text: 'Glossário'),
              Tab(text: 'Timeline'),
              Tab(text: 'Mundo'),
              Tab(text: 'Ideias'),
              Tab(text: 'Templates'),
              Tab(text: 'Publicação'),
              Tab(text: 'Insights'),
              Tab(text: 'Revisão'),
            ],
          ),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                project.description,
                style: Theme.of(context).textTheme.bodyLarge,
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 16,
                runSpacing: 8,
                children: [
                  _StatChip(label: 'Palavras totais', value: '$totalWords'),
                  _StatChip(label: 'Palavras hoje', value: '$todayWords'),
                  _StatChip(
                    label: 'Capítulos',
                    value: '${project.chapters.length}',
                  ),
                ],
              ),
              const SizedBox(height: 12),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    FilledButton.icon(
                      icon: const Icon(Icons.flag),
                      label: const Text('Metas'),
                      onPressed: () => _editGoals(context, currentProject),
                    ),
                    const SizedBox(width: 12),
                    FilledButton.icon(
                      icon: const Icon(Icons.lock_outline),
                      label: Text(
                        store.requiresPassword(currentProject)
                            ? 'Alterar senha'
                            : 'Proteger',
                      ),
                      onPressed: () => _editPassword(context, currentProject),
                    ),
                    const SizedBox(width: 12),
                    IconButton(
                      tooltip: 'Layout Scrivener',
                      onPressed: () => _openScrivenerLayout(currentProject),
                      icon: const Icon(Icons.view_sidebar),
                    ),
                    IconButton(
                      tooltip: 'Matriz Personagem × Cena',
                      onPressed:
                          () => _showCharacterMatrix(context, currentProject),
                      icon: const Icon(Icons.grid_on),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              _GoalProgress(
                current: todayWords,
                goal: project.dailyGoal,
                label: 'Meta diária',
              ),
              const SizedBox(height: 8),
              _GoalProgress(
                current: totalWords,
                goal: project.totalGoal,
                label: 'Meta total',
              ),
              const SizedBox(height: 12),
              Expanded(
                child: TabBarView(
                  children: [
                    _ChaptersTab(
                      project: currentProject,
                      onAddChapter: () => _addChapter(context, currentProject),
                      onEditChapter:
                          (chapter) =>
                              _editChapter(context, currentProject, chapter),
                      onReorder:
                          (oldIndex, newIndex) => _reorderChapter(
                            currentProject,
                            oldIndex,
                            newIndex,
                          ),
                      onDeleteChapter:
                          (chapter) =>
                              _deleteChapter(context, currentProject, chapter),
                    ),
                    _CorkboardTab(
                      project: currentProject,
                      onOpenEditor:
                          (chapter) =>
                              _editChapter(
                                context,
                                currentProject,
                                chapter,
                              ),
                      onEditCard:
                          (chapter) =>
                              _editCorkboardCard(context, currentProject, chapter),
                      onStatusChange:
                          (chapter, status) => _updateChapterStatus(
                            context,
                            currentProject,
                            chapter,
                            status,
                          ),
                    ),
                    _CharactersTab(
                      project: currentProject,
                      onCreate: () => _openCharacterSheet(context),
                      onEdit:
                          (sheet) => _openCharacterSheet(context, sheet: sheet),
                      onDelete: (id) => _deleteCharacter(context, id),
                      onToggleChapter:
                          (characterId, chapterId) => context
                              .read<ProjectStore>()
                              .toggleCharacterChapter(
                                currentProject.id,
                                characterId,
                                chapterId,
                              ),
                    ),
                    _GlossaryTab(
                      project: currentProject,
                      onCreate: () => _openGlossaryEntry(context),
                      onEdit:
                          (entry) => _openGlossaryEntry(context, entry: entry),
                      onDelete: (id) => _deleteGlossaryEntry(context, id),
                      onToggleChapter:
                          (entryId, chapterId) => context
                              .read<ProjectStore>()
                              .toggleGlossaryChapter(
                                currentProject.id,
                                entryId,
                                chapterId,
                              ),
                    ),
                    _TimelineTab(
                      project: currentProject,
                      onCreate: () => _openTimelineEvent(context),
                      onEdit:
                          (event) => _openTimelineEvent(context, event: event),
                      onDelete: (id) => _deleteTimelineEvent(context, id),
                      onReorder:
                          (events) => context
                              .read<ProjectStore>()
                              .reorderTimeline(currentProject.id, events),
                    ),
                    _WorldTab(
                      project: currentProject,
                      onCreate:
                          (type) => _openWorldElement(context, type: type),
                      onEdit:
                          (element) =>
                              _openWorldElement(context, element: element),
                      onDelete: (id) => _deleteWorldElement(context, id),
                      onToggleChapter:
                          (elementId, chapterId) => context
                              .read<ProjectStore>()
                              .toggleWorldElementChapter(
                                currentProject.id,
                                elementId,
                                chapterId,
                              ),
                    ),
                    _CreativeIdeasTab(
                      project: currentProject,
                      onCreate: () => _openCreativeIdeaDialog(context),
                      onEdit:
                          (idea) =>
                              _openCreativeIdeaDialog(context, idea: idea),
                      onDelete: (id) => _deleteCreativeIdea(context, id),
                      onStatusChange:
                          (id, status) =>
                              _updateIdeaStatus(context, id, status),
                    ),
                    _TemplatesTab(
                      project: currentProject,
                      onCreate: () => _openTemplateDialog(context),
                      onEdit:
                          (template) =>
                              _openTemplateDialog(context, template: template),
                      onDelete: (id) => _deleteTemplate(context, id),
                      onToggleStep:
                          (templateId, step) =>
                              _toggleTemplateStep(context, templateId, step),
                      onApplyBuiltIn:
                          (template) =>
                              _applyBuiltInTemplate(context, template),
                      builtInTemplates: _builtInTemplates,
                    ),
                    _PublicationTab(
                      project: currentProject,
                      onExportEpub:
                          () => _handleEpubExport(context, currentProject),
                      onExportBundle:
                          () => _handleBundleExport(context, currentProject),
                      onOpenKdpWizard:
                          () => _handleKdpWizard(context, currentProject),
                      onChecklistChanged:
                          (checklist) => _updatePublicationChecklist(
                            context,
                            currentProject,
                            checklist,
                          ),
                    ),
                    _InsightsTab(
                      project: currentProject,
                      snapshot: store.insightsFor(currentProject.id),
                      onRefresh:
                          () => _refreshInsights(context, currentProject),
                      onExport: () => _exportInsights(context, currentProject),
                    ),
                    _ReviewTab(
                      project: currentProject,
                      onCreate:
                          () =>
                              _openReviewCommentDialog(context, currentProject),
                      onToggle:
                          (commentId, resolved) => _toggleReviewComment(
                            context,
                            currentProject.id,
                            commentId,
                            resolved,
                          ),
                      onDelete:
                          (commentId) => _deleteReviewComment(
                            context,
                            currentProject.id,
                            commentId,
                          ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _addChapter(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    final controller = TextEditingController();
    final title = await showDialog<String>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Novo capítulo'),
            content: TextField(
              controller: controller,
              decoration: const InputDecoration(labelText: 'Título'),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed:
                    () => Navigator.of(context).pop(controller.text.trim()),
                child: const Text('Criar'),
              ),
            ],
          ),
    );
    if (title == null || title.isEmpty) return;
    await store.addChapter(project.id, title);
  }

  Future<void> _editGoals(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    final result = await showModalBottomSheet<_GoalResult>(
      context: context,
      isScrollControlled: true,
      builder:
          (_) => _GoalSheet(
            dailyGoal: project.dailyGoal,
            totalGoal: project.totalGoal,
          ),
    );
    if (result == null) return;
    await store.updateGoals(
      project: project,
      dailyGoal: result.dailyGoal,
      totalGoal: result.totalGoal,
    );
  }

  Future<void> _editPassword(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    final result = await showModalBottomSheet<String?>(
      context: context,
      isScrollControlled: true,
      builder:
          (_) => _PasswordSheet(initialProtected: project.passwordHash != null),
    );
    if (result == null) return;
    await store.setPassword(project, result);
  }

  Future<void> _refreshInsights(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    try {
      await store.refreshInsights(project.id);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Insights regenerados com sucesso.')),
      );
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Falha ao gerar insights: $error')),
      );
    }
  }

  Future<void> _exportInsights(
    BuildContext context,
    Project project,
  ) async {
    final store = context.read<ProjectStore>();
    final snapshot = store.insightsFor(project.id);
    if (snapshot == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Gere os insights antes de exportar.'),
        ),
      );
      return;
    }
    try {
      final file = await _insightsExportService.export(project, snapshot);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Insights exportados em ${file.path}')),
      );
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Falha ao exportar insights: $error')),
      );
    }
  }

  Future<void> _openReviewCommentDialog(
    BuildContext context,
    Project project,
  ) async {
    final store = context.read<ProjectStore>();
    final messageController = TextEditingController();
    final contextController = TextEditingController();
    String? chapterId;
    _ReviewCommentResult? result;
    try {
      result = await showModalBottomSheet<_ReviewCommentResult>(
        context: context,
        isScrollControlled: true,
        builder: (sheetContext) {
          final bottom = MediaQuery.of(sheetContext).viewInsets.bottom;
          return Padding(
            padding: EdgeInsets.only(
              left: 16,
              right: 16,
              top: 24,
              bottom: bottom + 24,
            ),
            child: StatefulBuilder(
              builder: (context, setModalState) {
                return Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Novo comentário de revisão',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: messageController,
                      maxLines: 4,
                      decoration: const InputDecoration(
                        labelText: 'Comentário',
                        border: OutlineInputBorder(),
                      ),
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<String?>(
                      value: chapterId,
                      items: [
                        const DropdownMenuItem<String?>(
                          value: null,
                          child: Text('Sem capítulo específico'),
                        ),
                        ...project.chapters.map(
                          (chapter) => DropdownMenuItem<String?>(
                            value: chapter.id,
                            child: Text(chapter.title),
                          ),
                        ),
                      ],
                      decoration: const InputDecoration(
                        labelText: 'Capítulo relacionado',
                        border: OutlineInputBorder(),
                      ),
                      onChanged: (value) {
                        setModalState(() {
                          chapterId = value;
                        });
                      },
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: contextController,
                      decoration: const InputDecoration(
                        labelText: 'Contexto adicional (opcional)',
                        border: OutlineInputBorder(),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        TextButton(
                          onPressed: () => Navigator.of(sheetContext).pop(),
                          child: const Text('Cancelar'),
                        ),
                        const SizedBox(width: 12),
                        FilledButton(
                          onPressed: () {
                            final message = messageController.text.trim();
                            if (message.isEmpty) return;
                            Navigator.of(sheetContext).pop(
                              _ReviewCommentResult(
                                message: message,
                                chapterId: chapterId,
                                context:
                                    contextController.text.trim().isEmpty
                                        ? null
                                        : contextController.text.trim(),
                              ),
                            );
                          },
                          child: const Text('Salvar'),
                        ),
                      ],
                    ),
                  ],
                );
              },
            ),
          );
        },
      );
    } finally {
      messageController.dispose();
      contextController.dispose();
    }
    if (result == null) return;
    await store.addReviewComment(
      projectId: project.id,
      message: result.message,
      chapterId: result.chapterId,
      context: result.context,
    );
  }

  Future<void> _toggleReviewComment(
    BuildContext context,
    String projectId,
    String commentId,
    bool resolved,
  ) async {
    final store = context.read<ProjectStore>();
    await store.toggleReviewComment(
      projectId: projectId,
      commentId: commentId,
      resolved: resolved,
    );
  }

  Future<void> _deleteReviewComment(
    BuildContext context,
    String projectId,
    String commentId,
  ) async {
    final store = context.read<ProjectStore>();
    final confirm = await showDialog<bool>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Excluir comentário'),
            content: const Text('Deseja remover este comentário?'),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: const Text('Excluir'),
              ),
            ],
          ),
    );
    if (confirm != true) return;
    await store.deleteReviewComment(projectId, commentId);
  }

  Future<void> _editChapter(
    BuildContext context,
    Project project,
    Chapter chapter,
  ) async {
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder:
            (_) => ChapterEditorPage(projectId: project.id, chapter: chapter),
      ),
    );
  }

  Future<void> _editCorkboardCard(
    BuildContext context,
    Project project,
    Chapter chapter,
  ) async {
    final summaryController = TextEditingController(text: chapter.summary);
    final coverController = TextEditingController(text: chapter.coverImage);
    var status = chapter.status;
    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (sheetContext) {
        final bottom = MediaQuery.of(sheetContext).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: StatefulBuilder(
            builder: (context, setModalState) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Text(
                    'Editar cartão',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 12),
                  DropdownButtonFormField<ChapterStatus>(
                    value: status,
                    decoration: const InputDecoration(
                      labelText: 'Status',
                      border: OutlineInputBorder(),
                    ),
                    items: ChapterStatus.values
                        .map(
                          (value) => DropdownMenuItem(
                            value: value,
                            child: Text(value.label),
                          ),
                        )
                        .toList(),
                    onChanged: (value) {
                      if (value == null) return;
                      setModalState(() => status = value);
                    },
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: summaryController,
                    decoration: const InputDecoration(
                      labelText: 'Resumo curto',
                      border: OutlineInputBorder(),
                    ),
                    maxLines: 3,
                    minLines: 2,
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: coverController,
                    decoration: const InputDecoration(
                      labelText: 'URL da capa (opcional)',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      TextButton(
                        onPressed: () => Navigator.of(sheetContext).pop(false),
                        child: const Text('Cancelar'),
                      ),
                      const SizedBox(width: 12),
                      FilledButton(
                        onPressed: () => Navigator.of(sheetContext).pop(true),
                        child: const Text('Salvar'),
                      ),
                    ],
                  ),
                ],
              );
            },
          ),
        );
      },
    );
    if (result == true) {
      final store = context.read<ProjectStore>();
      final updated = chapter.copyWith(
        summary: summaryController.text.trim(),
        coverImage: coverController.text.trim(),
        status: status,
        updatedAt: DateTime.now(),
      );
      await store.updateChapter(project.id, updated);
    }
    summaryController.dispose();
    coverController.dispose();
  }

  Future<void> _updateChapterStatus(
    BuildContext context,
    Project project,
    Chapter chapter,
    ChapterStatus status,
  ) async {
    final store = context.read<ProjectStore>();
    final updated = chapter.copyWith(
      status: status,
      updatedAt: DateTime.now(),
    );
    await store.updateChapter(project.id, updated);
  }

  void _openScrivenerLayout(Project project) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ScrivenerLayoutPage(projectId: project.id),
      ),
    );
  }

  Future<void> _exportProject(Project project, ExportType type) async {
    late final File file;
    switch (type) {
      case ExportType.txt:
        file = await _exportService.exportTxt(project);
        break;
      case ExportType.pdf:
        file = await _exportService.exportPdf(project);
        break;
    }
    if (!mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text('Exportado em ${file.path}')));
  }

  Future<void> _reorderChapter(
    Project project,
    int oldIndex,
    int newIndex,
  ) async {
    final store = context.read<ProjectStore>();
    final chapters = List<Chapter>.from(project.chapters);
    if (newIndex > oldIndex) newIndex -= 1;
    final item = chapters.removeAt(oldIndex);
    chapters.insert(newIndex, item);
    await store.reorderChapters(project.id, chapters);
  }

  Future<void> _openTimelineEvent(
    BuildContext context, {
    TimelineEvent? event,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final titleController = TextEditingController(text: event?.title ?? '');
    final descriptionController = TextEditingController(
      text: event?.description ?? '',
    );
    final tagsController = TextEditingController(
      text: event == null ? '' : event.tags.join(', '),
    );
    DateTime? date = event?.date;
    String? chapterId = event?.chapterId;

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return StatefulBuilder(
          builder: (context, setModalState) {
            Future<void> pickDate() async {
              final now = DateTime.now();
              final datePicked = await showDatePicker(
                context: context,
                initialDate: date ?? now,
                firstDate: DateTime(now.year - 5),
                lastDate: DateTime(now.year + 5),
              );
              if (datePicked == null) return;
              if (!context.mounted) return;
              final timePicked = await showTimePicker(
                context: context,
                initialTime:
                    date == null
                        ? TimeOfDay.now()
                        : TimeOfDay.fromDateTime(date!),
              );
              setModalState(() {
                date = DateTime(
                  datePicked.year,
                  datePicked.month,
                  datePicked.day,
                  timePicked?.hour ?? 0,
                  timePicked?.minute ?? 0,
                );
              });
            }

            return Padding(
              padding: EdgeInsets.only(
                left: 16,
                right: 16,
                top: 24,
                bottom: bottom + 24,
              ),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: titleController,
                      decoration: const InputDecoration(labelText: 'Título'),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: descriptionController,
                      decoration: const InputDecoration(labelText: 'Descrição'),
                      minLines: 2,
                      maxLines: 4,
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<String?>(
                      value: chapterId,
                      decoration: const InputDecoration(
                        labelText: 'Capítulo relacionado',
                      ),
                      items: [
                        const DropdownMenuItem(
                          value: null,
                          child: Text('Nenhum'),
                        ),
                        ...project.chapters.map(
                          (chapter) => DropdownMenuItem(
                            value: chapter.id,
                            child: Text(chapter.title),
                          ),
                        ),
                      ],
                      onChanged:
                          (value) => setModalState(() => chapterId = value),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            date == null
                                ? 'Sem data definida'
                                : 'Data: ${date!.toLocal().toString().split('.').first}',
                          ),
                        ),
                        TextButton.icon(
                          onPressed: pickDate,
                          icon: const Icon(Icons.calendar_month),
                          label: const Text('Definir'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: tagsController,
                      decoration: const InputDecoration(
                        labelText: 'Tags (separadas por vírgula)',
                      ),
                    ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton(
                        onPressed: () => Navigator.of(context).pop(true),
                        child: const Text('Salvar evento'),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );

    if (result != true) return;
    final newEvent = TimelineEvent(
      id: event?.id ?? _uuid.v4(),
      title: titleController.text.trim(),
      description: descriptionController.text.trim(),
      date: date,
      chapterId: chapterId,
      order: event?.order ?? project.timeline.length,
      tags: _splitInlineList(tagsController.text),
    );
    await store.saveTimelineEvent(project.id, newEvent);
  }

  Future<void> _handleEpubExport(BuildContext context, Project project) async {
    final messenger = ScaffoldMessenger.of(context);
    final store = context.read<ProjectStore>();
    final file = await _publishingService.exportEpub(project);
    await _recordPublication(
      store,
      project,
      PublicationChannel.epub,
      file.path,
      'ePub gerado',
    );
    if (!mounted) return;
    messenger.showSnackBar(
      SnackBar(content: Text('ePub exportado em ${file.path}')),
    );
  }

  Future<void> _handleBundleExport(
    BuildContext context,
    Project project,
  ) async {
    final messenger = ScaffoldMessenger.of(context);
    final store = context.read<ProjectStore>();
    final file = await _publishingService.exportBundle(project);
    await _recordPublication(
      store,
      project,
      PublicationChannel.bundle,
      file.path,
      'Pacote completo (PDF/JSON) gerado',
    );
    if (!mounted) return;
    messenger.showSnackBar(
      SnackBar(content: Text('Pacote exportado em ${file.path}')),
    );
  }

  Future<void> _handleKdpWizard(BuildContext context, Project project) async {
    final messenger = ScaffoldMessenger.of(context);
    final store = context.read<ProjectStore>();
    final synopsisController = TextEditingController();
    final categoriesController = TextEditingController();
    final keywordsController = TextEditingController();
    final audienceController = TextEditingController();

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: synopsisController,
                  decoration: const InputDecoration(labelText: 'Sinopse'),
                  minLines: 3,
                  maxLines: 5,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: categoriesController,
                  decoration: const InputDecoration(
                    labelText: 'Categorias (separadas por vírgula)',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: keywordsController,
                  decoration: const InputDecoration(
                    labelText: 'Palavras-chave (separadas por vírgula)',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: audienceController,
                  decoration: const InputDecoration(labelText: 'Público-alvo'),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () => Navigator.of(context).pop(true),
                    child: const Text('Gerar pacote KDP'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (result != true) return;
    final metadata = PublicationMetadata(
      synopsis: synopsisController.text.trim(),
      categories: _splitInlineList(categoriesController.text),
      keywords: _splitInlineList(keywordsController.text),
      audience: audienceController.text.trim(),
    );
    final file = await _publishingService.createKdpPackage(project, metadata);
    await _recordPublication(
      store,
      project,
      PublicationChannel.kdp,
      file.path,
      'Pacote KDP com metadados',
    );
    if (!mounted) return;
    messenger.showSnackBar(
      SnackBar(content: Text('Pacote KDP salvo em ${file.path}')),
    );
  }

  Future<void> _deleteChapter(
    BuildContext context,
    Project project,
    Chapter chapter,
  ) async {
    await context.read<ProjectStore>().deleteChapter(project.id, chapter.id);
  }

  Future<void> _deleteTimelineEvent(
    BuildContext context,
    String eventId,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteTimelineEvent(project.id, eventId);
  }

  Future<void> _openCharacterSheet(
    BuildContext context, {
    CharacterSheet? sheet,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final nameController = TextEditingController(text: sheet?.name ?? '');
    final aliasController = TextEditingController(text: sheet?.alias ?? '');
    final descController = TextEditingController(
      text: sheet?.description ?? '',
    );
    final historyController = TextEditingController(text: sheet?.history ?? '');
    final traitsController = TextEditingController(
      text: (sheet?.traits ?? <String>[]).join(', '),
    );
    final relController = TextEditingController(
      text: (sheet?.relationships ?? <String>[]).join('\n'),
    );
    final notesController = TextEditingController(
      text: (sheet?.notes ?? <String>[]).join('\n'),
    );

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Nome'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: aliasController,
                  decoration: const InputDecoration(labelText: 'Apelido'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: descController,
                  decoration: const InputDecoration(labelText: 'Descrição'),
                  minLines: 2,
                  maxLines: 3,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: historyController,
                  decoration: const InputDecoration(labelText: 'História'),
                  minLines: 2,
                  maxLines: 4,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: traitsController,
                  decoration: const InputDecoration(
                    labelText: 'Traços (separados por vírgula)',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: relController,
                  decoration: const InputDecoration(
                    labelText: 'Relacionamentos (um por linha)',
                  ),
                  minLines: 2,
                  maxLines: 3,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: notesController,
                  decoration: const InputDecoration(
                    labelText: 'Notas (uma por linha)',
                  ),
                  minLines: 2,
                  maxLines: 3,
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () => Navigator.of(context).pop(true),
                    child: const Text('Salvar personagem'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (result != true) return;
    final character = CharacterSheet(
      id: sheet?.id ?? _uuid.v4(),
      name: nameController.text.trim(),
      alias: aliasController.text.trim(),
      description: descController.text.trim(),
      history: historyController.text.trim(),
      traits: _splitInlineList(traitsController.text),
      relationships: _splitMultilineList(relController.text),
      notes: _splitMultilineList(notesController.text),
      chapterIds: sheet?.chapterIds ?? <String>[],
      updatedAt: DateTime.now(),
    );
    await store.saveCharacter(project.id, character);
  }

  Future<void> _deleteCharacter(
    BuildContext context,
    String characterId,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteCharacter(project.id, characterId);
  }

  Future<void> _openGlossaryEntry(
    BuildContext context, {
    GlossaryEntry? entry,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final termController = TextEditingController(text: entry?.term ?? '');
    final definitionController = TextEditingController(
      text: entry?.definition ?? '',
    );
    final categoryController = TextEditingController(
      text: entry?.category ?? '',
    );
    final notesController = TextEditingController(text: entry?.notes ?? '');

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: termController,
                  decoration: const InputDecoration(labelText: 'Termo'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: definitionController,
                  decoration: const InputDecoration(labelText: 'Definição'),
                  minLines: 2,
                  maxLines: 4,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: categoryController,
                  decoration: const InputDecoration(labelText: 'Categoria'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: notesController,
                  decoration: const InputDecoration(labelText: 'Notas/uso'),
                  minLines: 2,
                  maxLines: 3,
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () => Navigator.of(context).pop(true),
                    child: const Text('Salvar termo'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (result != true) return;
    final entryToSave = GlossaryEntry(
      id: entry?.id ?? _uuid.v4(),
      term: termController.text.trim(),
      definition: definitionController.text.trim(),
      category: categoryController.text.trim(),
      notes: notesController.text.trim(),
      chapterIds: entry?.chapterIds ?? <String>[],
      updatedAt: DateTime.now(),
    );
    await store.saveGlossaryEntry(project.id, entryToSave);
  }

  Future<void> _deleteGlossaryEntry(
    BuildContext context,
    String entryId,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteGlossaryEntry(project.id, entryId);
  }

  Future<void> _openWorldElement(
    BuildContext context, {
    WorldElement? element,
    WorldElementType? type,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    var currentType = element?.type ?? type ?? WorldElementType.location;
    final nameController = TextEditingController(text: element?.name ?? '');
    final descriptionController = TextEditingController(
      text: element?.description ?? '',
    );
    final loreController = TextEditingController(text: element?.lore ?? '');
    List<String> chapterLinks = List<String>.from(
      element?.chapterIds ?? <String>[],
    );

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return StatefulBuilder(
          builder: (context, setModalState) {
            return Padding(
              padding: EdgeInsets.only(
                left: 16,
                right: 16,
                top: 24,
                bottom: bottom + 24,
              ),
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    DropdownButtonFormField<WorldElementType>(
                      value: currentType,
                      decoration: const InputDecoration(labelText: 'Tipo'),
                      items:
                          WorldElementType.values
                              .map(
                                (type) => DropdownMenuItem(
                                  value: type,
                                  child: Text(
                                    type == WorldElementType.location
                                        ? 'Local'
                                        : 'Item',
                                  ),
                                ),
                              )
                              .toList(),
                      onChanged:
                          (value) => setModalState(() {
                            currentType = value ?? currentType;
                          }),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: nameController,
                      decoration: const InputDecoration(labelText: 'Nome'),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: descriptionController,
                      decoration: const InputDecoration(labelText: 'Descrição'),
                      minLines: 2,
                      maxLines: 3,
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: loreController,
                      decoration: const InputDecoration(
                        labelText: 'Lore/Notas',
                      ),
                      minLines: 2,
                      maxLines: 4,
                    ),
                    const SizedBox(height: 12),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Capítulos relacionados',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ),
                    Wrap(
                      spacing: 8,
                      children:
                          project.chapters
                              .map(
                                (chapter) => FilterChip(
                                  label: Text(chapter.title),
                                  selected: chapterLinks.contains(chapter.id),
                                  onSelected:
                                      (_) => setModalState(() {
                                        if (chapterLinks.contains(chapter.id)) {
                                          chapterLinks =
                                              chapterLinks
                                                  .where(
                                                    (id) => id != chapter.id,
                                                  )
                                                  .toList();
                                        } else {
                                          chapterLinks = <String>[
                                            ...chapterLinks,
                                            chapter.id,
                                          ];
                                        }
                                      }),
                                ),
                              )
                              .toList(),
                    ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton(
                        onPressed: () => Navigator.of(context).pop(true),
                        child: const Text('Salvar elemento'),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );

    if (result != true) return;
    final elementToSave = WorldElement(
      id: element?.id ?? _uuid.v4(),
      name: nameController.text.trim(),
      description: descriptionController.text.trim(),
      lore: loreController.text.trim(),
      chapterIds: chapterLinks,
      type: currentType,
      updatedAt: DateTime.now(),
    );
    await store.saveWorldElement(project.id, elementToSave);
  }

  Future<void> _deleteWorldElement(
    BuildContext context,
    String elementId,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteWorldElement(
      project.id,
      elementId,
    );
  }

  Future<void> _openCreativeIdeaDialog(
    BuildContext context, {
    CreativeIdea? idea,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final titleController = TextEditingController(text: idea?.title ?? '');
    final descriptionController = TextEditingController(
      text: idea?.description ?? '',
    );
    final tagsController = TextEditingController(
      text: (idea?.tags ?? <String>[]).join(', '),
    );
    var status = idea?.status ?? CreativeIdeaStatus.idea;

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: StatefulBuilder(
            builder: (context, setModalState) {
              return SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: titleController,
                      decoration: const InputDecoration(
                        labelText: 'Título da ideia',
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: descriptionController,
                      decoration: const InputDecoration(
                        labelText: 'Descrição / prompt',
                      ),
                      minLines: 3,
                      maxLines: 5,
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: tagsController,
                      decoration: const InputDecoration(
                        labelText: 'Tags (separadas por vírgula)',
                      ),
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<CreativeIdeaStatus>(
                      value: status,
                      decoration: const InputDecoration(labelText: 'Status'),
                      items:
                          CreativeIdeaStatus.values
                              .map(
                                (value) => DropdownMenuItem(
                                  value: value,
                                  child: Text(switch (value) {
                                    CreativeIdeaStatus.idea => 'Ideia',
                                    CreativeIdeaStatus.draft => 'Rascunho',
                                    CreativeIdeaStatus.done => 'Concluída',
                                  }),
                                ),
                              )
                              .toList(),
                      onChanged:
                          (value) => setModalState(() {
                            status = value ?? CreativeIdeaStatus.idea;
                          }),
                    ),
                    const SizedBox(height: 16),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton(
                        onPressed: () => Navigator.of(context).pop(true),
                        child: const Text('Salvar ideia'),
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        );
      },
    );

    if (result != true) return;
    final ideaToSave = CreativeIdea(
      id: idea?.id ?? _uuid.v4(),
      title: titleController.text.trim(),
      description: descriptionController.text.trim(),
      tags: _splitInlineList(tagsController.text),
      status: status,
      updatedAt: DateTime.now(),
    );
    await store.saveCreativeIdea(project.id, ideaToSave);
  }

  Future<void> _deleteCreativeIdea(BuildContext context, String ideaId) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteCreativeIdea(project.id, ideaId);
  }

  Future<void> _updateIdeaStatus(
    BuildContext context,
    String ideaId,
    CreativeIdeaStatus status,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    final idea = project.creativeIdeas.firstWhere(
      (candidate) => candidate.id == ideaId,
    );
    await context.read<ProjectStore>().saveCreativeIdea(
      project.id,
      idea.copyWith(status: status, updatedAt: DateTime.now()),
    );
  }

  Future<void> _openTemplateDialog(
    BuildContext context, {
    NarrativeTemplate? template,
  }) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final nameController = TextEditingController(text: template?.name ?? '');
    final descriptionController = TextEditingController(
      text: template?.description ?? '',
    );
    final stepsController = TextEditingController(
      text: (template?.steps ?? <String>[]).join('\n'),
    );

    final result = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final bottom = MediaQuery.of(context).viewInsets.bottom;
        return Padding(
          padding: EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: bottom + 24,
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(
                    labelText: 'Nome do template',
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: descriptionController,
                  decoration: const InputDecoration(labelText: 'Descrição'),
                  minLines: 2,
                  maxLines: 4,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: stepsController,
                  decoration: const InputDecoration(
                    labelText: 'Passos (um por linha)',
                  ),
                  minLines: 4,
                  maxLines: 6,
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () => Navigator.of(context).pop(true),
                    child: const Text('Salvar template'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (result != true) return;
    final templateToSave = NarrativeTemplate(
      id: template?.id ?? _uuid.v4(),
      name: nameController.text.trim(),
      description: descriptionController.text.trim(),
      steps: _splitMultilineList(stepsController.text),
      completedSteps: template?.completedSteps ?? const <String>[],
      updatedAt: DateTime.now(),
    );
    await store.saveTemplate(project.id, templateToSave);
  }

  Future<void> _deleteTemplate(BuildContext context, String templateId) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().deleteTemplate(project.id, templateId);
  }

  Future<void> _toggleTemplateStep(
    BuildContext context,
    String templateId,
    String step,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    await context.read<ProjectStore>().toggleTemplateStep(
      project.id,
      templateId,
      step,
    );
  }

  Future<void> _applyBuiltInTemplate(
    BuildContext context,
    NarrativeTemplate template,
  ) async {
    final project = _getProject(context);
    if (project == null) return;
    final store = context.read<ProjectStore>();
    final messenger = ScaffoldMessenger.of(context);
    final instance = template.copyWith(
      id: _uuid.v4(),
      updatedAt: DateTime.now(),
      completedSteps: const <String>[],
    );
    await store.saveTemplate(project.id, instance);
    if (!mounted) return;
    messenger.showSnackBar(
      SnackBar(content: Text('Template "${template.name}" adicionado.')),
    );
  }

  Future<void> _updatePublicationChecklist(
    BuildContext context,
    Project project,
    PublicationChecklist checklist,
  ) async {
    await context.read<ProjectStore>().updatePublicationChecklist(
      project.id,
      checklist,
    );
  }

  Future<void> _recordPublication(
    ProjectStore store,
    Project project,
    PublicationChannel channel,
    String path,
    String notes,
  ) async {
    final record = PublicationRecord(
      id: _uuid.v4(),
      channel: channel,
      filePath: path,
      notes: notes,
      createdAt: DateTime.now(),
    );
    await store.addPublicationRecord(project.id, record);
  }

  List<String> _splitInlineList(String input) =>
      input
          .split(RegExp(r'[\n,]'))
          .map((value) => value.trim())
          .where((value) => value.isNotEmpty)
          .toList();

  List<String> _splitMultilineList(String input) =>
      input
          .split(RegExp(r'\n'))
          .map((value) => value.trim())
          .where((value) => value.isNotEmpty)
          .toList();

  void _showCharacterMatrix(BuildContext context, Project project) {
    showDialog<void>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Participação de personagens'),
            content: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: DataTable(
                columns: [
                  const DataColumn(label: Text('Personagem')),
                  ...project.chapters.map(
                    (chapter) => DataColumn(label: Text(chapter.title)),
                  ),
                ],
                rows:
                    project.characters.map((character) {
                      return DataRow(
                        cells: [
                          DataCell(Text(character.name)),
                          ...project.chapters.map(
                            (chapter) => DataCell(
                              Icon(
                                character.chapterIds.contains(chapter.id)
                                    ? Icons.check_circle
                                    : Icons.circle_outlined,
                                color:
                                    character.chapterIds.contains(chapter.id)
                                        ? Colors.green
                                        : null,
                              ),
                            ),
                          ),
                        ],
                      );
                    }).toList(),
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Fechar'),
              ),
            ],
          ),
    );
  }

  Project? _getProject(BuildContext context) {
    try {
      return context.read<ProjectStore>().projects.firstWhere(
        (project) => project.id == widget.projectId,
      );
    } catch (_) {
      return null;
    }
  }
}

enum ExportType { txt, pdf }

class _ChaptersTab extends StatelessWidget {
  const _ChaptersTab({
    required this.project,
    required this.onAddChapter,
    required this.onEditChapter,
    required this.onReorder,
    required this.onDeleteChapter,
  });

  final Project project;
  final VoidCallback onAddChapter;
  final void Function(Chapter chapter) onEditChapter;
  final Future<void> Function(int oldIndex, int newIndex) onReorder;
  final Future<void> Function(Chapter chapter) onDeleteChapter;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: OutlinedButton.icon(
            icon: const Icon(Icons.add),
            label: const Text('Novo capítulo'),
            onPressed: onAddChapter,
          ),
        ),
        const SizedBox(height: 12),
        Expanded(
          child: ReorderableListView.builder(
            itemCount: project.chapters.length,
            onReorder: (oldIndex, newIndex) => onReorder(oldIndex, newIndex),
            itemBuilder: (context, index) {
              final chapter = project.chapters[index];
              return Card(
                key: ValueKey(chapter.id),
                child: ListTile(
                  title: Text(chapter.title),
                  subtitle: Text(
                    chapter.summary.isEmpty ? 'Sem resumo' : chapter.summary,
                  ),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text('${chapter.wordCount} palavras'),
                      IconButton(
                        icon: const Icon(Icons.delete_outline),
                        tooltip: 'Excluir capítulo',
                        onPressed: () => _confirmDelete(context, chapter),
                      ),
                    ],
                  ),
                  onTap: () => onEditChapter(chapter),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Future<void> _confirmDelete(BuildContext context, Chapter chapter) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Excluir capítulo'),
            content: Text(
              'Deseja excluir "${chapter.title}"? Esta ação não poderá ser desfeita.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(true),
                child: const Text('Excluir'),
              ),
            ],
          ),
    );
    if (confirm == true) {
      await onDeleteChapter(chapter);
    }
  }
}

class _CharactersTab extends StatelessWidget {
  const _CharactersTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onToggleChapter,
  });

  final Project project;
  final VoidCallback onCreate;
  final void Function(CharacterSheet sheet) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(String characterId, String chapterId)
  onToggleChapter;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            FilledButton.icon(
              onPressed: onCreate,
              icon: const Icon(Icons.add),
              label: const Text('Adicionar personagem'),
            ),
            const SizedBox(width: 12),
            Text('${project.characters.length} personagens'),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              project.characters.isEmpty
                  ? const Center(child: Text('Nenhum personagem cadastrado.'))
                  : ListView.separated(
                    itemCount: project.characters.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final character = project.characters[index];
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      character.name,
                                      style:
                                          Theme.of(
                                            context,
                                          ).textTheme.titleMedium,
                                    ),
                                  ),
                                  IconButton(
                                    tooltip: 'Editar',
                                    icon: const Icon(Icons.edit),
                                    onPressed: () => onEdit(character),
                                  ),
                                  IconButton(
                                    tooltip: 'Excluir',
                                    icon: const Icon(Icons.delete_outline),
                                    onPressed: () => onDelete(character.id),
                                  ),
                                ],
                              ),
                              if (character.alias.isNotEmpty)
                                Text('Apelido: ${character.alias}'),
                              const SizedBox(height: 8),
                              Text(character.description),
                              if (character.traits.isNotEmpty) ...[
                                const SizedBox(height: 8),
                                Wrap(
                                  spacing: 8,
                                  runSpacing: 4,
                                  children:
                                      character.traits
                                          .map(
                                            (trait) => Chip(label: Text(trait)),
                                          )
                                          .toList(),
                                ),
                              ],
                              const SizedBox(height: 8),
                              Text(
                                'Participa nos capítulos:',
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              Wrap(
                                spacing: 8,
                                children:
                                    project.chapters
                                        .map(
                                          (chapter) => FilterChip(
                                            label: Text(chapter.title),
                                            selected: character.chapterIds
                                                .contains(chapter.id),
                                            onSelected:
                                                (_) => onToggleChapter(
                                                  character.id,
                                                  chapter.id,
                                                ),
                                          ),
                                        )
                                        .toList(),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

class _GlossaryTab extends StatelessWidget {
  const _GlossaryTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onToggleChapter,
  });

  final Project project;
  final VoidCallback onCreate;
  final void Function(GlossaryEntry entry) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(String entryId, String chapterId) onToggleChapter;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        FilledButton.icon(
          onPressed: onCreate,
          icon: const Icon(Icons.library_add),
          label: const Text('Novo termo'),
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              project.glossary.isEmpty
                  ? const Center(child: Text('Nenhum termo cadastrado.'))
                  : ListView.separated(
                    itemCount: project.glossary.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final entry = project.glossary[index];
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      entry.term,
                                      style:
                                          Theme.of(
                                            context,
                                          ).textTheme.titleMedium,
                                    ),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.edit),
                                    onPressed: () => onEdit(entry),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete_outline),
                                    onPressed: () => onDelete(entry.id),
                                  ),
                                ],
                              ),
                              Text(entry.definition),
                              if (entry.category.isNotEmpty)
                                Padding(
                                  padding: const EdgeInsets.only(top: 4),
                                  child: Text(
                                    'Categoria: ${entry.category}',
                                    style:
                                        Theme.of(context).textTheme.bodySmall,
                                  ),
                                ),
                              const SizedBox(height: 8),
                              Wrap(
                                spacing: 8,
                                children:
                                    project.chapters
                                        .map(
                                          (chapter) => FilterChip(
                                            label: Text(chapter.title),
                                            selected: entry.chapterIds.contains(
                                              chapter.id,
                                            ),
                                            onSelected:
                                                (_) => onToggleChapter(
                                                  entry.id,
                                                  chapter.id,
                                                ),
                                          ),
                                        )
                                        .toList(),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

class _ReviewCommentResult {
  const _ReviewCommentResult({
    required this.message,
    this.chapterId,
    this.context,
  });

  final String message;
  final String? chapterId;
  final String? context;
}

class _CorkboardTab extends StatelessWidget {
  const _CorkboardTab({
    required this.project,
    required this.onOpenEditor,
    required this.onEditCard,
    required this.onStatusChange,
  });

  final Project project;
  final void Function(Chapter chapter) onOpenEditor;
  final void Function(Chapter chapter) onEditCard;
  final Future<void> Function(Chapter chapter, ChapterStatus status)
  onStatusChange;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: ChapterStatus.values
            .map(
              (status) => Expanded(
                child: _CorkboardColumn(
                  status: status,
                  chapters: project.chapters
                      .where((chapter) => chapter.status == status)
                      .toList(),
                  onOpenEditor: onOpenEditor,
                  onEditCard: onEditCard,
                  onStatusChange: (chapter) => onStatusChange(chapter, status),
                ),
              ),
            )
            .toList(),
      ),
    );
  }
}

class _CorkboardColumn extends StatelessWidget {
  const _CorkboardColumn({
    required this.status,
    required this.chapters,
    required this.onOpenEditor,
    required this.onEditCard,
    required this.onStatusChange,
  });

  final ChapterStatus status;
  final List<Chapter> chapters;
  final void Function(Chapter chapter) onOpenEditor;
  final void Function(Chapter chapter) onEditCard;
  final Future<void> Function(Chapter chapter) onStatusChange;

  @override
  Widget build(BuildContext context) {
    final meta = _statusMeta(status);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 10,
                height: 10,
                decoration: BoxDecoration(
                  color: meta.color,
                  shape: BoxShape.circle,
                ),
              ),
              const SizedBox(width: 8),
              Text(
                meta.label,
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(width: 6),
              Chip(
                label: Text('${chapters.length}'),
                materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
            ],
          ),
          const SizedBox(height: 12),
          Expanded(
            child: DragTarget<Chapter>(
              onWillAccept: (chapter) => chapter?.status != status,
              onAccept: onStatusChange,
              builder: (context, candidates, rejects) {
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  padding: const EdgeInsets.only(bottom: 16),
                  decoration: BoxDecoration(
                    color:
                        candidates.isNotEmpty
                            ? meta.color.withOpacity(0.1)
                            : Colors.transparent,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: meta.color.withOpacity(0.4),
                    ),
                  ),
                  child: chapters.isEmpty
                      ? const Center(
                        child: Padding(
                          padding: EdgeInsets.all(12),
                          child: Text('Sem cartões.'),
                        ),
                      )
                      : ListView.separated(
                        padding: const EdgeInsets.all(12),
                        itemCount: chapters.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 12),
                        itemBuilder: (context, index) {
                          final chapter = chapters[index];
                          return LongPressDraggable<Chapter>(
                            data: chapter,
                            feedback: Material(
                              elevation: 6,
                              borderRadius: BorderRadius.circular(12),
                              color: Colors.transparent,
                              child: SizedBox(
                                width: 200,
                                child: _CorkboardCard(
                                  chapter: chapter,
                                  onTap: () {},
                                  onEdit: () {},
                                ),
                              ),
                            ),
                            child: _CorkboardCard(
                              chapter: chapter,
                              onTap: () => onOpenEditor(chapter),
                              onEdit: () => onEditCard(chapter),
                            ),
                          );
                        },
                      ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _CorkboardCard extends StatelessWidget {
  const _CorkboardCard({
    required this.chapter,
    required this.onTap,
    required this.onEdit,
  });

  final Chapter chapter;
  final VoidCallback onTap;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context) {
    final meta = _statusMeta(chapter.status);
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (chapter.coverImage.isNotEmpty)
              AspectRatio(
                aspectRatio: 16 / 9,
                child: Image.network(
                  chapter.coverImage,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    color: Colors.grey.shade200,
                    alignment: Alignment.center,
                    child: const Icon(Icons.image_not_supported_outlined),
                  ),
                ),
              )
            else
              Container(
                height: 90,
                color: Colors.grey.shade200,
                alignment: Alignment.center,
                child: const Icon(Icons.image_outlined),
              ),
            Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          chapter.title,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                      ),
                      IconButton(
                        onPressed: onEdit,
                        icon: const Icon(Icons.edit),
                        tooltip: 'Editar cartão',
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    chapter.summary.isEmpty
                        ? 'Sem sinopse.'
                        : chapter.summary,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 8),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Chip(
                      label: Text(meta.label),
                      backgroundColor: meta.color.withOpacity(0.15),
                      side: BorderSide(color: meta.color.withOpacity(0.3)),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusMeta {
  const _StatusMeta(this.label, this.color);
  final String label;
  final Color color;
}

_StatusMeta _statusMeta(ChapterStatus status) {
  switch (status) {
    case ChapterStatus.draft:
      return const _StatusMeta('Rascunho', Colors.orange);
    case ChapterStatus.revision:
      return const _StatusMeta('Revisão', Colors.blueAccent);
    case ChapterStatus.finalDraft:
      return const _StatusMeta('Pronto', Colors.green);
  }
}

class _InsightsTab extends StatefulWidget {
  const _InsightsTab({
    required this.project,
    required this.snapshot,
    required this.onRefresh,
    required this.onExport,
  });

  final Project project;
  final InsightsSnapshot? snapshot;
  final Future<void> Function() onRefresh;
  final Future<void> Function() onExport;

  @override
  State<_InsightsTab> createState() => _InsightsTabState();
}

class _InsightsTabState extends State<_InsightsTab> {
  bool _refreshing = false;

  Future<void> _refresh() async {
    setState(() {
      _refreshing = true;
    });
    try {
      await widget.onRefresh();
    } finally {
      if (mounted) {
        setState(() {
          _refreshing = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final snapshot = widget.snapshot;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            FilledButton.icon(
              onPressed: _refreshing ? null : _refresh,
              icon: const Icon(Icons.auto_graph),
              label: Text(_refreshing ? 'Gerando...' : 'Gerar insights'),
            ),
            const SizedBox(width: 12),
            OutlinedButton.icon(
              onPressed:
                  _refreshing || snapshot == null ? null : widget.onExport,
              icon: const Icon(Icons.download_outlined),
              label: const Text('Exportar TXT'),
            ),
            const SizedBox(width: 12),
            if (snapshot != null)
              Text(
                'Atualizado em ${snapshot.generatedAt.toLocal()}',
                style: Theme.of(context).textTheme.bodySmall,
              ),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              snapshot == null
                  ? const Center(
                    child: Text(
                      'Nenhum insight disponível ainda. Gere uma análise para '
                      'visualizar estilo, alertas e sugestões.',
                    ),
                  )
                  : ListView(
                    children: [
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Métricas de estilo',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 12),
                              Wrap(
                                spacing: 12,
                                runSpacing: 8,
                                children: [
                                  _StatChip(
                                    label: 'Palavras / sentença',
                                    value: snapshot.style.averageSentenceLength
                                        .toStringAsFixed(1),
                                  ),
                                  _StatChip(
                                    label: 'Densidade de diálogos',
                                    value:
                                        '${(snapshot.style.dialogueDensity * 100).round()}%',
                                  ),
                                  _StatChip(
                                    label: 'Rácio descrição',
                                    value:
                                        '${(snapshot.style.descriptionRatio * 100).round()}%',
                                  ),
                                  _StatChip(
                                    label: 'Ritmo (0-1)',
                                    value: snapshot.style.paceScore
                                        .toStringAsFixed(2),
                                  ),
                                  _StatChip(
                                    label: 'Var. emocional',
                                    value: snapshot.style.emotionalVariance
                                        .toStringAsFixed(3),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Extensões instaladas',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 8),
                              if (snapshot.extensions.isEmpty)
                                const Text(
                                  'Nenhuma extensão ativa. Adicione manifests JSON à '
                                  'pasta storyflame_extensions para expandir as análises.',
                                )
                              else
                                ...snapshot.extensions.map(
                                  (extension) => ListTile(
                                    contentPadding: EdgeInsets.zero,
                                    leading: const Icon(Icons.extension),
                                    title: Text(extension.name),
                                    subtitle: Text(
                                      'v${extension.version} · ${extension.author}',
                                    ),
                                    trailing: Chip(
                                      label: Text(
                                        '${extension.rules.length} regra(s)',
                                      ),
                                    ),
                                  ),
                                ),
                              const SizedBox(height: 8),
                              Text(
                                'Copie arquivos JSON para o diretório de documentos/'
                                'storyflame_extensions (ou adicione em assets) '
                                'para registrar novas extensões.',
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                            ],
                          ),
                        ),
                      ),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Insights gerados',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 8),
                              if (snapshot.insights.isEmpty)
                                const Text(
                                  'Nenhum alerta detectado. Continue acompanhando.',
                                )
                              else
                                ...snapshot.insights.map(
                                  (insight) => ListTile(
                                    contentPadding: EdgeInsets.zero,
                                    leading: Icon(
                                      Icons.bolt,
                                      color: _severityColor(insight.severity),
                                    ),
                                    title: Text(insight.title),
                                    subtitle: Text(insight.description),
                                    trailing: Chip(
                                      label: Text(insight.category.label),
                                    ),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Sugestões de prompts',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 8),
                              if (snapshot.prompts.isEmpty)
                                const Text('Sem prompts sugeridos no momento.')
                              else
                                ...snapshot.prompts.map(
                                  (prompt) => ListTile(
                                    contentPadding: EdgeInsets.zero,
                                    leading: const Icon(
                                      Icons.lightbulb_outline,
                                    ),
                                    title: Text(prompt.prompt),
                                    subtitle: Text(prompt.context),
                                    trailing: Text(
                                      prompt.tags.join(', '),
                                      style:
                                          Theme.of(context).textTheme.bodySmall,
                                    ),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Radar por capítulo',
                                style: Theme.of(context).textTheme.titleMedium,
                              ),
                              const SizedBox(height: 8),
                              ...snapshot.chapters.map(
                                (entry) => ListTile(
                                  contentPadding: EdgeInsets.zero,
                                  title: Text(entry.chapterTitle),
                                  subtitle: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Palavras: ${entry.wordCount} | '
                                        'Diálogos: ${(entry.dialogueDensity * 100).round()}%',
                                      ),
                                      if (entry.repeatedTerms.isNotEmpty)
                                        Text(
                                          'Termos frequentes: ${entry.repeatedTerms.join(', ')}',
                                          style:
                                              Theme.of(
                                                context,
                                              ).textTheme.bodySmall,
                                        ),
                                      if (entry.characterMentions.isNotEmpty)
                                        Text(
                                          'Personagens: ${entry.characterMentions.join(', ')}',
                                          style:
                                              Theme.of(
                                                context,
                                              ).textTheme.bodySmall,
                                        ),
                                    ],
                                  ),
                                  trailing:
                                      entry.goalDelta >= 0
                                          ? const Icon(
                                            Icons.check_circle_outline,
                                          )
                                          : Icon(
                                            Icons.info_outline,
                                            color:
                                                Theme.of(
                                                  context,
                                                ).colorScheme.error,
                                          ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
        ),
      ],
    );
  }

  Color _severityColor(InsightSeverity severity) {
    switch (severity) {
      case InsightSeverity.info:
        return Colors.blueAccent;
      case InsightSeverity.warning:
        return Colors.orange;
      case InsightSeverity.critical:
        return Colors.red;
    }
  }
}

class _ReviewTab extends StatelessWidget {
  const _ReviewTab({
    required this.project,
    required this.onCreate,
    required this.onToggle,
    required this.onDelete,
  });

  final Project project;
  final VoidCallback onCreate;
  final Future<void> Function(String id, bool resolved) onToggle;
  final Future<void> Function(String id) onDelete;

  @override
  Widget build(BuildContext context) {
    final comments = project.reviewComments;
    final openCount = comments.where((comment) => !comment.resolved).length;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            FilledButton.icon(
              onPressed: onCreate,
              icon: const Icon(Icons.add_comment),
              label: const Text('Novo comentário'),
            ),
            const SizedBox(width: 12),
            Text(
              '$openCount pendente(s)',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              comments.isEmpty
                  ? const Center(child: Text('Nenhum comentário registrado.'))
                  : ListView.separated(
                    itemCount: comments.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final comment = comments[index];
                      String? chapterName;
                      if (comment.targetChapterId != null) {
                        try {
                          chapterName =
                              project.chapters
                                  .firstWhere(
                                    (chapter) =>
                                        chapter.id == comment.targetChapterId,
                                  )
                                  .title;
                        } catch (_) {
                          chapterName = 'Capítulo removido';
                        }
                      }
                      return Card(
                        child: ListTile(
                          leading: Checkbox(
                            value: comment.resolved,
                            onChanged:
                                (value) => onToggle(comment.id, value ?? false),
                          ),
                          title: Text(comment.message),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              if (comment.context != null &&
                                  comment.context!.isNotEmpty)
                                Text(comment.context!),
                              const SizedBox(height: 4),
                              Text(
                                'Autor: ${comment.author} · '
                                'Criado em ${comment.createdAt.toLocal()}',
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              if (comment.targetChapterId != null)
                                Text(
                                  'Capítulo: $chapterName',
                                  style: Theme.of(context).textTheme.bodySmall,
                                ),
                            ],
                          ),
                          trailing: IconButton(
                            icon: const Icon(Icons.delete_outline),
                            onPressed: () => onDelete(comment.id),
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

class _TimelineTab extends StatefulWidget {
  const _TimelineTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onReorder,
  });

  final Project project;
  final VoidCallback onCreate;
  final void Function(TimelineEvent event) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(List<TimelineEvent> events) onReorder;

  @override
  State<_TimelineTab> createState() => _TimelineTabState();
}

class _TimelineTabState extends State<_TimelineTab> {
  late List<TimelineEvent> _events;

  @override
  void initState() {
    super.initState();
    _events = List<TimelineEvent>.from(widget.project.timeline);
  }

  @override
  void didUpdateWidget(covariant _TimelineTab oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.project.timeline != widget.project.timeline) {
      _events = List<TimelineEvent>.from(widget.project.timeline);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        FilledButton.icon(
          onPressed: widget.onCreate,
          icon: const Icon(Icons.add),
          label: const Text('Novo evento'),
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              _events.isEmpty
                  ? const Center(child: Text('Nenhum evento na timeline.'))
                  : ReorderableListView.builder(
                    itemCount: _events.length,
                    onReorder: (oldIndex, newIndex) {
                      setState(() {
                        if (newIndex > oldIndex) newIndex -= 1;
                        final item = _events.removeAt(oldIndex);
                        _events.insert(newIndex, item);
                      });
                      widget.onReorder(_events);
                    },
                    itemBuilder: (context, index) {
                      final event = _events[index];
                      return Card(
                        key: ValueKey(event.id),
                        child: ListTile(
                          title: Text(event.title),
                          subtitle: Text(
                            event.date == null
                                ? 'Sem data'
                                : event.date!
                                    .toLocal()
                                    .toString()
                                    .split('.')
                                    .first,
                          ),
                          trailing: Wrap(
                            spacing: 8,
                            children: [
                              IconButton(
                                icon: const Icon(Icons.edit),
                                onPressed: () => widget.onEdit(event),
                              ),
                              IconButton(
                                icon: const Icon(Icons.delete_outline),
                                onPressed: () => widget.onDelete(event.id),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

class _WorldTab extends StatefulWidget {
  const _WorldTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onToggleChapter,
  });

  final Project project;
  final void Function(WorldElementType type) onCreate;
  final void Function(WorldElement element) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(String elementId, String chapterId)
  onToggleChapter;

  @override
  State<_WorldTab> createState() => _WorldTabState();
}

class _CreativeIdeasTab extends StatefulWidget {
  const _CreativeIdeasTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onStatusChange,
  });

  final Project project;
  final VoidCallback onCreate;
  final void Function(CreativeIdea idea) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(String id, CreativeIdeaStatus status)
  onStatusChange;

  @override
  State<_CreativeIdeasTab> createState() => _CreativeIdeasTabState();
}

class _CreativeIdeasTabState extends State<_CreativeIdeasTab> {
  CreativeIdeaStatus? _filter;

  @override
  Widget build(BuildContext context) {
    final ideas =
        _filter == null
            ? widget.project.creativeIdeas
            : widget.project.creativeIdeas
                .where((idea) => idea.status == _filter)
                .toList();
    return Column(
      children: [
        Row(
          children: [
            FilledButton.icon(
              onPressed: widget.onCreate,
              icon: const Icon(Icons.add_comment),
              label: const Text('Nova ideia'),
            ),
            const SizedBox(width: 12),
            DropdownButton<CreativeIdeaStatus?>(
              value: _filter,
              hint: const Text('Filtro'),
              items: [
                const DropdownMenuItem(value: null, child: Text('Todas')),
                ...CreativeIdeaStatus.values.map(
                  (value) => DropdownMenuItem(
                    value: value,
                    child: Text(switch (value) {
                      CreativeIdeaStatus.idea => 'Ideias',
                      CreativeIdeaStatus.draft => 'Rascunhos',
                      CreativeIdeaStatus.done => 'Concluídas',
                    }),
                  ),
                ),
              ],
              onChanged: (value) => setState(() => _filter = value),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              ideas.isEmpty
                  ? const Center(child: Text('Nenhuma ideia cadastrada.'))
                  : ListView.separated(
                    itemCount: ideas.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final idea = ideas[index];
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      idea.title,
                                      style:
                                          Theme.of(
                                            context,
                                          ).textTheme.titleMedium,
                                    ),
                                  ),
                                  PopupMenuButton<CreativeIdeaStatus>(
                                    icon: Icon(
                                      Icons.flag,
                                      color: _statusColor(idea.status),
                                    ),
                                    tooltip: 'Alterar status',
                                    onSelected:
                                        (status) => widget.onStatusChange(
                                          idea.id,
                                          status,
                                        ),
                                    itemBuilder:
                                        (_) =>
                                            CreativeIdeaStatus.values
                                                .map(
                                                  (value) => PopupMenuItem(
                                                    value: value,
                                                    child: Text(switch (value) {
                                                      CreativeIdeaStatus.idea =>
                                                        'Ideia',
                                                      CreativeIdeaStatus
                                                          .draft =>
                                                        'Rascunho',
                                                      CreativeIdeaStatus.done =>
                                                        'Concluída',
                                                    }),
                                                  ),
                                                )
                                                .toList(),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.edit),
                                    onPressed: () => widget.onEdit(idea),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete_outline),
                                    onPressed: () => widget.onDelete(idea.id),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 8),
                              Text(idea.description),
                              if (idea.tags.isNotEmpty) ...[
                                const SizedBox(height: 8),
                                Wrap(
                                  spacing: 8,
                                  children:
                                      idea.tags
                                          .map(
                                            (tag) => Chip(label: Text('#$tag')),
                                          )
                                          .toList(),
                                ),
                              ],
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }

  Color? _statusColor(CreativeIdeaStatus status) {
    switch (status) {
      case CreativeIdeaStatus.idea:
        return Colors.blueAccent;
      case CreativeIdeaStatus.draft:
        return Colors.orangeAccent;
      case CreativeIdeaStatus.done:
        return Colors.green;
    }
  }
}

class _TemplatesTab extends StatelessWidget {
  const _TemplatesTab({
    required this.project,
    required this.onCreate,
    required this.onEdit,
    required this.onDelete,
    required this.onToggleStep,
    required this.onApplyBuiltIn,
    required this.builtInTemplates,
  });

  final Project project;
  final VoidCallback onCreate;
  final void Function(NarrativeTemplate template) onEdit;
  final Future<void> Function(String id) onDelete;
  final Future<void> Function(String templateId, String step) onToggleStep;
  final Future<void> Function(NarrativeTemplate template) onApplyBuiltIn;
  final List<NarrativeTemplate> builtInTemplates;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            FilledButton.icon(
              onPressed: onCreate,
              icon: const Icon(Icons.auto_stories),
              label: const Text('Novo template'),
            ),
            const SizedBox(width: 12),
            OutlinedButton.icon(
              onPressed: () => _showBuiltInPicker(context),
              icon: const Icon(Icons.library_books),
              label: const Text('Galeria de templates'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              project.templates.isEmpty
                  ? const Center(child: Text('Nenhum template no projeto.'))
                  : ListView.separated(
                    itemCount: project.templates.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final template = project.templates[index];
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      template.name,
                                      style:
                                          Theme.of(
                                            context,
                                          ).textTheme.titleMedium,
                                    ),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.edit),
                                    onPressed: () => onEdit(template),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete_outline),
                                    onPressed: () => onDelete(template.id),
                                  ),
                                ],
                              ),
                              Text(template.description),
                              const SizedBox(height: 8),
                              ...template.steps.map(
                                (step) => CheckboxListTile(
                                  dense: true,
                                  title: Text(step),
                                  value: template.completedSteps.contains(step),
                                  onChanged:
                                      (_) => onToggleStep(template.id, step),
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

class _PublicationTab extends StatelessWidget {
  const _PublicationTab({
    required this.project,
    required this.onExportEpub,
    required this.onExportBundle,
    required this.onOpenKdpWizard,
    required this.onChecklistChanged,
  });

  final Project project;
  final VoidCallback onExportEpub;
  final VoidCallback onExportBundle;
  final VoidCallback onOpenKdpWizard;
  final Future<void> Function(PublicationChecklist checklist)
  onChecklistChanged;

  @override
  Widget build(BuildContext context) {
    final checklist = project.publicationChecklist;
    final history = project.publicationHistory.reversed.toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            FilledButton.icon(
              onPressed: onExportEpub,
              icon: const Icon(Icons.book_outlined),
              label: const Text('Exportar ePub/Mobi'),
            ),
            FilledButton.icon(
              onPressed: onExportBundle,
              icon: const Icon(Icons.archive_outlined),
              label: const Text('Pacote multimídia'),
            ),
            OutlinedButton.icon(
              onPressed: onOpenKdpWizard,
              icon: const Icon(Icons.cloud_upload_outlined),
              label: const Text('Assistente KDP/Wattpad'),
            ),
          ],
        ),
        const SizedBox(height: 16),
        Expanded(
          child: ListView(
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Checklist de publicação',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 8),
                      _ChecklistTile(
                        label: 'Beta readers concluídos',
                        value: checklist.betaRead,
                        onChanged:
                            (value) => onChecklistChanged(
                              checklist.copyWith(betaRead: value),
                            ),
                      ),
                      _ChecklistTile(
                        label: 'Capa aprovada',
                        value: checklist.coverReady,
                        onChanged:
                            (value) => onChecklistChanged(
                              checklist.copyWith(coverReady: value),
                            ),
                      ),
                      _ChecklistTile(
                        label: 'ISBN registrado',
                        value: checklist.isbnRegistered,
                        onChanged:
                            (value) => onChecklistChanged(
                              checklist.copyWith(isbnRegistered: value),
                            ),
                      ),
                      _ChecklistTile(
                        label: 'eBook exportado',
                        value: checklist.ebookExported,
                        onChanged:
                            (value) => onChecklistChanged(
                              checklist.copyWith(ebookExported: value),
                            ),
                      ),
                      _ChecklistTile(
                        label: 'Pacote KDP pronto',
                        value: checklist.kdpPackageReady,
                        onChanged:
                            (value) => onChecklistChanged(
                              checklist.copyWith(kdpPackageReady: value),
                            ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Histórico de releases',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      if (history.isEmpty)
                        const Padding(
                          padding: EdgeInsets.symmetric(vertical: 8),
                          child: Text('Nenhum registro ainda.'),
                        )
                      else
                        ...history.map(
                          (record) => ListTile(
                            contentPadding: EdgeInsets.zero,
                            leading: Icon(_iconForChannel(record.channel)),
                            title: Text(
                              record.notes.isEmpty
                                  ? record.channel.name
                                  : record.notes,
                            ),
                            subtitle: Text(
                              '${record.createdAt.toLocal().toString().split('.').first}\n${record.filePath}',
                            ),
                            isThreeLine: true,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  static IconData _iconForChannel(PublicationChannel channel) {
    switch (channel) {
      case PublicationChannel.epub:
        return Icons.book_outlined;
      case PublicationChannel.mobi:
        return Icons.auto_stories;
      case PublicationChannel.bundle:
        return Icons.archive_outlined;
      case PublicationChannel.kdp:
        return Icons.cloud_upload_outlined;
      case PublicationChannel.wattpad:
        return Icons.public;
    }
  }
}

class _ChecklistTile extends StatelessWidget {
  const _ChecklistTile({
    required this.label,
    required this.value,
    required this.onChanged,
  });

  final String label;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return SwitchListTile(
      contentPadding: EdgeInsets.zero,
      title: Text(label),
      value: value,
      onChanged: (newValue) => onChanged(newValue),
    );
  }
}

extension on _TemplatesTab {
  Future<void> _showBuiltInPicker(BuildContext context) async {
    final template = await showModalBottomSheet<NarrativeTemplate>(
      context: context,
      builder:
          (_) => ListView.separated(
            padding: const EdgeInsets.all(16),
            itemBuilder: (context, index) {
              final candidate = builtInTemplates[index];
              return ListTile(
                title: Text(candidate.name),
                subtitle: Text(candidate.description),
                onTap: () => Navigator.of(context).pop(candidate),
              );
            },
            separatorBuilder: (_, __) => const Divider(),
            itemCount: builtInTemplates.length,
          ),
    );
    if (template != null) {
      await onApplyBuiltIn(template);
    }
  }
}

class _WorldTabState extends State<_WorldTab> {
  WorldElementType _filter = WorldElementType.location;

  @override
  Widget build(BuildContext context) {
    final elements =
        widget.project.worldElements
            .where((element) => element.type == _filter)
            .toList();
    return Column(
      children: [
        Row(
          children: [
            SegmentedButton<WorldElementType>(
              segments: const [
                ButtonSegment(
                  value: WorldElementType.location,
                  label: Text('Locais'),
                  icon: Icon(Icons.place_outlined),
                ),
                ButtonSegment(
                  value: WorldElementType.item,
                  label: Text('Itens'),
                  icon: Icon(Icons.auto_awesome),
                ),
              ],
              selected: {_filter},
              onSelectionChanged:
                  (selection) => setState(() => _filter = selection.first),
            ),
            const Spacer(),
            FilledButton.icon(
              onPressed: () => widget.onCreate(_filter),
              icon: const Icon(Icons.add),
              label: const Text('Adicionar'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Expanded(
          child:
              elements.isEmpty
                  ? Center(
                    child: Text(
                      _filter == WorldElementType.location
                          ? 'Nenhum local cadastrado.'
                          : 'Nenhum item cadastrado.',
                    ),
                  )
                  : ListView.separated(
                    itemCount: elements.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final element = elements[index];
                      return Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      element.name,
                                      style:
                                          Theme.of(
                                            context,
                                          ).textTheme.titleMedium,
                                    ),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.edit),
                                    onPressed: () => widget.onEdit(element),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete_outline),
                                    onPressed:
                                        () => widget.onDelete(element.id),
                                  ),
                                ],
                              ),
                              Text(element.description),
                              if (element.lore.isNotEmpty) ...[
                                const SizedBox(height: 8),
                                Text(
                                  element.lore,
                                  style: Theme.of(context).textTheme.bodySmall,
                                ),
                              ],
                              const SizedBox(height: 8),
                              Wrap(
                                spacing: 8,
                                children:
                                    widget.project.chapters
                                        .map(
                                          (chapter) => FilterChip(
                                            label: Text(chapter.title),
                                            selected: element.chapterIds
                                                .contains(chapter.id),
                                            onSelected:
                                                (_) => widget.onToggleChapter(
                                                  element.id,
                                                  chapter.id,
                                                ),
                                          ),
                                        )
                                        .toList(),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
        ),
      ],
    );
  }
}

List<NarrativeTemplate> get _builtInTemplates => [
  NarrativeTemplate(
    id: 'three-acts',
    name: 'Estrutura de Três Atos',
    description: 'Divida a narrativa em preparação, confronto e resolução.',
    steps: const [
      'Ato I - Apresentação dos personagens e gatilho',
      'Ato II - Conflito crescente e ponto médio',
      'Ato III - Clímax e desfecho',
    ],
    completedSteps: const [],
    updatedAt: DateTime.now(),
  ),
  NarrativeTemplate(
    id: 'hero-journey',
    name: 'Jornada do Herói',
    description: 'Etapas clássicas de transformação do protagonista.',
    steps: const [
      'Mundo comum e chamado à aventura',
      'Recusa, mentor e travessia do limiar',
      'Provas, aliados, inimigos',
      'Provação suprema e recompensa',
      'Retorno e elixir',
    ],
    completedSteps: const [],
    updatedAt: DateTime.now(),
  ),
];

class _StatChip extends StatelessWidget {
  const _StatChip({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Chip(
      label: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(value, style: Theme.of(context).textTheme.titleMedium),
          Text(label, style: Theme.of(context).textTheme.bodySmall),
        ],
      ),
    );
  }
}

class _GoalProgress extends StatelessWidget {
  const _GoalProgress({
    required this.current,
    required this.goal,
    required this.label,
  });

  final int current;
  final int goal;
  final String label;

  @override
  Widget build(BuildContext context) {
    final goalLabel = goal <= 0 ? '-' : goal.toString();
    final progress = goal <= 0 ? 0.0 : (current / goal).clamp(0.0, 1.0);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('$label ($current/$goalLabel)'),
        const SizedBox(height: 4),
        LinearProgressIndicator(value: goal <= 0 ? null : progress),
      ],
    );
  }
}

class _GoalResult {
  const _GoalResult(this.dailyGoal, this.totalGoal);
  final int dailyGoal;
  final int totalGoal;
}

class _GoalSheet extends StatefulWidget {
  const _GoalSheet({required this.dailyGoal, required this.totalGoal});
  final int dailyGoal;
  final int totalGoal;

  @override
  State<_GoalSheet> createState() => _GoalSheetState();
}

class _GoalSheetState extends State<_GoalSheet> {
  late final TextEditingController _dailyController;
  late final TextEditingController _totalController;

  @override
  void initState() {
    super.initState();
    _dailyController = TextEditingController(text: widget.dailyGoal.toString());
    _totalController = TextEditingController(text: widget.totalGoal.toString());
  }

  @override
  void dispose() {
    _dailyController.dispose();
    _totalController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.of(context).viewInsets.bottom;
    return Padding(
      padding: EdgeInsets.only(bottom: bottom, left: 16, right: 16, top: 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _dailyController,
            decoration: const InputDecoration(
              labelText: 'Meta diária (palavras)',
            ),
            keyboardType: TextInputType.number,
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _totalController,
            decoration: const InputDecoration(
              labelText: 'Meta total (palavras)',
            ),
            keyboardType: TextInputType.number,
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed:
                  () => Navigator.of(context).pop(
                    _GoalResult(
                      int.tryParse(_dailyController.text) ?? 0,
                      int.tryParse(_totalController.text) ?? 0,
                    ),
                  ),
              child: const Text('Salvar'),
            ),
          ),
          const SizedBox(height: 12),
        ],
      ),
    );
  }
}

class _PasswordSheet extends StatefulWidget {
  const _PasswordSheet({required this.initialProtected});
  final bool initialProtected;

  @override
  State<_PasswordSheet> createState() => _PasswordSheetState();
}

class _PasswordSheetState extends State<_PasswordSheet> {
  final TextEditingController _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.of(context).viewInsets.bottom;
    return Padding(
      padding: EdgeInsets.only(bottom: bottom, left: 16, right: 16, top: 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            widget.initialProtected
                ? 'Informe uma nova senha ou deixe em branco para remover.'
                : 'Defina uma senha para proteger o projeto.',
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _controller,
            decoration: const InputDecoration(labelText: 'Senha'),
            obscureText: true,
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed: () => Navigator.of(context).pop(_controller.text),
              child: const Text('Salvar'),
            ),
          ),
          const SizedBox(height: 12),
        ],
      ),
    );
  }
}
