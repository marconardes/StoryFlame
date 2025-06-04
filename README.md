# StoryFlame Swing

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

## Roadmap

Para detalhes sobre funcionalidades planejadas e a visão de futuro para o StoryFlame Swing, por favor veja o nosso arquivo [ROADMAP.md](ROADMAP.md).
