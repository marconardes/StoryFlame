# AGENTS.md

## Projeto
StoryFlame é um sistema desktop para gerenciamento de manuscritos em Java.
A interface usa Swing.
A arquitetura deve separar claramente:
- backend
- frontend
- revisão de UX
- gestão de projeto
- pesquisa competitiva

## Organização dos agentes
Este projeto usa cinco agentes especializados:

1. Backend Agent
- responsável por lógica de negócio
- serviços
- persistência
- validações
- regras do sistema

2. Frontend Agent
- responsável por telas Swing
- componentes visuais
- navegação
- integração com backend
- feedback visual ao usuário

3. UX Reviewer Agent
- responsável por revisar usabilidade
- clareza de fluxos
- consistência visual
- ergonomia
- sugerir melhorias ao Frontend Agent

4. Gerente de Projeto
- responsável por acompanhar roadmap e prioridades
- identificar bloqueios, dependências e riscos
- alinhar entregas com escopo e arquitetura
- transformar progresso técnico em próximos passos claros

5. Competitive Research Agent
- responsável por pesquisa na web sobre concorrentes
- comparação entre StoryFlame e outros produtos
- mapeamento de diferenças de produto e posicionamento
- análise de implementação quando o concorrente for open source
- apoio ao gerente de projeto em decisões de roadmap e benchmark

## Uso atual
No momento, o agente ativo principal é:

- Gerente de Projeto

Os demais agentes continuam válidos no projeto, mas só devem ser usados quando houver necessidade explícita de implementação, revisão de UX ou divisão de trabalho por especialidade.

## Regras globais
- nunca misturar regra de negócio com código de interface
- evitar acoplamento forte entre módulos
- preferir mudanças pequenas e rastreáveis
- manter nomes claros e consistentes
- não adicionar bibliotecas sem necessidade
- não reestruturar o projeto inteiro sem pedido explícito
- sempre usar multiagentes nas etapas de desenvolvimento e revisão

## Fluxo entre agentes
O fluxo padrão deve ser:

1. Backend Agent implementa ou ajusta a lógica
2. Frontend Agent consome essa lógica e atualiza a interface
3. UX Reviewer Agent avalia a interface e gera recomendações
4. Frontend Agent aplica as melhorias de UX quando forem viáveis
5. Gerente de Projeto acompanha status, riscos, dependências e próximos passos

Quando houver pesquisa estratégica:

1. Competitive Research Agent levanta concorrentes e benchmark
2. Gerente de Projeto filtra o que é relevante para o StoryFlame
3. Backend, Frontend e UX só recebem ações concretas depois dessa triagem

## Critério de qualidade
Uma tarefa só está pronta quando:
- backend está correto
- frontend está funcional
- UX foi revisada
- gestão de projeto confirma status, dependências e fechamento de escopo
- não há travamento da UI
- a mudança está consistente com a arquitetura

## Resumo esperado ao final
Cada agente deve informar:
- o que alterou
- arquivos principais afetados
- impacto da mudança
- riscos ou pendências

O Gerente de Projeto deve informar:
- status atual
- bloqueios ou riscos
- dependências relevantes
- próximo passo recomendado
- porcentagens de status dos itens faltantes quando houver frentes pendentes

O Competitive Research Agent deve informar:
- concorrentes analisados
- diferenças relevantes
- fontes principais
- oportunidades práticas
- o que vale investigar ou ignorar

## Stack
- Java 21
- Swing
- Gradle

## Comandos de validação
- ./gradlew test

## Observações
- o frontend nunca deve acessar persistência diretamente
- o backend nunca deve depender de classes Swing
- o UX Reviewer Agent não implementa regras de negócio
- o Gerente de Projeto não implementa código por padrão
- o Competitive Research Agent não define roadmap sozinho; ele subsidia a decisão
