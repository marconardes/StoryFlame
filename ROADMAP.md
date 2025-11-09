**Roadmap do Projeto StoryFlame (Flutter multiplataforma)**

StoryFlame é uma aplicação Flutter (Dart) multiplataforma voltada para organização de projetos literários. O objetivo deste roadmap é conectar visão de produto, arquitetura e execução, priorizando entregas incrementais com critérios claros de aceite.

## 1. Pilar Técnico

- **Stack**: Flutter 3.24 (Dart 3.5), arquitetura por packages (`app`, `domain`, `data`), Riverpod/Bloc para estado, Drift/sqflite + JSON para compatibilidade, pacotes `printing`/`pdf` para exportações.
- **Qualidade**: cobertura mínima 65% no MVP, `flutter analyze`, formatadores automáticos, GitHub Actions (build + testes + pana/dart analyze).
- **Dados**: persistência primária em JSON local via `path_provider` + `dart:io`, com roadmap para criptografia e sincronização futura (cloud opcional).
- **Deploy**: `flutter build` desktop/mobile, distribuição com `flutter_distributor` ou CI dedicado; releases versionadas (`v0.x` MVP, `v1.x` pós-MVP).

## 2. Legenda de Status

- ✅ Concluído
- 🟡 Em andamento ou planejado para a fase atual
- 💤 Backlog futuro

## 3. Visão por Releases

### Release 0 – Fundamentos (Semana 0)

Status: desenvolvimento iniciado em 2024-05-28.

- Backlog imediato:
  - [x] Finalizar setup de ferramentas (lint, format, CI, documentação).
  - [x] Evoluir protótipo navegável com dados JSON reais para validar fluxo completo.
  - [x] Documentar fluxo de criação de workspaces para novos contribuintes.
  - [ ] Ampliar cobertura de testes widget/integration para fluxos chave.

Objetivo: consolidar a base Flutter multiplataforma com navegação ponta a ponta, pipeline de qualidade mínimo e documentação inicial.

Plano de execução:
1. **Workspace e packages** — usar `flutter create storyflame` em modo multiplataforma, mover código para packages `app`, `domain`, `data` gerenciados por `melos`, configurar `analysis_options.yaml` compartilhado. (✅)
2. **Ferramentas de engenharia** — habilitar `flutter format`, `flutter analyze`, hooks com `melos run ...`, pipeline CI (GitHub Actions) com lint + testes + build desktop, atualizar README e scripts. (✅)
3. **Protótipo navegável** — implementar navegação Projetos → Capítulos → Editor usando dados mockados carregados de JSON local, responsivo para desktop/mobile, validando temas claro/escuro básicos. (✅)
4. **Validação final** — rodar `flutter test`, `melos bootstrap`, build mínimo desktop (`flutter build linux` ou `flutter build windows`) e registrar resultado em README/CHANGELOG. (✅)

- **Estrutura Flutter multi-package** (✅) — Workspace com `app`, `domain`, `data`; `flutter test` e `melos bootstrap` verdes.
- **Setup de ferramentas** (✅) — `flutter analyze`, formatadores, pipeline CI local configurado; README com instruções de build.
- **Protótipo de UI navegável** (✅) — Fluxo Projetos → Capítulos → Editor funcional responsivo com dados mockados.

### Release 1 – Núcleo Essencial (MVP, Semanas 1-3)

- **Gerenciamento de Projetos** (✅) — CRUD completo com persistência JSON local (`storyflame_projects.json`), busca em tempo real e exclusão segura.
- **Capítulos/Cenas** (✅) — Estrutura hierárquica com reorder drag-and-drop, timestamps por capítulo e histórico mínimo.
- **Resumos + Conteúdo** (✅) — Editor Markdown com toolbar, autosave com debounce, preview opcional e contagem de palavras em tempo real.
- **Exportação TXT/PDF** (✅) — Botões dedicados geram arquivos completos (títulos, resumos, conteúdo) usando `pdf`/`txt` e informam o caminho salvo.
- **Dark/Light Mode** (✅) — Toggle persistido em `SharedPreferences`, aplicado ao app inteiro antes da renderização.
- **Segurança básica** (✅) — Senha por projeto com hash SHA-256, 5 tentativas e bloqueio de 1 min; prompts integrados à UI.
- **Métricas de Escrita** (✅) — Dashboard com palavras totais/diárias, metas configuráveis e indicadores de progresso.
- **Testes automatizados** (✅) — `flutter_test` cobrindo parsing, repositório mock e fluxo principal; rodados via `melos run test`.

Entregável: builds `flutter build windows/macos/linux/apk`, pacote ZIP com instruções rápidas, manual do usuário e checklist de QA atualizados no README.

### Release 2 – Organização Narrativa (Semanas 4-6) — Concluído

- Status: desenvolvimento encerrado em 2024-05-29.

- Objetivo: consolidar recursos de worldbuilding (personagens, glossário, timeline) e criar conexões explícitas com capítulos/cenas.

- Backlog imediato:
  - [x] Definir modelo compartilhado `WorldElement` (`Character`, `Location`, `Item`).
  - [x] Prototipar tela de fichas multi-aba com filtros e busca.
  - [x] Investigar UX para timeline navegável e vínculos bidirecionais com capítulos.

- **Fichas de Personagens** (✅) — CRUD completo (nome, apelidos, descrições, relações), tags, vínculo a capítulos e visualização Matriz Personagem × Cena.
- **Glossário Interno** (✅) — Termos com categorias/notas, associação a capítulos e inserção direta no editor via atalho “Inserir termo”.
- **Linha do Tempo** (✅) — Eventos com data opcional, drag & drop para ordenação, tags e vínculo a capítulos (com filtros básicos).
- **Banco de Locais e Itens** (✅) — Modelagem comum `WorldElement`, filtro por tipo, chips de vínculo a capítulos e notas de lore.
- **Ligações Entre Elementos** (✅) — Chips interativos Personagem × Cena/Elemento × Cena + matriz dedicada para visão geral.

Critério de aceite: release `v0.2` publicado com testes de integração (domínio + widget) e documentação atualizada (README + notas de release).

### Release 3 – Produtividade & UX (Semanas 7-9) — Concluído

- **Editor Markdown avançado** (✅) — Preview lado a lado, toolbar estendida (tabelas, códigos, citações) e inserção de termos do glossário diretamente do editor.
- **Banco Criativo** (✅) — Repositório de ideias/prompts com status (ideia/rascunho/concluída), tags e filtros rápidos para desbloquear cenas.
- **Templates Narrativos** (✅) — Galeria com modelos clássicos (Três Atos, Jornada do Herói) + templates customizados, checklists por etapa e botões de aplicação.
- **Colaboração local** (✅) — Exportação/importação completa (`.storyflame`), com merge automático e feedback via Snackbar.
- **UI/UX refinements** (✅) — Painel multi-abas (capítulos, fichas, glossário, timeline, mundo, ideias, templates), matriz Personagem × Cena e melhorias de navegação.

### Release 4 – Publicação e Distribuição (Pós-MVP) — Concluído

- Objetivo: transformar o StoryFlame em uma estação final de entrega, facilitando exportações profissionais e submissões em poucas etapas.

- Entregas:
  - **Exportação e-book avançada** (✅) — Geração de `.epub` com sumário automático, metadados (ISBN/direitos) e arquivos compatíveis com leitores populares.
  - **Pacote multimídia** (✅) — Exportação “caixa” (`.zip`) contendo PDF, JSON e texto plano para beta readers/coautores.
  - **Integrações KDP/Wattpad** (✅) — Assistente passo a passo que coleta sinopse, categorias e palavras-chave, gerando pacote JSON pronto para upload.
  - **Checklist de publicação** (✅) — Aba dedicada com switches de progresso (beta, capa, ISBN, e-book, KDP) e histórico de releases.
  - **Histórico de releases** (✅) — Registro versionado de exports com notas/links para facilitar auditoria e duplicação.

- Critérios cumpridos: exportadores validados localmente, assistente KDP funcional, checklist integrado à UI e documentação atualizada no README.

### Release 5 – Automação & Insights (Pós-MVP)

- Status: desenvolvimento em andamento (iniciado em 2024-06) com foco em análises locais e ferramentas de revisão.
- Objetivo: oferecer inteligência embarcada para diagnóstico do manuscrito e apoio criativo, mantendo dados 100% locais.

- Entregas já disponíveis:
  - **Análise de estilo local** (✅) — Pipeline heurístico em Dart calculando ritmo, densidade de diálogos, tamanho médio de frases e variação emocional inteiramente offline.
  - **Insights acionáveis** (✅) — Dashboard dedicado exibindo alertas por categoria (metas, personagens, terminologia) e radar por capítulo com metas vs. execução.
  - **Assistente de prompts** (✅) — Sugestões contextuais para capítulos curtos, termos repetitivos, personagens ociosos e worldbuilding não utilizado, integradas ao painel de Insights.
  - **Modo revisão** (✅) — Aba específica para registrar comentários locais por capítulo, marcar resolvidos e acompanhar pendências do release.
  - **Exportação de insights** (✅) — Relatório `.txt` com métricas, achados e sugestões gerado diretamente do painel de Insights.
  - **Binder / Corkboard visual** (✅) — Quadro de cartões para capítulos/cenas com capa opcional, sinopse em destaque, status customizáveis (rascunho/revisão/pronto) e drag-and-drop entre colunas/atos, inspirado no corkboard do Scrivener.
  - **Skin Scrivener-like** (✅) — Layout alternativo com binder lateral + painel de detalhes que aproxima a UI do StoryFlame à ergonomia do Scrivener clássico.

- Backlog alvo:
  - **Conectores externos opcionais** (🟡) — Extensões compatíveis com o mini SDK que podem exportar projetos/insights para ferramentas de planejamento (Scrivener, Notion, Obsidian) ou executar scripts de limpeza/normalização em sandbox local, sempre respeitando aprovação explícita do autor.

- Critérios de saída: análises rodando offline em <5s por capítulo médio, UI dedicada para Insights/Revisão (entregue), documentação do SDK e exemplos de extensões.

## 4. Roadmap Futuro

### Release 6 – Plataforma Técnica & Estabilidade (Pós-MVP)

- Objetivo: fortalecer a base técnica para suportar crescimento de dados, depuração avançada e extensões da comunidade.
- Entregas planejadas:
  - **Observabilidade estruturada** (🟡) — Logging unificado (`logger` + Sentry/Crashlytics opcional) e dashboards locais de erros.
  - **Cache quente em Isolates** (🟡) — Prefetch de capítulos recentes com limites configuráveis para manter abertura <2s em projetos grandes.
  - **Persistência híbrida** (🟡) — Camada Drift/SQLite mantendo compatibilidade com JSON e scripts de migração versionados.
  - **Extensões avançadas** (🟡) — Suporte oficial a packages/plugins Flutter para distribuirmos analisadores comunitários.
- Critérios de saída: telemetria local habilitada, benchmark público demonstrando ganho de performance com cache + SQLite e guia de criação de extensões instaláveis via plugin.

### Release 7 – Paridade com Scrivener/Notion/Obsidian (Planejado)

- **Research Hub** — Biblioteca multimídia com anotação, tags e vinculação a capítulos.
- **Editor em blocos Notion-like** — Blocos drag-and-drop, templates e colunas com slash commands.
- **Bases relacionais** — Tabelas com filtros/visões (board/list/calendar), relacionando fichas/locais/itens.
- **Graph View** — Diagrama interativo mostrando backlinks entre personagens, cenas e elementos de worldbuilding.
- **Colaboração em tempo real** — Edição simultânea com indicadores de presença, comentários e histórico.
- Critério de saída: usuários experientes conseguem reproduzir seus fluxos atuais no Notion/Scrivener/Obsidian apenas dentro do StoryFlame, com benchmarks de UX e satisfação.

### Backlog Técnico & Paridade

> Todos os itens prioritários migraram para os Releases 6 e 7. Novas ideias serão registradas aqui após avaliação de impacto.

### Concorrentes Diretos e Inspirações

- **Scrivener** — Binder, corkboard e research robusto; referência para navegação hierárquica e exportação profissional.
- **Notion** — Blocos e databases colaborativas; inspira o editor em blocos e visões board/list/calendar.
- **Obsidian** — Graph view + backlinks markdown-first; base para visualização de tópicos e links cruzados.
- **Ulysses** — Foco em UX limpa e objetivos de escrita; reforça metas/contextos minimalistas.
- **Storyist** — App mac/iOS com research board e story sheets; referência para fichas e cenas conectadas.
- **Campfire / Campfire Blaze** — Plataforma web de worldbuilding colaborativo; inspira dashboards integrados e timelines.
- **Dabble Writer** — Ferramenta com plot grid e metas diárias; referência para integrações de plot/word goals.
- **Plottr** — Planejamento visual multi-linha temporal; reforça necessidade de views gráficas personalizáveis.
- **Manuskript (OSS)** — Alternativa open-source com modo “Snowflake” e outlines; guia features avançadas mantendo local-first.
- **bibisco (OSS)** — Conhecido por fichas detalhadas e análise de capítulos; inspira relatórios e métricas narrativas.
- **Quoll Writer (OSS)** — Editor desktop com bancos de ideias e objetivos; referência para extensões comunitárias.
- **Logseq (OSS)** — Workflow de blocos + graph open-source; referência para community plugins e sync local.
- **Joplin (OSS)** — Notas markdown com sync criptografado; inspira foco em privacidade/offline.
- **AppFlowy (OSS)** — Versão open-source do Notion; reforça nossa meta de APIs abertas e temas customizáveis.

## 5. Riscos & Mitigações

- **Crescimento do arquivo JSON** — Impacto em performance; mitigar com paginação virtual, compressão incremental e carregamento em Isolates.
- **Complexidade da UI Flutter multiplataforma** — Impacto em qualidade; mitigar separando camadas (presenter/domain/data), Storybook/Widgetbook e testes de widget/instrumentação.
- **Dependência de APIs externas para IA** — Impacto em escopo; mitigar priorizando pipelines locais e aprovação explícita para integrações.

## 6. Métricas de Sucesso

- Tempo de abertura de projeto < 2s para até 1.000 cenas.
- Crash rate < 1% por sessão (logs locais).
- ≥ 3 usuários piloto concluindo um livro curto usando apenas o MVP.
- Feedback NPS interno ≥ +30 após Release 2.

## 7. Checklist de Pronto para Release

1. `melos run format && melos run analyze && melos run test` executados em ambiente limpo (CI e local).
2. Builds principais verificados (`flutter build linux|macos|windows|apk`) para garantir portabilidade.
3. Documentação atualizada (`README`, `ROADMAP`, notas de versão) e instruções para novos recursos.
4. Scripts/migrações de dados (JSON/SQLite) versionados e testados.
5. Checklist manual de smoke-test preenchido (exportações, importações, insights e novas telas do release).

Este roadmap deverá ser revisitado a cada encerramento de release para incorporar feedback de usuários e reavaliar prioridades.
