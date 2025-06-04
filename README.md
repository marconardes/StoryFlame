# StoryFlame Swing

## Legenda de Status
- (V) Implementado e Verificado
- (P) Parcialmente Implementado
- (X) Não Implementado
- (🟡) Planejado (Backlog ou Próxima Sprint)
- (❗) Impedimento ou Problema Encontrado
- (R) Removido ou Descontinuado

## Funcionalidades Implementadas (Core)
(V) Gerenciamento de Projetos:
    (V) Criação, abertura (implícito ao carregar/selecionar) e listagem de múltiplos projetos de escrita. (ProjectViewModel: `createProject`, `loadProjects`; ProjectListView: exibe lista)
    (V) Persistência local dos dados do projeto em formato JSON. (ProjectViewModel: `saveProjects`, `loadProjects` usando GSON)
(V) Organização por Capítulos/Cenas:
    (V) Estruturação de projetos em capítulos ou cenas ordenáveis. (Project model tem `List<Chapter>`; Chapter model tem `order`; ProjectViewModel `moveChapter` atualiza a ordem)
    (V) CRUD completo para capítulos (adicionar, editar título, excluir, reordenar). (ProjectViewModel: `addChapter`, `updateChapterTitle`, `deleteChapter`, `moveChapter`)
(V) Resumo por Capítulo/Cena:
    (V) Adição (implícito na criação do capítulo) e edição de resumos textuais para cada capítulo/cena. (Chapter model tem `summary`; ChapterEditorView tem `summaryArea`; ProjectViewModel `updateChapterSummary`)
(P) Editor de Texto Simples (para conteúdo do capítulo):
    (V) Integração de editor de texto simples (JTextArea). (ChapterEditorView usa `JTextArea` para `contentArea`)
    (X) Funcionalidades de formatação básica (negrito, itálico, sublinhado, cabeçalhos H1-H3) via toolbar ou assistentes. (Não há evidência de JTextPane/JEditorPane com StyledDocument ou bibliotecas Markdown)
    (X) Salvamento automático com debounce do conteúdo do capítulo ativo. (Salvamento é manual via botão "Save" em ChapterEditorView, que chama `ProjectViewModel.updateChapterContent` e depois `saveProjects`)

## Funcionalidades Planejadas (Visão Geral)
(🟡) Melhorias no Editor de Texto/Markdown:
    (🟡) Adicionar uma toolbar de formatação rápida (negrito, itálico, listas, etc.).
    (🟡) Implementar um preview de Markdown em tempo real ou alternável (requer biblioteca Markdown).
    (🟡) Suporte para tabelas e blocos de código (requer biblioteca Markdown).
(🟡) Gerenciamento Avançado de Capítulos:
    (🟡) Visualização em Kanban/Painel dos capítulos (Ex: backlog, em progresso, concluído).
    (🟡) Metadados customizáveis para capítulos (personagens, locais, notas).
(🟡) Exportação e Backup:
    (🟡) Exportar projetos/capítulos para formatos comuns (PDF, DOCX, TXT, Markdown).
    (🟡) Opções de backup automático e manual.
(🟡) Ferramentas de Escrita Criativa:
    (🟡) Gerador de nomes/ideias.
    (🟡) Ferramenta de anotações e referências.
(🟡) UI/UX Melhorias:
    (🟡) Modo Escuro.
    (🟡) Interface personalizável (fontes, cores).

## Próximos Passos e Funcionalidades Futuras (Planejado 🟡)

### Sprint Atual/Próxima (Exemplo Fictício)
- (🟡) **Foco Principal:** Melhorar a experiência de edição.
    - (🟡) Tarefa 1: Implementar editor de Rich Text básico (JTextPane) em vez de JTextArea para o conteúdo do capítulo.
    - (🟡) Tarefa 2: Adicionar botões de formatação básica (negrito, itálico) ao editor de capítulo.
- (🟡) **Secundário:**
    - (🟡) Tarefa 3: Investigar bibliotecas Java para exportação para TXT simples.
    - (🟡) Tarefa 4: Implementar a exportação de um capítulo para TXT.

### Backlog de Médio Prazo
- (🟡) Sincronização com Cloud (Dropbox, Google Drive).
- (🟡) Ferramentas de World-Building (personagens, locais, itens).
- (🟡) Visualização de linha do tempo/cronologia dos capítulos.
- (🟡) Suporte a múltiplos idiomas para a UI.
- (🟡) Implementar salvamento automático (debounce) para o editor de capítulos.

### Visão de Longo Prazo
- (🟡) Versão Web do StoryFlame.
- (🟡) Funcionalidades colaborativas.
- (🟡) API para integrações externas.
- (🟡) Suporte completo a Markdown no editor, incluindo preview.
