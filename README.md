# StoryFlame

StoryFlame e um app offline-first para escrita de web novel, com `core` em Java e migracao planejada da interface desktop para Electron. O projeto salva tudo localmente em arquivos `.storyflame`, sem dependencia de servicos externos, e a diretriz atual e entregar o produto final como uma distribuicao unificada por plataforma.

## Estado atual

O MVP ja cobre:

- criacao, abertura, salvamento e autosave de projetos
- editor de cenas com `undo/redo` e contador de palavras
- estrutura de livro com CRUD de capitulos e cenas
- busca textual no manuscrito
- CRUD de personagens e associacao de POV por cena
- detection de tags narrativas como `{lfp1}`
- biblioteca local de tags e perfis por personagem
- expansao de templates em `modo rascunho` e `modo render`
- preview renderizado da cena atual no desktop
- analise emocional offline com relatorio heuristico por chunks no desktop

## Estrutura

```text
StoryFlame/
├── core/      # Dominio, persistencia ZIP/JSON, tags e validacoes
├── desktop/   # Interface Swing legada em transicao
├── android/   # App Android usando o mesmo nucleo
├── docs/      # Notas de arquitetura e especificacoes
├── README.md
└── ROADMAP.md
```

## Como rodar

Requisitos:

- JDK 21
- Android SDK configurado, se for buildar o app Android

Comandos principais:

```bash
./gradlew test
./gradlew :desktop:run
./gradlew :desktop:compileJava
./gradlew :android:assembleDebug
./gradlew :android:installDebug
```

O desktop Swing atual sobe com UTF-8 explicito via Gradle.

## Persistencia local

- Desktop: `~/.storyflame/projects`
- Extensao de projeto: `.storyflame`
- Formato: ZIP versionado com JSON interno

Arquivos relevantes do pacote atualmente incluem:

- `manifest.json`
- `project.json`
- `narrative_tags.json`
- `character_tag_profiles.json`
- `analysis/emotion.json`
- `chapters/*.json`
- `characters/*.json`

## Biblioteca de tags

O projeto ja inclui uma biblioteca inicial em [narrative_tags.json](/home/marconardes/IAS_Project/StoryFlame/core/src/main/resources/narrative_tags.json), carregada pelo `core`.

Exemplo de uso no texto:

```text
Ela {lfp1}, hesitou e {emo1}.
```

No desktop:

- `Modo rascunho` preserva as tags no texto
- `Modo render` expande as tags para texto real no preview

## Desktop atual

A interface Swing atual esta organizada assim:

- painel fixo de `Projeto` na esquerda
- abas para `Editor`, `Estrutura`, `Busca`, `Personagens` e `Tags`

Recursos relevantes:

- selecao de POV por personagem
- busca de personagens
- associacao simples entre tags e personagens
- preview de expansao no resumo
- aba `Analise` para gerar relatorio emocional offline de apoio, com heuristica lexical

## Transicao de plataforma

A direcao oficial do projeto agora e:

- `Electron` como interface principal alvo
- equivalencia funcional e estrutural com o produto atual
- `core` em Java como centro da regra de negocio, validacoes e persistencia enquanto a migracao acontece
- entrega final unificada por plataforma, com `Electron` como produto desktop distribuivel
- migracao incremental, sem reescrever o dominio inteiro em TypeScript por padrao

O plano consolidado dessa transicao esta em [docs/ELECTRON_MIGRATION_PLAN.md](/home/marconardes/IAS_Project/StoryFlame/docs/ELECTRON_MIGRATION_PLAN.md).

Comandos atuais do shell Electron:

```bash
cd electron
npm install
npm start
npm run package:dir
./scripts/verify-e2.sh
```

Os comandos de pacote do Electron preparam primeiro o bridge Java local e depois geram a distribuicao do app. O verificador do `E2` checa sintaxe, bridge vendorizado e, quando `dist/linux-unpacked` ja existe, valida tambem o executavel empacotado e o bridge embutido.

O `E4.5` cobre o ajuste final de empacotamento Linux e o tratamento definitivo dos avisos GTK/GLib no pacote distribuido.

## Android

O modulo Android existe no monorepo e compila, mas continua sendo modulo de apoio e validacao tardia do nucleo compartilhado, nao como frente principal de produto nem como interface editorial completa neste momento.

## Testes

Os testes atuais cobrem principalmente:

- roundtrip de persistencia do archive
- integridade narrativa entre cena e personagem
- deteccao e parse de tags
- validacao da biblioteca de tags
- expansao de templates
- pipeline emocional offline

Execute:

```bash
./gradlew test
```

## Roadmap

O planejamento oficial do projeto esta em [ROADMAP.md](ROADMAP.md). Este e o unico roadmap ativo do repositorio.

## Observacoes

- O projeto e pessoal e open source, com foco local/offline
- Nao ha dependencia de backend, cloud ou web para o fluxo principal
