# Roadmap Oficial do StoryFlame

Este e o unico roadmap ativo do repositorio. O nome oficial do projeto e `StoryFlame`, a stack alvo passa a ser `Java 21 + Electron`, com `core` em Java como nucleo de dominio e persistencia, e a prioridade atual do MVP continua sendo o desktop offline-first multiplataforma. A migracao deve preservar equivalencia estrutural e funcional com o produto atual e resultar em uma distribuicao unificada por plataforma.

## 1. Direcao do produto

- foco principal: escrita de web novel no desktop
- principio estrutural: offline-first
- persistencia canonica: ZIP versionado com JSON interno
- diferencial central: Narrative Tag Engine
- interface-alvo principal: Electron
- equivalencia funcional e estrutural com o produto atual
- o `core` em Java permanece como fonte principal da regra de negocio, validacoes e persistencia durante a migracao
- a entrega final deve acontecer como um unico app distribuivel por plataforma
- a migracao de `Swing` para `Electron` sera incremental e controlada
- plataforma secundaria nesta fase: Android como validacao tardia do nucleo compartilhado
- Android permanece como modulo de apoio e nao deve ser comunicado como interface editorial completa nesta fase

## 2. Regras de planejamento

- nenhum sprint esta pronto sem backend, frontend e revisao de UX
- nenhuma feature nova deve entrar se abrir risco claro de travamento na EDT enquanto o `desktop` Swing ainda estiver ativo
- o `core` concentra regra de negocio, validacoes e persistencia
- o frontend nao deve absorver regra de negocio, seja em `desktop` Swing ou em `Electron`
- a migracao para `Electron` nao justifica reescrever o dominio em TypeScript
- toda fase de migracao deve preservar compatibilidade com o formato `.storyflame`
- toda fase de migracao deve preservar equivalencia observavel do comportamento essencial
- o roadmap deve ser revisado ao fim de cada sprint com base no que foi validado de fato

## 3. Transicao oficial de plataforma

O StoryFlame deixa de tratar `Swing` como interface-alvo de longo prazo. A partir deste ponto:

- `Swing` passa a ser interface legada em transicao
- `Electron` passa a ser a interface principal alvo do produto
- o `core` continua como base reaproveitada da regra de negocio
- um novo modulo de aplicacao Java deve ser introduzido antes da migracao ampla da UI
- o produto final deve ser empacotado como distribuicao unificada por plataforma, mesmo que internamente use mais de um processo

Documento complementar:

- [docs/ELECTRON_MIGRATION_PLAN.md](/home/marconardes/IAS_Project/StoryFlame/docs/ELECTRON_MIGRATION_PLAN.md)

## 4. Estrutura oficial do roadmap

### Fase 1 - Base operacional

#### Sprint 1 - Fundacao tecnica
Objetivo:
- consolidar monorepo, build, modelos base e formato inicial de projeto

Escopo:
- monorepo com `core`, `desktop` e `android`
- estrutura inicial de pacotes
- modelos base `Project`, `Chapter`, `Scene` e `Character`
- definicao do ZIP v1
- build padronizado em Gradle

Criterio de pronto:
- `core` compila
- `desktop` compila
- `android` compila
- a documentacao inicial do formato de projeto existe

Status atual:
- concluido

### Fase E - Transicao para Electron

#### Marco E1 - Camada de aplicacao para migracao
Objetivo:
- criar a fronteira entre `core` e interfaces para viabilizar a migracao para Electron

Escopo:
- criar novo modulo Java de aplicacao
- encapsular casos de uso de criar, abrir e salvar projeto
- definir DTOs e respostas serializaveis
- provar consumo inicial desses contratos ainda com a UI Swing

Criterio de pronto:
- os fluxos de projeto nao dependem mais diretamente da UI Swing para orquestracao
- o `core` continua isolado de detalhes de interface
- os contratos novos ficam cobertos por testes automatizados

Status atual:
- concluido

#### Marco E2 - Shell Electron minima
Objetivo:
- estabelecer a nova interface principal do produto com escopo minimo e validavel

Escopo:
- criar modulo `electron`
- implementar processo principal e bridge com backend Java local
- abrir janela principal
- suportar criar, abrir e salvar projeto
- listar estrutura editorial minima
- definir o empacotamento inicial da distribuicao unificada por plataforma

Criterio de pronto:
- StoryFlame abre em Electron
- o fluxo basico de projeto funciona ponta a ponta usando o backend Java local
- UX valida navegacao inicial e estados de carregamento/erro
- a distribuicao minima do app fica coerente com a meta de entrega unificada por plataforma

Status atual:
- concluido

#### Marco E3 - Migracao incremental do fluxo editorial
Objetivo:
- substituir a experiencia principal de escrita do Swing por Electron sem perder produtividade

Escopo:
- migrar navegacao entre capitulos e cenas
- migrar editor de cena
- migrar resumo e contexto minimo
- manter compatibilidade com autosave e persistencia local

Criterio de pronto:
- o fluxo principal de escrita funciona em Electron
- a produtividade basica nao regrede em relacao ao desktop atual
- Swing passa a ser fallback temporario, nao frente principal

Status atual:
- concluido

#### Marco E4 - Refactor da UI Electron
Objetivo:
- consolidar a interface Electron em uma arquitetura de UI mais clara, coesa e escalavel apos a paridade funcional minima, preservando a familiaridade estrutural do layout principal hoje existente em Java

Escopo:
- refatorar layout, navegacao e composicao visual da shell Electron
- manter no Electron uma organizacao visual similar a da aplicacao Java atual, desde que isso nao reintroduza acoplamento ou limitacoes tecnicas do Swing
- reduzir acoplamento entre renderer, estado de sessao e componentes visuais
- padronizar feedback de loading, erro e sucesso
- melhorar ergonomia do fluxo principal de escrita, personagens, tags e analise
- preparar a UI para manutencao e evolucao sem depender da estrutura inicial de spike
- tratar avisos recorrentes de runtime no Linux (GLib/GTK) com abordagem definitiva de empacotamento e inicializacao

Criterio de pronto:
- a UI Electron fica mais consistente e previsivel do que a shell inicial do `E2`
- a estrutura visual principal permanece reconhecivel para usuarios da interface Java atual, mesmo com implementacao nativa em Electron
- estados visuais e feedbacks ficam padronizados em toda a app
- a estrutura do renderer fica modular o suficiente para evolucao sem concentracao excessiva
- UX revisa e aprova a nova base visual e de navegacao

Status atual:
- em andamento (refino visual, densidade, padronizacao de componentes e empacotamento Linux)

#### Marco E4.5 - Empacotamento Linux e GTK
Objetivo:
- fechar o empacotamento Linux da entrega Electron e tratar definitivamente os avisos GLib/GTK

Escopo:
- ajustar empacotamento Linux
- validar inicializacao e runtime empacotado
- tratar avisos GLib/GTK no fluxo final

Criterio de pronto:
- distribuicao Linux funciona sem travas de inicializacao
- avisos recorrentes de GTK/GLib ficam resolvidos ou mitigados de forma definitiva no pacote
- a entrega final permanece unificada por plataforma

Status atual:
- em andamento

#### Marco E5 - Mapeamento de paridade Swing
Objetivo:
- mapear funcionalidades presentes no Swing e ausentes no Electron para orientar a consolidacao da nova interface

Escopo:
- inventario funcional detalhado da UI Swing atual
- comparativo por fluxo/tela com o Electron
- lista priorizada de lacunas de paridade
- riscos e dependencias para cada lacuna

Criterio de pronto:
- mapa de paridade fechado e revisado
- backlog de lacunas priorizado e com impacto estimado

Status atual:
- planejado

#### Marco E6 - Remocao do bridge Java
Objetivo:
- eliminar a dependencia do bridge Java como processo separado, mantendo equivalencia estrutural e funcional do produto final

Escopo:
- definir arquitetura de substituicao do bridge atual
- remover chamadas Electron -> bridge Java do fluxo principal
- garantir persistencia, validacoes e regras de negocio sem regressao observavel
- ajustar empacotamento para entrega unificada por plataforma sem bridge externo

Criterio de pronto:
- Electron opera sem bridge Java externo nos fluxos principais
- regressao funcional validada contra paridade definida no `E5`
- empacotamento final por plataforma permanece unificado

Status atual:
- planejado

#### Marco E8 - Remocao total do Java da aplicacao
Objetivo:
- remover totalmente Java da aplicacao final, incluindo `core`, bridge e runtime Java embarcado

Escopo:
- substituir implementacoes Java de dominio, persistencia e validacao por stack nao-Java
- manter equivalencia estrutural e funcional validada contra baseline do `E5`
- migrar pipeline de build, testes e empacotamento para entrega sem dependencia de JVM

Criterio de pronto:
- produto final roda sem qualquer dependencia Java em runtime
- formato e comportamento principal permanecem equivalentes ao baseline definido
- distribuicao por plataforma continua unificada

Status atual:
- planejado

#### Sprint 2 - Persistencia local
Objetivo:
- salvar e carregar projetos sem dependencia externa

Escopo:
- criar projeto
- abrir projeto
- salvar projeto
- serializacao JSON
- autosave basico
- validacao de roundtrip de persistencia

Criterio de pronto:
- o projeto abre e salva sem perda de dados
- o `desktop` usa contratos do `core`
- erros de persistencia sao comunicados ao usuario
- testes de persistencia passam

Status atual:
- concluido

#### Sprint 3 - Editor MVP
Objetivo:
- permitir escrita real de cenas

Escopo:
- editor de cena
- binding entre cena e editor
- undo/redo
- contador de palavras
- validacao com texto longo

Criterio de pronto:
- o autor consegue editar cenas reais
- a UI continua responsiva durante edicao normal
- estados vazios e erros sao tratados
- UX revisa o fluxo principal de escrita

Status atual:
- concluido

#### Sprint 4 - Estrutura do livro
Objetivo:
- organizar o manuscrito de forma navegavel

Escopo:
- CRUD de capitulos
- CRUD de cenas
- reordenacao
- navegacao rapida
- busca textual simples

Criterio de pronto:
- o livro pode ser estruturado e navegado de ponta a ponta
- validacoes de integridade basica continuam no `core`
- frontend apresenta feedback claro de selecao e navegacao

Status atual:
- concluido

### Fase 2 - Coerencia narrativa e diferencial

#### Sprint 5 - Personagens e consistencia narrativa
Objetivo:
- conectar personagens ao manuscrito antes de expandir o uso de tags

Escopo:
- CRUD de personagens
- tela de personagens no desktop
- vinculo de POV por cena
- busca e selecao de personagem
- validacao de referencias quebradas

Criterio de pronto:
- personagens estao ligados ao manuscrito
- inconsistencias narrativas sao detectadas no `core`
- frontend comunica problemas sem expor detalhes tecnicos
- ha testes unitarios de integridade narrativa

Status atual:
- concluido

#### Sprint 6 - Narrative Tag Engine base
Objetivo:
- reconhecer tags narrativas dentro do texto

Escopo:
- `NarrativeTag`
- `CharacterTagProfile`
- detector por regex
- parser de tags
- validacao de existencia da tag

Criterio de pronto:
- o sistema detecta tags no texto
- parser e validacao ficam isolados no `core`
- frontend consegue exibir resultado de forma compreensivel
- testes de deteccao e parse passam

Status atual:
- concluido

#### Sprint 7 - Biblioteca de tags e perfis
Objetivo:
- tornar o sistema de tags utilizavel com contexto de personagem

Escopo:
- `narrative_tags.json`
- biblioteca inicial de tags
- prefixo por personagem
- editor de perfil de personagem
- criacao de nova tag
- validador de inconsistencias

Criterio de pronto:
- o autor consegue manter a biblioteca de tags
- perfis por personagem estao operacionais
- inconsistencias sao reportadas com clareza
- UX revisa nomenclatura e fluxo de edicao

Status atual:
- concluido

#### Sprint 8 - Expansao de templates
Objetivo:
- transformar tags em texto renderizado

Escopo:
- `TemplateExpansionEngine`
- mapeamento de tag para template
- preservacao de pontuacao
- modo rascunho e modo render
- preview de expansao

Criterio de pronto:
- a expansao funciona com multiplas tags
- regras de expansao permanecem no `core`
- o desktop exibe preview claro sem confundir o estado do texto original
- testes de expansao passam

Status atual:
- concluido

### Fase 3 - Estabilizacao desktop e UX

#### Sprint 9 - Desacoplamento incremental da UI
Objetivo:
- reduzir risco estrutural da janela principal antes de ampliar escopo funcional

Escopo:
- extrair responsabilidades pequenas da UI principal
- separar formatacao e regras derivadas em helpers e servicos de apoio
- reduzir acoplamento entre estado da tela e logica de integracao

Criterio de pronto:
- a janela principal fica menor e mais legivel
- a UI continua integrada ao `core` sem regressao funcional
- o frontend documenta os pontos de integracao

Debito tecnico remanescente:
- continuar a quebra da `StoryFlameDesktopApp` em paines e controladores por responsabilidade
- reduzir estado mutavel concentrado na janela principal
- isolar navegacao, formatacao e coordenacao de eventos fora da classe principal

Status atual:
- concluido

#### Sprint 10 - Responsividade e operacoes pesadas
Objetivo:
- proteger a EDT e melhorar o feedback operacional da UI

Escopo:
- executar abrir, salvar, importar, exportar e analisar fora da EDT
- exibir estado de carregamento
- comunicar falhas operacionais com clareza
- revisar fluxos com foco em travamento percebido

Criterio de pronto:
- nenhuma operacao pesada roda na EDT
- o usuario recebe feedback visivel durante I/O e analise
- UX revisa estados de carregamento, erro e sucesso
- regressao funcional de persistencia e exportacao esta coberta

Risco residual monitorado:
- listeners reentrantes e refresh indevido ainda merecem vigilancia na UI principal
- toda nova feature desktop deve evitar ampliar a superficie de mutacao direta em componentes Swing

Status atual:
- concluido

#### Sprint 11 - Produtividade de escrita
Objetivo:
- acelerar o fluxo de escrita com tags

Escopo:
- autocomplete de tags
- popup de sugestoes
- favoritos
- ultimos usados
- hover preview
- alternancia entre rascunho e leitura

Criterio de pronto:
- os atalhos de produtividade reduzem friccao real de uso
- a UI permanece responsiva com sugestoes e previews ativos
- UX revisa nomenclatura, descobribilidade e excesso de interacao

Status atual:
- concluido

### Fase 4 - Saida, validacao real e consolidacao

#### Sprint 12 - Portabilidade e backups
Objetivo:
- garantir portabilidade confiavel do projeto

Escopo:
- exportar ZIP
- importar ZIP
- validar integridade do pacote
- migracao de versao
- backups automaticos
- validacao com cenarios de stress

Criterio de pronto:
- o projeto e portavel e seguro
- a importacao comunica arquivo invalido e migracao necessaria
- a UI continua responsiva durante operacoes de pacote
- testes de archive e storage passam

Status atual:
- concluido

#### Sprint 13 - Exportacao publicavel fase 1
Objetivo:
- gerar saidas simples e validar com manuscrito real

Escopo:
- pipeline de expansao antes do export
- exportacao TXT
- exportacao Markdown
- validacao com livro real completo

Criterio de pronto:
- o manuscrito sai em formatos textuais utilizaveis
- o fluxo de exportacao nao trava a UI
- a validacao com material real foi registrada

Status atual:
- concluido

#### Sprint 14 - Exportacao publicavel fase 2
Objetivo:
- consolidar saida editorial minima

Escopo:
- exportacao PDF
- EPUB base
- templates de formatacao
- refinamento editorial minimo do PDF

Criterio de pronto:
- PDF e EPUB gerados com consistencia basica
- falhas de exportacao sao compreensiveis para o usuario
- a validacao editorial minima foi executada

Status atual:
- concluido

#### Sprint 14.1 - Infraestrutura de testes de interface
Objetivo:
- consolidar a base de testes graficos do desktop ao fim da Fase 4

Escopo:
- adicionar AssertJ-Swing ao modulo `desktop`
- preparar a base para testes de interface grafica
- definir cobertura inicial para fluxos Swing criticos

Criterio de pronto:
- AssertJ-Swing esta configurado no build do desktop
- o projeto continua compilando e executando os testes
- a base de testes de UI fica pronta para os proximos sprints

Proximo uso esperado:
- ampliar cobertura Swing para fluxos criticos da janela principal
- usar testes graficos como rede de seguranca para a modularizacao futura da UI

Status atual:
- concluido

### Fase 5 - Analise emocional e portabilidade secundaria

#### Sprint 15 - Pipeline emocional base
Objetivo:
- entregar a primeira camada util de analise offline

Escopo:
- `Chunker`
- `EmotionAggregator`
- `EmotionCache`
- estrutura `analysis/`
- `FastTextEmotionEngine`
- persistencia de `emotion.json`

Criterio de pronto:
- o `core` gera relatorio emocional coerente
- o desktop aciona a analise sem bloquear a EDT
- o resultado e persistido e reaproveitavel

Status atual:
- concluido

#### Sprint 16 - Refinamento do relatorio emocional
Objetivo:
- tornar a leitura do relatorio mais clara e util

Escopo:
- nomenclatura mais natural
- heuristica PT-BR revisada
- melhoria da leitura do relatorio
- revisao UX do fluxo de analise

Criterio de pronto:
- o relatorio e compreensivel sem linguagem tecnica desnecessaria
- o fluxo deixa claro quando a analise esta rodando e quando terminou
- heuristica minima revisada foi validada

Limite funcional assumido:
- o relatorio emocional atual e heuristico e offline-first
- nao deve ser tratado como analise semantica robusta de contexto, ambiguidade ou ironia

Status atual:
- concluido

#### Sprint 17 - Android de apoio
Objetivo:
- usar o Android como validacao tardia da portabilidade do `core`

Escopo:
- confirmar build com Java 21
- validar uso do `core` no Android
- definir um escopo leve de consulta ou edicao futura

Criterio de pronto:
- o Android nao interfere na evolucao do desktop
- a portabilidade do nucleo esta confirmada
- o escopo do modulo fica explicitamente limitado

Escopo assumido nesta fase:
- Android serve como modulo de apoio e validacao de portabilidade do `core`
- a interface atual nao representa um fluxo editorial completo
- qualquer ampliacao do Android deve ser reavaliada antes de virar meta de produto

Status atual:
- concluido

### Fase 6 - Adaptacoes guiadas por benchmark

#### Sprint 18 - Sinopse de cena
Objetivo:
- adicionar uma camada curta de planejamento e revisao diretamente na cena

Escopo:
- incluir `sinopse da cena` no modelo
- persistir a sinopse no archive
- exibir e editar a sinopse abaixo do titulo da cena
- validar roundtrip de persistencia

Criterio de pronto:
- a sinopse pode ser criada e editada sem atrito
- a persistencia nao perde a sinopse
- a UI continua simples e responsiva
- UX valida que a sinopse e curta, clara e util

Origem do benchmark:
- adaptacao guiada pelo benchmark de `Scrivener`

Status atual:
- concluido

#### Sprint 19 - Outline leve do manuscrito
Objetivo:
- reforcar a leitura estrutural do manuscrito sem abrir todas as cenas

Escopo:
- criar uma visao de `outline` para capitulos e cenas
- mostrar titulo e sinopse curta
- exibir contexto minimo como POV ou estado da cena
- manter reordenacao simples

Criterio de pronto:
- o usuario entende a estrutura do livro sem abrir o texto completo
- a visao de outline complementa a estrutura atual sem duplicar confusao
- a EDT segue protegida durante interacao e reordenacao

Origem do benchmark:
- adaptacao guiada pelo benchmark de `Scrivener`

Status atual:
- concluido

#### Sprint 20 - Contexto lateral fixo da cena atual
Objetivo:
- reduzir troca de aba no fluxo principal de escrita

Escopo:
- apresentar contexto lateral da cena atual no editor
- incluir POV, personagens ligados, tags detectadas e estado de integridade
- integrar a sinopse nesse bloco quando fizer sentido

Criterio de pronto:
- o autor acessa o contexto principal da cena sem sair do editor
- a interface nao vira um `Inspector` pesado
- a integracao nao reabre acoplamento estrutural excessivo no desktop

Origem do benchmark:
- adaptacao guiada pelo benchmark de `Scrivener`

Status atual:
- concluido

#### Sprint 21 - Presets de publicacao
Objetivo:
- tornar a publicacao mais previsivel e mais editorial

Escopo:
- definir presets claros por objetivo de saida
- deixar explicito o que entra na publicacao
- revisar a linguagem do fluxo de publicar manuscrito

Criterio de pronto:
- o usuario entende melhor o resultado esperado antes de publicar
- exportar projeto e publicar manuscrito continuam semanticamente separados
- o fluxo de saida fica mais confiavel sem abrir escopo de compilacao pesada

Origem do benchmark:
- adaptacao guiada pelo benchmark de `Scrivener`

Status atual:
- concluido

## 4. Itens fora do MVP imediato

- decomposicao adicional da `StoryFlameDesktopApp` em paines/controladores menores
- ampliacao da cobertura AssertJ-Swing para fluxos criticos de desktop
- analise emocional por personagem
- evolucao da heuristica emocional alem do modelo lexical offline atual
- Modo C com transformer
- revisao inteligente
- packs de templates
- otimizacoes Android alem da validacao do nucleo
- reducao de divida tecnica de build no Android
- `corkboard` completo ao estilo Scrivener
- `Inspector` multifuncional pesado com muitas abas e metadados
- multiplicacao de modos de visualizacao antes de consolidar sinopse e outline

## 5. Regra de revisao continua

Ao fim de cada sprint, revisar:

- o que foi entregue de fato
- o que ficou parcialmente pronto
- quais riscos de acoplamento surgiram
- quais riscos de EDT surgiram
- quais melhorias de UX precisam voltar para o backlog
- quais limites funcionais precisam ser comunicados sem sobrepromessa
- se a worktree e o lote de mudancas continuam rastreaveis para PR ou release
