# StoryFlame ZIP Format v1 (Draft)

## Objetivo

Definir um formato de projeto portavel, offline e versionado para o StoryFlame.

## Estrutura proposta

```text
storyflame-project.zip
├── manifest.json
├── project.json
├── chapters/
│   └── <chapter-id>.json
├── characters/
│   └── <character-id>.json
├── analysis/
│   └── reserved-for-future.json
└── assets/
```

## Regras iniciais

- `manifest.json` identifica a versao do formato, origem do app e metadados de migracao.
- `project.json` concentra metadados globais do projeto.
- `chapters/` armazena a estrutura narrativa serializada por capitulo.
- `characters/` armazena personagens independentes da ordem do manuscrito.
- `analysis/` fica reservado desde a v1 para manter compatibilidade com o pipeline emocional.
- `assets/` fica reservado para anexos futuros sem quebrar o pacote.

## Exemplo de manifest

```json
{
  "format": "storyflame-zip",
  "version": 1,
  "appVersion": "0.1.0",
  "createdAt": "2026-03-02T00:00:00Z"
}
```

## Decisoes da Semana 1

- ZIP e o formato canonico do projeto.
- JSON e o formato inicial de serializacao interna.
- O schema permanece extensivel para migracoes futuras.
