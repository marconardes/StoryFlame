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

## Comandos

```bash
./gradlew test
./gradlew :desktop:run
./gradlew :android:assembleDebug
./gradlew buildWeek1
```

## Formato ZIP

O rascunho inicial do pacote portavel esta em [docs/zip-format-v1.md](docs/zip-format-v1.md).

## Roadmap

O planejamento semanal do MVP esta em [ROADMAP.md](ROADMAP.md).
