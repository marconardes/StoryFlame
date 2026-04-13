# Revisao Tecnica 1

Data: 2026-03-29
Responsavel: Gerente de Projeto
Escopo: revisao tecnica do estado atual do codigo apos conclusao do roadmap principal

## Resumo

O projeto esta funcional e o roadmap principal foi implementado, mas ainda existem riscos tecnicos relevantes para manutencao e estabilidade. O principal ponto de atencao continua sendo a concentracao excessiva de responsabilidades na UI desktop. Android foi mantido corretamente como modulo de apoio, mas a entrega atual e mais uma prova de portabilidade do `core` do que um fluxo de produto real. O pipeline emocional evoluiu, mas ainda opera com heuristica simples e deve ser tratado como recurso MVP, nao como analise semantica robusta.

## Achados

### 1. Critico

- `desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:93`
  - A classe principal da UI continua grande demais e mistura montagem de tela, estado de navegacao, coordenacao de servicos, formatacao de feedback e orquestracao de eventos.
  - Evidencia: o arquivo tem `3384` linhas.
  - Impacto: manutencao lenta, regressao facil em fluxos Swing, revisao de threading mais dificil e baixa testabilidade estrutural.
  - Recomendacao: quebrar a classe por responsabilidade, pelo menos em `workspace/editor`, `characters`, `tags`, `publication`, `analysis` e `background operations`.

### 2. Alto

- `desktop/src/main/java/io/storyflame/desktop/StoryFlameDesktopApp.java:97-188`
  - O numero de campos Swing e de estado mutavel em memoria e muito alto em uma unica classe.
  - Impacto: alto acoplamento interno, maior chance de bugs por sincronizacao parcial de UI, listeners reentrantes e refresh indevido de componentes.
  - Contexto: o bug recente de `Attempt to mutate in notification` confirma esse risco.
  - Recomendacao: introduzir controladores menores ou paines dedicados com API explicita entre tela e estado.

### 3. Alto

- `android/src/main/java/io/storyflame/android/MainActivity.java:9-23`
  - O modulo Android esta coerente com a proposta de apoio, mas a entrega atual e apenas um preview de consulta sobre projeto de exemplo em memoria.
  - Impacto: o roadmap pode aparecer como concluido enquanto a experiencia Android real ainda nao valida abrir pacote, listar manuscrito real ou navegar em dados persistidos.
  - Recomendacao: manter o status funcional como "apoio" mesmo em comunicacao externa e registrar explicitamente que Android nao e uma interface editorial completa.

### 4. Medio

- `android/build.gradle.kts:38-52`
  - O modulo Android depende de exclusao manual de `META-INF/DEPENDENCIES` e de `junit-vintage-engine` para se acomodar ao estado atual da arvore de dependencias.
  - Impacto: build mais fragil e sujeito a regressao quando `core` ou dependencias de exportacao mudarem.
  - Recomendacao: monitorar esse ponto como divida tecnica de build; se surgirem novos conflitos de empacotamento, isolar melhor dependencias de exportacao do app Android.

### 5. Medio

- `core/src/main/java/io/storyflame/core/analysis/FastTextEmotionEngine.java:15-128`
  - A nomenclatura sugere engine mais sofisticada do que a implementacao real. Hoje ela opera por lexico PT-BR com contagem de ocorrencias.
  - Impacto: risco de interpretacao errada por manutencao futura ou por comunicacao de produto.
  - Recomendacao: deixar claro na documentacao e no backlog que se trata de heuristica lexical offline, adequada para MVP, mas limitada em ambiguidade, contexto e ironia.

### 6. Medio

- `git status --short`
  - A arvore esta muito suja, com grande volume de arquivos `M`, `AM` e `??`.
  - Impacto: revisao, release e rastreabilidade mais dificeis; aumenta a chance de misturar mudancas de documentacao, arquitetura, UX e features no mesmo pacote.
  - Recomendacao: consolidar em lotes menores antes de qualquer release ou PR, separando no minimo `core`, `desktop`, `android` e `docs`.

## Pontos Positivos

- O `core` permaneceu reutilizavel entre desktop e Android.
- Exportacao, analise emocional e persistencia estao cobertos por testes automatizados.
- Houve cuidado real com EDT e operacoes pesadas no desktop.
- Os formatters extraidos no modulo desktop melhoraram legibilidade e reduziram parte da pressao dentro da janela principal.

## Riscos Remanescentes

- Regressao de UI por centralizacao excessiva em `StoryFlameDesktopApp`.
- Fragilidade de build Android se novas dependencias transitivas entrarem no `core`.
- Sobrepromessa do recurso emocional se ele for apresentado como analise inteligente completa.
- Dificuldade de manutencao caso novos fluxos sejam adicionados sem mais decomposicao da UI.

## Prioridades Recomendadas

1. Reduzir a superficie de `StoryFlameDesktopApp` antes de adicionar novas features de desktop.
2. Organizar a worktree e separar mudancas em lotes coerentes.
3. Congelar a mensagem de produto do Android como modulo de apoio.
4. Documentar explicitamente as limitacoes da heuristica emocional no backlog pos-MVP.

## Conclusao

O projeto esta em um bom ponto funcional, mas ainda nao esta tecnicamente "folgado". A base aguenta fechamento de MVP, testes manuais e estabilizacao curta. O proximo ganho real nao vem de nova feature; vem de reduzir risco estrutural no desktop e melhorar rastreabilidade das mudancas.
