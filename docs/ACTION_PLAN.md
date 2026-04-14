# Plano de Acao do StoryFlame

## Objetivo

Manter um plano operacional curto e atualizado para consolidar o StoryFlame como projeto desktop offline-first multiplataforma, com `core` em `Java 21` e interface Swing, preservando equivalencia estrutural e funcional e preparando a entrega final como distribuicao desktop coesa.

## Estado atual

### Concluido
- nome oficial padronizado como `StoryFlame`
- stack padronizada em `Java 21`
- build Gradle ajustado para Java 21
- documentacao principal alinhada entre `README`, `AGENTS`, `ARCHITECTURE` e `ROADMAP`
- roadmap oficial reorganizado e definido como fonte unica
- operacoes pesadas principais do desktop movidas para execucao em background

### Em andamento
- estabilizacao estrutural do `desktop`
- reducao de acoplamento da `StoryFlameDesktopApp`
- consolidacao de criterios de pronto por sprint no uso real
- revisao de UX dos fluxos apos a mudanca de responsividade
- preparacao do `E4`, com refactor estrutural da UI Swing apos a paridade funcional minima

### Pendente
- refactor estrutural da UI Swing apos a paridade funcional minima
- mapeamento de paridade e consistencia da UI (`E5`)
- planejamento e execucao do `E6` para reduzir acoplamento entre UI e backend
- planejamento do `E8` para consolidar o desktop Java
- preservacao da familiaridade do layout Java na composicao final da UI
- cobertura de testes especifica da evolucao da interface
- definicao mais objetiva do escopo pratico do `android`
- refinamento de UX nos fluxos de exportacao, importacao e analise

## Eixo 1 - Alinhamento de base

### Status
- concluido

### O que foi aplicado
- `StoryFlame` mantido como nome oficial
- referencias divergentes removidas da documentacao principal
- `Java 21` adotado como versao alvo do build e da documentacao
- Gradle consolidado como caminho padrao de validacao

### Resultado esperado
- nenhum documento central deve contradizer nome, stack ou forma de build

## Eixo 2 - Correcao arquitetural

### Status
- parcialmente concluido

### O que foi aplicado
- a arquitetura agora explica explicitamente a relacao entre `backend/frontend/ux` e `core/desktop/android`
- o papel do `android` foi reduzido para validacao tardia do nucleo compartilhado
- as regras de separacao entre `core` e `desktop` ficaram mais objetivas

### O que ainda falta
- transformar a separacao arquitetural em menor concentracao real de responsabilidades na UI principal
- estabilizar contratos mais enxutos para consumo da UI Swing

## Eixo 3 - Estabilizacao tecnica do desktop

### Status
- parcialmente concluido

### O que foi aplicado
- abrir projeto
- salvar projeto
- importar projeto
- verificar arquivo
- exportar projeto
- exportar manuscrito
- gerar analise emocional

Essas operacoes passaram a rodar fora da EDT, com estado de espera e tratamento centralizado de falha.

### O que ainda falta
- revisar se todo feedback visual de operacao longa esta consistente
- reduzir mais o tamanho e a responsabilidade da `StoryFlameDesktopApp`
- criar cobertura automatizada mais direta para os fluxos desktop

## Eixo 4 - Reorganizacao do roadmap

### Status
- concluido

### O que foi aplicado
- o roadmap oficial passou a ser unico
- os sprints foram reordenados para priorizar desktop e estabilizacao tecnica
- Android foi reposicionado como trilha tardia
- exportacao e analise emocional foram separados em fases mais claras
- criterios de pronto passaram a aparecer por sprint

### Resultado esperado
- o planejamento deixa de empurrar pendencias estruturais para o fim

## Eixo 5 - Equilibrio entre backend, frontend e UX

### Status
- parcialmente concluido

### O que foi aplicado
- o roadmap agora exige backend, frontend e revisao de UX como parte do pronto
- a arquitetura reforca a divisao de responsabilidade entre dominio, interface e revisao de experiencia

### O que ainda falta
- refletir isso com mais evidenca nas entregas reais da UI
- registrar revisoes de UX com mais frequencia e rastreabilidade

## Proximas acoes recomendadas

1. Executar o `E3` pela consolidacao dos modulos editoriais restantes em Swing.
2. Criar testes focados nos contratos da interface e nos fluxos ja ativos.
3. Estabilizar DTOs e contratos de sessao para reduzir dependencia de objetos de dominio ricos na UI.
4. Planejar o `E4` como refactor da UI Swing, separado da consolidacao funcional.
   O refactor deve manter a organizacao visual principal proxima da experiencia Java atual, mas com melhor separacao interna.
5. Limitar o `android` a validacao de portabilidade ate a UI desktop estabilizar o fluxo principal.

## Politica de uso de modelo (operacao)

Objetivo:
- reduzir consumo de cota sem perder qualidade nas etapas criticas

Regra atual:
- usar `codex medium` em tarefas de arquitetura, refactor estrutural, integracao backend e depuracao de regressao
- usar `gpt-5.4-mini low` em tarefas mecanicas de UI, ajustes pequenos de texto/estilo e organizacao repetitiva

Gatilhos de troca:
- trocar para `codex medium` quando houver decisao tecnica com impacto de arquitetura, risco de regressao funcional ou mudanca cross-modulo
- trocar para `gpt-5.4-mini low` quando a tarefa for local, previsivel e sem alteracao de regra de negocio

Regra de aviso ao usuario:
- em cada nova etapa do plano, informar explicitamente se o modelo atual continua adequado
- quando houver gatilho de troca, avisar antes de continuar a implementacao

## Criterio para encerrar este plano

Este plano so pode ser considerado encerrado quando:

- a documentacao principal permanecer consistente
- a UI desktop nao bloquear a EDT nos fluxos criticos
- a janela principal estiver menos concentrada
- o roadmap estiver sendo usado como referencia real de execucao
- backend, frontend e UX estiverem refletidos no pronto das entregas
