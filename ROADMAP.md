Roadmap do Projeto StoryFlame

Este documento descreve as funcionalidades implementadas e os planos futuros para o StoryFlame, uma ferramenta de escrita criativa desenvolvida em Flutter.
Funcionalidades Implementadas (V)
Funcionalidades Não Implementadas(X)
Funcionalidades Parciais (P)

As seguintes funcionalidades foram desenvolvidas e estão presentes na versão atual do aplicativo:
Núcleo Essencial e Gerenciamento 

    Gerenciamento de Projetos:
        Criação, abertura e listagem de múltiplos projetos de escrita.
        Persistência local dos dados do projeto em formato JSON.
    Organização por Capítulos/Cenas:
        Estruturação de projetos em capítulos ou cenas ordenáveis.
        CRUD completo para capítulos (adicionar, editar título, excluir, reordenar).
    Resumo por Capítulo/Cena:
        Adição e edição de resumos textuais para cada capítulo/cena.
    Editor de Texto Focado:
        Integração do editor de texto rico flutter_quill.
        Funcionalidades de formatação básica (negrito, itálico, sublinhado, cabeçalhos H1-H3).
        Salvamento automático com debounce do conteúdo do capítulo ativo.
    Acompanhamento de Progresso:
        Contador de palavras em tempo real no editor para o capítulo atual.
        Estatísticas diárias da contagem total de palavras do projeto.
        Definição de metas de escrita diárias e totais por projeto.
        Visualização do progresso em relação às metas.
    Modo Escuro:
        Alternância entre tema claro e escuro com persistência da preferência do usuário.
        Adaptação da UI, incluindo o editor, para ambos os modos.
    Exportação de Conteúdo:
        Exportação do projeto completo para formato .txt (incluindo títulos de projeto/capítulo, resumos e conteúdo).
        Exportação do projeto completo para formato .pdf (conteúdo principal dos capítulos como texto puro, mas estrutura de títulos e resumos preservada).
    Segurança Básica:
        Proteção de projetos individuais por senha simples (hash SHA-256 da senha é armazenado).
        Interface para definir, alterar, remover e verificar senhas.

Organização Narrativa

    Fichas de Personagem:
        Criação, edição e exclusão de fichas de personagem detalhadas (nome, apelido, descrições, história, traços, relacionamentos, notas).
        Listagem e acesso às fichas dentro de cada projeto.
    Glossário Interno:
        Criação, edição e exclusão de termos e suas definições, categorização e notas.
        Listagem e acesso aos termos do glossário dentro de cada projeto.
    Timeline de Eventos:
        Criação, edição e exclusão de eventos em uma linha do tempo narrativa.
        Atributos como título, descrição, data/hora do evento (flexível), ordem manual, notas.
        Possibilidade de vincular eventos a capítulos específicos.
        Reordenação manual dos eventos na timeline.

Próximos Passos e Funcionalidades Futuras (Planejado 🟡)

As seguintes funcionalidades e melhorias estão planejadas para futuras versões do StoryFlame:
Organização Narrativa Avançada (Restante da Fase 2)

    Banco de Locais:
        Criação de fichas detalhadas para locais (descrição, história, atmosfera, etc.).
        Vinculação de locais a cenas/eventos.
    Banco de Objetos/Itens Mágicos:
        Criação de fichas para objetos importantes (descrição, relevância, poderes, etc.).
    Ligações Explícitas entre Elementos:
        Interface para vincular explicitamente personagens a cenas/capítulos específicos.
        Visualização de quais personagens participam em cada cena/evento.
        Vinculação de termos do glossário diretamente no texto do editor (ex: tooltip ou link).
    Melhorias na Visualização da Timeline:
        Interface gráfica mais elaborada para a timeline (ex: barras cronológicas, filtros por data/personagem/capítulo).
        Diferentes modos de visualização da timeline.

Recursos Avançados de Escrita e Produtividade (Fase 3)

    Banco Criativo:
        Seção para armazenar ideias, frases inspiradoras, ganchos de enredo, prompts de escrita.
        Organização por tags ou categorias.
    Templates de Estrutura Narrativa:
        Disponibilização de templates comuns (ex: Jornada do Herói, Estrutura de Três Atos) para auxiliar no planejamento.
        Possibilidade de criar e salvar templates customizados.
    Colaboração Local (Simples):
        Funcionalidade para exportar um projeto inteiro em um formato que possa ser importado por outra instância do StoryFlame (ex: arquivo zip contendo o JSON do projeto e imagens associadas).
        Mecanismo de feedback/revisão simples se dois usuários estiverem trabalhando no mesmo local (não colaboração em tempo real).

Melhorias Gerais e de UI/UX

    Exportação PDF Avançada:
        Preservar a formatação rica do editor (negrito, itálico, sublinhado, cabeçalhos, listas) na exportação para PDF.
    Personalização Avançada do Editor:
        Mais opções de fontes.
        Configurações de espaçamento, indentação.
    Interface de Usuário (UI) e Experiência do Usuário (UX):
        Refinamentos gerais na interface para torná-la mais polida e intuitiva.
        Melhorias na navegação e feedback visual.
        Otimizações de performance, especialmente ao lidar com projetos muito grandes.
    Testes Automatizados:
        Implementação de testes unitários e de widget para garantir a estabilidade do código.

Recursos de Inteligência Artificial (Fase Futura - Pós-MVP)

    Sugestões de Escrita:
        Sugestões contextuais para sinônimos, frases alternativas, ou continuação de ideias.
    Análise de Texto:
        Correção gramatical e de estilo mais avançada.
        Análise de ritmo, clareza e tom do texto.
    Geração Assistida:
        Assistência na geração de nomes de personagens, locais ou títulos.
        Sugestões de plots ou desenvolvimento de cenas com base em prompts.

Publicação e Integração (Fase Futura - Pós-MVP)

    Exportação para Formatos de E-book:
        Suporte para exportar em formatos como ePub ou Mobi.
    Integração com Plataformas de Publicação:
        (Muito ambicioso) Possibilidade de publicação direta ou preparação de manuscrito para plataformas como Kindle Direct Publishing (KDP), Wattpad, etc.

Este roadmap é um guia e poderá ser ajustado conforme o desenvolvimento do projeto e o feedback dos usuários.
