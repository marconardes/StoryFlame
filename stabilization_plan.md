# Stabilization Plan

Data: 2026-03-29
Base: `review2.md`, `ROADMAP.md`, `docs/ARCHITECTURE.md`
Objetivo: estabilizar o MVP atual antes de abrir novo escopo funcional

## 1. Diretriz

Este plano prioriza:
- integridade de persistencia
- previsibilidade do autosave
- reducao de risco estrutural no desktop Swing
- contratos explicitos entre `core` e `desktop`
- cobertura de testes nos fluxos mais criticos

Este plano nao inclui:
- novas features de produto
- ampliacao do Android alem do papel atual de apoio
- troca de stack ou novas dependencias fora do escopo atual

## 2. Ordem de execucao

### Fase A - Blindagem de persistencia

#### A1. Endurecer abertura de arquivo ZIP
Objetivo:
- impedir abertura silenciosa de pacote parcialmente corrompido

Escopo:
- revisar `ProjectArchiveStore.open()`
- falhar cedo quando entradas obrigatorias do pacote estiverem ausentes
- diferenciar claramente:
  - arquivo invalido
  - arquivo migravel
  - arquivo parcialmente corrompido

Arquivos principais:
- [ProjectArchiveStore.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectArchiveStore.java)

Criterio de saida:
- pacote incompleto nao abre como se estivesse valido
- erros retornam mensagem estruturada para a UI
- testes de persistencia cobrem corrupcao parcial e cenarios de migracao

Status atual:
- concluido

#### A2. Blindar o autosave com snapshot
Objetivo:
- eliminar corrida entre edicao viva e serializacao em background

Escopo:
- revisar `ProjectAutosaveService.schedule()`
- introduzir snapshot imutavel ou copia defensiva antes de persistir
- reduzir exposicao de colecoes mutaveis do modelo quando necessario

Arquivos principais:
- [ProjectAutosaveService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectAutosaveService.java)
- [Project.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/model/Project.java)

Criterio de saida:
- autosave opera sobre estado consistente
- testes cobrem agendamento e serializacao sob mutacao do projeto
- nao ha acesso concorrente inseguro entre EDT e thread de persistencia

Status atual:
- concluido

### Fase B - Contratos de backend

#### B1. Formalizar validacao para salvar e publicar
Objetivo:
- tirar da UI a responsabilidade implicita de lembrar quando validar

Escopo:
- definir contrato claro no `core` para:
  - salvar
  - exportar
  - publicar
- retornar resultado estruturado com:
  - sucesso
  - bloqueios
  - avisos

Arquivos principais:
- [NarrativeIntegrityValidator.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/validation/NarrativeIntegrityValidator.java)
- [TagLibraryValidator.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/tags/TagLibraryValidator.java)
- [PublicationExportService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/publication/PublicationExportService.java)

Criterio de saida:
- `desktop` nao depende de validacao espalhada ou implícita
- inconsistencias criticas chegam a UI em formato tratavel
- testes cobrem bloqueio e aviso em persistencia/exportacao

Status atual:
- concluido

### Fase C - Reducao de risco no desktop

#### C1. Quebrar a `StoryFlameDesktopApp` por responsabilidade
Objetivo:
- reduzir acoplamento, tamanho e risco de regressao da janela principal

Escopo:
- extrair por fatia funcional:
  - editor
  - personagens
  - tags
  - publicacao
  - analise emocional
  - operacoes de fundo
- manter a janela principal como orquestradora leve

Arquivo principal:
- [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)

Criterio de saida:
- a janela principal perde responsabilidades concretas
- listeners e refreshs deixam de concentrar tudo em uma unica classe
- a integracao com o `core` continua sem regressao funcional

Status atual:
- concluido

Progresso realizado:
- infraestrutura de operacoes em background, validacao de arquivo e falha de autosave extraida para classe dedicada
- operacoes de projeto e publicacao extraidas para fluxo dedicado no desktop
- aba de analise emocional encapsulada em componente proprio
- casca visual de editor e estrutura extraida para `DesktopEditorStructurePanels`, reduzindo montagem de layout na janela principal
- sincronizacao entre arvore, listas e campos do editor extraida para `DesktopEditorStructureCoordinator`, mantendo a janela principal como dona do estado e da orquestracao
- aba de personagens separada em `DesktopCharacterPanels` e `DesktopCharacterCoordinator`, com layout e refresh/seleção fora da janela principal
- comandos operacionais de personagens extraidos para `DesktopCharacterWorkflow`, reduzindo mutacao direta na janela principal
- aba de tags separada em `DesktopTagPanels` e `DesktopTagCoordinator`, com layout e refresh/seleção de biblioteca e perfis fora da janela principal
- comandos operacionais de tags extraidos para `DesktopTagWorkflow`, reduzindo mutacao direta da biblioteca e dos perfis na janela principal
- autocomplete e sugestao de tags no editor extraidos para `DesktopEditorTagAutocomplete`, isolando o bloco final de UX mais sensivel da escrita

#### C2. Melhorar o feedback operacional da UI
Objetivo:
- reduzir clique repetido, estado concorrente e percepcao de travamento

Escopo:
- revisar fluxos de abrir, salvar, importar, exportar e analisar
- desabilitar ou proteger acoes incompatíveis durante operacoes longas
- padronizar empty/loading/error/success entre os paineis mais criticos

Arquivos principais:
- [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)
- formatters em `desktop/src/main/java/io/storyflame/desktop/`

Criterio de saida:
- o usuario entende quando a operacao comecou, esta em andamento e terminou
- a UI evita concorrencia basica de comandos
- os estados sao consistentes entre paines e fluxos

Status atual:
- concluido

Progresso realizado:
- `DesktopBackgroundCoordinator` agora aplica um `busy lock` nos gatilhos principais de operacao longa
- comandos de `Novo`, `Abrir`, `Salvar`, `Exportar`, `Importar`, `Publicar` e `Gerar analise` ficam indisponiveis enquanto uma operacao em background esta ativa
- cobertura automatizada adicionada para o bloqueio e restauracao de estado dos controles
- status operacionais de abrir, salvar, importar, inspecionar, publicar e analisar passaram a usar uma semantica unica: `Carregando`, `Concluido`, `Concluido com aviso` e `Falhou`
- formatters e testes do desktop foram alinhados para refletir essa linguagem unica sem quebrar os fluxos Swing existentes
- a aba de analise emocional passou a expor estados locais explicitos de `empty`, `loading`, `success` e `failure`
- quando a analise falha apos uma leitura valida anterior, o painel preserva o ultimo relatorio disponivel em vez de parecer vazio ou inconsistente
- cancelamentos de importacao e publicacao deixaram de falhar em silencio e agora fecham com aviso explicito no `statusLabel`
- a publicacao passou a usar uma mensagem final unica de exportacao, tratando a abertura externa como detalhe do resultado e nao como um fluxo paralelo de status
- a inspecao de arquivo passou a expor cancelamento explicito, e as operacoes com avisos de validacao deixam rastro visivel no `statusLabel` mesmo quando o usuario decide continuar

### Fase D - Cobertura de testes

#### D1. Expandir testes Swing
Objetivo:
- criar rede de seguranca real para refatoracao do desktop

Escopo:
- ampliar AssertJ-Swing para cobrir:
  - abrir projeto
  - salvar projeto
  - exportar/publicar
  - importar pacote
  - analise emocional

Arquivos principais:
- [StoryFlameDesktopAppSwingTest.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/test/java/io/storyflame/desktop/StoryFlameDesktopAppSwingTest.java)

Criterio de saida:
- os fluxos principais do desktop tem cobertura grafica basica
- a refatoracao da UI passa a ter protecao automatizada minima

Status atual:
- concluido

Progresso realizado:
- a suite AssertJ-Swing do desktop deixou de cobrir apenas o boot e passou a validar tambem o fluxo de `Salvar`
- o teste Swing agora confirma edicao de titulo, autor e cena, aciona `Salvar` pela UI e valida a persistencia real do arquivo gerado
- a aba de analise ganhou nomes estaveis para componentes criticos, e a suite Swing passou a cobrir o fluxo de `Gerar analise` com atualizacao de status e do painel
- hooks pequenos por `Path` foram adicionados ao desktop apenas para testes, permitindo cobrir `Abrir` e `Importar` sem automacao fragil de `JFileChooser`
- a suite Swing agora valida tambem os fluxos de `Abrir projeto` e `Importar ZIP`, confirmando troca real do projeto carregado e refresh do editor
- a publicacao passou a ter um hook de teste por `Path` e formato, permitindo cobrir `Exportar/Publicar` sem depender de chooser ou abertura externa do arquivo
- a suite Swing agora cobre `boot`, `salvar`, `abrir`, `importar`, `gerar analise` e `publicar`, fechando a rede basica de seguranca grafica do desktop

#### D2. Expandir testes de backend para cenarios de corrupcao e validacao
Objetivo:
- cobrir os pontos de falha mais provaveis do `core`

Escopo:
- testes para ZIP incompleto
- testes para autosave com snapshot
- testes para contratos de validacao antes de salvar/exportar

Arquivos principais:
- `core/src/test/java/...`

Criterio de saida:
- riscos principais do `review2.md` ficam cobertos em teste automatizado

Status atual:
- concluido

Progresso realizado:
- a cobertura de persistencia agora inclui `ProjectArchiveInspectorTest`, com cenarios de `project.json` ausente, versao futura/incompativel e `manifest.json` malformado
- `ProjectArchiveStoreTest` passou a validar explicitamente os contratos publicos de `validateForSave(...)` e `validateForArchiveExport(...)`
- `ProjectValidationServiceTest` ganhou matriz adicional para `EXPORT_ARCHIVE` e caso limpo sem avisos ou bloqueios
- `PublicationExportServiceTest` passou a validar diretamente o contrato de `validate(...)` antes do export real, garantindo o bloqueio estruturado da publicacao

### Fase E - Ajustes de UX de consolidacao

#### E1. Guiar melhor criacao de personagem e tag
Objetivo:
- reduzir fluxo interrompido e ambiguidade de conclusao

Escopo:
- explicitar estado de rascunho
- validacao inline
- CTA final unico e claro

Arquivos principais:
- [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)

Criterio de saida:
- o usuario entende quando esta criando, editando ou concluindo
- ha menos ida e volta entre lista, campos e confirmacao

Status atual:
- concluido

Progresso realizado:
- a UI passou a separar com mais clareza `buscar existente` de `criar novo` nas areas de personagens e tags
- os campos obrigatorios de criacao ficaram explicitados com titulos mais objetivos, reduzindo ambiguidade de preenchimento
- personagens e tags agora exibem dicas inline de rascunho e conclusao, deixando claro quando o cadastro ainda esta incompleto
- os CTAs e mensagens de status foram reescritos para orientar o proximo passo esperado, incluindo criacao, edicao e conclusao do rascunho
- a area de tags passou a comunicar melhor o estado de `rascunho`, `em edicao` e `pronta para revisar`, sem depender so do rodape da janela

#### E2. Ajustar comunicacao de escopo
Objetivo:
- evitar sobrepromessa funcional

Escopo:
- deixar Android explicitamente como modulo de apoio
- manter aviso visivel sobre limite heuristico da analise emocional
- separar melhor `Exportar projeto` de `Publicar manuscrito`

Arquivos principais:
- [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)
- [MainActivity.java](/home/marconardes/IAS_Project/StoryFlame/android/src/main/java/io/storyflame/android/MainActivity.java)
- documentacao relevante

Criterio de saida:
- o produto comunica corretamente o que faz hoje
- Android nao parece mais amplo do que realmente e
- o relatorio emocional nao sugere inteligencia semantica que o sistema nao entrega

Status atual:
- concluido

Progresso realizado:
- a comunicacao do Android foi ajustada no app e na documentacao para explicitar o modulo como apoio e validacao do `core`, nao como fluxo editorial principal
- a aba de analise passou a exibir aviso visivel de que o relatorio emocional e heuristico e offline, reforcando o uso como apoio e nao como avaliacao final
- o relatorio emocional agora incorpora esse aviso no proprio texto exibido ao usuario
- a nomenclatura do desktop passou a separar melhor `Exportar projeto ZIP` de `Publicar manuscrito`, reduzindo ambiguidade entre persistencia do projeto e saida editorial
- os fluxos e mensagens de publicacao foram reescritos para falar em `publicar manuscrito` em vez de misturar esse conceito com `exportar projeto`

## 3. Dependencias

- `A1` antes de `D2`
- `A2` antes de `D2`
- `B1` antes de `C2`
- `C1` antes de ampliar qualquer nova feature desktop
- `D1` deve acompanhar `C1`, nao ficar so para o final
- `E1` e `E2` dependem do estado estabilizado de `C2`

## 4. Prioridade recomendada

1. `A1` Endurecer abertura de arquivo ZIP
2. `A2` Blindar autosave com snapshot
3. `B1` Formalizar validacao para salvar e publicar
4. `D2` Cobrir persistencia e validacao em testes
5. `C1` Quebrar a `StoryFlameDesktopApp`
6. `D1` Expandir testes Swing
7. `C2` Melhorar feedback operacional da UI
8. `E1` Guiar criacao de personagem e tag
9. `E2` Ajustar comunicacao de escopo

## 5. Regra de execucao

- implementar uma frente por vez
- nao misturar refatoracao estrutural com nova feature
- toda mudanca no `desktop` deve preservar responsividade da EDT
- toda mudanca no `core` deve expor contrato claro para o frontend
- toda melhoria de UX deve ser objetiva e verificavel

## 6. Criterio de encerramento do plano

O plano pode ser considerado concluido quando:
- persistencia deixa de aceitar corrupcao parcial silenciosa
- autosave deixa de depender de objeto mutavel vivo
- salvar/exportar/publicar usam contrato formal de validacao
- a janela principal desktop perde parte relevante da concentracao atual
- os fluxos criticos estao cobertos por testes backend e Swing
- a comunicacao de escopo do Android e da analise emocional esta clara para o usuario
