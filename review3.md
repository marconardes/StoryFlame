# Revisao Tecnica 3

Data: 2026-03-29
Modelo de revisao: 4 agentes
- Gerente de Projeto: Pasteur
- Backend Agent: contratos, persistencia e corrupcao
- Frontend Agent: Swing, testabilidade e manutencao
- UX Reviewer Agent: clareza de fluxo, consistencia e escopo

## Resumo executivo

O `stabilization_plan.md` foi concluido. O proximo ciclo nao deve abrir novo escopo de produto. O foco correto agora e consolidacao de pos-MVP com tres objetivos:

- endurecer as bordas restantes do backend
- ampliar a rede de seguranca do desktop nas areas ainda mais frageis
- evitar retrabalho em UX e contratos que ja foram fechados no plano

O `Pasteur`, atuando como gerente de projeto nesta rodada, revisou criticamente o `review3` original e corrigiu um ponto central: parte do texto anterior tratava itens ja concluídos em `B1`, `C2`, `E1` e `E2` como se ainda fossem frentes abertas. A versao abaixo ja incorpora essa correção e inclui as explicações pedidas aos outros agentes.

## Propostas dos agentes e decisao do gerente

### Backend Agent

#### 1. Ampliar a matriz de corrupcao do ZIP em `ProjectArchiveStore.open()`
- Origem: Backend Agent
- Impacto: alto
- Risco: baixo
- Dependencia: nenhuma nova
- Decisao do gerente: ACEITO
- Motivo:
  - continua sendo o melhor endurecimento tecnico restante no `core`
  - fecha bordas reais de pacote malformado
  - pode ser tratado junto com importacao e migracao, evitando duas frentes separadas

#### 2. Fechar contrato direto dos validadores expostos ao desktop
- Origem: Backend Agent
- Impacto: medio-alto
- Risco: baixo
- Dependencia: nenhuma nova
- Decisao do gerente: REJEITADO COMO NOVO TRABALHO
- Motivo:
  - isso ja foi absorvido em `B1`
  - se algo ainda couber aqui, e reforco de teste, nao nova frente funcional ou contratual
  - reabrir esse item como backlog novo confundiria o estado real do projeto

#### 3. Adicionar stress test mais agressivo para autosave
- Origem: Backend Agent
- Impacto: medio
- Risco: medio
- Dependencia: desenho de teste estavel
- Decisao do gerente: REJEITADO POR ENQUANTO
- Motivo:
  - o objetivo e valido
  - mas nao vale introduzir teste flakey dependente de timing agressivo
  - so volta se surgir um seam deterministico no autosave

#### 4. Garantir preservacao de conteudo nas rotas de importacao/migracao
- Origem: Backend Agent
- Impacto: alto
- Risco: baixo-medio
- Dependencia: ampliar testes do archive
- Decisao do gerente: ACEITO
- Motivo:
  - cobre uma rota sensivel e pouco visivel para o usuario
  - conversa diretamente com o risco de perda silenciosa
  - deve caminhar junto com o item 1 como uma unica frente de hardening do archive

### Frontend Agent

#### 5. Refinar mais o fluxo de personagem e tag para separar criacao, edicao e conclusao
- Origem: Frontend Agent
- Impacto: medio
- Risco: baixo
- Dependencia: nenhuma estrutural
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - isso ja foi tratado no `E1`
  - antes de reabrir UX nessa area, vale validar com testes e uso real se ainda existe problema concreto
  - o retorno agora e menor do que backend e cobertura

#### 6. Criar regressao Swing para personagem/tag
- Origem: Frontend Agent
- Impacto: alto
- Risco: medio
- Dependencia: nomes estaveis e hooks pequenos de teste
- Decisao do gerente: ACEITO
- Motivo:
  - hoje a maior area ainda pouco protegida da UI e exatamente personagem/tag
  - protege o que acabou de ser refinado em `E1`
  - tem ganho claro sem abrir nova feature
  - segundo explicacao do Frontend Agent, o menor escopo viavel deve evitar modais e focar so nos ciclos mais comuns, para nao virar teste fragil

#### 7. Normalizar ainda mais os estados de feedback em personagem/tag
- Origem: Frontend Agent
- Impacto: medio
- Risco: baixo
- Dependencia: nenhuma nova
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - `C2` e `E1` ja cobriram essa direcao
  - so vale reabrir se a regressao Swing ou o uso manual mostrar lacuna real

#### 8. Limpeza pequena de manutencao em helpers e coordinators
- Origem: Frontend Agent
- Impacto: medio
- Risco: baixo-medio
- Dependencia: estabilizar primeiro personagem/tag
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - retorno baixo para agora
  - risco de churn em codigo ja estabilizado
  - o Frontend Agent explicitou que teste e cobertura protegem mais o usuario hoje do que nova limpeza estrutural

### UX Reviewer Agent

#### 9. Fechar fluxo de personagem/tag com rascunho explicito, validacao inline e CTA final unico
- Origem: UX Reviewer Agent
- Impacto: medio
- Risco: baixo
- Dependencia: desktop
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - ja foi tratado em `E1`
  - o UX Reviewer Agent esclareceu que isso nao era proposta de retrabalho imediato, mas criterio de observacao
  - so volta como frente se houver repeticao observavel de hesitacao, clique errado ou dependencia excessiva do `statusLabel`

#### 10. Unificar linguagem de status em toda a UI
- Origem: UX Reviewer Agent
- Impacto: medio
- Risco: baixo
- Dependencia: desktop
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - isso ja foi entregue em `C2`
  - manter observacao apenas como criterio de regressao futura
  - nao vira backlog novo sem divergencia concreta entre UI, documentacao e status

#### 11. Explicitar escopo do produto no desktop e no Android
- Origem: UX Reviewer Agent
- Impacto: medio
- Risco: baixo
- Dependencia: textos e documentacao
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - isso ja foi concluido em `E2`
  - nao precisa virar nova frente
  - o UX Reviewer Agent confirmou que isso so deve ser reaberto com evidencia repetida de expectativa errada do usuario

#### 12. Reduzir redundancia entre menu, toolbar e acoes principais
- Origem: UX Reviewer Agent
- Impacto: baixo-medio
- Risco: medio
- Dependencia: observacao de uso real
- Decisao do gerente: REJEITADO NESTE MOMENTO
- Motivo:
  - ha risco de cortar atalhos uteis sem dado real de uso
  - nao e prioridade frente a persistencia e testes

## Proximo ciclo aprovado

### Prioridade 1
- endurecer o archive no `core`
- cobrir:
  - corrupcao estrutural restante em `ProjectArchiveStore.open()`
  - preservacao de conteudo nas rotas de importacao e migracao

### Prioridade 2
- reforcar testes dos contratos ja entregues em `B1`
- cobrir:
  - `validateForSave(...)`
  - `validateForArchiveExport(...)`
  - `PublicationExportService.validate(...)`

### Prioridade 3
- criar regressao Swing para personagem e tag
- cobrir:
  - editar personagem existente
  - editar tag existente
  - refletir mudanca em lista e status
- restricao:
  - evitar `JOptionPane`, `JFileChooser`, exclusao e fluxos modais nesta primeira passada

### Prioridade 4
- reavaliar autosave agressivo somente se houver seam deterministico
- status:
  - fora do ciclo por enquanto

## O que fica explicitamente fora do proximo ciclo

- novas features de produto
- expansao do Android alem do papel de apoio
- nova rodada grande de refatoracao da `StoryFlameDesktopApp`
- nova rodada de UX em personagem/tag sem evidencia de problema remanescente
- evolucao da analise emocional alem do modo heuristico atual
- limpeza estrutural cosmetica em helpers sem ganho direto de seguranca ou cobertura
- qualquer reabertura de `B1`, `C2`, `E1` ou `E2` como se ainda fossem frentes abertas

## Direcao do gerente de projeto

O proximo passo correto nao e abrir backlog novo de produto. E consolidar garantias no que ja foi entregue. A ordem aprovada por `Pasteur` e:

1. endurecer persistencia e migracao no `core`
2. reforcar por teste os contratos ja formalizados em `B1`
3. ampliar a cobertura Swing nas areas de personagem e tag com escopo minimo e estavel
4. so depois reavaliar se existe evidencia real para reabrir UX residual

## Conclusao

O projeto saiu da fase de estabilizacao ampla e entrou em consolidacao seletiva. O que entra agora precisa defender o MVP ja entregue, nao expandi-lo. O gerente de projeto aprovou apenas propostas com retorno direto em integridade, previsibilidade e seguranca de regressao.
