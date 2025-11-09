import 'dart:async';

import 'package:domain/domain.dart';
import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:provider/provider.dart';

import '../project_store.dart';

class ChapterEditorPage extends StatefulWidget {
  const ChapterEditorPage({
    super.key,
    required this.projectId,
    required this.chapter,
  });

  final String projectId;
  final Chapter chapter;

  @override
  State<ChapterEditorPage> createState() => _ChapterEditorPageState();
}

class _ChapterEditorPageState extends State<ChapterEditorPage> {
  late TextEditingController _summaryController;
  late TextEditingController _contentController;
  late TextEditingController _coverController;
  late Chapter _current;
  Timer? _debounce;
  DateTime? _lastSaved;
  bool _saving = false;
  bool _showPreview = false;

  @override
  void initState() {
    super.initState();
    _current = widget.chapter;
    _summaryController = TextEditingController(text: widget.chapter.summary)
      ..addListener(_scheduleSave);
    _contentController = TextEditingController(text: widget.chapter.content)
      ..addListener(() {
        _scheduleSave();
        setState(() {}); // atualiza preview/contador
      });
    _coverController = TextEditingController(text: widget.chapter.coverImage);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _summaryController.dispose();
    _contentController.dispose();
    _coverController.dispose();
    super.dispose();
  }

  void _scheduleSave() {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 800), _persist);
  }

  Future<void> _persist() async {
    setState(() => _saving = true);
    final updated = _current.copyWith(
      summary: _summaryController.text,
      content: _contentController.text,
      wordCount: Chapter.countWords(_contentController.text),
      updatedAt: DateTime.now(),
    );
    await context.read<ProjectStore>().updateChapter(widget.projectId, updated);
    if (!mounted) return;
    setState(() {
      _current = updated;
      _saving = false;
      _lastSaved = DateTime.now();
    });
  }

  void _wrapSelection(String left, String right) {
    final selection = _contentController.selection;
    final text = _contentController.text;
    final selectedText = selection.textInside(text);
    final newText =
        selection.textBefore(text) +
        left +
        selectedText +
        right +
        selection.textAfter(text);
    _contentController.value = _contentController.value.copyWith(
      text: newText,
      selection: TextSelection.collapsed(
        offset:
            selection.start + left.length + selectedText.length + right.length,
      ),
    );
  }

  Future<void> _updateStatus(ChapterStatus status) async {
    final updated = _current.copyWith(
      status: status,
      updatedAt: DateTime.now(),
    );
    await context.read<ProjectStore>().updateChapter(
          widget.projectId,
          updated,
        );
    if (!mounted) return;
    setState(() => _current = updated);
  }

  Future<void> _updateCover(String url) async {
    final updated = _current.copyWith(
      coverImage: url.trim(),
      updatedAt: DateTime.now(),
    );
    await context.read<ProjectStore>().updateChapter(
          widget.projectId,
          updated,
        );
    if (!mounted) return;
    setState(() => _current = updated);
  }

  @override
  Widget build(BuildContext context) {
    final wordCount = Chapter.countWords(_contentController.text);
    return Scaffold(
      appBar: AppBar(
        title: Text(_current.title),
        actions: [
          IconButton(
            tooltip: 'Alternar preview',
            icon: Icon(_showPreview ? Icons.visibility_off : Icons.visibility),
            onPressed: () => setState(() => _showPreview = !_showPreview),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: DropdownButtonFormField<ChapterStatus>(
                    value: _current.status,
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
                    onChanged: (value) {
                      if (value != null) _updateStatus(value);
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: _coverController,
                    decoration: InputDecoration(
                      labelText: 'URL da capa (opcional)',
                      border: const OutlineInputBorder(),
                      suffixIcon: IconButton(
                        icon: const Icon(Icons.check),
                        tooltip: 'Aplicar',
                        onPressed: () => _updateCover(_coverController.text),
                      ),
                    ),
                    onSubmitted: _updateCover,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _summaryController,
              decoration: const InputDecoration(
                labelText: 'Resumo',
                border: OutlineInputBorder(),
              ),
              minLines: 2,
              maxLines: 3,
            ),
            const SizedBox(height: 12),
            _MarkdownToolbar(
              onAction: _wrapSelection,
              onInsertSnippet: _insertText,
            ),
            const SizedBox(height: 8),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton.icon(
                onPressed: _showGlossaryPicker,
                icon: const Icon(Icons.bookmark_add_outlined),
                label: const Text('Inserir termo do glossário'),
              ),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _contentController,
                      decoration: const InputDecoration(
                        labelText: 'Conteúdo em Markdown',
                        border: OutlineInputBorder(),
                        alignLabelWithHint: true,
                      ),
                      expands: true,
                      maxLines: null,
                      minLines: null,
                      keyboardType: TextInputType.multiline,
                    ),
                  ),
                  if (_showPreview) ...[
                    const VerticalDivider(width: 24),
                    Expanded(child: Markdown(data: _contentController.text)),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Text('Palavras: $wordCount'),
                const SizedBox(width: 16),
                if (_saving)
                  const Text('Salvando...')
                else if (_lastSaved != null)
                  Text(
                    'Último salvamento: ${_lastSaved!.toLocal().toString().split('.').first}',
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showGlossaryPicker() async {
    final store = context.read<ProjectStore>();
    Project? project;
    try {
      project = store.projects.firstWhere((p) => p.id == widget.projectId);
    } catch (_) {
      project = null;
    }
    if (project == null || project.glossary.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Nenhum termo no glossário.')),
      );
      return;
    }

    final entry = await showModalBottomSheet<GlossaryEntry>(
      context: context,
      builder:
          (_) => ListView.separated(
            padding: const EdgeInsets.all(16),
            itemBuilder: (context, index) {
              final term = project!.glossary[index];
              return ListTile(
                title: Text(term.term),
                subtitle: Text(term.definition),
                onTap: () => Navigator.of(context).pop(term),
              );
            },
            separatorBuilder: (_, __) => const Divider(),
            itemCount: project!.glossary.length,
          ),
    );

    if (entry == null) return;
    _insertText(entry.term);
  }

  void _insertText(String value) {
    final selection = _contentController.selection;
    final text = _contentController.text;
    final newText =
        selection.textBefore(text) + value + selection.textAfter(text);
    final caret = selection.start + value.length;
    _contentController.value = _contentController.value.copyWith(
      text: newText,
      selection: TextSelection.collapsed(offset: caret),
    );
  }
}

class _MarkdownToolbar extends StatelessWidget {
  const _MarkdownToolbar({
    required this.onAction,
    required this.onInsertSnippet,
  });

  final void Function(String left, String right) onAction;
  final void Function(String snippet) onInsertSnippet;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 8,
      children: [
        IconButton(
          tooltip: 'Negrito',
          icon: const Icon(Icons.format_bold),
          onPressed: () => onAction('**', '**'),
        ),
        IconButton(
          tooltip: 'Itálico',
          icon: const Icon(Icons.format_italic),
          onPressed: () => onAction('*', '*'),
        ),
        IconButton(
          tooltip: 'Título',
          icon: const Icon(Icons.title),
          onPressed: () => onAction('\n# ', '\n'),
        ),
        IconButton(
          tooltip: 'Lista',
          icon: const Icon(Icons.format_list_bulleted),
          onPressed: () => onAction('\n- ', ''),
        ),
        IconButton(
          tooltip: 'Citação',
          icon: const Icon(Icons.format_quote),
          onPressed: () => onAction('\n> ', ''),
        ),
        IconButton(
          tooltip: 'Código',
          icon: const Icon(Icons.code),
          onPressed: () => onAction('\n```\n', '\n```\n'),
        ),
        IconButton(
          tooltip: 'Tabela básica',
          icon: const Icon(Icons.table_chart_outlined),
          onPressed:
              () => onInsertSnippet(
                '\n| Coluna 1 | Coluna 2 |\n| --- | --- |\n| Texto | Texto |\n',
              ),
        ),
      ],
    );
  }
}
