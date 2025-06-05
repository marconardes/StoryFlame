# Roadmap do Projeto StoryFlame

**Nota Importante:** Este roadmap descreve as funcionalidades planejadas para o StoryFlame. As funcionalidades do "Núcleo Essencial e Gerenciamento" (como gerenciamento de projetos, capítulos e resumos) já possuem uma implementação base funcional. Outras funcionalidades listadas abaixo estão em diversos estágios de planejamento ou não implementadas.

## Legenda de Status
- (V) Funcionalidade Implementada
- (X) Funcionalidade Não Implementada
- (P) Funcionalidade Parcial
- (🟡) Funcionalidade Planejada

## Funcionalidades Planejadas (Visão Geral)

A seguir estão as funcionalidades planejadas para o StoryFlame, atualmente em estágio inicial de desenvolvimento (código placeholder):

### Núcleo Essencial e Gerenciamento

    (V) Gerenciamento de Projetos: (Prioridade: 10/10)
        (V) Criação, abertura e listagem de múltiplos projetos de escrita. (Prioridade: 10/10)
        (V) Persistência local dos dados do projeto em formato JSON. (Prioridade: 10/10)
    (V) Organização por Capítulos/Cenas: (Prioridade: 10/10)
        (V) Estruturação de projetos em capítulos ou cenas ordenáveis. (Prioridade: 10/10)
        (V) CRUD completo para capítulos (adicionar, editar título, excluir, reordenar). (Prioridade: 10/10)
    (V) Resumo por Capítulo/Cena: (Prioridade: 9/10)
        (V) Adição e edição de resumos textuais para cada capítulo/cena. (Prioridade: 9/10)
    (X) Editor de Simples: (Prioridade: 10/10)
        (V) Integração de editor de Markdown simples (JTextArea). (Prioridade: 10/10)
        (V) Funcionalidades de formatação básica (negrito, itálico, sublinhado HTML <u>, cabeçalhos H1-H3) via toolbar. (Prioridade: 9/10)
        (V) Salvamento automático com debounce do conteúdo Markdown do capítulo ativo. (Prioridade: 9/10)
    (X) Acompanhamento de Progresso: (Prioridade: 9/10)
        (V) Contador de palavras em tempo real no editor para o capítulo atual. (Prioridade: 9/10)
        (P) Estatísticas diárias da contagem total de palavras do projeto (coleta de dados implementada). (Prioridade: 9/10)
        (V) Definição de metas de escrita diárias e totais por projeto (armazenamento e UI para definir implementados). (Prioridade: 9/10)
        (X) Visualização do progresso em relação às metas. (Prioridade: 9/10)
    (X) Modo Escuro: (Prioridade: 9/10)
        (X) Alternância entre tema claro e escuro com persistência da preferência do usuário. (Prioridade: 9/10)
        (X) Adaptação da UI, incluindo o editor, para ambos os modos. (Prioridade: 9/10)
    (X) Exportação de Conteúdo: (Prioridade: 9/10)
        (X) Exportação do projeto completo para formato .txt (incluindo títulos de projeto/capítulo, resumos e conteúdo). (Prioridade: 9/10)
        (X) Exportação do projeto completo para formato .pdf (conteúdo principal dos capítulos como texto puro, mas estrutura de títulos e resumos preservada). (Prioridade: 8/10)
    (X) Segurança Básica: (Prioridade: 9/10)
        (X) Proteção de projetos individuais por senha simples (hash SHA-256 da senha é armazenado). (Prioridade: 9/10)
        (X) Interface para definir, alterar, remover e verificar senhas. (Prioridade: 9/10)

### Organização Narrativa

    (X) Fichas de Personagem: (Prioridade: 10/10)
        (X) Criação, edição e exclusão de fichas de personagem detalhadas (nome, apelido, descrições, história, traços, relacionamentos, notas). (Prioridade: 10/10)
        (X) Listagem e acesso às fichas dentro de cada projeto. (Prioridade: 10/10)
    (X) Glossário Interno: (Prioridade: 9/10)
        (X) Criação, edição e exclusão de termos e suas definições, categorização e notas. (Prioridade: 9/10)
        (X) Listagem e acesso aos termos do glossário dentro de cada projeto. (Prioridade: 9/10)
    (X) Timeline de Eventos: (Prioridade: 9/10)
        (X) Criação, edição e exclusão de eventos em uma linha do tempo narrativa. (Prioridade: 9/10)
        (X) Atributos como título, descrição, data/hora do evento (flexível), ordem manual, notas. (Prioridade: 9/10)
        (X) Possibilidade de vincular eventos a capítulos específicos. (Prioridade: 9/10)
        (X) Reordenação manual dos eventos na timeline. (Prioridade: 9/10)

## Próximos Passos e Funcionalidades Futuras (Planejado 🟡)

### Organização Narrativa Avançada (Restante da Fase 2)

    (🟡) Banco de Locais: (Prioridade: 8/10)
        (🟡) Criação de fichas detalhadas para locais (descrição, história, atmosfera, etc.). (Prioridade: 8/10)
        (🟡) Vinculação de locais a cenas/eventos. (Prioridade: 7/10)
    (🟡) Banco de Objetos/Itens Mágicos: (Prioridade: 7/10)
        (🟡) Criação de fichas para objetos importantes (descrição, relevância, poderes, etc.). (Prioridade: 7/10)
    (🟡) Ligações Explícitas entre Elementos: (Prioridade: 8/10)
        (🟡) Interface para vincular explicitamente personagens a cenas/capítulos específicos. (Prioridade: 8/10)
        (🟡) Visualização de quais personagens participam em cada cena/evento. (Prioridade: 8/10)
        (🟡) Vinculação de termos do glossário diretamente no texto do editor (ex: tooltip ou link). (Prioridade: 7/10)
    (🟡) Melhorias na Visualização da Timeline: (Prioridade: 7/10)
        (🟡) Interface gráfica mais elaborada para a timeline (ex: barras cronológicas, filtros por data/personagem/capítulo). (Prioridade: 7/10)
        (🟡) Diferentes modos de visualização da timeline. (Prioridade: 7/10)

### Melhorias no Editor de Markdown
    (🟡) Pré-visualização de Markdown em tempo real no editor.
    (🟡) Barra de ferramentas para auxiliar na escrita de Markdown (inserção de negrito, itálico, listas, links, etc.).
    (🟡) Suporte para sintaxe de Markdown estendida (tabelas, notas de rodapé, etc.), se aplicável.
    (🟡) Opção de conversão de HTML existente (de capítulos antigos) para Markdown (investigar viabilidade).

### Recursos Avançados de Escrita e Produtividade (Fase 3)

    (X) Banco Criativo: (Prioridade: 6/10)
        (X) Seção para armazenar ideias, frases inspiradoras, ganchos de enredo, prompts de escrita. (Prioridade: 6/10)
        (X) Organização por tags ou categorias. (Prioridade: 6/10)
    (X) Templates de Estrutura Narrativa: (Prioridade: 7/10)
        (X) Disponibilização de templates comuns (ex: Jornada do Herói, Estrutura de Três Atos) para auxiliar no planejamento. (Prioridade: 7/10)
        (X) Possibilidade de criar e salvar templates customizados. (Prioridade: 6/10)
    (X) Colaboração Local (Simples): (Prioridade: 5/10)
        (X) Funcionalidade para exportar um projeto inteiro em um formato que possa ser importado por outra instância do StoryFlame (ex: arquivo zip contendo o JSON do projeto e imagens associadas). (Prioridade: 5/10)
        (X) Mecanismo de feedback/revisão simples se dois usuários estiverem trabalhando no mesmo local (não colaboração em tempo real). (Prioridade: 5/10)

### Melhorias Gerais e de UI/UX

    (X) Exportação PDF Avançada: (Prioridade: 7/10)
        (X) Preservar a formatação rica do editor (negrito, itálico, sublinhado, cabeçalhos, listas) na exportação para PDF. (Prioridade: 7/10)
    (X) Personalização Avançada do Editor: (Prioridade: 6/10)
        (X) Mais opções de fontes. (Prioridade: 6/10)
        (X) Configurações de espaçamento, indentação. (Prioridade: 6/10)
    (X) Interface de Usuário (UI) e Experiência do Usuário (UX): (Prioridade: 8/10)
        (V) Refinamentos gerais na interface para torná-la mais polida e intuitiva. (Prioridade: 8/10) # Status to V due to navigation refactor
            - (V) Implementação de arquitetura multi-telas para melhor navegação (Projetos -> Capítulos -> Tela de Escolha de Ação -> Editor Dedicado para Sumário/Conteúdo).
        (X) Melhorias na navegação e feedback visual. (Prioridade: 7/10)
        (X) Otimizações de performance, especialmente ao lidar com projetos muito grandes. (Prioridade: 7/10)
    (P) Testes Automatizados: (Prioridade: 8/10) # Assuming tests are partially implemented due to previous step
        (P) Implementação de testes unitários e de widget para garantir a estabilidade do código. (Prioridade: 8/10) # Assuming tests are partially implemented

### Recursos de Inteligência Artificial (Fase Futura - Pós-MVP)

    (X) Sugestões de Escrita: (Prioridade: 5/10)
        (X) Sugestões contextuais para sinônimos, frases alternativas, ou continuação de ideias. (Prioridade: 5/10)
    (X) Análise de Texto: (Prioridade: 4/10)
        (X) Correção gramatical e de estilo mais avançada. (Prioridade: 4/10)
        (X) Análise de ritmo, clareza e tom do texto. (Prioridade: 4/10)
    (X) Geração Assistida: (Prioridade: 3/10)
        (X) Assistência na geração de nomes de personagens, locais ou títulos. (Prioridade: 3/10)
        (X) Sugestões de plots ou desenvolvimento de cenas com base em prompts. (Prioridade: 3/10)

### Publicação e Integração (Fase Futura - Pós-MVP)

    (X) Exportação para Formatos de E-book: (Prioridade: 4/10)
        (X) Suporte para exportar em formatos como ePub ou Mobi. (Prioridade: 4/10)
    (X) Integração com Plataformas de Publicação: (Prioridade: 2/10)
        (X) (Muito ambicioso) Possibilidade de publicação direta ou preparação de manuscrito para plataformas como Kindle Direct Publishing (KDP), Wattpad, etc. (Prioridade: 2/10)

Este roadmap é um guia e poderá ser ajustado conforme o desenvolvimento do projeto e o feedback dos usuários.