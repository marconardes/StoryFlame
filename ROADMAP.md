# Roadmap do Projeto StoryFlame

StoryFlame e o nome oficial do produto neste repositório. Este roadmap substitui o planejamento anterior e organiza as 12 semanas iniciais do MVP em sprints semanais, com foco em entregas incrementais reais para autores de web novel.

## 1. Visao Geral

- Duracao: 12 semanas
- Cadencia: 1 semana por sprint
- Meta: tornar o StoryFlame utilizavel por autores de web novel
- Plataformas alvo: Java Desktop + Android
- Direcao tecnica: offline first, projeto portavel em ZIP, expansao narrativa por tags e analise emocional local

## 2. Principios do MVP

- Offline first como requisito estrutural, nao como melhoria futura.
- ZIP como formato canonico do projeto, com JSON e artefatos internos versionados.
- Narrative Tag Engine como diferencial central do produto desde o inicio.
- Pipeline emocional priorizando o Modo B antes do Modo C.
- Cada sprint precisa deixar uma capacidade concreta utilizavel pelo autor.

## 3. Norte Tecnico

- Monorepo com modulos `core`, `desktop` e `android`.
- `core` concentra dominio, persistencia, parser de tags, expansao e pipeline de analise.
- `desktop` entrega a experiencia principal de escrita.
- `android` garante mobilidade, consulta, edicao leve e validacao da portabilidade do nucleo.
- Persistencia local baseada em JSON empacotado em ZIP versionado.
- Build padronizado com Gradle, com espaco para Maven apenas se houver necessidade especifica de distribuicao.

## 4. Backlog por Sprints Semanais

### Sprint 1 - Fundacao do projeto

Objetivo: criar um esqueleto tecnico solido.

Backlog:
- [x] Criar monorepo (`core`, `desktop`, `android`)
- [x] Definir estrutura de pacotes
- [x] Criar modelos base `Project`, `Chapter`, `Scene` e `Character`
- [x] Definir formato ZIP v1 em rascunho
- [x] Configurar build com Gradle

Entregavel:
- Projeto compila nas duas plataformas

### Sprint 2 - Persistencia local

Objetivo: salvar e carregar projetos sem dependencia externa.

Backlog:
- [x] Implementar storage local de projeto
- [x] Implementar serializacao JSON
- [x] Criar fluxo de criar projeto
- [x] Criar fluxo de abrir projeto
- [x] Criar fluxo de salvar projeto
- [x] Adicionar autosave basico
- [x] Testar com projeto grande

Entregavel:
- Projeto abre e salva sem perder dados

### Sprint 3 - Editor MVP

Objetivo: permitir escrita real de cenas.

Backlog:
- [x] Implementar editor de cena
- [x] Criar binding entre cena e editor
- [x] Adicionar undo/redo
- [x] Adicionar contador de palavras
- [x] Tratar performance de scroll
- [x] Testar com texto longo

Entregavel:
- Ja e possivel escrever capitulos

### Sprint 4 - Estrutura do livro

Objetivo: organizar o manuscrito de forma navegavel.

Backlog:
- [x] CRUD de capitulos
- [x] CRUD de cenas
- [x] Reordenar capitulos
- [x] Reordenar cenas
- [x] Criar navegacao rapida
- [x] Implementar busca textual simples

Entregavel:
- Livro navegavel dentro da aplicacao

### Sprint 5 - Personagens e consistencia narrativa

Objetivo: dar contexto narrativo ao manuscrito antes de introduzir tags.

Backlog:
- [x] Criar CRUD de personagens
- [x] Criar tela de personagens no desktop
- [x] Vincular POV da cena a personagem
- [x] Permitir busca e selecao de personagem
- [x] Validar referencias quebradas entre cena e personagem
- [x] Criar testes unitarios de integridade narrativa

Entregavel:
- O manuscrito passa a ter personagens editaveis e ligados as cenas

### Sprint 6 - Narrative Tag Engine (base)

Objetivo: reconhecer tags narrativas como `{lfp1}` dentro do texto.

Backlog:
- [x] Criar modelo `NarrativeTag`
- [x] Criar modelo `CharacterTagProfile`
- [x] Implementar detector por regex para `{tag}`
- [x] Implementar parser de tags
- [x] Validar existencia da tag
- [x] Criar testes unitarios

Entregavel:
- Sistema detecta tags no texto

Marco:
- Primeiro momento de diferenciacao clara do produto

### Sprint 7 - Biblioteca de tags e perfis

Objetivo: tornar o sistema de tags utilizavel com contexto de personagem.

Backlog:
- [ ] Criar `narrative_tags.json`
- [ ] Montar biblioteca inicial de tags
- [ ] Suportar prefixo por personagem
- [ ] Criar editor de perfil de personagem
- [ ] Permitir criar nova tag
- [ ] Implementar validador de inconsistencias

Entregavel:
- O escritor consegue manter biblioteca de tags e perfis narrativos por personagem

### Sprint 8 - Expansao de templates

Objetivo: transformar tags em texto renderizado.

Backlog:
- [ ] Implementar `TemplateExpansionEngine`
- [ ] Mapear tag para template
- [ ] Preservar pontuacao na expansao
- [ ] Separar modo rascunho e modo render
- [ ] Criar preview de expansao
- [ ] Testar multiplas tags no mesmo trecho

Entregavel:
- Tags como `{lfp1}` passam a virar texto real

Marco:
- O diferencial narrativo do StoryFlame passa a ser visivel no uso diario

### Sprint 9 - UX de produtividade

Objetivo: acelerar o ritmo de escrita com tags e navegacao.

Backlog:
- [ ] Autocomplete de tags
- [ ] Popup de sugestoes
- [ ] Favoritos
- [ ] Lista de ultimos usados
- [ ] Hover preview
- [ ] Toggle entre rascunho e leitura

Entregavel:
- Escrita com tags fica realmente rapida

Marco:
- O StoryFlame ganha um diferencial forte de produtividade

### Sprint 10 - Portabilidade e backups

Objetivo: garantir portabilidade confiavel do projeto.

Backlog:
- [ ] Exportar ZIP
- [ ] Importar ZIP
- [ ] Validar integridade do pacote
- [ ] Implementar migracao de versao
- [ ] Criar backups automaticos
- [ ] Executar teste de stress

Entregavel:
- Projeto portavel e seguro

### Sprint 11 - Exportacao publicavel

Objetivo: transformar o manuscrito em saida utilizavel fora do app.

Backlog:
- [ ] Criar pipeline de expansao antes do export
- [ ] Exportar TXT e MD
- [ ] Exportar PDF
- [ ] Estruturar EPUB base
- [ ] Criar templates de formatacao
- [ ] Testar com um livro real

Entregavel:
- Manuscrito sai em formato publicavel

### Sprint 12 - Pipeline emocional (infra + Modo B inicial)

Objetivo: fechar o MVP com a primeira camada util de analise offline.

Backlog:
- [ ] Implementar `Chunker`
- [ ] Implementar `EmotionAggregator`
- [ ] Implementar `EmotionCache`
- [ ] Criar estrutura `analysis/`
- [ ] Implementar `FastTextEmotionEngine`
- [ ] Integrar modelo PT-BR
- [ ] Classificar sentimento
- [ ] Classificar emocoes
- [ ] Persistir `emotion.json`
- [ ] Criar UI de relatorio

Entregavel:
- StoryFlame passa a oferecer analise emocional offline no Modo B inicial

Marco:
- Grande marco funcional do MVP

## 5. Resultado Esperado ao Final da Semana 12

- StoryFlame compila em desktop e Android
- Projetos funcionam offline com armazenamento local e portabilidade em ZIP
- O autor consegue estruturar livro, escrever cenas, navegar no manuscrito e gerenciar personagens
- O sistema de Narrative Tags ja suporta deteccao, biblioteca inicial, perfis e expansao
- A produtividade de escrita melhora com autocomplete, preview e alternancia de modos
- O manuscrito pode ser exportado tanto como projeto portavel quanto em formatos publicaveis
- A analise emocional offline funciona em um primeiro recorte util do Modo B
- O manuscrito pode ser exportado para formatos de publicacao

## 6. Pos-MVP

Proximos grandes blocos:

- Modo C com transformer
- Revisao inteligente
- Analise por personagem
- Packs de templates
- Otimizacoes Android

## 7. Criterios de Revisao do Roadmap

- Revisar ao fim de cada sprint com base no que ficou pronto de fato
- Nao mover feature complexa para frente sem validar impacto no offline first
- Manter a Narrative Tag Engine como prioridade ate consolidar o diferencial do produto
- So iniciar o Modo C depois de estabilizar persistencia, expansao e Modo B
