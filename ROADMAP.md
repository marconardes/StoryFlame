# Roadmap do Projeto StoryFlame

Este documento descreve as funcionalidades implementadas e os planos futuros para o StoryFlame, uma ferramenta de escrita criativa desenvolvida em Flutter.

## Legenda de Status
- (V) Funcionalidade Implementada
- (X) Funcionalidade Não Implementada
- (P) Funcionalidade Parcial
- (🟡) Funcionalidade Planejada

## Funcionalidades Implementadas

As seguintes funcionalidades foram desenvolvidas e estão presentes na versão atual do aplicativo:

### Núcleo Essencial e Gerenciamento

    (V) Gerenciamento de Projetos: (Prioridade: 10/10)
        (V) Criação, abertura e listagem de múltiplos projetos de escrita. (Prioridade: 10/10)
        (V) Persistência local dos dados do projeto em formato JSON. (Prioridade: 10/10)
    (V) Organização por Capítulos/Cenas: (Prioridade: 10/10)
        (V) Estruturação de projetos em capítulos ou cenas ordenáveis. (Prioridade: 10/10)
        (V) CRUD completo para capítulos (adicionar, editar título, excluir, reordenar). (Prioridade: 10/10)
    (V) Resumo por Capítulo/Cena: (Prioridade: 9/10)
        (V) Adição e edição de resumos textuais para cada capítulo/cena. (Prioridade: 9/10)
    (V) Editor de Texto Focado: (Prioridade: 10/10)
        (V) Integração do editor de texto rico flutter_quill. (Prioridade: 10/10)
        (V) Funcionalidades de formatação básica (negrito, itálico, sublinhado, cabeçalhos H1-H3). (Prioridade: 9/10)
        (V) Salvamento automático com debounce do conteúdo do capítulo ativo. (Prioridade: 9/10)
    (V) Acompanhamento de Progresso: (Prioridade: 9/10)
        (V) Contador de palavras em tempo real no editor para o capítulo atual. (Prioridade: 9/10)
        (V) Estatísticas diárias da contagem total de palavras do projeto. (Prioridade: 9/10)
        (V) Definição de metas de escrita diárias e totais por projeto. (Prioridade: 9/10)
        (V) Visualização do progresso em relação às metas. (Prioridade: 9/10)
    (V) Modo Escuro: (Prioridade: 9/10)
        (V) Alternância entre tema claro e escuro com persistência da preferência do usuário. (Prioridade: 9/10)
        (V) Adaptação da UI, incluindo o editor, para ambos os modos. (Prioridade: 9/10)
    (V) Exportação de Conteúdo: (Prioridade: 9/10)
        (V) Exportação do projeto completo para formato .txt (incluindo títulos de projeto/capítulo, resumos e conteúdo). (Prioridade: 9/10)
        (V) Exportação do projeto completo para formato .pdf (conteúdo principal dos capítulos como texto puro, mas estrutura de títulos e resumos preservada). (Prioridade: 8/10)
    (V) Segurança Básica: (Prioridade: 9/10)
        (V) Proteção de projetos individuais por senha simples (hash SHA-256 da senha é armazenado). (Prioridade: 9/10)
        (V) Interface para definir, alterar, remover e verificar senhas. (Prioridade: 9/10)

### Organização Narrativa

    (V) Fichas de Personagem: (Prioridade: 10/10)
        (V) Criação, edição e exclusão de fichas de personagem detalhadas (nome, apelido, descrições, história, traços, relacionamentos, notas). (Prioridade: 10/10)
        (V) Listagem e acesso às fichas dentro de cada projeto. (Prioridade: 10/10)
    (V) Glossário Interno: (Prioridade: 9/10)
        (V) Criação, edição e exclusão de termos e suas definições, categorização e notas. (Prioridade: 9/10)
        (V) Listagem e acesso aos termos do glossário dentro de cada projeto. (Prioridade: 9/10)
    (V) Timeline de Eventos: (Prioridade: 9/10)
        (V) Criação, edição e exclusão de eventos em uma linha do tempo narrativa. (Prioridade: 9/10)
        (V) Atributos como título, descrição, data/hora do evento (flexível), ordem manual, notas. (Prioridade: 9/10)
        (V) Possibilidade de vincular eventos a capítulos específicos. (Prioridade: 9/10)
        (V) Reordenação manual dos eventos na timeline. (Prioridade: 9/10)

## Próximos Passos e Funcionalidades Futuras (Planejado 🟡)

### (X) Organização Narrativa Avançada (Restante da Fase 2)

    (X) Banco de Locais: (Prioridade: 8/10)
        (X) Criação de fichas detalhadas para locais (descrição, história, atmosfera, etc.). (Prioridade: 8/10)
        (X) Vinculação de locais a cenas/eventos. (Prioridade: 7/10)
    (X) Banco de Objetos/Itens Mágicos: (Prioridade: 7/10)
        (X) Criação de fichas para objetos importantes (descrição, relevância, poderes, etc.). (Prioridade: 7/10)
    (X) Ligações Explícitas entre Elementos: (Prioridade: 8/10)
        (X) Interface para vincular explicitamente personagens a cenas/capítulos específicos. (Prioridade: 8/10)
        (X) Visualização de quais personagens participam em cada cena/evento. (Prioridade: 8/10)
        (X) Vinculação de termos do glossário diretamente no texto do editor (ex: tooltip ou link). (Prioridade: 7/10)
    (X) Melhorias na Visualização da Timeline: (Prioridade: 7/10)
        (X) Interface gráfica mais elaborada para a timeline (ex: barras cronológicas, filtros por data/personagem/capítulo). (Prioridade: 7/10)
        (X) Diferentes modos de visualização da timeline. (Prioridade: 7/10)

### (X) Recursos Avançados de Escrita e Produtividade (Fase 3)

    (X) Banco Criativo: (Prioridade: 6/10)
        (X) Seção para armazenar ideias, frases inspiradoras, ganchos de enredo, prompts de escrita. (Prioridade: 6/10)
        (X) Organização por tags ou categorias. (Prioridade: 6/10)
    (X) Templates de Estrutura Narrativa: (Prioridade: 7/10)
        (X) Disponibilização de templates comuns (ex: Jornada do Herói, Estrutura de Três Atos) para auxiliar no planejamento. (Prioridade: 7/10)
        (X) Possibilidade de criar e salvar templates customizados. (Prioridade: 6/10)
    (X) Colaboração Local (Simples): (Prioridade: 5/10)
        (X) Funcionalidade para exportar um projeto inteiro em um formato que possa ser importado por outra instância do StoryFlame (ex: arquivo zip contendo o JSON do projeto e imagens associadas). (Prioridade: 5/10)
        (X) Mecanismo de feedback/revisão simples se dois usuários estiverem trabalhando no mesmo local (não colaboração em tempo real). (Prioridade: 5/10)

### (X) Melhorias Gerais e de UI/UX

    (X) Exportação PDF Avançada: (Prioridade: 7/10)
        (X) Preservar a formatação rica do editor (negrito, itálico, sublinhado, cabeçalhos, listas) na exportação para PDF. (Prioridade: 7/10)
    (X) Personalização Avançada do Editor: (Prioridade: 6/10)
        (X) Mais opções de fontes. (Prioridade: 6/10)
        (X) Configurações de espaçamento, indentação. (Prioridade: 6/10)
    (X) Interface de Usuário (UI) e Experiência do Usuário (UX): (Prioridade: 8/10)
        (X) Refinamentos gerais na interface para torná-la mais polida e intuitiva. (Prioridade: 8/10)
        (X) Melhorias na navegação e feedback visual. (Prioridade: 7/10)
        (X) Otimizações de performance, especialmente ao lidar com projetos muito grandes. (Prioridade: 7/10)
    (X) Testes Automatizados: (Prioridade: 8/10)
        (X) Implementação de testes unitários e de widget para garantir a estabilidade do código. (Prioridade: 8/10)

### (X) Recursos de Inteligência Artificial (Fase Futura - Pós-MVP)

    (X) Sugestões de Escrita: (Prioridade: 5/10)
        (X) Sugestões contextuais para sinônimos, frases alternativas, ou continuação de ideias. (Prioridade: 5/10)
    (X) Análise de Texto: (Prioridade: 4/10)
        (X) Correção gramatical e de estilo mais avançada. (Prioridade: 4/10)
        (X) Análise de ritmo, clareza e tom do texto. (Prioridade: 4/10)
    (X) Geração Assistida: (Prioridade: 3/10)
        (X) Assistência na geração de nomes de personagens, locais ou títulos. (Prioridade: 3/10)
        (X) Sugestões de plots ou desenvolvimento de cenas com base em prompts. (Prioridade: 3/10)

### (X) Publicação e Integração (Fase Futura - Pós-MVP)

    (X) Exportação para Formatos de E-book: (Prioridade: 4/10)
        (X) Suporte para exportar em formatos como ePub ou Mobi. (Prioridade: 4/10)
    (X) Integração com Plataformas de Publicação: (Prioridade: 2/10)
        (X) (Muito ambicioso) Possibilidade de publicação direta ou preparação de manuscrito para plataformas como Kindle Direct Publishing (KDP), Wattpad, etc. (Prioridade: 2/10)

Este roadmap é um guia e poderá ser ajustado conforme o desenvolvimento do projeto e o feedback dos usuários.
