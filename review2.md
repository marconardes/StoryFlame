# Revisao Tecnica 2

Data: 2026-03-29
Modelo de revisao: 4 agentes
- Gerente de Projeto: consolidacao e priorizacao
- Backend Agent: regras, persistencia e contratos
- Frontend Agent: Swing, EDT, modularizacao e testes de UI
- UX Reviewer Agent: clareza de fluxo, feedback e consistencia

## Resumo executivo

O codigo esta funcional e cobre o roadmap principal, mas ainda carrega riscos estruturais relevantes. O maior risco atual nao e falta de feature; e combinacao de integridade de persistencia permissiva, autosave concorrendo com objeto mutavel vivo e uma UI desktop ainda concentrada demais em uma unica classe. Android permanece coerente como modulo de apoio, mas nao deve ser lido como fluxo real de produto. O proximo ciclo deve priorizar estabilizacao tecnica e clareza contratual entre `core` e `desktop`.

## Top 5 riscos

### 1. Persistencia permissiva pode esconder corrupcao de pacote

- Severidade: alta
- Origem: Backend Agent
- Achado: `ProjectArchiveStore.open()` reconstrui o projeto de forma tolerante demais e pode abrir ZIP parcialmente corrompido sem falhar cedo.
- Arquivos:
  - [ProjectArchiveStore.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectArchiveStore.java)
- Impacto:
  - risco de perda silenciosa de dados
  - pacote invalido pode parecer valido para o usuario
- Direcao recomendada:
  - endurecer validacao de integridade na abertura
  - diferenciar erro recuperavel de arquivo parcialmente invalido

### 2. Autosave usa objeto mutavel vivo em background

- Severidade: alta
- Origem: Backend Agent
- Achado: `ProjectAutosaveService.schedule()` serializa o `Project` enquanto a UI ainda pode mutar listas internas expostas pelo modelo.
- Arquivos:
  - [ProjectAutosaveService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectAutosaveService.java)
  - [Project.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/model/Project.java)
- Impacto:
  - snapshot inconsistente
  - corrida entre EDT e thread de persistencia
  - falha dificil de reproduzir
- Direcao recomendada:
  - introduzir snapshot imutavel ou copia defensiva antes do autosave
  - revisar exposicao de colecoes mutaveis no dominio

### 3. UI desktop ainda esta grande demais e muito acoplada

- Severidade: critica
- Origem: Frontend Agent
- Achado: [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java) continua concentrando montagem de tela, estado, listeners, integracao com `core`, background operations e feedback visual.
- Evidencia:
  - o arquivo segue com milhares de linhas
  - `DocumentListener`s, autosave e refreshs convivem na mesma classe
- Impacto:
  - alta chance de regressao
  - baixo seam para testes
  - manutencao lenta
- Direcao recomendada:
  - quebrar por responsabilidade: editor, personagens, tags, publicacao, analise e operacoes de fundo
  - reduzir estado mutavel centralizado

### 4. Contrato de validacao entre backend e frontend esta implicito

- Severidade: media-alta
- Origem: Backend Agent
- Achado: validadores existem, mas `save()` e `export()` ainda aceitam estado narrativo invalido sem contrato estruturado de bloqueio ou retorno de inconsistencias.
- Arquivos:
  - [NarrativeIntegrityValidator.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/validation/NarrativeIntegrityValidator.java)
  - [TagLibraryValidator.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/tags/TagLibraryValidator.java)
  - [PublicationExportService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/publication/PublicationExportService.java)
- Impacto:
  - responsabilidade difusa
  - UI precisa "lembrar" de validar
  - risco de exportar ou persistir estado inconsistente
- Direcao recomendada:
  - formalizar contrato de validacao no `core`
  - devolver resultado estruturado para a UI decidir bloqueio ou aviso

### 5. UX e comunicacao de escopo ainda podem induzir interpretacao errada

- Severidade: media
- Origem: UX Reviewer Agent + Frontend Agent
- Achados:
  - Android atual e demonstracao de apoio, nao fluxo editorial real
  - criacao de personagem/tag ainda mistura rascunho e edicao posterior
  - publicacao/exportacao e analise emocional podem parecer mais "finais" do que realmente sao
- Arquivos:
  - [MainActivity.java](/home/marconardes/IAS_Project/StoryFlame/android/src/main/java/io/storyflame/android/MainActivity.java)
  - [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)
- Impacto:
  - expectativa errada sobre Android
  - friccao nos fluxos de personagem e tag
  - risco de sobrepromessa do relatorio emocional
- Direcao recomendada:
  - explicitar melhor estados de rascunho e conclusao
  - separar visualmente exportacao de projeto e publicacao de manuscrito
  - manter aviso claro de heuristica no relatorio emocional

## Achados por agente

### Gerente de Projeto

- O roadmap principal foi fechado, mas o codigo ainda nao esta em estado folgado.
- O proximo ciclo deve ser de estabilizacao, nao de expansao funcional.
- Os riscos se concentram em integridade, concorrencia, modularidade e clareza de escopo.

### Backend

- Abertura de pacote ainda tolera corrupcao parcial em excesso.
- Autosave concorre com objeto mutavel vivo.
- Validacoes nao estao acopladas formalmente aos pontos de salvar/publicar/exportar.

### Frontend

- A janela principal permanece supercarregada.
- O uso de `SwingWorker` protege os fluxos principais, mas o feedback operacional ainda e grosso e pouco granular.
- A cobertura Swing existe, mas ainda cobre pouco dos fluxos criticos reais.

### UX

- Ha redundancia de acoes entre menu, toolbar e superficie principal.
- Personagem e tag ainda pedem fluxo mais guiado e menos interrompivel.
- Estados de empty/loading/error nao estao igualmente claros em todos os fluxos.

## Prioridades praticas para o proximo ciclo

1. Endurecer `open()` do ZIP e formalizar o que e pacote invalido, parcialmente invalido e migravel.
2. Blindar o autosave com snapshot/copia defensiva antes de persistir.
3. Extrair responsabilidades da `StoryFlameDesktopApp` antes de adicionar novas features desktop.
4. Criar contrato de validacao do `core` para salvar/exportar/publicar.
5. Ampliar os testes AssertJ-Swing para fluxos reais: salvar, abrir, exportar, importar e analisar.
6. Refinar a UX de personagem/tag com estado de rascunho explicito e CTA unico de conclusao.
7. Manter Android explicitamente como modulo de apoio ate existir fluxo real de produto.

## Pendencias

- Esta revisao foi analitica. Nao executei testes novos nesta rodada.
- O `review1.md` continua valido; o `review2.md` aprofunda e prioriza os achados com a estrutura de 4 agentes.

## Conclusao

O StoryFlame esta em ponto bom para consolidacao de MVP, mas ainda nao para expansao segura sem pagar debito tecnico. O principal risco mudou de "faltam recursos" para "faltam garantias estruturais". O proximo passo correto e estabilizar backend e desktop antes de abrir novo escopo.
