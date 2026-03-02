# StoryFlame

StoryFlame agora esta em migracao para uma base Java com foco em desktop e Android, arquitetura offline first e projeto portavel em ZIP.

## Estrutura atual

```text
StoryFlame/
├── core/      # Modelos de dominio e contratos centrais
├── desktop/   # Aplicacao desktop em Java
├── android/   # Aplicacao Android nativa
└── docs/      # Especificacoes e notas de arquitetura
```

## Semana 1 entregue

- Monorepo Gradle com modulos `core`, `desktop` e `android`
- Modelos base `Project`, `Chapter`, `Scene` e `Character`
- Rascunho do formato `StoryFlame ZIP v1`
- Entry points minimos para desktop e Android

## Semana 2 entregue

- Persistencia local em arquivos `.storyflame`
- Serializacao JSON dentro de ZIP versionado
- Fluxos de criar, abrir e salvar projeto
- Autosave basico no desktop
- Teste de roundtrip e teste com projeto grande no `core`

## Comandos

```bash
./gradlew test
./gradlew :desktop:run
./gradlew :android:assembleDebug
./gradlew :android:installDebug
./gradlew buildWeek1
```

## Formato ZIP

O rascunho inicial do pacote portavel esta em [docs/zip-format-v1.md](docs/zip-format-v1.md).

## Persistencia local

- Desktop: projetos sao salvos em `~/.storyflame/projects`
- Android: projetos sao salvos no diretorio interno do app em `files/projects`
- Extensao atual: `.storyflame`

## Roadmap

O planejamento semanal do MVP esta em [ROADMAP.md](ROADMAP.md).
