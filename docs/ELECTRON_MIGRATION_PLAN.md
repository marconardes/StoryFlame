# Plano de Migracao para Electron

## Objetivo

Migrar o StoryFlame da experiencia desktop em `Java 21 + Swing` para uma experiencia desktop em Electron sem perder:

- o modo offline-first
- a persistencia local em `.storyflame`
- a separacao entre regra de negocio e interface
- a responsividade da UI
- a rastreabilidade por entregas pequenas
- a equivalencia estrutural e funcional com o produto atual
- a capacidade de distribuir o app final como uma entrega unificada por plataforma

Este documento consolida a analise por papel de agente:

- Backend Agent
- Frontend Agent
- UX Reviewer Agent
- Competitive Research Agent
- Gerente de Projeto

## Decisao estrutural recomendada

A recomendacao para a migracao e:

- nao fazer `big bang`
- nao reescrever o dominio inteiro em TypeScript no primeiro movimento
- preservar o `core` Java como fonte de verdade inicial e base de equivalencia
- introduzir uma camada de aplicacao/contratos estaveis entre UI e dominio
- usar Electron como nova casca de interface, com integracao local a um backend JVM
- empacotar a entrega final como um unico produto por plataforma, mesmo que internamente exista mais de um processo

Arquitetura alvo recomendada para a Fase 1:

```text
Electron
├── main process
├── preload
├── renderer UI
└── IPC
      ↓
StoryFlame local application bridge
      ↓
Java core/application services
      ↓
.storyflame ZIP + JSON
```

Isso reduz risco porque o maior patrimonio tecnico hoje esta no `core`, enquanto o maior debito estrutural esta na UI Swing. Tambem preserva equivalencia sem transformar a migracao em uma reescrita total obrigatoria logo na primeira onda.

## Backend Agent

### O que pode ser reaproveitado

O modulo `core` ja concentra boa parte do que deve sobreviver a troca de frontend:

- modelos de dominio em [Project.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/model/Project.java:13)
- persistencia do arquivo `.storyflame` em [ProjectArchiveStore.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectArchiveStore.java:42)
- autosave em [ProjectAutosaveService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectAutosaveService.java:27)
- busca textual em [ProjectSearch.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/search/ProjectSearch.java:12)
- validacoes em [ProjectValidationService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/validation/ProjectValidationService.java:9)
- analise emocional em [EmotionAnalysisService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/analysis/EmotionAnalysisService.java:10)

### O que falta para suportar Electron melhor

Hoje a UI conversa com o dominio quase diretamente. Para Electron, faltam contratos de aplicacao mais explicitos, por exemplo:

- `ProjectAppService` para criar, abrir, salvar, importar, exportar e inspecionar projeto
- `EditorAppService` para editar estrutura, cena, POV e resumo
- `CharacterAppService` para CRUD e relacoes com perfis
- `TagAppService` para biblioteca, perfis e validacoes
- `AnalysisAppService` para analise emocional

Esses contratos devem retornar DTOs simples e erros controlados, em vez de deixar a UI manipular objetos ricos do dominio por toda parte.

### Riscos atuais de acoplamento

- a UI atual instancia servicos de `core` diretamente em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:86)
- a tela principal segura muito estado mutavel ao mesmo tempo em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:172)
- o fluxo de persistencia depende de componentes Swing em [DesktopProjectWorkflow.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopProjectWorkflow.java:72)
- o autosave faz snapshot profundo do projeto inteiro, o que e reaproveitavel, mas ainda sem uma fronteira de comando de aplicacao bem definida em [ProjectAutosaveService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectAutosaveService.java:43)

### Direcao backend recomendada

Fase backend para a migracao:

1. extrair servicos de aplicacao do `desktop` para uma camada Java reutilizavel fora de Swing
2. definir DTOs e contratos serializaveis para uso local por IPC
3. expor essa camada para Electron por um bridge local
4. so depois decidir se vale portar partes do `core` para TypeScript
5. manter o foco da entrega final no app unificado por plataforma, nao em expor a divisao interna da stack ao usuario

### Status consolidado do Marco E1

Status atual:

- concluido

O que ja foi extraido do Swing para o modulo `app`:

- operacoes de projeto:
  - criar
  - abrir
  - salvar
  - importar
  - exportar archive
  - inspecionar archive
- operacoes editoriais basicas:
  - atualizar metadados do projeto
  - atualizar titulo de capitulo
  - atualizar titulo, sinopse e conteudo de cena
- operacoes estruturais:
  - adicionar, remover e mover capitulo
  - adicionar, remover e mover cena
- operacoes de personagem:
  - criar, atualizar e excluir personagem
  - associar e remover tag de personagem
  - definir e limpar POV
- operacoes de tag:
  - criar, atualizar, duplicar e excluir tag
  - migrar referencias de id de tag
  - associar e remover tag de perfil

Arquivos principais do `E1`:

- [ProjectApplicationService.java](/home/marconardes/IAS_Project/StoryFlame/app/src/main/java/io/storyflame/app/project/ProjectApplicationService.java)
- [ProjectEditorApplicationService.java](/home/marconardes/IAS_Project/StoryFlame/app/src/main/java/io/storyflame/app/project/ProjectEditorApplicationService.java)
- [ProjectStructureApplicationService.java](/home/marconardes/IAS_Project/StoryFlame/app/src/main/java/io/storyflame/app/project/ProjectStructureApplicationService.java)
- [ProjectCharacterApplicationService.java](/home/marconardes/IAS_Project/StoryFlame/app/src/main/java/io/storyflame/app/project/ProjectCharacterApplicationService.java)
- [ProjectTagApplicationService.java](/home/marconardes/IAS_Project/StoryFlame/app/src/main/java/io/storyflame/app/project/ProjectTagApplicationService.java)

Pendencia residual apos o `E1`:

- alguns contratos ainda retornam objetos de dominio ricos em vez de snapshots/DTOs de sessao mais estaveis
- a fase seguinte precisa definir como esses contratos serao expostos no app Electron e empacotados na distribuicao final

Leitura de gerencia:

- o objetivo estrutural do `E1` foi atingido
- a proxima frente racional passa a ser o `E2`, com shell Electron minima usando os contratos do modulo `app`
- o `E2` deve nascer ja alinhado com a meta de entrega unificada por plataforma

## Frontend Agent

### Mapa funcional da UI atual

A UI atual tem seis areas principais:

- `Editor`, `Estrutura`, `Busca`, `Personagens`, `Tags` e `Analise` em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:1160)
- acoes de projeto em toolbar como `Novo`, `Abrir`, `Salvar`, `Exportar projeto`, `Importar projeto` e `Publicar manuscrito` em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:1084)
- fluxo de projeto centralizado em [DesktopProjectWorkflow.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopProjectWorkflow.java:28)
- fluxo de estrutura/editor em [DesktopEditorStructureCoordinator.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopEditorStructureCoordinator.java:19)
- fluxo de personagens em [DesktopCharacterCoordinator.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopCharacterCoordinator.java:17)
- fluxo de tags e perfis em [DesktopTagCoordinator.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopTagCoordinator.java:16)

### Pontos que dificultam a migracao

- a classe principal ainda concentra widgets, estado, navegacao e integracao em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:82)
- varios coordenadores ainda dependem fortemente de componentes concretos Swing
- selecao e refresh dependem de sincronizacao manual de UI com `syncingUi`
- dialogs de arquivo e confirmacao estao embutidos nos workflows do desktop
- a logica de tela e estado de dominio ainda se misturam em pontos de selecao, refresh e status

### Decomposicao recomendada para Electron

Renderer:

- `workspace-shell`
- `project-sidebar`
- `editor-view`
- `structure-view`
- `search-view`
- `characters-view`
- `tags-view`
- `analysis-view`
- `status-center`

Estado no renderer:

- `projectSessionStore`
- `editorStore`
- `searchStore`
- `characterStore`
- `tagStore`
- `analysisStore`
- `uiFeedbackStore`

IPC no preload:

- `project.create/open/save/import/export/inspect/publish`
- `editor.selectChapter/selectScene/updateScene/updateSynopsis/updatePOV`
- `structure.createChapter/createScene/move/delete`
- `search.run`
- `character.list/create/update/delete/select`
- `tag.list/create/update/delete/profile.update`
- `analysis.run`

### Recomendacao de estrategia

Recomendacao clara: migracao incremental, nao `big bang`.

Sequencia sugerida:

1. manter `core` Java
2. criar contratos de aplicacao locais
3. construir um shell Electron com leitura basica de projeto
4. migrar o editor e a estrutura primeiro
5. migrar personagens, tags e analise em ondas seguintes
6. remover Swing apenas quando a paridade minima estiver validada
7. preservar equivalencia do fluxo principal em cada fatia migrada

## UX Reviewer Agent

### Problemas encontrados

- a experiencia atual espalha o fluxo entre toolbar, abas, listas e arvore ao mesmo tempo
- o usuario precisa entender varios contextos paralelos: capitulo, cena, arvore, lista, resumo e status
- a tela principal ainda transmite densidade alta de informacao
- o feedback operacional existe, mas esta muito ligado a labels e dialogs modais

### Impacto no usuario

- maior curva de aprendizado
- risco de perda de contexto durante navegacao e selecao
- cansaco cognitivo para tarefas simples como editar cena e alternar para personagens ou tags
- dificuldade maior para descobrir a acao principal de cada area

### Melhorias recomendadas ao Frontend Agent

- transformar a navegacao em layout mais previsivel: sidebar de projeto, area central de edicao e painel contextual
- padronizar feedback de sucesso, erro e carregamento em uma unica linguagem visual
- reduzir alternancia entre lista e arvore para editar a mesma estrutura
- destacar melhor o estado atual: projeto aberto, capitulo, cena, modo de escrita e status de salvamento
- deixar tags, personagens e analise como paines mais contextuais ao manuscrito, nao apenas areas paralelas

### Prioridade

- alta

### Criterios de UX para considerar a migracao pronta

- o fluxo principal de abrir projeto, navegar e escrever esta mais simples do que no Swing
- o usuario identifica claramente onde esta e o que esta editando
- o feedback de autosave, erro e processamento nao depende de dialog modal para tudo
- personagens e tags ficam acessiveis sem quebrar o foco de escrita
- a nova UI preserva responsividade mesmo com operacoes longas

## Competitive Research Agent

### Concorrentes analisados

#### Scrivener

- tipo: direto, fechado
- proposta principal: organizacao longa de manuscrito com binder, outliner e corkboard
- melhor que o StoryFlame hoje em maturidade de organizacao de projeto e fluxo de manuscrito longo
- oportunidade para o StoryFlame: competir em simplicidade, offline-first e diferencial de Narrative Tag Engine
- recomendacao final: investigar
- fontes:
  - https://www.literatureandlatte.com/blog/organize-your-scrivener-project-with-the-corkboard
  - https://www.literatureandlatte.com/docs/Scrivener_Manual-v2-Mac.pdf

#### Obsidian

- tipo: indireto, fechado no app principal com ecossistema extensivel
- proposta principal: base de conhecimento local extensivel por plugins
- melhor que o StoryFlame em ecossistema, extensibilidade e UX de desktop moderna
- oportunidade para o StoryFlame: aprender com arquitetura de shell + plugin mindset sem perder foco editorial
- recomendacao final: adaptar parcialmente
- fontes:
  - https://help.obsidian.md/
  - https://www.electronjs.org/docs/latest/tutorial/process-model

#### novelWriter

- tipo: direto, open source
- proposta principal: escrita de romance em texto puro com foco em organizacao por projeto
- diferenca relevante: mantem foco forte em escrita longa com stack desktop mais leve que Electron
- licao tecnica: um app de escrita pode manter arquitetura clara com modulo central e GUI separada
- recomendacao final: investigar
- fontes:
  - https://novelwriter.io/
  - https://novelwriter.io/download/install_win.html
  - https://github.com/vkbo/novelWriter

#### Manuskript

- tipo: direto, open source
- proposta principal: ferramenta de escrita longa com organizacao de personagens, cenas e planejamento
- melhor ponto para benchmark: fluxo funcional proximo ao do StoryFlame
- alerta: nao assumir arquitetura interna sem validacao direta do repositorio
- recomendacao final: investigar
- fontes:
  - https://www.theologeek.ch/manuskript/
  - https://github.com/olivierkes/manuskript

### Conclusao competitiva

O benchmark aponta que migrar para Electron so faz sentido se a mudanca vier junto com:

- simplificacao do fluxo principal
- melhor navegacao estrutural
- feedback operacional moderno
- espaco futuro para extensibilidade

Migrar apenas a tecnologia da UI sem redesenhar a experiencia produziria custo alto com ganho pequeno.

## Gerente de Projeto

### Status atual

Viabilidade tecnica: boa, com risco moderado.

O `core` atual e um ativo reaproveitavel. A maior incerteza nao esta em persistencia ou regras de negocio, e sim em como expor isso para uma UI Electron sem duplicar regra nem criar um bridge fraco.

### Bloqueios ou riscos

- ainda nao foi escolhida a forma do bridge local entre Electron e Java
- ainda nao foi formalizado o modelo de empacotamento unificado por plataforma
- migrar por reescrita total elevaria muito o risco de regressao

### Dependencias relevantes

- definicao do contrato entre renderer, main process e backend local
- escolha do formato de IPC local
- confirmacao da pilha frontend Electron
- backlog incremental para manter paridade funcional

### Proximo passo recomendado

Executar uma `Fase 0` de preparacao antes de qualquer reescrita visual ampla:

1. extrair uma camada `application` do lado Java
2. definir DTOs e casos de uso
3. criar um shell Electron minimo
4. provar abrir projeto, listar estrutura e editar cena
5. definir o modelo inicial de empacotamento unificado por plataforma

### Impacto esperado da decisao

Se a migracao seguir por fases, o StoryFlame ganha:

- caminho mais seguro para abandonar Swing
- reaproveitamento alto do dominio atual
- menor risco de regressao funcional
- base melhor para UX moderna e futura extensibilidade
- caminho mais seguro para entregar Electron sem perder equivalencia funcional

Se seguir por `big bang`, o risco principal sera:

- duplicacao de regras
- atraso alto
- regressao em persistencia e fluxos centrais
- paridade funcional demorada

## Roadmap recomendado de migracao

### Fase 0 - Preparacao

- mapear casos de uso que hoje estao no `desktop`
- extrair camada de aplicacao Java sem Swing
- definir esquema de DTOs e erros
- definir protocolo local de integracao com Electron

### Fase 1 - Shell Electron

- criar app Electron com `main`, `preload` e `renderer`
- implementar abrir projeto e listar estrutura
- implementar visualizacao basica de metadados e cena
- validar o formato inicial da distribuicao unificada por plataforma

Status atual desta frente:

- shell Electron criada
- bridge Java local funcional e testado
- distribuicao inicial preparada via scripts de pacote do modulo `electron`
- artefato `linux-unpacked` validado com executavel gerado e bridge embutido em `resources/storyflame-bridge`
- percentual aproximado do `E2`: `100%`

### Fase 2 - Editor principal

- migrar edicao de cena
- migrar navegacao de capitulo/cena
- migrar autosave e feedback de status

### Fase 3 - Modulos de apoio

- migrar busca
- migrar personagens
- migrar tags e perfis

### Fase 4 - Ferramentas avancadas

- migrar exportacao
- migrar validacoes de projeto
- migrar analise emocional

### Fase 5 - Descomissionamento do Swing

- validar paridade funcional minima
- executar testes de regressao
- congelar manutencao da UI Swing
- atualizar roadmap oficial e documentacao principal

### Fase 6 - Refactor da UI Electron

- reorganizar a shell inicial do `E2` em uma arquitetura de UI mais modular
- manter no Electron um layout estruturalmente similar ao da aplicacao Java atual, preservando familiaridade de uso sem carregar as limitacoes tecnicas do Swing
- separar melhor layout, estado, componentes e fluxos de tela
- padronizar feedback visual, navegacao e estados vazios
- revisar ergonomia do fluxo principal com apoio de UX
- preparar a UI Electron para manutencao e crescimento apos a migracao funcional do `E3`
- resolver definitivamente os avisos GLib/GTK em Linux via ajuste de empacotamento e flags de inicializacao

#### Status detalhado do E4

- E4 geral: concluido, 100% concluido, 0% pendente
- E4.1 (layout e navegacao similar ao Java): 100% concluido, 0% pendente
- E4.2 (padronizacao de estados visuais): 100% concluido, 0% pendente
- E4.3 (modularizacao do renderer): concluido
- E4.3 arquivos base: `electron/renderer/ui-core-actions.js`, `electron/renderer/ui-actions.js`, `electron/renderer/ui-project-actions.js`, `electron/renderer/ui-entity-actions.js`, `electron/renderer/ui-search-actions.js`, `electron/renderer/ui-render.js`, `electron/renderer/ui-bindings.js`, `electron/renderer/ui-app.js`
- E4.3 percentual: 100% concluido, 0% pendente
- E4.4 (layout similar ao Java com ajustes ergonômicos): 100% concluido, 0% pendente
- E4.5 (empacotamento Linux e tratamento GTK/GLib): 100% concluido, 0% pendente

### Fase 7 - Remocao do bridge Java (E6)

- definir arquitetura alvo para eliminar o bridge Java como processo separado
- migrar chamadas do renderer para a nova camada de execucao
- validar paridade funcional com baseline do `E5`
- manter empacotamento unificado por plataforma sem dependencia de bridge externo

### Fase 8 - Empacotamento Linux e GTK (E4.5)

- fechar a estrategia de empacotamento Linux para a entrega Electron
- tratar os avisos GLib/GTK de forma definitiva no pacote e na inicializacao
- validar a experiencia empacotada sem regressao visual ou de carregamento

### Fase 9 - Remocao total do Java (E8)

- remover dependencias Java de dominio, persistencia e validacoes
- migrar runtime para execucao sem JVM
- validar equivalencia funcional contra baseline do `E5`
- manter entrega final unificada por plataforma

### Observacoes de empacotamento Linux

- existem avisos recorrentes do Electron/GTK em Linux (GLib-GObject) durante execucao local
- esses avisos nao bloqueiam a UI, mas devem ser tratados de forma definitiva no empacotamento e/ou runtime do `E4.5`
- o renderer Electron foi modularizado em varios arquivos globais; cada modulo precisa manter escopo isolado para evitar colisao de identificadores no carregamento

## O que vale investigar agora

- usar processo Java local dedicado como backend de aplicacao do Electron
- usar IPC estrito via preload, sem expor Node diretamente ao renderer
- definir desde cedo contratos versionados entre Electron e Java
- validar se Android continuara reaproveitando o mesmo `core` sem impacto

## O que vale ignorar por agora

- reescrever o `core` inteiro em TypeScript
- plugin system amplo antes da paridade funcional minima
- redesenho visual completo antes de provar o bridge de aplicacao
- mudar persistencia canonica do `.storyflame`

## Fontes principais

- arquitetura local do repositorio em [docs/ARCHITECTURE.md](/home/marconardes/IAS_Project/StoryFlame/docs/ARCHITECTURE.md:1)
- UI principal em [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:82)
- workflow de projeto em [DesktopProjectWorkflow.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopProjectWorkflow.java:28)
- coordenacao de responsividade em [DesktopBackgroundCoordinator.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/DesktopBackgroundCoordinator.java:17)
- processo do Electron:
  - https://www.electronjs.org/docs/latest/tutorial/process-model
- benchmark:
  - https://www.literatureandlatte.com/blog/organize-your-scrivener-project-with-the-corkboard
  - https://help.obsidian.md/
  - https://novelwriter.io/
  - https://github.com/vkbo/novelWriter
  - https://www.theologeek.ch/manuskript/
  - https://github.com/olivierkes/manuskript
