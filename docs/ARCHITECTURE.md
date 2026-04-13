# ARCHITECTURE.md

## Visao geral

O StoryFlame e um sistema desktop offline-first para gerenciamento de manuscritos, com `core` em Java 21 e migracao planejada da interface de `Swing` para `Electron`. O Android continua como validacao tardia de portabilidade do nucleo. A diretriz atual da migracao e preservar equivalencia estrutural e funcional com o produto existente, entregando ao final uma distribuicao unificada por plataforma.

O projeto trabalha com duas visoes complementares:

- visao de responsabilidade: `backend`, `frontend` e `ux`
- visao de modulo: `core`, `desktop`, `electron` e `android`

Essas visoes nao competem entre si. Elas se sobrepoem da seguinte forma:

- `backend` vive principalmente no modulo `core`
- `frontend` vive hoje principalmente no modulo `desktop`, mas a interface-alvo passa a ser `electron`
- `ux` revisa fluxos e interfaces, sem virar modulo de runtime
- `android` reutiliza o `core`, mas nao e a experiencia principal do MVP

## Objetivos arquiteturais

- separar regra de negocio da interface
- reduzir acoplamento entre modulos
- permitir evolucao incremental do desktop
- preservar responsividade da UI durante a transicao de plataforma
- manter o produto offline-first
- permitir migracao para Electron sem reescrever o dominio por padrao
- preservar equivalencia estrutural entre a logica atual e a entrega final
- permitir empacotamento unificado do produto final por plataforma

## Mapeamento de responsabilidades

### Backend
Responsavel por:
- entidades de dominio
- regras de negocio
- validacoes
- persistencia
- servicos de aplicacao
- contratos consumidos pela UI

No repositorio atual, isso fica concentrado em `core`.

### Frontend
Responsavel por:
- janelas
- paineis
- formularios
- navegacao
- feedback visual
- integracao com contratos do `core`

No repositorio atual, isso fica concentrado em `desktop`.

Na arquitetura-alvo, isso sera repartido entre:

- modulo Java de aplicacao para contratos e casos de uso
- modulo `electron` para shell, navegacao e apresentacao

### UX
Responsavel por:
- revisar clareza de fluxo
- apontar friccao de uso
- avaliar consistencia visual
- recomendar melhorias praticas ao frontend

UX nao implementa regra de negocio nem acessa persistencia.

## Mapeamento de modulos

### `core`
Responsavel por:
- modelos de dominio
- persistencia ZIP/JSON
- busca
- tags narrativas
- exportacao publicavel
- analise emocional
- validacoes de integridade

### `desktop`
Responsavel por:
- experiencia Swing legada enquanto a migracao nao termina
- integracao com servicos do `core`
- exibicao de estados, erros e resultados
- operacoes Swing executadas na thread correta

### `electron`
Responsavel por:
- nova interface principal do produto
- shell desktop multiplataforma
- navegacao, layout e feedback visual modernos
- integracao com backend Java local por bridge ou IPC
- composicao da distribuicao final unificada do produto desktop

### `android`
Responsavel por:
- validacao de portabilidade do nucleo
- consulta e edicao leve em momento posterior

O Android nao deve guiar a ordem das prioridades do MVP enquanto a transicao principal de interface ainda estiver em execucao.

## Regras de separacao

- `core` nao depende de Swing
- `desktop` nao implementa regra de negocio
- `desktop` nao acessa persistencia fora dos contratos do `core`
- `electron` nao implementa regra de negocio
- validacoes criticas ficam no `core`
- UX revisa comportamento e clareza, nao logica de dominio

## Contratos entre `core` e as interfaces

As interfaces devem consumir o `core` por servicos e tipos explicitos, evitando espalhar regra de negocio na UI.

Durante a transicao para Electron, deve existir uma camada de aplicacao Java entre `core` e frontend.

Essa camada existe para dois objetivos:

- preservar equivalencia de comportamento entre Swing e Electron durante a migracao
- permitir que a distribuicao final em Electron embarque a logica equivalente sem mover regra de negocio para o renderer

Contratos atuais relevantes incluem:

- persistencia de projeto
- integridade narrativa
- busca no manuscrito
- biblioteca e expansao de tags
- exportacao publicavel
- analise emocional

Sempre que possivel, a UI deve apenas:

1. coletar entrada do usuario
2. acionar um contrato do `core`
3. interpretar o resultado
4. exibir feedback adequado

## Concorrencia e responsividade

Enquanto a UI Swing ainda estiver ativa:

- nunca bloquear a EDT
- operacoes pesadas de abrir, salvar, importar, exportar e analisar devem rodar fora da EDT
- atualizacoes visuais devem acontecer apenas na thread correta
- operacoes longas devem exibir estado de carregamento ou progresso

Na nova frente Electron:

- operacoes pesadas continuam fora da camada de apresentacao
- a bridge entre Electron e Java nao deve transportar regra de negocio para o renderer
- estados de carregamento, erro e sucesso devem ser unificados na nova linguagem visual
- o empacotamento final deve ser percebido pelo usuario como um unico app por plataforma, mesmo que internamente exista mais de um processo

## Risco arquitetural atual

O principal risco tecnico atual esta na concentracao de responsabilidades da UI desktop. A janela principal ainda acumula estado, navegacao, integracao e acoes demais.

O principal risco da migracao e repetir esse acoplamento em Electron sem antes criar contratos de aplicacao claros.

Diretriz de mitigacao:

- extrair responsabilidades em passos pequenos
- separar acoes de I/O da logica de montagem da UI
- manter nomes e contratos claros
- evitar refatoracao ampla sem entregas concretas
- migrar por fatias funcionais, nao por reescrita total
- tratar a equivalencia funcional como criterio de aceite em cada fatia migrada

## Fluxo padrao entre agentes

1. Backend Agent implementa ou ajusta modelos, servicos, validacoes e contratos
2. Frontend Agent consome esses contratos e atualiza a UI
3. UX Reviewer Agent revisa a experiencia
4. Frontend Agent aplica as melhorias de UX viaveis

## Criterio de pronto

Uma funcionalidade so esta pronta quando:

- o backend esta correto
- o frontend esta integrado
- UX revisou o fluxo alterado
- testes relevantes passam
- a interface continua responsiva
- a mudanca permanece coerente com `core`, `desktop`, `electron` e `android`
