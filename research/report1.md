# Competitive Research Report 1

Data: 2026-03-29
Agente: `Competitive Research Agent`
Projeto analisado: `StoryFlame`

## Escopo

Objetivo desta rodada:

- identificar concorrentes relevantes para o StoryFlame
- mapear diferenças de produto
- avaliar onde o StoryFlame já se diferencia
- analisar implementação quando o concorrente é open source

Base de comparação do StoryFlame:

- app desktop offline-first em Java Swing
- persistência local `.storyflame` em ZIP/JSON
- capítulos, cenas, personagens, POV e tags narrativas
- preview renderizado por expansão de tags
- exportação de manuscrito
- análise emocional offline
- núcleo compartilhado com Android de apoio

Fonte da baseline:
- [README.md](/home/marconardes/IAS_Project/StoryFlame/README.md)

## Resumo executivo

Os concorrentes mais relevantes para o StoryFlame hoje são:

1. `Scrivener`
2. `yWriter`
3. `Manuskript`
4. `novelWriter`
5. `bibisco`

Leitura consolidada:

- `Scrivener` e `yWriter` representam a referência de mercado em fluxo de manuscrito estruturado por partes menores.
- `Manuskript`, `novelWriter` e `bibisco` são os benchmarks open source mais úteis.
- O StoryFlame já tem diferenciais claros em:
  - pipeline local com formato versionado próprio
  - tags narrativas com modo rascunho/render
  - análise emocional offline
  - stack simples e controlada no desktop principal
- O StoryFlame ainda fica atrás em:
  - maturidade de organização visual do manuscrito
  - profundidade de planejamento narrativo
  - riqueza de visão estrutural da obra
  - onboarding e fluidez de criação para personagens e tags

## Concorrente 1. Scrivener

### Tipo

- direto
- fechado

### Proposta principal

Ferramenta de escrita e organização de manuscritos com foco em `binder`, `corkboard`, `outliner`, metadados de projeto e pipeline forte de compilação/exportação.

### Diferenças em relação ao StoryFlame

- Scrivener é mais forte em organização visual do manuscrito do que em automação semântica.
- StoryFlame tem diferenciais que Scrivener não evidencia nesta pesquisa:
  - tags narrativas com expansão no texto
  - análise emocional offline
  - formato de projeto simples e explicitamente open source

### Onde Scrivener é melhor

- organização hierárquica madura por `binder`
- `corkboard` e `outliner` como visões centrais do trabalho
- ecossistema de metadados e notas de projeto mais amplo
- compile/export muito mais maduro
- amplitude de fluxo editorial

### Onde o StoryFlame pode se diferenciar

- foco em web novel e marcação narrativa explícita
- fluxo offline e open source mais transparente
- persistência versionada legível por engenharia
- recursos analíticos locais no próprio editor

### Implementação

- não analisada
- produto fechado

### Recomendação final

- `investigar`

### Fontes

- Scrivener manual oficial: https://www.literatureandlatte.com/docs/Scrivener_Manual-v1-Win.pdf

## Concorrente 2. yWriter

### Tipo

- direto
- fechado

### Proposta principal

Ferramenta orientada a `chapters + scenes`, com gestão de personagens, itens, locais, notas, metas de palavra e fluxo fortemente centrado em cena.

### Diferenças em relação ao StoryFlame

- yWriter enfatiza a unidade `cena` como elemento operacional principal.
- StoryFlame já oferece personagens, POV, tags e exportação, mas ainda com menos profundidade operacional em cena do que o benchmark sugere.

### Onde yWriter é melhor

- foco explícito em cena como unidade de trabalho
- criação guiada por wizard
- associação prática de personagens, itens e locais à cena
- metas e backups como parte visível do fluxo

### Onde o StoryFlame pode se diferenciar

- sistema de tags narrativas é mais flexível do que o modelo clássico de itens associados
- renderização por expansão de tags abre caminho para automação narrativa
- núcleo compartilhado e persistência aberta tornam o produto mais sustentável tecnicamente

### Implementação

- não analisada
- produto fechado

### Recomendação final

- `adaptar`

### Fontes

- yWriter quickstart oficial: https://www.spacejock.com/files/yWriter5Guide.pdf

## Concorrente 3. Manuskript

### Tipo

- direto
- open source

### Proposta principal

Ferramenta open source para escritores com `outliner`, `snowflake method`, personagens, plots, worldbuilding, storyline, escrita focada e exportação múltipla.

### Diferenças em relação ao StoryFlame

- Manuskript é mais forte em planejamento narrativo clássico do que em automação semântica.
- StoryFlame é mais enxuto e hoje mais focado no núcleo de manuscrito, tags narrativas e análise offline.

### Onde Manuskript é melhor

- planejamento narrativo mais completo
- suporte explícito a personagens, plots e worldbuilding
- storyline e modos de escrita bem definidos
- import/export de vários formatos

### Onde o StoryFlame pode se diferenciar

- stack Java alinhada ao projeto atual
- formato de archive mais direto e mais fácil de endurecer
- tags narrativas e análise emocional como diferenciais não centrais no Manuskript

### Implementação

- stack confirmada:
  - Python 3
  - PyQt5
- evidências:
  - `README.md` do repositório
  - `requirements.txt`
  - `bin/manuskript`
- arquitetura observada:
  - bootstrap em `bin/manuskript` chamando `manuskript.main.run()`
  - janela principal concentrada em `manuskript/mainWindow.py`
  - modelos específicos para `outline`, `character`, `plot` e `world` em `manuskript/models/*`
- leitura técnica:
  - a aplicação usa um `MainWindow` grande, com muitos menus, ações e integração centralizada
  - os dados visíveis da obra são representados por modelos especializados
  - o benchmark é bom para módulos de planejamento, mas menos atraente como referência de desacoplamento estrutural

### Lições técnicas aproveitáveis

- vale observar o recorte funcional de `outline`, `character`, `plot` e `world` como módulos de produto
- vale evitar repetir a concentração excessiva da janela principal

### Recomendação final

- `adaptar`

### Fontes

- site oficial: https://www.theologeek.ch/manuskript/
- repositório: https://github.com/olivierkes/manuskript
- README: https://github.com/olivierkes/manuskript/blob/develop/README.md
- requirements: https://github.com/olivierkes/manuskript/blob/develop/requirements.txt

## Concorrente 4. novelWriter

### Tipo

- direto
- open source

### Proposta principal

Editor de texto puro voltado a romances, com projeto fragmentado em muitos documentos menores, árvore de projeto, referências via metadados, outline e pipeline forte de build/export.

### Diferenças em relação ao StoryFlame

- novelWriter aposta em plain text, estrutura em árvore e build de manuscrito a partir de documentos pequenos.
- StoryFlame trabalha com estrutura mais guiada por capítulo/cena e diferenciais de tag/render/análise.

### Onde novelWriter é melhor

- robustez do formato baseado em texto simples
- separação clara entre `core`, `storage`, `tree`, `project xml` e `formats`
- visão de outline mais madura
- build/export muito forte
- referências e metadados integrados ao texto

### Onde o StoryFlame pode se diferenciar

- modelo de edição mais guiado para usuário menos técnico
- persistência ZIP/JSON com controle interno mais simples para o escopo atual
- tags narrativas explícitas e preview renderizado
- análise emocional offline e potencial de features assistidas locais

### Implementação

- stack confirmada:
  - Python
  - Qt6 com PyQt6
- evidências:
  - `README.md`
  - `pyproject.toml`
- arquitetura observada:
  - ponto de entrada fino em `novelWriter.py`
  - pacote principal com separação forte entre `core`, `formats`, `gui` e utilitários
  - módulo `core` dividido em `project.py`, `storage.py`, `tree.py`, `itemmodel.py`, `projectxml.py`, `document.py`
  - persistência baseada em projeto com arquivo `.nwx`, diretórios `meta` e `content` e controle de lock
  - exportação separada por formato em `formats/*`
- leitura técnica:
  - é o benchmark open source mais alinhado com uma arquitetura de núcleo bem separada
  - o recorte entre `project`, `storage` e `format` é particularmente bom como referência
  - a persistência e o build são tratados como partes centrais do domínio, não como detalhe da UI

### Lições técnicas aproveitáveis

- boa referência para evoluir o `core` sem empurrar regra para a UI
- boa referência para expandir exportação e visão de outline
- boa referência de separação entre formato de projeto, árvore lógica e compilação manuscrita

### Recomendação final

- `investigar`

### Fontes

- site oficial: https://novelwriter.io/
- features: https://novelwriter.io/features.html
- documentação: https://docs.novelwriter.io
- repositório: https://github.com/vkbo/novelWriter
- README: https://github.com/vkbo/novelWriter/blob/main/README.md
- pyproject: https://github.com/vkbo/novelWriter/blob/main/pyproject.toml

## Concorrente 5. bibisco

### Tipo

- direto
- open source

### Proposta principal

Ferramenta de escrita de romance com grande ênfase em personagens, arquitetura da história, strands narrativas, contexto, revisões, análise e exportação.

### Diferenças em relação ao StoryFlame

- bibisco é mais profundo em questionários de personagem e estrutura analítica de romance.
- StoryFlame é mais simples, mais leve e hoje mais controlado em arquitetura e escopo.

### Onde bibisco é melhor

- profundidade de caracterização
- arquitetura da história mais rica
- análise e dashboards mais visíveis
- tratamento mais detalhado de contexto, grupos, lugares, objetos e strands

### Onde o StoryFlame pode se diferenciar

- escopo mais enxuto e mais fácil de manter
- desktop principal em Java Swing, sem peso de Electron
- persistência de projeto mais direta
- tags narrativas e preview renderizado são diferenciais próprios

### Implementação

- stack confirmada:
  - Electron
  - AngularJS 1.x
  - LokiJS
- evidências:
  - `app/package.json`
  - `app/index.js`
  - `app/app.module.js`
- arquitetura observada:
  - processo principal Electron em `app/index.js`
  - SPA AngularJS em `app/app.module.js`
  - muitas features organizadas por componentes em `app/components/*`
  - persistência local apoiada por `lokijs`
  - exportação separada em componentes dedicados de export
- leitura técnica:
  - a cobertura funcional é grande, especialmente em personagens e análise
  - a arquitetura é extensa e orientada a muitos componentes por feature
  - como benchmark técnico, é útil pela cobertura funcional, mas menos alinhado ao stack e à simplicidade desejada do StoryFlame

### Lições técnicas aproveitáveis

- excelente referência funcional para aprofundar personagens e metadados de cena
- referência forte para taxonomia de contexto narrativo
- não é boa referência de stack para o StoryFlame atual

### Recomendação final

- `adaptar`

### Fontes

- site oficial: https://www.bibisco.com
- repositório: https://github.com/andreafeccomandi/bibisco
- README: https://github.com/andreafeccomandi/bibisco/blob/master/README.md

## Comparacao cruzada

### Onde o StoryFlame ja tem identidade propria

- tags narrativas como mecanismo central, nao apenas metadado lateral
- `modo rascunho` e `modo render`
- análise emocional offline local
- formato de projeto versionado em ZIP/JSON
- foco em produto offline, sem backend

### Onde o StoryFlame ainda perde de forma clara

- visualização estrutural do manuscrito
- maturidade do pipeline de publicação
- profundidade de planejamento narrativo
- riqueza de dados de personagem
- onboarding de recursos complexos

### Benchmarks mais úteis por tema

- `estrutura e build do projeto`: novelWriter
- `planejamento narrativo`: Manuskript
- `profundidade de personagem e arquitetura`: bibisco
- `maturidade de edição de manuscrito`: Scrivener
- `fluxo centrado em cena`: yWriter

## Recomendações praticas para o StoryFlame

### Prioridade alta

- investigar uma visão estrutural mais forte para capítulos e cenas
- reforçar o fluxo de build/export com presets mais claros
- aprofundar o painel de personagem sem inflar a UI principal

### Prioridade média

- estudar uma visão alternativa de outline inspirada em `novelWriter`
- estudar um recorte de planejamento narrativo inspirado em `Manuskript`
- avaliar um subconjunto enxuto de metadados de cena inspirado em `yWriter`

### Prioridade baixa

- estudar dashboards analíticos ao estilo `bibisco`
- reabrir Android como frente de produto

## Decisão consolidada

- `Scrivener`: investigar como benchmark de UX e estrutura
- `yWriter`: adaptar ideias de fluxo de cena
- `Manuskript`: adaptar ideias de planejamento
- `novelWriter`: investigar com profundidade como benchmark técnico
- `bibisco`: adaptar recortes funcionais, não a arquitetura

## Observações metodológicas

- produtos fechados tiveram apenas comparação funcional, não análise interna
- a análise de implementação open source foi feita com base em:
  - site oficial
  - README
  - arquivos de entrada
  - arquivos de build
  - estrutura de módulos e arquivos centrais
- quando houve inferência, ela foi mantida em nível arquitetural alto e sempre baseada em estrutura observável do repositório
