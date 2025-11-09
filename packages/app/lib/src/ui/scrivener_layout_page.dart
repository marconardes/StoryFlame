import 'package:domain/domain.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../project_store.dart';
import 'chapter_editor_page.dart';

class ScrivenerLayoutPage extends StatefulWidget {
  const ScrivenerLayoutPage({super.key, required this.projectId});

  final String projectId;

  @override
  State<ScrivenerLayoutPage> createState() => _ScrivenerLayoutPageState();
}

class _ScrivenerLayoutPageState extends State<ScrivenerLayoutPage> {
  int _selectedIndex = 0;

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
        appBar: AppBar(title: const Text('Layout Scrivener')),
        body: const Center(child: Text('Projeto não encontrado.')),
      );
    }
    final project = maybeProject;

    final chapters = project.chapters;
    final selectedIndex =
        chapters.isEmpty
            ? -1
            : _selectedIndex.clamp(0, chapters.length - 1);
    final Chapter? selected =
        chapters.isEmpty ? null : chapters[selectedIndex];

    return Scaffold(
      appBar: AppBar(
        title: Text('${project.title} · Layout Scrivener'),
        actions: [
          if (selected != null)
            IconButton(
              tooltip: 'Editar capítulo',
              icon: const Icon(Icons.edit_note),
              onPressed: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder:
                        (_) => ChapterEditorPage(
                          projectId: project.id,
                          chapter: selected,
                        ),
                  ),
                );
              },
            ),
        ],
      ),
      body: Row(
        children: [
          Container(
            width: 250,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surfaceVariant.withOpacity(0.2),
              border: Border(
                right: BorderSide(
                  color: Theme.of(context).dividerColor,
                ),
              ),
            ),
            child: _BinderList(
              chapters: chapters,
              selectedIndex: selectedIndex,
              onSelected: (index) => setState(() => _selectedIndex = index),
              statusIconBuilder: _statusIcon,
            ),
          ),
          const VerticalDivider(width: 1),
          Expanded(
            child:
                selected == null
                    ? const Center(
                      child: Text('Crie capítulos para visualizar o binder.'),
                    )
                    : _ScrivenerDetail(
                      projectId: project.id,
                      chapter: selected,
                    ),
          ),
        ],
      ),
    );
  }

  Icon _statusIcon(ChapterStatus status) {
    switch (status) {
      case ChapterStatus.draft:
        return const Icon(Icons.circle, color: Colors.orange);
      case ChapterStatus.revision:
        return const Icon(Icons.circle, color: Colors.blueAccent);
      case ChapterStatus.finalDraft:
        return const Icon(Icons.circle, color: Colors.green);
    }
  }
}

class _ScrivenerDetail extends StatelessWidget {
  const _ScrivenerDetail({
    required this.projectId,
    required this.chapter,
  });

  final String projectId;
  final Chapter chapter;

  @override
  Widget build(BuildContext context) {
    final store = context.read<ProjectStore>();
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            chapter.title,
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              Chip(label: Text(chapter.status.label)),
              const SizedBox(width: 12),
              Text('Palavras: ${chapter.wordCount}'),
              const SizedBox(width: 12),
              Text(
                'Atualizado em: '
                '${chapter.updatedAt.toLocal().toString().split('.').first}',
              ),
            ],
          ),
          const SizedBox(height: 16),
          Expanded(
            child: Row(
              children: [
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Resumo',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 8),
                          Expanded(
                            child: SingleChildScrollView(
                              child: Text(
                                chapter.summary.isEmpty
                                    ? 'Sem resumo.'
                                    : chapter.summary,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Notas rápidas',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 8),
                          DropdownButtonFormField<ChapterStatus>(
                            value: chapter.status,
                            decoration: const InputDecoration(
                              labelText: 'Status',
                              border: OutlineInputBorder(),
                            ),
                            items: ChapterStatus.values
                                .map(
                                  (status) => DropdownMenuItem(
                                    value: status,
                                    child: Text(status.label),
                                  ),
                                )
                                .toList(),
                            onChanged: (value) async {
                              if (value == null) return;
                              final updated = chapter.copyWith(
                                status: value,
                                updatedAt: DateTime.now(),
                              );
                              await store.updateChapter(projectId, updated);
                            },
                          ),
                          const SizedBox(height: 12),
                          TextFormField(
                            initialValue: chapter.coverImage,
                            decoration: const InputDecoration(
                              labelText: 'URL da capa',
                              border: OutlineInputBorder(),
                            ),
                            onFieldSubmitted: (value) async {
                              final updated = chapter.copyWith(
                                coverImage: value.trim(),
                                updatedAt: DateTime.now(),
                              );
                              await store.updateChapter(projectId, updated);
                            },
                          ),
                          const SizedBox(height: 12),
                          Align(
                            alignment: Alignment.centerRight,
                            child: TextButton.icon(
                              onPressed: () {
                                Navigator.of(context).push(
                                  MaterialPageRoute(
                                    builder:
                                        (_) => ChapterEditorPage(
                                          projectId: projectId,
                                          chapter: chapter,
                                        ),
                                  ),
                                );
                              },
                              icon: const Icon(Icons.open_in_new),
                              label: const Text('Abrir editor completo'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _BinderList extends StatelessWidget {
  const _BinderList({
    required this.chapters,
    required this.selectedIndex,
    required this.onSelected,
    required this.statusIconBuilder,
  });

  final List<Chapter> chapters;
  final int selectedIndex;
  final void Function(int) onSelected;
  final Icon Function(ChapterStatus) statusIconBuilder;

  @override
  Widget build(BuildContext context) {
    if (chapters.isEmpty) {
      return const Center(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Text('Sem capítulos cadastrados.'),
        ),
      );
    }
    return ListView.separated(
      itemCount: chapters.length,
      separatorBuilder: (_, __) => const Divider(height: 1),
      itemBuilder: (context, index) {
        final chapter = chapters[index];
        final selected = index == selectedIndex;
        return Material(
          color: selected
              ? Theme.of(context).colorScheme.primary.withOpacity(0.1)
              : Colors.transparent,
          child: ListTile(
            dense: true,
            leading: statusIconBuilder(chapter.status),
            title: Text(
              chapter.title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            subtitle:
                chapter.summary.isEmpty
                    ? null
                    : Text(
                      chapter.summary,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
            selected: selected,
            onTap: () => onSelected(index),
          ),
        );
      },
    );
  }
}
