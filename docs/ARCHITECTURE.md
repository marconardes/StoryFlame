# ARCHITECTURE.md

## Visao geral

O StoryFlame e um sistema desktop offline-first para gerenciamento de manuscritos, com `core` em Java 21 e interface desktop Swing. O Android continua como validacao tardia de portabilidade do nucleo. A diretriz atual e preservar equivalencia estrutural e funcional dentro da stack Java existente, com frontend Swing e backend Java bem separados.

O projeto trabalha com duas visoes complementares:

- visao de responsabilidade: `backend`, `frontend` e `ux`
- visao de modulo: `core`, `desktop` e `android`

Essas visoes nao competem entre si. Elas se sobrepoem da seguinte forma:

- `backend` vive principalmente no modulo `core`
- `frontend` vive principalmente no modulo `desktop`, com foco em Swing
- `ux` revisa fluxos e interfaces, sem virar modulo de runtime
- `android` reutiliza o `core`, mas nao e a experiencia principal do MVP

## Objetivos arquiteturais

- separar regra de negocio da interface
- reduzir acoplamento entre modulos
- permitir evolucao incremental do desktop
- preservar responsividade da UI durante a transicao de plataforma
- manter o produto offline-first
- preservar equivalencia estrutural entre a logica atual e a entrega final
- manter o produto desktop coerente e responsivo

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

Na arquitetura atual, isso continua concentrado na aplicacao Swing, com contratos Java claros entre o frontend e o backend.

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

### `android`
Responsavel por:
- validacao de portabilidade do nucleo
- consulta e edicao leve em momento posterior

O Android nao deve guiar a ordem das prioridades do MVP enquanto a transicao principal de interface ainda estiver em execucao.

## Regras de separacao

- `core` nao depende de Swing
- `desktop` nao implementa regra de negocio
- `desktop` nao acessa persistencia fora dos contratos do `core`
- validacoes criticas ficam no `core`
- UX revisa comportamento e clareza, nao logica de dominio

## Contratos entre `core` e as interfaces

As interfaces devem consumir o `core` por servicos e tipos explicitos, evitando espalhar regra de negocio na UI.

A camada de aplicacao Java entre `core` e frontend existe para manter os casos de uso claros e evitar que a UI Swing acesse persistencia ou regra de negocio diretamente.

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

Na UI Swing:

- operacoes pesadas continuam fora da camada de apresentacao
- estados de carregamento, erro e sucesso devem ser consistentes
- o empacotamento final deve ser percebido pelo usuario como um unico app desktop coeso

## Risco arquitetural atual

O principal risco tecnico atual esta na concentracao de responsabilidades da UI desktop. A janela principal ainda acumula estado, navegacao, integracao e acoes demais.

O principal risco tecnico atual e repetir esse acoplamento dentro da propria janela Swing sem separar bem contratos de aplicacao.

Diretriz de mitigacao:

- extrair responsabilidades em passos pequenos
- separar acoes de I/O da logica de montagem da UI
- manter nomes e contratos claros
- evitar refatoracao ampla sem entregas concretas
- migrar por fatias funcionais, nao por reescrita total
- tratar a equivalencia funcional como criterio de aceite em cada fatia entregue

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
- a mudanca permanece coerente com `core`, `desktop` e `android`
