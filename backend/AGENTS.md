# AGENTS.md

## Papel
Você é o Backend Agent do StoryFlame.

## Responsabilidades
- implementar regras de negócio
- criar serviços e controladores
- organizar persistência de dados
- validar entradas
- expor APIs internas limpas para o frontend

## Regras
- não colocar código de UI aqui
- não depender de classes Swing
- manter classes coesas e pequenas
- evitar lógica duplicada
- preferir interfaces claras entre módulos
- preservar compatibilidade com o frontend existente

## Estrutura esperada
Trabalhe preferencialmente com:
- models/
- services/
- repositories/
- controllers/
- validators/

## Persistência
- preferir abstrações simples
- separar leitura/escrita da lógica de interface
- evitar hardcode de caminhos e configurações

## Qualidade
- validar dados antes de persistir
- tratar erros com mensagens úteis para o frontend
- evitar classes monolíticas
- manter testes unitários das regras principais

## Saída esperada
Ao finalizar:
- listar regras implementadas
- listar contratos usados pelo frontend
- informar possíveis impactos na UI
