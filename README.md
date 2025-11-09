# StoryFlame

StoryFlame é uma aplicação Flutter multiplataforma voltada para planejamento e escrita de projetos literários. O roadmap oficial (`ROADMAP.md`) guia as releases; este README complementa com instruções práticas do Release 0 – Fundamentos.

## Requisitos

- Flutter 3.24+ / Dart 3.5+
- Melos 5+ (`dart pub global activate melos`)
- make (opcional) ou scripts equivalentes
- Git e acesso às plataformas alvo (desktop e mobile)

## Estrutura prevista

```
storyflame/
├─ packages/
│  ├─ app/        # Widgets/Fluxos Flutter, navegação, temas
│  ├─ domain/     # Casos de uso, entidades, validações
│  └─ data/       # Persistência (JSON/SQLite), serviços externos
├─ tools/
│  └─ melos.yaml  # Configuração dos packages/comandos
├─ analysis_options.yaml
└─ melos.yaml
```

- `packages/app`: ponto de entrada (`main.dart`), rotas Projetos → Capítulos → Editor e temas claro/escuro.
- `packages/domain`: modelos `Project`, `Chapter`, `Scene`, interfaces de repositório e regras de negócio.
- `packages/data`: implementações baseadas em JSON local, abstraindo acesso a arquivos.

## Configuração rápida (Release 0/1)

1. **Criar workspace (já concluído no repo)**  
   - Os packages `app`, `domain` e `data` estão em `packages/`.
   - Execute `dart run melos bootstrap` para sincronizar dependências.

2. **Padronizar análise**  
   - Adicione `analysis_options.yaml` compartilhado.
   - Configure `melos.yaml` com scripts `melos run analyze`, `melos run format`, `melos run test`.

3. **Fluxo principal**  
   - `packages/app` usa o `LocalProjectRepository` para persistir projetos em JSON no diretório de documentos do usuário (`storyflame_projects.json`).  
   - A UI oferece: listagem com busca, criação/remoção de projetos, toggle de tema, proteção por senha e estatísticas rápidas.  
   - Abra a tela de detalhes para reordenar capítulos via drag-and-drop, definir metas, exportar TXT/PDF e abrir o editor Markdown com autosave e preview.

4. **Executar verificações**  
   ```bash
   melos run format
   melos run analyze
   melos run test
   flutter build linux   # ou windows/macos conforme plataforma
   ```

5. **Documentar**  
   - Atualize o ROADMAP com o status da release.  
   - Registre resultados de build/teste na seção “Checklist de Pronto para Release”.

## Funcionalidades do Release 1

- **Gerenciamento de projetos**: CRUD completo, busca rápida e persistência local em JSON (`getApplicationDocumentsDirectory`).
- **Capítulos/Cenas**: criação, exclusão e reordenação via `ReorderableListView` com histórico de edição (timestamp por capítulo).
- **Editor Markdown**: resumo + conteúdo com toolbar (negrito, itálico, listas e títulos), preview opcional e autosave com debounce.
- **Exportações**: botões para gerar `.txt` e `.pdf` por projeto (arquivos são gravados na mesma pasta de documentos e informados via Snackbar).
- **Tema e preferências**: modo claro/escuro persistido com `SharedPreferences` e restaurado no boot.
- **Segurança básica**: senha por projeto com hash SHA-256, limite de 5 tentativas falhas e bloqueio temporário de 1 minuto.
- **Métricas**: contador de palavras em tempo real, cálculo diário (por `updatedAt`) e acompanhamento das metas diária/total via barras de progresso.

## Funcionalidades do Release 2

- **Fichas de Personagem**: CRUD completo com campos de história, traços, relacionamentos e notas; vínculo com capítulos via chips e visualização Matriz Personagem × Cena.
- **Glossário Interno**: termos com categorias/notas, associação a capítulos e inserção rápida no editor (botão “Inserir termo do glossário”).
- **Timeline narrativa**: eventos com data flexível, ordenação manual (drag & drop), tags e vínculo a capítulos; visualização em lista dedicada.
- **Banco de Locais/Itens**: elementos compartilhando modelo `WorldElement`, filtrados por tipo (locais × itens) e vinculados a capítulos.
- **Ligações explícitas**: chips de capítulos em personagens, glossário e worldbuilding para rastrear participações e destacar contexto diretamente na UI.

## Funcionalidades do Release 3

- **Editor Markdown avançado**: toolbar rica (código, tabelas, citações), preview em paralelo e atalho para inserir termos do glossário diretamente no texto.
- **Banco Criativo**: registro de ideias/prompts com status (ideia, rascunho, concluída), tags e filtros rápidos para desbloquear cenas.
- **Templates Narrativos**: galeria embutida (Três Atos, Jornada do Herói) + templates customizados com checklist de etapas aplicadas ao projeto.
- **Colaboração Local**: exportação/importação de projetos completos (`.storyflame`) a partir da tela inicial, facilitando revisão offline.
- **UX refinements**: painel multi-abas no projeto (capítulos, fichas, glossário, timeline, mundo, ideias, templates) e matriz Personagem × Cena.

## Funcionalidades do Release 4

- **Exportação e-book**: geração de `.epub` com sumário automático, metadados (ISBN, direitos autorais) e arquivos compatíveis com leitores populares.
- **Pacote multimídia**: exportação “caixa” (`.zip`) contendo PDF, JSON do projeto e texto plano para compartilhar com beta readers ou coautores.
- **Assistente KDP/Wattpad**: formulário guiado que coleta sinopse, categorias BISAC, palavras-chave e gera pacote JSON para submissão rápida.
- **Checklist & histórico**: aba dedicada com switches de progresso (beta, capa, ISBN, e-book, pacote KDP) e registros dos exports realizados.

## Funcionalidades do Release 5 (em desenvolvimento)

- **Painel de insights**: geração offline de métricas de estilo (ritmo, densidade de diálogos, variação emocional) com chips por capítulo e alertas categorizados.
- **Sugestões inteligentes**: prompts contextuais destacando capítulos curtos, termos repetidos, personagens ociosos e elementos de worldbuilding sem uso.
- **Modo revisão**: aba dedicada para registrar comentários por capítulo, marcar resolvidos e acompanhar pendências por release.
- **Exportação de insights**: botão dedicado gera um relatório `.txt` com métricas, alertas, prompts e achados das extensões instalado no diretório de documentos do usuário.
- **Binder/Corkboard visual**: visão kanban de capítulos/cenas com cartões (capa, sinopse, status) e drag-and-drop entre colunas, semelhante ao painel de cartões do Scrivener.
- **Layout Scrivener-like**: navegação alternativa com binder lateral hierárquico + painel de detalhes/edição rápida para aproximar a ergonomia do StoryFlame do Scrivener clássico.
- **API de extensões locais**: manifestos JSON adicionados à pasta `storyflame_extensions/` permitem criar analisadores personalizados que rodam dentro do dispositivo, enriquecendo o painel de Insights.
- **Execução local**: todo o pipeline (nativo e extensões) roda no dispositivo via heurísticas determinísticas, preservando os manuscritos no workspace.

### Mini SDK para extensões

1. Crie um arquivo `.json` na pasta `storyflame_extensions/` (mesma raiz do `storyflame_projects.json`) ou dentro de `assets/extensions/` antes do build.
2. Estrutura básica:

```json
{
  "id": "meu.detectorde.cliches",
  "name": "Detector de Cliqués",
  "description": "Marca palavras proibidas nos capítulos.",
  "version": "0.1.0",
  "author": "Comunidade",
  "type": "analyzer",
  "rules": [
    {
      "id": "keyword.choque",
      "kind": "keyword",
      "scope": "content",
      "pattern": "choque elétrico",
      "message": "Considere variar a metáfora para choques elétricos.",
      "threshold": 3,
      "severity": "warning"
    }
  ]
}
```

3. Os tipos suportados atualmente são:
   - `kind: keyword` — Conta ocorrências de palavras/frases em `scope: content|summary|description`.
   - `threshold` controla o número mínimo de ocorrências para gerar um alerta.
4. Ao abrir o painel de Insights, as extensões instaladas aparecem listadas e seus alertas são incorporados automaticamente aos Insights.

## Persistência e senhas

- O arquivo `storyflame_projects.json` fica no diretório retornado por `getApplicationDocumentsDirectory` (Linux: `~/.local/share/storyflame/`).
- Cada projeto contém `passwordHash` (SHA-256). Para remover a proteção basta salvar senha vazia na folha de configurações.
- As exportações são gravadas como `storyflame_<nome>_export.txt|pdf` no mesmo diretório.

## Scripts úteis

| Objetivo | Comando |
| --- | --- |
| Sincronizar packages | `dart run melos bootstrap` |
| Lint/análise | `dart run melos analyze` |
| Formatar código | `dart run melos format` |
| Testes unitários | `dart run melos test` |
| Executar app | `cd packages/app && flutter run` |
| Build desktop | `cd packages/app && flutter build windows` (ou `macos`/`linux`) |

## Integração Contínua

O workflow `.github/workflows/flutter_ci.yaml` executa `melos bootstrap`, formatação, análise e testes em cada push/pull request para `main/master`.

## Próximos passos

- Medir cobertura atual (objetivo ≥50%) e incluir testes de integração para os novos fluxos (fichas, timeline, banco criativo).
- Evoluir persistência para sincronizar metas/estatísticas multi-dispositivo.
- Estender o SDK de extensões para suportar conectores externos, tipos adicionais de regras (ex.: comparação de versões) e preparar o Release 7, focado em paridade com Scrivener/Notion/Obsidian (Research Hub, editor em blocos, bases relacionais, graph view e colaboração em tempo real).

Consulte `ROADMAP.md` para detalhes sobre os próximos releases e indicadores de sucesso.
