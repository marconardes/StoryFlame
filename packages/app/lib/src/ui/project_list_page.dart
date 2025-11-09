import 'package:domain/domain.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../project_store.dart';
import '../services/collaboration_service.dart';
import '../theme_controller.dart';
import 'project_details_page.dart';

class ProjectListPage extends StatefulWidget {
  const ProjectListPage({super.key});

  @override
  State<ProjectListPage> createState() => _ProjectListPageState();
}

class _ProjectListPageState extends State<ProjectListPage> {
  final TextEditingController _searchController = TextEditingController();
  final CollaborationService _collaborationService = CollaborationService();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final store = context.watch<ProjectStore>();
    final themeController = context.watch<ThemeController>();
    return Scaffold(
      appBar: AppBar(
        title: const Text('StoryFlame'),
        actions: [
          IconButton(
            tooltip: 'Importar projeto (.storyflame)',
            onPressed: () => _showImportDialog(context),
            icon: const Icon(Icons.file_download),
          ),
          IconButton(
            tooltip: themeController.isDark ? 'Tema claro' : 'Tema escuro',
            onPressed: themeController.toggle,
            icon: Icon(
              themeController.isDark ? Icons.wb_sunny : Icons.dark_mode,
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showCreateProjectSheet(context),
        icon: const Icon(Icons.add),
        label: const Text('Novo projeto'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              controller: _searchController,
              onChanged: store.search,
              decoration: const InputDecoration(
                prefixIcon: Icon(Icons.search),
                hintText: 'Buscar por título ou descrição',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            Expanded(
              child:
                  store.isLoading
                      ? const Center(child: CircularProgressIndicator())
                      : store.projects.isEmpty
                      ? const Center(
                        child: Text('Comece criando seu primeiro projeto.'),
                      )
                      : ListView.separated(
                        itemCount: store.projects.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 12),
                        itemBuilder: (context, index) {
                          final project = store.projects[index];
                          final totalWords = store.projectWordCount(project);
                          return Card(
                            child: ListTile(
                              title: Text(project.title),
                              subtitle: Text(
                                '${project.chapters.length} capítulos • $totalWords palavras',
                              ),
                              trailing: Wrap(
                                spacing: 8,
                                children: [
                                  if (store.requiresPassword(project))
                                    const Icon(Icons.lock, size: 18),
                                  IconButton(
                                    icon: const Icon(
                                      Icons.file_upload_outlined,
                                    ),
                                    tooltip: 'Exportar (.storyflame)',
                                    onPressed:
                                        () => _exportProject(context, project),
                                  ),
                                  IconButton(
                                    icon: const Icon(Icons.delete_outline),
                                    tooltip: 'Excluir projeto',
                                    onPressed:
                                        () => _confirmDelete(context, project),
                                  ),
                                ],
                              ),
                              onTap: () => _openProject(context, project),
                            ),
                          );
                        },
                      ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _exportProject(BuildContext context, Project project) async {
    final messenger = ScaffoldMessenger.of(context);
    final file = await _collaborationService.exportProject(project);
    if (!mounted) return;
    messenger.showSnackBar(
      SnackBar(content: Text('Exportado para ${file.path}')),
    );
  }

  Future<void> _showImportDialog(BuildContext context) async {
    final store = context.read<ProjectStore>();
    final pathController = TextEditingController();
    final messenger = ScaffoldMessenger.of(context);
    final result = await showDialog<String>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Importar projeto (.storyflame)'),
            content: TextField(
              controller: pathController,
              decoration: const InputDecoration(
                labelText: 'Caminho completo do arquivo',
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed:
                    () => Navigator.of(context).pop(pathController.text.trim()),
                child: const Text('Importar'),
              ),
            ],
          ),
    );
    if (result == null || result.isEmpty) return;
    try {
      final project = await _collaborationService.importProject(result);
      await store.importProject(project);
      if (!mounted) return;
      messenger.showSnackBar(
        SnackBar(content: Text('Projeto "${project.title}" importado.')),
      );
    } catch (error) {
      if (!mounted) return;
      messenger.showSnackBar(
        SnackBar(content: Text('Falha ao importar: $error')),
      );
    }
  }

  Future<void> _showCreateProjectSheet(BuildContext context) async {
    final store = context.read<ProjectStore>();
    final messenger = ScaffoldMessenger.of(context);
    final result = await showModalBottomSheet<_ProjectFormResult>(
      context: context,
      isScrollControlled: true,
      builder: (_) => const _ProjectFormSheet(),
    );
    if (result == null) return;
    await store.createProject(
      title: result.title,
      description: result.description,
    );
    if (!mounted) return;
    messenger.showSnackBar(
      const SnackBar(content: Text('Projeto criado com sucesso.')),
    );
  }

  Future<void> _confirmDelete(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    final messenger = ScaffoldMessenger.of(context);
    final confirm = await showDialog<bool>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Excluir projeto'),
            content: Text(
              'Deseja excluir "${project.title}"? Esta ação não pode ser desfeita.',
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
      await store.deleteProject(project.id);
      if (!mounted) return;
      messenger.showSnackBar(
        const SnackBar(content: Text('Projeto excluído.')),
      );
    }
  }

  Future<void> _openProject(BuildContext context, Project project) async {
    final store = context.read<ProjectStore>();
    final messenger = ScaffoldMessenger.of(context);
    final navigator = Navigator.of(context);
    if (store.requiresPassword(project)) {
      if (store.isLocked(project)) {
        messenger.showSnackBar(
          const SnackBar(
            content: Text(
              'Projeto bloqueado por tentativas inválidas. Aguarde um minuto.',
            ),
          ),
        );
        return;
      }
      final password = await _PasswordPrompt.request(context);
      if (!mounted || password == null) return;
      final success = store.verifyPassword(project, password);
      if (!success) {
        messenger.showSnackBar(
          const SnackBar(content: Text('Senha incorreta.')),
        );
        return;
      }
    }
    if (!mounted) return;
    navigator.pushNamed(ProjectDetailsPage.routeName, arguments: project.id);
  }
}

class _ProjectFormResult {
  const _ProjectFormResult(this.title, this.description);
  final String title;
  final String description;
}

class _ProjectFormSheet extends StatefulWidget {
  const _ProjectFormSheet();

  @override
  State<_ProjectFormSheet> createState() => _ProjectFormSheetState();
}

class _ProjectFormSheetState extends State<_ProjectFormSheet> {
  final _formKey = GlobalKey<FormState>();
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.of(context).viewInsets.bottom;
    return Padding(
      padding: EdgeInsets.only(bottom: bottom, left: 16, right: 16, top: 24),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(
              controller: _titleController,
              decoration: const InputDecoration(labelText: 'Título'),
              validator:
                  (value) =>
                      value == null || value.trim().isEmpty
                          ? 'Informe um título'
                          : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(labelText: 'Descrição'),
              minLines: 2,
              maxLines: 4,
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () {
                  if (_formKey.currentState?.validate() ?? false) {
                    Navigator.of(context).pop(
                      _ProjectFormResult(
                        _titleController.text.trim(),
                        _descriptionController.text.trim(),
                      ),
                    );
                  }
                },
                child: const Text('Criar'),
              ),
            ),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }
}

class _PasswordPrompt {
  static Future<String?> request(BuildContext context) {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder:
          (_) => AlertDialog(
            title: const Text('Digite a senha do projeto'),
            content: TextField(
              controller: controller,
              decoration: const InputDecoration(labelText: 'Senha'),
              obscureText: true,
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(controller.text),
                child: const Text('Confirmar'),
              ),
            ],
          ),
    );
  }
}
