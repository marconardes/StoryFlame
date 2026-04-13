# Next Action Plan

Data: 2026-03-29
Base: `review3.md`
Responsavel pela consolidacao: Gerente de Projeto (`Pasteur`)

## Objetivo

Conduzir o proximo ciclo do StoryFlame sem abrir novo escopo funcional, priorizando:

- integridade de persistencia
- reforco dos contratos ja formalizados no `core`
- ampliacao da rede de seguranca Swing nas areas ainda mais frageis

## Diretriz

Este plano nao inclui:

- novas features de produto
- expansao do Android alem do papel atual de apoio
- nova refatoracao estrutural grande da `StoryFlameDesktopApp`
- reabertura de `B1`, `C2`, `E1` ou `E2` como se ainda estivessem pendentes

## Ordem de execucao

### Fase 1 - Hardening do archive

#### 1.1 Endurecer corrupcao estrutural na abertura
Objetivo:
- fechar bordas restantes de arquivo malformado no `core`

Escopo:
- ampliar cenarios de corrupcao em `ProjectArchiveStore.open()`
- cobrir:
  - `project.json` malformado
  - `manifest.json` inconsistente
  - versao futura/incompativel
  - entradas ausentes ainda nao cobertas

Arquivos principais:
- [ProjectArchiveStore.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectArchiveStore.java)
- [ProjectArchiveStoreTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/storage/ProjectArchiveStoreTest.java)
- [ProjectArchiveInspectorTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/storage/ProjectArchiveInspectorTest.java)

Criterio de saida:
- pacote malformado nao abre como valido
- os cenarios de corrupcao residual ficam cobertos por teste

Status atual:
- concluido

#### 1.2 Garantir preservacao de conteudo em importacao e migracao
Objetivo:
- impedir perda silenciosa de dados em rotas de archive

Escopo:
- validar roundtrip de importacao e migracao preservando:
  - estrutura do livro
  - `narrativeTags`
  - `characterTagProfiles`
  - `emotionCache`

Arquivos principais:
- [ProjectArchiveStore.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectArchiveStore.java)
- [ProjectArchiveStoreTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/storage/ProjectArchiveStoreTest.java)

Criterio de saida:
- importacao e migracao preservam os campos relevantes do modelo
- os testes falham se houver perda de conteudo estrutural ou analitico

Status atual:
- concluido

### Fase 2 - Reforco dos contratos do core

#### 2.1 Reforcar testes dos contratos ja entregues em `B1`
Objetivo:
- consolidar os contratos usados pelo desktop sem reabrir API nova

Escopo:
- ampliar cobertura para:
  - `validateForSave(...)`
  - `validateForArchiveExport(...)`
  - `PublicationExportService.validate(...)`
- tratar isso como reforco de teste, nao como nova frente funcional

Arquivos principais:
- [ProjectValidationServiceTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/validation/ProjectValidationServiceTest.java)
- [ProjectArchiveStoreTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/storage/ProjectArchiveStoreTest.java)
- [PublicationExportServiceTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/publication/PublicationExportServiceTest.java)

Criterio de saida:
- as rotas que o desktop consome ficam blindadas por testes
- nenhuma mudanca futura quebra validacao sem falha automatica clara

Status atual:
- concluido

### Fase 3 - Regressao Swing focada

#### 3.1 Cobrir personagem e tag com escopo minimo e estavel
Objetivo:
- proteger a area mais sensivel restante da UI sem abrir teste fragil

Escopo:
- adicionar regressao Swing para:
  - editar personagem existente
  - editar tag existente
  - refletir mudanca em lista e feedback visual
- evitar nesta primeira passada:
  - `JOptionPane`
  - `JFileChooser`
  - exclusao
  - duplicacao
  - fluxos modais

Arquivos principais:
- [StoryFlameDesktopAppSwingTest.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/test/java/io/storyflame/desktop/StoryFlameDesktopAppSwingTest.java)
- [StoryFlameDesktopApp.java](/home/marconardes/IAS_Project/StoryFlame/desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java)

Criterio de saida:
- a suite Swing cobre os ciclos mais comuns de personagem e tag
- os testes permanecem estaveis e sem dependencia modal

Status atual:
- concluido

### Fase 4 - Avaliacao condicionada

#### 4.1 Reavaliar stress test de autosave
Objetivo:
- decidir com criterio se vale ampliar a agressividade de teste do autosave

Escopo:
- verificar se existe seam deterministico suficiente
- so avancar se o teste puder ser estavel

Arquivos principais:
- [ProjectAutosaveService.java](/home/marconardes/IAS_Project/StoryFlame/core/src/main/java/io/storyflame/core/storage/ProjectAutosaveService.java)
- [ProjectAutosaveServiceTest.java](/home/marconardes/IAS_Project/StoryFlame/core/src/test/java/io/storyflame/core/storage/ProjectAutosaveServiceTest.java)

Criterio de saida:
- ou o teste entra de forma deterministica
- ou o item e encerrado explicitamente como adiado por risco de flakiness

Status atual:
- concluido

## Fora do ciclo

- novas features de produto
- expansao Android
- nova quebra grande da `StoryFlameDesktopApp`
- retrabalho de UX em `personagem/tag` sem evidencia nova
- nova frente de escopo para analise emocional
- limpeza estrutural cosmetica de helpers e coordinators

## Regra de decisao

Uma acao so entra em execucao se:

- tiver retorno direto em integridade, previsibilidade ou cobertura
- nao reabrir frente ja concluida sem evidencia nova
- nao criar risco desnecessario de teste fragil

## Fechamento esperado

Este plano pode ser considerado concluido quando:

- o archive estiver endurecido contra corrupcao residual
- importacao e migracao preservarem conteudo relevante sob teste
- os contratos do `core` consumidos pelo desktop estiverem reforcados por cobertura
- a suite Swing cobrir personagem e tag com escopo pequeno e estavel
- o item de autosave tiver decisao objetiva: implementar ou adiar formalmente
