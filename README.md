# 🏭 Nexus Logística — Backend (WMS & TMS)

Sistema integrado de **Warehouse Management System (WMS)** e **Transportation Management System (TMS)** para gerenciamento completo do ciclo de vida de mercadorias: entrada no estoque, armazenamento, separação (picking), expedição e entrega ao cliente final.

---

## 📋 Sumário

- [Stack Tecnológica](#-stack-tecnológica)
- [Pré-requisitos](#-pré-requisitos)
- [Como Rodar](#-como-rodar)
- [Arquitetura do Projeto](#-arquitetura-do-projeto)
- [Módulos de Negócio](#-módulos-de-negócio)
- [Modelo de Dados](#-modelo-de-dados)
- [Flyway Migrations](#-flyway-migrations)
- [Comandos Úteis](#-comandos-úteis)
- [Padrões e Convenções](#-padrões-e-convenções)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)

---

## 🛠 Stack Tecnológica

| Tecnologia          | Versão | Propósito                                 |
| ------------------- | ------ | ----------------------------------------- |
| **Java**            | 25     | Linguagem principal                       |
| **Spring Boot**     | 4.0.3  | Framework backend                         |
| **Spring Security** | —      | Autenticação JWT + OAuth2 Resource Server |
| **Spring Data JPA** | —      | Persistência / ORM                        |
| **Flyway**          | —      | Versionamento de banco de dados           |
| **PostgreSQL**      | latest | Banco de dados relacional                 |
| **Lombok**          | —      | Redução de boilerplate                    |
| **Docker Compose**  | —      | Infraestrutura local (PostgreSQL)         |
| **Gradle**          | 9.3.1  | Build tool                                |
| **Testcontainers**  | —      | Testes de integração com DB real          |

---

## ✅ Pré-requisitos

1. **JDK 25** — [Download Adoptium](https://adoptium.net/) ou [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. **Docker Desktop** — [Download](https://www.docker.com/products/docker-desktop/)
   - Necessário para o PostgreSQL subir automaticamente via `compose.yaml`
3. **Git** — Para versionamento

> **Nota:** Você **não precisa instalar o PostgreSQL manualmente** nem criar banco no pgAdmin. O Spring Boot Docker Compose sobe tudo automaticamente.

---

## 🚀 Como Rodar

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd nexus-wms-back
```

### 2. Inicie o projeto
```bash
.\gradlew.bat bootRun
```

O Spring Boot vai automaticamente:
1. **Subir o PostgreSQL** via Docker Compose (contêiner `postgres`)
2. **Executar as migrations** do Flyway (criando as 15 tabelas)
3. **Validar o mapeamento** entidade↔tabela (`ddl-auto: validate`)
4. **Iniciar o servidor** na porta `8080`

### 3. Acesse e Teste a API (Swagger)
A forma mais fácil de interagir com o código como usuário/cliente da API é pelo **Swagger UI**, uma interface visual de documentação gerada automaticamente.

1. **Garanta que a aplicação iniciou:** Rode `.\gradlew.bat bootRun` no terminal.
2. **Abra no navegador:** http://localhost:8080/swagger-ui.html
3. **Faça o Login:**
   - Procure e abra a rota `POST /api/auth/login` no Swagger.
   - Clique no botão "Try it out" e altere o JSON para enviar `email: "admin@nexus.com"` e `password: "admin123"` (os usuários padrão criados pela aplicação).
   - Clique em "Execute" e copie o `token` longo que aparecer na caixa de resposta.
4. **Liberando as rotas bloqueadas:**
   - Suba até o topo da página do Swagger e clique no botão verde com um cadeado **"Authorize"**.
   - Digite a palavra `Bearer ` + dê um espaço + e cole o seu token (ex: `Bearer eyJhb...`). 
   - Clique novamente no botão verde Authorize dentro do modal.
   - Pronto! Agora a aplicação sabe quem você é e você pode testar todas as rotas de Categoria, Produto, etc.

> 💡 **Nota sobre falsos erros na IDE:** Ás vezes a sua IDE (tipo VS Code) vai mostrar uma linha vermelha no import de arquivos do Swagger (como o `io.swagger.v3...`). Se a aplicação rodar normal no terminal, é apenas a IDE que está "lenta" ou "atrasada" para ler a biblioteca do cache. Você pode apenas ignorar.

- **Banco de Dados (para os curiosos acessarem pelo dbeaver/pgadmin):** `localhost:5432` | DB: `nexus_wms` | User: `nexus` | Senha: `nexus123`

---

## 🏗 Arquitetura do Projeto

```
src/main/java/br/com/nexus/nexus_wms/
├── domain/
│   ├── entity/                    # Entidades JPA
│   │   ├── BaseEntity.java        # @MappedSuperclass (id, createdAt, updatedAt)
│   │   ├── iam/                   # User, RefreshToken, AuditLog
│   │   ├── wms/                   # Category, Product, WarehouseLocation, Stock
│   │   ├── order/                 # Order, OrderItem, PickingList, PickingListItem
│   │   └── tms/                   # Vehicle, Driver, Shipment, ShipmentOrder
│   └── enums/                     # UserRole, OrderStatus, ShipmentStatus, etc.
└── infrastructure/
    └── exception/
        └── GlobalExceptionHandler.java  # RFC 7807 Problem Details

src/main/resources/
├── application.yaml               # Configuração principal
└── db/migration/                   # Flyway SQL migrations
    ├── V1__Create_IAM_tables.sql
    ├── V2__Create_WMS_tables.sql
    ├── V3__Create_Orders_tables.sql
    ├── V4__Create_TMS_tables.sql
    └── V5__Create_Indexes.sql
```

### Decisões de Arquitetura

| Decisão                                       | Motivo                                                                                 |
| --------------------------------------------- | -------------------------------------------------------------------------------------- |
| **`BaseEntity`** (`@MappedSuperclass`)        | Centraliza `id`, `createdAt`, `updatedAt` — evita repetição em 15 entidades            |
| **`@Version` (Optimistic Locking)**           | Em `User`, `Product`, `Stock`, `Order`, `Shipment` — resolve conflitos de concorrência |
| **`FetchType.LAZY`** em todos os `@ManyToOne` | Evita problema N+1 do Hibernate                                                        |
| **`ddl-auto: validate`**                      | Hibernate valida mapeamento vs banco, mas nunca altera o schema — Flyway controla tudo |
| **`open-in-view: false`**                     | Anti-pattern desabilitado — queries lazy só executam dentro de `@Transactional`        |
| **RFC 7807 Problem Details**                  | Respostas de erro padronizadas e estruturadas                                          |

---

## 📦 Módulos de Negócio

### Módulo A — IAM (Identity & Access Management)
- **Autenticação** via JWT (Access Token + Refresh Token)
- **RBAC** com 4 perfis: `ADMIN`, `OPERADOR_ESTOQUE`, `MOTORISTA`, `GERENTE`
- **Audit Log** — registra quem fez, o quê e quando

### Módulo B — WMS (Warehouse Management)
- **Cadastro de Produtos** com SKU, dimensões, peso, curva ABC e preço
- **Endereçamento** posicional: Corredor → Prateleira → Nível → Vão
- **Controle de Lote e Validade** — suporta estratégias FIFO/FEFO
- **Estoque posicional** — um produto pode estar em múltiplas localizações

### Módulo C — Orders (Pedidos & Expedição)
- **State Machine** de status: `CRIADO` → `APROVADO` → `EM_SEPARACAO` → `SEPARADO` → `EM_TRANSITO` → `ENTREGUE`
- **Picking Lists** com sequência otimizada por corredor
- **Snapshot de preço** — preço do item é congelado no momento do pedido

### Módulo D — TMS (Transportation Management)
- **Gestão de Frota** — veículos com capacidade de peso e volume
- **Gestão de Motoristas** — CNH com categoria validada
- **Manifesto de Carga** — agrupa N pedidos em um veículo + motorista

---

## 🗄 Modelo de Dados

### 15 Tabelas (4 domínios)

| #   | Tabela                   | Domínio | Descrição                                  |
| --- | ------------------------ | ------- | ------------------------------------------ |
| 1   | `tb_users`               | IAM     | Usuários com RBAC                          |
| 2   | `tb_refresh_tokens`      | IAM     | Tokens JWT com revogação                   |
| 3   | `tb_audit_logs`          | IAM     | Trilha de auditoria imutável               |
| 4   | `tb_categories`          | WMS     | Categorias de produto                      |
| 5   | `tb_products`            | WMS     | Catálogo com dimensões e classificação ABC |
| 6   | `tb_warehouse_locations` | WMS     | Posições físicas do armazém                |
| 7   | `tb_stocks`              | WMS     | Estoque posicional com lote/validade       |
| 8   | `tb_orders`              | Orders  | Pedidos com state machine                  |
| 9   | `tb_order_items`         | Orders  | Itens do pedido com snapshot de preço      |
| 10  | `tb_picking_lists`       | Orders  | Listas de picking por pedido               |
| 11  | `tb_picking_list_items`  | Orders  | Itens de picking com sequência             |
| 12  | `tb_vehicles`            | TMS     | Frota com capacidade                       |
| 13  | `tb_drivers`             | TMS     | Motoristas com CNH                         |
| 14  | `tb_shipments`           | TMS     | Manifestos de carga                        |
| 15  | `tb_shipment_orders`     | TMS     | Junção pedidos ↔ cargas                    |

### Concorrência (Optimistic Locking)

As tabelas `tb_users`, `tb_products`, `tb_stocks`, `tb_orders` e `tb_shipments` possuem coluna `version BIGINT` mapeada com `@Version` do JPA. Quando dois operadores tentam alterar o mesmo registro simultaneamente, o segundo recebe um erro **409 Conflict** com mensagem amigável.

---

## 🔄 Flyway Migrations

| Arquivo                        | Conteúdo                                                    |
| ------------------------------ | ----------------------------------------------------------- |
| `V1__Create_IAM_tables.sql`    | Users, RefreshTokens, AuditLogs + CHECK constraint de roles |
| `V2__Create_WMS_tables.sql`    | Categories, Products, Locations, Stocks + UNIQUE composto   |
| `V3__Create_Orders_tables.sql` | Orders, Items, PickingLists + CHECK de status               |
| `V4__Create_TMS_tables.sql`    | Vehicles, Drivers, Shipments, ShipmentOrders                |
| `V5__Create_Indexes.sql`       | 38 índices de performance incluindo FEFO composto           |

As migrations rodam automaticamente no startup. Para ver o histórico:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## ⚡ Comandos Úteis

### Build & Run

```bash
# Compilar o projeto (sem rodar)
.\gradlew.bat compileJava

# Rodar o projeto (sobe PostgreSQL + Flyway + app)
.\gradlew.bat bootRun

# Build completo (compilar + testes)
.\gradlew.bat build

# Gerar JAR para produção
.\gradlew.bat bootJar
```

### 🧪 Testes e Qualidade (Mockito / JUnit)

A arquitetura de testes de Serviço do projeto valida de forma rigorosa as "Regras de Ouro" das transações:

- `StockServiceTest`: Verifica **Entrada e Saídas** com Mockito interceptando repositórios.
  - Verifica adição de estoque existente vs novos locais.
  - Assegura que tentar retirar mais estoque que o disponível lança o erro apropriado validando quantidade exata.
- `OrderServiceTest`: Funciona orientando as **Listas de Separação**.
  - Garante que a aplicação vasculhe e extraia precisamente de localizações diferentes o número de produtos comprados sem "estourar" o lote.
  - Verifica o avanço da Máquina de Estado do pedido (`CRIADO` -> `EM_SEPARACAO`).

```bash
# Rodar todos os testes (usa Testcontainers — precisa de Docker)
.\gradlew.bat test

# Rodar testes com output detalhado
.\gradlew.bat test --info

# Ver relatório visual (após rodar) abra o arquivo gerado:
# build/reports/tests/test/index.html
```

### Gradle

```bash
# Limpar build anterior (apaga pasta de compilação velha)
.\gradlew.bat clean

# Limpa e compila tudo do zero (Clean + Build completo)
.\gradlew.bat clean build

# Ver todas as dependências do projeto
.\gradlew.bat dependencies
```

### 🧰 Resolução de Problemas (Troubleshooting)

**O Problema (Erros Fantasmas na IDE):** 
A sua IDE VS Code ou IntelliJ está cheia de erros vermelhos do nada (como pacote `não encontrado` ou `cannot resolve symbol`), ou as dependências que nós acabamos de baixar não estão sendo carregadas, mesmo o código estando completamente correto e compilando de boa pelo terminal.

**A Solução (Forçar Atualização de Dependências):**  
Existe um comando que força o Gradle a dizer: *"Ignore tudo o que você acha que já tem na memória, conecte-se na internet, baixe as bibliotecas originais nas versões corretas de novo e refaça a sincronização dos arquivos localmente"*.  

Para forçar o sistema a limpar seu cache corrompido ou sincronizar os arquivos, rode:
```bash
.\gradlew.bat build --refresh-dependencies
```
*Dica:* Após rodar o comando acima, a pasta do gradle no projeto vai estar atualizada. Se a IDE persistir visualmente no erro, você precisará fechar e abri-la ou usar a função da IDE (botãozinho na interface) chamado "Reload All Gradle Projects".

### 🐳 Estruturação Docker (Deployment para Produção)

Para rodar no seu computador (ambiente de Desenvolvimento), nós já usamos o `docker compose` em conjunto do Spring Boot para iniciar atomaticamente o PostgreSQL na sua máquina. Sendo ótimo para desenvolver mais rápido.

Porém, quando o projeto estiver pronto e você quiser colocá-lo no ar e hospedá-lo (AWS, Google Cloud, DigitalOcean, etc), você não enviará o seu código fonte. Você precisará empacotar o Java, as bibliotecas do projeto, o código compilado e o sistema Linux em um **"caixote isolado"**, que nós chamamos de **Imagem Docker**. A genialidade é que o Spring Boot já faz isso inteiramente para você através de uma tecnologia oficial chamada **Buildpacks**.

**Como funciona o poderoso comando `bootBuildImage`:**
Quando você aciona o gradle com esse comando, o Spring entra em modo de arquiteto. Ele analisa o seu código, descobre que nós usamos Java 25, vai até a nuvem, baixa para sua máquina e extrai um sistema operacional Linux mínimo (só o estritamente essencial para poupar recurso). Ele então instala o Java limpo nele e coloca seu projeto lá dentro de forma mágica, gerando na sua máquina uma "Imagem de Computador Virtual pronta com seu APP".

**Como empacotar a aplicação final para a Nuvem na vida real:**
1. Apenas verifique se o **Docker Desktop** está aberto e rodando em sua barra de tarefas/Windows.
2. Pare o seu projeto, e em nosso terminal, rode:
```bash
# Manda o Spring empacotar nossa aplicação final numa imagem Docker:
.\gradlew.bat bootBuildImage
```
3. O empacotamento demorará uma média de 1 a 3 minutos apenas.
4. Quando finalizar, se você abrir o terminal e rodar `docker images`, verá a nossa nova aplicação compactada e listada (ex: `nexus-wms-back:x.x.x`). A partir daí, sua API "está numa sacola portátil". Qualquer pessoa e provedor que instale ela e a execute, fará a aplicação e suas rotas rodarem integralmente, seja na India, na America, sem precisar instalar linguagens de código na máquina de hospedagem local.

### Docker / Banco de Dados

```bash
# Subir só o PostgreSQL (sem a app)
docker compose up -d

# Ver logs do contêiner
docker compose logs postgres -f

# Parar o PostgreSQL
docker compose down

# Parar e apagar dados (volume)
docker compose down -v

# Conectar ao banco via psql
docker compose exec postgres psql -U nexus -d nexus_wms
```

### Consultas Úteis (psql / pgAdmin)

```sql
-- Listar todas as tabelas
\dt

-- Ver estrutura de uma tabela
\d tb_users

-- Ver todos os índices
\di

-- Ver constraints de uma tabela
SELECT conname, contype, pg_get_constraintdef(oid)
FROM pg_constraint WHERE conrelid = 'tb_users'::regclass;

-- Ver migrations executadas
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## 📐 Padrões e Convenções

### Nomenclatura
| Onde           | Convenção                        | Exemplo                                  |
| -------------- | -------------------------------- | ---------------------------------------- |
| Tabelas        | `snake_case` com prefixo `tb_`   | `tb_warehouse_locations`                 |
| Colunas        | `snake_case` (inglês)            | `created_at`, `unit_price`               |
| Entidades Java | `PascalCase` (sem prefixo `tb_`) | `WarehouseLocation`                      |
| Enums          | `UPPER_SNAKE_CASE`               | `EM_SEPARACAO`, `OPERADOR_ESTOQUE`       |
| Packages       | Por domínio                      | `domain.entity.wms`, `domain.entity.tms` |

### Tratamento de Erros (RFC 7807)

Todas as exceções retornam `ProblemDetail` padronizado:

| Exceção                           | HTTP Status               | Título                   |
| --------------------------------- | ------------------------- | ------------------------ |
| `OptimisticLockException`         | 409 Conflict              | Conflito de Concorrência |
| `MethodArgumentNotValidException` | 422 Unprocessable Entity  | Erro de Validação        |
| `DataIntegrityViolationException` | 409 Conflict              | Violação de Integridade  |
| `Exception` (genérica)            | 500 Internal Server Error | Erro Interno             |

### Commits (Conventional Commits)
```
feat(domain): cria entidades JPA do modulo WMS
fix(stock): corrige validacao de quantidade negativa
chore: atualiza dependencias do Gradle
perf(db): cria indices de performance
```

---

## 🔐 Variáveis de Ambiente

Para **desenvolvimento local**, as configurações já estão no `application.yaml`. Para **produção**, sobrescreva com variáveis de ambiente:

| Variável                        | Padrão (dev)                                 | Descrição                    |
| ------------------------------- | -------------------------------------------- | ---------------------------- |
| `SPRING_DATASOURCE_URL`         | `jdbc:postgresql://localhost:5432/nexus_wms` | URL do banco                 |
| `SPRING_DATASOURCE_USERNAME`    | `nexus`                                      | Usuário do banco             |
| `SPRING_DATASOURCE_PASSWORD`    | `nexus123`                                   | Senha do banco               |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate`                                   | Modo de DDL do Hibernate     |
| `SPRING_FLYWAY_ENABLED`         | `true`                                       | Habilitar/desabilitar Flyway |

---

## 📄 Licença

Este projeto é privado e de uso exclusivo para fins educacionais e de portfólio.
