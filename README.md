# StoryFlame Swing

## Visão Geral do Projeto (Project Overview)

StoryFlame Swing é uma aplicação desktop desenvolvida em Java Swing, destinada a escritores e contadores de histórias. O objetivo principal é fornecer uma ferramenta simples, mas eficaz, para ajudar na organização de projetos de escrita, permitindo que os usuários estruturem suas narrativas em capítulos, gerenciem resumos, acompanhem o progresso da escrita e mantenham suas ideias seguras.

A aplicação armazena os dados localmente em formato JSON, oferecendo funcionalidades como criação e gerenciamento de múltiplos projetos, organização por capítulos, edição de conteúdo em Markdown simples, contagem de palavras, metas de escrita e um modo escuro para conforto visual. Recentemente, foi adicionada a funcionalidade de proteção de projetos por senha.

## Como Trabalhar com o Projeto (How to Work with the Project)

Esta seção descreve como construir, executar e entender a estrutura do projeto StoryFlame Swing.

### Pré-requisitos

Antes de começar, certifique-se de ter o seguinte software instalado:
- **Java Development Kit (JDK)**: Versão 11 ou superior.
- **Apache Maven**: Versão 3.6 ou superior (para gerenciamento de dependências e build).

### Construindo o Projeto (Building the Project)

1. Clone o repositório para sua máquina local (se ainda não o fez).
2. Abra um terminal ou prompt de comando na raiz do projeto.
3. Execute o seguinte comando Maven para limpar, compilar, testar e empacotar o projeto:
   ```bash
   mvn clean install
   ```
   Alternativamente, se desejar pular os testes durante o build (não recomendado para desenvolvimento regular):
   ```bash
   mvn clean package -DskipTests
   ```
   Isso gerará um arquivo JAR executável com todas as dependências incluídas no diretório `target/`.

### Executando a Aplicação (Running the Application)

Após construir o projeto com sucesso, você pode executar a aplicação de algumas maneiras:

- **Via JAR Executável**:
  Navegue até o diretório `target/` e execute o JAR que contém as dependências (o nome pode variar ligeiramente com a versão):
  ```bash
  java -jar storyflame-1.0-SNAPSHOT-jar-with-dependencies.jar
  ```
- **Via IDE**:
  Importe o projeto como um projeto Maven em seu IDE favorito (IntelliJ IDEA, Eclipse, NetBeans).
  Localize a classe principal `br.com.marconardes.storyflame.swing.Main` e execute-a.

### Estrutura do Projeto (Project Structure)

O projeto segue uma estrutura Maven padrão:

- `pom.xml`: Define as dependências do projeto, plugins e configurações de build.
- `src/main/java/`: Contém o código fonte principal da aplicação.
  - `br/com/marconardes/storyflame/swing/`: Pacote raiz da aplicação.
    - `model/`: Classes de modelo de dados (ex: `Project.java`, `Chapter.java`).
    - `view/`: Classes da interface gráfica do usuário (Swing) (ex: `ProjectListView.java`, `ChapterEditorView.java`, Dialogs).
    - `viewmodel/`: Classes ViewModel que fazem a ponte entre a View e o Model, contendo a lógica de apresentação (ex: `ProjectViewModel.java`).
    - `util/`: Classes utilitárias (ex: `SecurityUtils.java`, `PdfProjectExporter.java`).
    - `Main.java`: Classe principal que inicia a aplicação.
- `src/test/java/`: Contém os testes unitários.
- `target/`: Diretório gerado pelo Maven contendo os artefatos do build (incluindo o JAR).
- `projects.json` (em `~/.storyflame/`): Arquivo padrão onde os dados dos projetos são armazenados localmente.
- `ROADMAP.md`: Descreve as funcionalidades planejadas e o progresso.
- `README.md`: Este arquivo.

### Principais Funcionalidades (Key Features)

StoryFlame Swing oferece um conjunto de ferramentas para auxiliar escritores:
- **Gerenciamento de Projetos**: Crie e organize múltiplos projetos de escrita.
- **Estrutura em Capítulos**: Divida seus projetos em capítulos ou cenas, com resumos individuais.
- **Editor Simples**: Um editor de texto com suporte básico a Markdown para escrever o conteúdo dos capítulos.
- **Acompanhamento de Progresso**: Contador de palavras e metas de escrita diárias/totais.
- **Modo Escuro**: Tema alternável para maior conforto visual.
- **Exportação**: Exporte seus projetos para formatos `.txt` e `.pdf`.
- **Segurança Básica**: Proteja seus projetos individualmente com senhas (hash SHA-256).

### Contribuindo (Contributing)

Contribuições são bem-vindas! Se você tem ideias para novas funcionalidades, encontrou um bug ou quer contribuir com código:
1. **Bugs e Features**: Abra uma "Issue" no repositório do projeto.
2. **Código**: Faça um "Fork" do repositório, crie um "Branch" para sua feature/correção e envie um "Pull Request".

## Roadmap

Para detalhes sobre funcionalidades planejadas e a visão de futuro para o StoryFlame Swing, por favor veja o nosso arquivo [ROADMAP.md](ROADMAP.md).
