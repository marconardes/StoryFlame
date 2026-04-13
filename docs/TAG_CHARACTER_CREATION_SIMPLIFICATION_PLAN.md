# Plano de Simplificacao de Criacao de Tags e Personagens

Data: 2026-03-29
Origem:
- UX: `Ramanujan`
- Refinamento e consolidacao: Gerente de Projeto `Pasteur`

## Status de execucao

- `concluido`
- validado com teste Swing
- sem modal obrigatoria na criacao de personagem e tag
- mantendo o modelo `lista + painel de detalhe`

## Objetivo

Reduzir a dificuldade de criacao de novas tags e novos personagens no StoryFlame, simplificando o fluxo de uso sem abrir nova regra de negocio nem reestruturar a aplicacao inteira.

## Diagnostico consolidado

As dificuldades mais provaveis do fluxo atual sao:

- criacao e edicao ainda aparecem misturadas no mesmo painel
- ha mais acoes do que o necessario competindo pela atencao
- alguns verbos de interface ainda sao genericos demais
- o usuario ainda precisa interpretar termos demais cedo demais no fluxo de tags
- o rodape ainda pode virar explicacao principal do que aconteceu, quando deveria ser apenas feedback secundario

## Decisao do gerente de projeto

### O que foi aceito integralmente

- tratar `criacao e edicao misturadas` como dor real
- reduzir a carga cognitiva gerada por muitos botoes concorrentes
- trocar verbos genericos por linguagem direta
- evitar modal como etapa principal
- evitar depender do rodape para explicar o fluxo

### O que foi aceito com ajuste

- manter o modelo atual de `lista + painel de detalhe`, sem criar wizard
- manter a lista como contexto de navegacao, mas nao como lugar onde o usuario descobre o que fazer
- usar `Editar personagem` e `Editar tag` como estado/titulo do painel, nao como botao principal
- manter `Adicionar ao personagem` e `Remover tag` como acoes secundarias contextuais

### O que foi rejeitado por agora

- transformar o fluxo em assistente passo a passo
- reorganizar toda a tela de uma vez
- misturar a melhoria de UX com novas regras de negocio
- obrigar o usuario a entender `template`, `perfil` e `favoritos` antes de criar a primeira tag

## Fluxo simplificado proposto

### Personagem

Fluxo alvo:

1. Clicar em `Novo personagem`
2. Abrir um rascunho limpo no painel de personagem
3. Foco automatico em `Nome do personagem`
4. Preencher `Nome do personagem`
5. Preencher `Descricao do personagem`
6. Concluir com um unico CTA primario: `Salvar personagem`
7. Manter o personagem criado selecionado na lista

Diretriz:
- o usuario deve entender claramente quando esta criando um personagem novo e quando esta editando um personagem existente

### Tag

Fluxo alvo:

1. Clicar em `Nova tag`
2. Abrir um rascunho limpo no painel de tag
3. Foco automatico em `Rotulo da tag`
4. Preencher `Rotulo da tag`
5. Preencher `Texto renderizado`
6. Concluir com um unico CTA primario: `Salvar tag`
7. Manter a tag criada selecionada e pronta para reutilizacao

Diretriz:
- o usuario nao deve precisar entender termos avancados antes de conseguir criar a primeira tag

## Microcopy recomendada

### Personagem

- `Novo personagem`
- `Nome do personagem`
- `Descricao do personagem`
- `Salvar personagem`
- `Personagem criado`
- `Editando personagem`

### Tag

- `Nova tag`
- `Novo rascunho de tag`
- `Rotulo da tag`
- `Texto renderizado`
- `Salvar tag`
- `Tag criada`
- `Editando tag`

### Acoes contextuais

- `Adicionar ao personagem`
- `Remover tag`

## Plano de execucao

### Etapa 1. Ajuste de linguagem

Objetivo:
- trocar rotulos genericos por linguagem direta e consistente

Escopo:
- botoes principais
- titulos de painel
- mensagens curtas de criacao e conclusao

Resultado esperado:
- o usuario entende com uma leitura se esta criando, editando ou concluindo

### Etapa 2. Separacao visual entre criacao e edicao

Objetivo:
- deixar o estado do painel evidente sem criar nova arquitetura de fluxo

Escopo:
- reforcar visualmente quando o painel esta em `criacao`
- reforcar visualmente quando o painel esta em `edicao`
- manter a lista apenas como contexto, nao como instrucao principal

Resultado esperado:
- o usuario nao precisa deduzir pelo rodape ou pela selecao da lista o que esta fazendo

### Etapa 3. CTA principal unico por painel

Objetivo:
- reduzir ambiguidade de decisao

Escopo:
- `Salvar personagem` como CTA principal do painel de personagem
- `Salvar tag` como CTA principal do painel de tag
- manter acoes secundarias com menos destaque visual

Resultado esperado:
- a proxima acao principal fica obvia

### Etapa 4. Ajuda local curta

Objetivo:
- apoiar o fluxo sem depender do rodape

Escopo:
- uma linha curta de ajuda fixa no painel
- ajuda local explicando o minimo necessario para concluir o cadastro

Resultado esperado:
- o usuario recebe orientacao no lugar certo, sem poluir a interface

### Etapa 5. Validacao por teste Swing

Objetivo:
- proteger a melhoria de UX contra regressao

Escopo:
- um teste Swing simples para personagem
- um teste Swing simples para tag
- sem modais, sem chooser, sem automacao fragil

Resultado esperado:
- a simplificacao continua funcionando depois de futuras mudancas

## Criterio de pronto

Esta frente pode ser considerada pronta quando:

- o usuario entende com clareza o que esta criando ou editando
- cada painel tem um unico CTA principal visivel
- tags e personagens podem ser criados sem exigir interpretacao de conceitos avancados antes da hora
- o rodape deixa de ser o componente central de orientacao
- nao ha modal obrigatoria como etapa principal
- a melhoria fica protegida por teste Swing estavel
- a UI continua responsiva e sem impacto na EDT

## Restricoes

- nao criar wizard
- nao abrir nova regra de negocio
- nao reestruturar a tela inteira no mesmo passo
- nao misturar essa frente com novas features
- nao depender do rodape para guiar o fluxo principal
