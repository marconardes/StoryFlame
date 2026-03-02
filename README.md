# StoryFlame

StoryFlame e um app offline-first para escrita de web novel, com foco em Java Desktop e nucleo compartilhado com Android. O projeto salva tudo localmente em arquivos `.storyflame`, sem dependencia de servicos externos.

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

## Estrutura

```text
StoryFlame/
├── core/      # Dominio, persistencia ZIP/JSON, tags e validacoes
├── desktop/   # Aplicacao principal em Java Swing
├── android/   # App Android usando o mesmo nucleo
├── docs/      # Notas de arquitetura e especificacoes
├── README.md
└── ROADMAP.md
```

## Como rodar

Requisitos:

- JDK 17
- Android SDK configurado, se for buildar o app Android

Comandos principais:

```bash
./gradlew test
./gradlew :desktop:run
./gradlew :desktop:compileJava
./gradlew :android:assembleDebug
./gradlew :android:installDebug
```

O desktop sobe com UTF-8 explicito via Gradle.

## Persistencia local

- Desktop: `~/.storyflame/projects`
- Extensao de projeto: `.storyflame`
- Formato: ZIP versionado com JSON interno

Arquivos relevantes do pacote atualmente incluem:

- `manifest.json`
- `project.json`
- `narrative_tags.json`
- `character_tag_profiles.json`
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

## Desktop

A app desktop hoje esta organizada assim:

- painel fixo de `Projeto` na esquerda
- abas para `Editor`, `Estrutura`, `Busca`, `Personagens` e `Tags`

Recursos relevantes:

- selecao de POV por personagem
- busca de personagens
- associacao simples entre tags e personagens
- preview de expansao no resumo

## Android

O modulo Android existe no monorepo e compila, mas a experiencia principal continua sendo a versao desktop nesta fase do MVP.

## Testes

Os testes atuais cobrem principalmente:

- roundtrip de persistencia do archive
- integridade narrativa entre cena e personagem
- deteccao e parse de tags
- validacao da biblioteca de tags
- expansao de templates

Execute:

```bash
./gradlew test
```

## Roadmap

O planejamento semanal do MVP esta em [ROADMAP.md](ROADMAP.md).

## Observacoes

- O projeto e pessoal e open source, com foco local/offline
- Nao ha dependencia de backend, cloud ou web para o fluxo principal
