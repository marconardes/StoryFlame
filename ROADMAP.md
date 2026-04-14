# Roadmap Oficial do StoryFlame

Este e o unico roadmap ativo do repositorio. O nome oficial do projeto e `StoryFlame`, a base atual e `Java 21 + Swing`, com `core` em Java como fonte de dominio, validacoes e persistencia. A prioridade do MVP continua sendo o desktop offline-first multiplataforma. A evolucao do produto deve preservar equivalencia estrutural e funcional com o estado atual, sem introduzir uma nova shell desktop.

## 1. Direcao do produto

- foco principal: escrita de web novel no desktop
- principio estrutural: offline-first
- persistencia canonica: ZIP versionado com JSON interno
- diferencial central: Narrative Tag Engine
- interface-alvo principal: Swing
- o `core` em Java permanece como fonte principal da regra de negocio, validacoes e persistencia
- a evolucao da UI deve ser incremental e controlada
- plataforma secundaria nesta fase: Android como validacao tardia do nucleo compartilhado
- Android permanece como modulo de apoio e nao deve ser comunicado como interface editorial completa nesta fase

## 2. Regras de planejamento

- nenhum sprint esta pronto sem backend, frontend e revisao de UX
- nenhuma feature nova deve entrar se abrir risco claro de travamento na EDT enquanto o `desktop` Swing ainda estiver ativo
- o `core` concentra regra de negocio, validacoes e persistencia
- o frontend nao deve absorver regra de negocio
- toda fase de evolucao deve preservar compatibilidade com o formato `.storyflame`
- toda fase de evolucao deve preservar equivalencia observavel do comportamento essencial
- o roadmap deve ser revisado ao fim de cada sprint com base no que foi validado de fato

## 3. Estrutura atual de plataforma

O StoryFlame trata `Swing` como interface desktop principal nesta linha de produto:

- `Swing` permanece como frontend ativo
- o `core` continua como base reaproveitada da regra de negocio
- a fronteira entre `core` e UI deve seguir clara e enxuta
- o objetivo do roadmap e consolidar a aplicacao desktop Java sem adicionar outra shell

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
- entregar o fluxo essencial de escrita e navegacao editorial

Escopo:
- editor de cena
- navegacao entre capitulos e cenas
- preview e resumo de contexto
- contador de palavras
- validacao visual minima

Criterio de pronto:
- o fluxo principal de escrita funciona
- a interface nao bloqueia a EDT
- os estados de carregamento e erro sao claros

Status atual:
- concluido

#### Sprint 4 - Estrutura e analise
Objetivo:
- consolidar estrutura editorial, busca e analise

Escopo:
- estrutura de livro com CRUD de capitulos e cenas
- busca textual
- CRUD de personagens
- associacao de POV
- tags narrativas
- analise emocional offline

Criterio de pronto:
- os fluxos editoriais principais ficam acessiveis na UI
- a analise e a busca entregam retorno util
- o usuario consegue operar sem travamentos aparentes

Status atual:
- concluido

#### Sprint 5 - UX e manutencao
Objetivo:
- reduzir friccao de uso e melhorar manutencao da base Swing

Escopo:
- separar melhor responsabilidades na janela principal
- uniformizar feedback visual
- revisar estados vazios, erros e carregamento
- ajustar nomenclatura e consistencia de comandos

Criterio de pronto:
- a UI fica mais previsivel
- os fluxos mais usados ficam mais claros
- UX aprova as mudancas visuais e de navegacao

Status atual:
- em andamento

## 5. Prioridades imediatas

1. Reduzir concentracao de responsabilidade da `StoryFlameDesktopApp`.
2. Separar melhor contratos de aplicacao da camada Swing.
3. Melhorar consistencia visual e feedback de operacoes longas.
4. Ampliar cobertura automatizada dos fluxos desktop.
5. Limitar o `android` a validacao de portabilidade ate a UI desktop estabilizar o fluxo principal.

## 6. Critério de pronto do produto

Este plano so pode ser considerado encerrado quando:

- a documentacao principal permanecer consistente
- a UI desktop nao bloquear a EDT nos fluxos criticos
- a janela principal estiver menos concentrada
- o roadmap estiver sendo usado como referencia real de execucao
- backend, frontend e UX estiverem refletidos no pronto das entregas
