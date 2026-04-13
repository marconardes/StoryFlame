# Competitive Research Report 2

Data: 2026-03-29
Foco: `Scrivener` como benchmark de UX e estrutura
Agente: `Competitive Research Agent`

## Objetivo

Investigar o `Scrivener` especificamente como referência para:

- UX de escrita não linear
- estrutura de projeto/manuscrito
- organização visual do material
- decisões que podem inspirar o StoryFlame sem sair do escopo atual

## Conclusão executiva

O `Scrivener` não é o melhor benchmark para stack nem para implementação interna, porque é fechado. Mas ele é o benchmark mais forte desta rodada para:

- modelo mental de manuscrito não linear
- organização de projeto em múltiplos documentos
- alternância entre escrita detalhada e visão estrutural
- metadados integrados ao fluxo de escrita
- compile/export como etapa central, não lateral

Para o StoryFlame, a principal lição não é “copiar a interface do Scrivener”. A lição é:

- o núcleo da UX deve permitir alternar sem atrito entre `escrever`, `ver estrutura`, `reorganizar`, `anotar contexto` e `publicar`

Hoje o StoryFlame já cobre parte disso, mas ainda com menos maturidade visual e com menos profundidade estrutural.

## O que o Scrivener está fazendo certo

### 1. Modelo mental claro de escrita não linear

O manual do Scrivener define explicitamente que ele não impõe capítulos, cenas ou um método de escrita. Em vez disso, ele oferece um ambiente para organizar muitos fragmentos e reestruturá-los com fluidez.

O ponto forte aqui é conceitual:

- o projeto é tratado como coleção viva de partes pequenas
- o usuário pode trabalhar fragmentado sem perder a visão do todo
- estrutura e texto não são fluxos separados

Para o StoryFlame, isso reforça que:

- `capítulo/cena` não pode ser apenas CRUD
- a estrutura precisa funcionar como ferramenta ativa de escrita

### 2. Tríade central de navegação: Binder + Editor + Inspector

O Scrivener gira em torno de três elementos principais:

- `Binder`: árvore do projeto
- `Editor`: escrita/leitura
- `Inspector`: metadados, notas, referências, snapshots e keywords

Essa divisão é forte porque:

- navegação fica concentrada em uma superfície previsível
- escrita continua no centro
- contexto e metadados ficam numa lateral estável, sem competir com o editor

Para o StoryFlame, a implicação é direta:

- o painel principal já está razoavelmente alinhado com essa ideia
- mas o uso de contexto ainda pode ficar mais estável e mais “sempre disponível”
- personagens, tags, integridade e análise ainda aparecem mais como abas paralelas do que como apoio contínuo à escrita

### 3. Três visões complementares da mesma estrutura

O Scrivener trata como primeira classe:

- `Document/Scrivenings`
- `Corkboard`
- `Outline`

O ganho de UX aqui é alto:

- o mesmo conteúdo pode ser lido como texto contínuo
- ou como cartões/sinopses
- ou como estrutura com colunas e metadados

Isso reduz troca de contexto mental. O usuário não “sai” do manuscrito para organizar; ele continua no mesmo manuscrito em outra visão.

Para o StoryFlame, essa é a maior lacuna atual.

Hoje o projeto tem:

- editor
- estrutura
- busca
- personagens
- tags

Mas ainda não tem uma visão estrutural alternativa do próprio manuscrito com força equivalente a `corkboard` ou `outliner`.

### 4. Synopsis como unidade leve de planejamento

O Scrivener liga cada documento a uma `synopsis`, que pode ser vista isoladamente ou em conjunto no corkboard/outliner.

Isso é importante porque:

- a synopsis é curta, barata de preencher e útil
- permite reorganizar sem abrir o texto inteiro
- cria uma camada intermediária entre “só título” e “texto completo”

Para o StoryFlame, isso aponta uma oportunidade clara:

- capítulos e cenas hoje têm título e conteúdo
- falta uma camada curta e explícita de `sinopse de cena`
- isso melhoraria muito:
  - visão estrutural
  - busca de intenção narrativa
  - reorganização
  - futura exportação de outline

### 5. Inspector como metadado integrado, não enterrado

O manual mostra o `Inspector` como local para:

- synopsis
- notes
- references
- keywords
- custom metadata
- snapshots

O mérito aqui não é apenas ter metadado. É ter metadado no lugar certo:

- ao lado do documento atual
- associado ao item focado
- sem esconder a escrita

Para o StoryFlame, isso é uma direção melhor do que continuar adicionando abas independentes para tudo.

Hoje personagens, tags e análise são úteis, mas ainda exigem troca de aba e mudança de contexto maior do que o ideal.

## Diferenças importantes em relação ao StoryFlame

### Onde o StoryFlame já está melhor posicionado

- tags narrativas são um diferencial mais explícito do que o sistema padrão do Scrivener
- preview `rascunho/render` é uma ideia própria forte
- análise emocional offline cria uma camada analítica inexistente no benchmark pesquisado
- persistência do projeto é mais transparente para engenharia

### Onde o StoryFlame está atrás

- visão estrutural do manuscrito
- planejamento leve por sinopse/cartão
- metadados continuamente acessíveis ao lado do editor
- maturidade do pipeline de publicação
- sensação de sistema integrado para “crescer o livro”

## O que vale adaptar no StoryFlame

### 1. Visão estrutural alternativa do manuscrito

Aceitação recomendada: `alta`

O StoryFlame deveria ganhar uma visão estrutural do manuscrito além da lista tradicional.

Não precisa copiar um corkboard literal no primeiro passo. O ganho real já viria com:

- uma visão de outline de capítulos e cenas
- colunas resumidas por cena
- ordenação clara
- leitura rápida sem abrir o conteúdo completo

### 2. Campo de sinopse por cena

Aceitação recomendada: `alta`

Esse é o melhor recorte prático inspirado no Scrivener.

Ganhos:

- melhora estrutura
- melhora busca
- melhora revisão
- melhora exportação de outline
- prepara terreno para visão de cartões sem exigir refatoração agressiva

### 3. Contexto lateral estável no editor

Aceitação recomendada: `média`

Em vez de jogar tudo em abas separadas, vale estudar um painel lateral de apoio à cena atual com:

- personagem POV
- personagens ligados
- tags detectadas
- estado de integridade
- talvez sinopse/notas da cena

Isso aproxima o fluxo do padrão `editor + contexto` do Scrivener.

### 4. Publicação tratada como fluxo central

Aceitação recomendada: `média`

No Scrivener, `Compile` é peça central. No StoryFlame, exportação já existe, mas ainda é mais operacional do que editorial.

O benchmark sugere:

- presets claros por objetivo de saída
- distinção melhor entre exportar projeto e publicar manuscrito
- mais previsibilidade sobre o que entra na saída final

### 5. Metadado leve e útil, não inflado

Aceitação recomendada: `média`

O StoryFlame não deve copiar o volume de metadados do Scrivener. Mas deve aprender o princípio:

- todo metadado precisa melhorar uma decisão real de escrita ou revisão

O recorte mínimo com melhor custo-benefício hoje parece ser:

- sinopse de cena
- nota curta de cena
- inclusão/exclusão clara em publicação

## O que não vale copiar agora

### 1. Complexidade total do Scrivener

Recomendação: `não`

O Scrivener é maduro, denso e cheio de superfícies. Copiar isso agora aumentaria o peso da UI e o custo de manutenção.

### 2. Multiplicação de painéis e modos

Recomendação: `não`

O StoryFlame ainda está consolidando UX. Adicionar muitos modos antes de fechar a visão estrutural mínima tende a piorar a clareza.

### 3. Metadado em excesso

Recomendação: `não`

O risco é recriar um sistema pesado de formulário e perder o diferencial de simplicidade.

## Proposta objetiva para o StoryFlame após este benchmark

Ordem sugerida:

1. adicionar `sinopse de cena`
2. criar `visao de outline` para capítulos e cenas
3. aproximar contexto da cena atual do editor principal
4. refinar publicação com presets mais explícitos

## Decisão do agente

- `Scrivener` deve continuar como benchmark principal de UX e estrutura
- o melhor aprendizado imediato não é visual; é de `modelo mental`
- a melhor adaptação prática e de baixo risco é:
  - `sinopse de cena`
  - `outline de manuscrito`

## Fontes

- manual oficial do Scrivener: https://www.literatureandlatte.com/docs/Scrivener_Manual-v1-Win.pdf
- manual oficial do Scrivener 3 para Windows: https://www.literatureandlatte.com/docs/Scrivener_Manual-Win.pdf

## Trechos usados como base

- filosofia do produto e escrita não linear
- binder, corkboard, outliner, inspector
- compile/export como parte central do fluxo
- formato de projeto e bundle mencionados no manual

## Observação

Como o Scrivener é fechado, esta rodada é um benchmark de `produto e UX`, não de implementação interna.
