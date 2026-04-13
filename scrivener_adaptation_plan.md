# Scrivener Adaptation Plan

Data: 2026-03-29
Origem:
- benchmark base: [research/report2.md](/home/marconardes/IAS_Project/StoryFlame/research/report2.md)
- consolidacao de produto: agente com papel de `Gerente de Projeto`
- consolidacao de UX: agente com papel de `UX Reviewer Agent`
- validacao tecnica: agente com papel de `revisor tecnico do frontend`

## Objetivo

Adaptar para o StoryFlame apenas o que o benchmark do Scrivener mostrou ser realmente valioso para:

- escrita nao linear
- visao estrutural do manuscrito
- contexto editorial ao lado da escrita
- publicacao mais previsivel

Sem copiar:

- a complexidade total do Scrivener
- o volume de modos e paines
- um `Inspector` multifuncional pesado
- um `Corkboard` completo nesta fase

## Decisao consolidada

### Entra agora

1. `Sinopse de cena`
2. `Visao de outline` para capitulos e cenas
3. `Contexto lateral fixo` da cena atual
4. `Presets mais claros de exportacao/publicacao`

### Nao entra agora

- `Corkboard` completo
- `Inspector` completo com muitas abas e metadados
- reestruturacao visual grande da aplicacao
- multiplicacao de modos de edicao
- tentativa de copiar a experiencia total do Scrivener

## Ordem recomendada

## Divisao por camada

### Backend

Responsavel por:

- evoluir modelos do `core`
- persistencia e migracao do archive
- validacoes de consistencia
- contratos consumidos pelo desktop
- testes de roundtrip e regressao funcional

### Frontend

Responsavel por:

- apresentar os novos campos e visoes no desktop Swing
- integrar a UI aos contratos do `core`
- manter a EDT protegida
- preservar clareza de fluxo e responsividade

### UX

Responsavel por:

- validar se a adaptacao melhora a escrita e a organizacao
- evitar inflacao de interface
- impedir que o StoryFlame vire uma copia pesada do Scrivener

### Etapa 1. Sinopse de cena

Objetivo:
- criar uma camada curta entre `titulo` e `texto completo`

Escopo:
- adicionar `sinopse da cena` no modelo de `Scene`
- persistir a sinopse no archive
- mostrar o campo no editor, logo abaixo do titulo da cena
- manter o campo curto e visivelmente orientado a resumo rapido

Backend:
- adicionar `synopsis` ao modelo de `Scene`
- atualizar serializacao e desserializacao do archive
- tratar compatibilidade com projetos antigos sem sinopse
- cobrir roundtrip de persistencia e migracao

Frontend:
- exibir o campo no editor abaixo de `Titulo da cena`
- limitar o componente a um uso curto e objetivo
- sincronizar alteracoes sem degradar a responsividade da UI

Contrato esperado:
- o desktop recebe e persiste `synopsis` como parte da `Scene`
- ausencia do campo em projetos antigos nao quebra abertura nem migracao

Diretriz de UX:
- o campo deve ter no maximo 2 ou 3 linhas no fluxo principal
- a funcao precisa ficar obvia: resumir a cena para estrutura e revisao

Critério de pronto:
- o usuario consegue resumir rapidamente a funcao da cena
- a sinopse aparece sem abrir uma nova tela
- a persistencia e os testes de roundtrip sao atualizados

Status:
- concluido

### Etapa 2. Outline leve de manuscrito

Objetivo:
- permitir revisar a estrutura sem abrir cada cena

Escopo:
- adicionar uma visao de `outline` na area de estrutura
- listar `capitulo > cena`
- mostrar pelo menos:
  - titulo da cena
  - sinopse curta
  - POV atual ou estado equivalente
- manter reordenacao simples

Backend:
- expor dados suficientes para um resumo estrutural de cena
- se necessario, criar um formatter ou DTO interno de outline sem empurrar regra para a UI
- validar que reordenacao continua preservando integridade do manuscrito

Frontend:
- montar a visao de outline como extensao da estrutura atual
- reutilizar contratos existentes de capitulo/cena sempre que possivel
- mostrar resumo estrutural sem abrir o texto completo

Contrato esperado:
- a UI consegue listar cena com `titulo + sinopse + contexto minimo`
- reordenacao continua a usar o dominio atual, sem duplicar logica na interface

Diretriz de UX:
- a visao deve ser leve e complementar, nao um novo sistema paralelo
- nao copiar `corkboard` neste passo

Critério de pronto:
- o usuario entende a progressao do manuscrito sem abrir o texto completo
- a reorganizacao continua clara e responsiva
- a UI nao ganha um fluxo excessivamente pesado

Status:
- concluido

### Etapa 3. Contexto lateral fixo da cena atual

Objetivo:
- reduzir troca de aba para o contexto mais util durante a escrita

Escopo:
- painel lateral ou bloco lateral estavel no editor com:
  - POV da cena
  - personagens ligados
  - tags detectadas
  - estado de integridade
  - sinopse da cena, se fizer sentido no layout final

Backend:
- consolidar os dados de contexto da cena em contratos claros
- evitar que a UI tenha de consultar e derivar tudo por conta propria
- manter integridade narrativa e parse de tags no `core`

Frontend:
- apresentar o contexto de forma estavel ao lado do editor
- evitar abrir nova aba para o caso comum
- manter sincronizacao visual previsivel quando a cena muda

Contrato esperado:
- o desktop recebe um conjunto coerente de dados da cena atual
- personagens, POV, integridade e tags continuam resolvidos fora da UI

Diretriz de UX:
- contexto deve apoiar a escrita, nao competir com ela
- o essencial precisa estar visivel sem abrir mais uma aba

Critério de pronto:
- o autor ve o contexto principal da cena no mesmo fluxo do editor
- personagens/tags/integridade deixam de exigir troca de aba para o caso comum
- a EDT continua protegida e a janela principal nao volta a inflar sem controle

Status:
- concluido

### Etapa 4. Publicacao com presets mais claros

Objetivo:
- aproximar o fluxo de `publicar manuscrito` de um processo editorial previsivel

Escopo:
- presets explicitos de publicacao por objetivo
- separar melhor o que entra no manuscrito final
- revisar a linguagem do fluxo de exportacao/publicacao

Backend:
- formalizar presets e opcoes de publicacao no `core`
- manter regras de inclusao/exclusao fora da interface
- garantir validacao previsivel antes de exportar

Frontend:
- apresentar presets com linguagem clara
- mostrar o que sera publicado sem inventar regra local
- preservar separacao entre `exportar projeto` e `publicar manuscrito`

Contrato esperado:
- o desktop consome presets do `core`
- a composicao do manuscrito publicado continua centralizada fora da UI

Diretriz de UX:
- publicacao deve parecer um fluxo principal do produto, nao um utilitario solto

Critério de pronto:
- o usuario entende o que vai sair antes de publicar
- exportar projeto e publicar manuscrito continuam semanticamente separados

Status:
- concluido

## Riscos principais

- inflar a `StoryFlameDesktopApp` outra vez
- misturar regra de negocio com detalhe de UI Swing
- criar uma segunda interface estrutural que o usuario nao entende
- transformar a sinopse em mais um campo grande e ambíguo
- aumentar superficie de sincronizacao de selecao e refresh sem cobertura de testes
- deixar contratos de backend implicitos e forcar a UI a derivar estado demais

## Regras de implementacao

- cada etapa deve ser pequena e rastreavel
- backend continua dono de modelo, persistencia e validacao
- frontend so consome contratos do `core`
- toda etapa deve explicitar impacto em `core`, `desktop` e testes
- toda adaptacao deve parecer evolucao do StoryFlame, nao imitacao do Scrivener
- qualquer ampliacao alem deste plano precisa de nova validacao de escopo

## Recomendacao final

Melhor recorte de adaptacao do benchmark:

1. `sinopse de cena`
2. `outline leve`
3. `contexto lateral estavel`
4. `presets de publicacao`

Tudo alem disso deve ficar fora do ciclo atual.
