# Challenge Sprint 4 — MOTIVA com Spring Boot + JPA + API REST

Sistema de monitoramento e priorização de roçada de vegetação nas rodovias da **Motiva**.

Na Sprint 3 o sistema era um programa de console com JDBC puro e um DAO por tabela. Nesta
sprint ele virou uma **API REST** em Spring Boot, com **Spring Data JPA** lendo e gravando nas
**mesmas tabelas e sequences** modeladas na Sprint 3 (`sql/script-criacao.sql`).

## O que mudou em relação à Sprint 3

| Sprint 3 (JDBC puro) | Sprint 4 (Spring Boot) |
|---|---|
| Records + `TrechoRodoviaDAO.montar()` escolhendo a subclasse pelo `TP_PERFIL` | `@Entity` com herança `SINGLE_TABLE`; o JPA usa `TP_PERFIL` como discriminador e devolve `TrechoUmido`/`TrechoSeco` sozinho |
| 4 DAOs com `PreparedStatement`, `ResultSet` e SQL na mão | 4 interfaces `JpaRepository`, sem nenhuma linha de SQL |
| `ConexaoBD` (singleton) com usuário e senha no código | `application.properties` + pool HikariCP do Spring; credenciais por variável de ambiente |
| `sq_x.NEXTVAL` no `INSERT` + `getGeneratedKeys()` | `@GeneratedValue` + `@SequenceGenerator` apontando para `SQ_EQUIPE`, `SQ_TRECHO`, `SQ_INTERVENCAO`, `SQ_RELATORIO` |
| `GeradorRelatorio` imprimindo no console | `RelatorioService` devolvendo JSON |
| `Main` com `System.out.println` | `@RestController` + códigos HTTP |
| `ORA-02292` estourando ao apagar equipe com trechos | Service checa antes e devolve **409 Conflict** |
| Testes com runner próprio | JUnit 5 + `@SpringBootTest` + `MockMvc` |

## Tecnologias

- Java 17 · Spring Boot 4.1.2 · Maven (wrapper incluído)
- `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`
- Oracle FIAP com `com.oracle.database.jdbc:ojdbc17` (dependência Maven, sem JAR manual)
- H2 em modo Oracle só para o perfil `local` e para os testes

## Estrutura

```
sprint4_POO/
├── pom.xml
├── sql/                         # scripts da Sprint 3, sem alteração
│   ├── script-criacao.sql
│   └── script-dados.sql
├── docs/requisicoes.md          # registro das requisições executadas
└── src/
    ├── main/java/br/com/motiva/
    │   ├── MotivaApplication.java     # @SpringBootApplication
    │   ├── model/        # @Entity + regras de domínio (Sprint 1/2)
    │   ├── repository/   # interfaces JpaRepository com derived queries
    │   ├── service/      # @Service: validações, motor de prioridade, relatório
    │   ├── controller/   # @RestController: só recebe, delega e devolve o status
    │   ├── dto/          # contratos da API (request/response) com Bean Validation
    │   └── exception/    # exceções de negócio + @RestControllerAdvice
    ├── main/resources/
    │   ├── application.properties        # Oracle FIAP
    │   └── application-local.properties  # H2 com os scripts da Sprint 3
    └── test/java/br/com/motiva/
        ├── controller/MotivaApiTest.java # 17 testes de API (MockMvc)
        └── model/TrechoRodoviaTest.java  # 8 testes de domínio
```

O fluxo é sempre **Controller → Service → Repository → Banco**. Controller não fala com
Repository e não tem regra de negócio; Service não conhece HTTP.

| Camada | Classes |
|---|---|
| `model` | `EquipeManutencao`, `TrechoRodovia` (abstrata) → `TrechoUmido` / `TrechoSeco`, `Intervencao`, `RelatorioPrioridade` (entities) · `IntervencaoOperacional` → `RocadaMecanizada` / `Pulverizacao`, `Prioridade`, `MonitoravelViaIoT` (domínio) |
| `repository` | `EquipeManutencaoRepository`, `TrechoRodoviaRepository`, `IntervencaoRepository`, `RelatorioPrioridadeRepository` |
| `service` | `EquipeService`, `TrechoService`, `IntervencaoService`, `RelatorioService`, `MotorDeRegras` |
| `controller` | `EquipeController`, `TrechoController`, `IntervencaoController`, `RelatorioController` |

## Modelo de dados (o mesmo da Sprint 3)

| Tabela | Entity | Sequence |
|---|---|---|
| `MT_EQUIPE_MANUTENCAO` | `EquipeManutencao` | `SQ_EQUIPE` |
| `MT_TRECHO_RODOVIA` | `TrechoRodovia` (`TP_PERFIL` = `UMIDO` → `TrechoUmido`, `SECO` → `TrechoSeco`) | `SQ_TRECHO` |
| `MT_INTERVENCAO` | `Intervencao` (`@ManyToOne` para trecho e equipe) | `SQ_INTERVENCAO` |
| `MT_RELATORIO_PRIORIDADE` | `RelatorioPrioridade` | `SQ_RELATORIO` |

As sequences foram criadas com `INCREMENT BY 1 NOCACHE`, então todo `@SequenceGenerator` usa
`allocationSize = 1` (o padrão do Hibernate é 50 e geraria ids errados).

`spring.jpa.hibernate.ddl-auto=none`: o Hibernate **não cria nem altera** tabela nenhuma, só usa
as que já existem. O perfil `local` e os testes sobem um H2 rodando os próprios scripts da Sprint 3,
o que também prova que o mapeamento bate com o DDL original.

## Faixas de prioridade (limiares da Sprint 2/3)

| Nível de vegetação | Faixa | Intervenção recomendada |
|---|---|---|
| ≥ 60 cm | `URGENTE` | Roçada Mecanizada (zera o nível, R$ 1.200/km) |
| ≥ 40 cm | `CRITICO` | Roçada Mecanizada |
| ≥ 20 cm | `ATENCAO` | Pulverização (reduz 60%, R$ 450/km) |
| < 20 cm | `NORMAL` | Sem ação |

A classificação está no enum `Prioridade` e a decisão no `MotorDeRegras` (camada service).

## Como executar

### 1. Banco Oracle

Se as tabelas da Sprint 3 ainda existem no seu usuário da FIAP, não precisa fazer nada.
Senão, rode no DBeaver/SQL Developer, nesta ordem, com *Execute script* (Alt+X):

```
sql/script-criacao.sql
sql/script-dados.sql
```

### 2. Credenciais (nunca no código)

O `application.properties` lê as credenciais de variáveis de ambiente. No repositório só há
placeholders (`SEU_RM` / `SUA_SENHA`).

```bash
# Linux / macOS / Git Bash
export MOTIVA_DB_USUARIO=RM000000
export MOTIVA_DB_SENHA=sua_senha
```

```powershell
# Windows PowerShell
$env:MOTIVA_DB_USUARIO="RM000000"
$env:MOTIVA_DB_SENHA="sua_senha"
```

`MOTIVA_DB_URL` também pode ser sobrescrita (padrão: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`).

### 3. Subir a API

```bash
./mvnw spring-boot:run          # ou: mvn spring-boot:run   (Windows: mvnw.cmd spring-boot:run)
```

A API fica em `http://localhost:8080`.

### Sem acesso ao Oracle (perfil `local`)

Sobe tudo num H2 em memória que executa `sql/script-criacao.sql` e `sql/script-dados.sql`
(rode a partir da raiz do projeto):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Testes

```bash
./mvnw test
```

25 testes: 17 de API (`@SpringBootTest` + `MockMvc`, perfil `local`, cada teste com rollback)
e 8 de domínio portados da Sprint 3. Não precisam do Oracle.

## Endpoints

### Trechos — `/api/trechos`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/trechos` | Lista todos (maior nível primeiro) | 200 | |
| GET | `/api/trechos?nivelMinimo=40` | Trechos com nível ≥ valor | 200 | 400 se não for número |
| GET | `/api/trechos?perfil=UMIDO` | Trechos por perfil | 200 | |
| GET | `/api/trechos?rodovia=BR-116` | Trechos de uma rodovia | 200 | |
| GET | `/api/trechos/{id}` | Busca por id | 200 | 404 |
| POST | `/api/trechos` | Cria | 201 + `Location` | 400 dados inválidos · 404 equipe · 409 faixa já cadastrada |
| PUT | `/api/trechos/{id}` | Atualiza | 200 | 400 · 404 · 409 |
| DELETE | `/api/trechos/{id}` | Remove | 204 | 404 · 409 se tiver intervenções |

Corpo de POST/PUT:

```json
{
  "rodovia": "SP-280",
  "kmInicial": 30.0,
  "kmFinal": 36.5,
  "nivelVegetacao": 52.0,
  "perfil": "UMIDO",
  "idEquipe": 1
}
```

`idEquipe` é opcional. O `perfil` (`UMIDO`/`SECO`) é fixo depois de criado — ele define a
subclasse da entity — então um PUT com perfil diferente devolve 400. A resposta também traz
`extensaoKm`, `prioridade` e `intervencaoRecomendada`, calculadas pelo service.

### Equipes — `/api/equipes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/equipes` | Lista todas (por nome) | 200 | |
| GET | `/api/equipes?minimoOperadores=5` | Equipes com pelo menos N operadores | 200 | |
| GET | `/api/equipes/{id}` | Busca por id | 200 | 404 |
| POST | `/api/equipes` | Cria | 201 + `Location` | 400 · 409 nome repetido |
| PUT | `/api/equipes/{id}` | Atualiza | 200 | 400 · 404 · 409 |
| DELETE | `/api/equipes/{id}` | Remove | 204 | 404 · 409 se ainda tiver trechos ou intervenções |

```json
{ "nome": "Equipe Delta", "quantidadeOperadores": 5 }
```

### Intervenções — `/api/intervencoes`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/intervencoes` | Lista (mais recentes primeiro) | 200 | |
| GET | `/api/intervencoes?tipo=ROCADA` | Por tipo (`findByTipo`) | 200 | |
| GET | `/api/intervencoes?idTrecho=1` | Histórico de um trecho | 200 | |
| GET | `/api/intervencoes/{id}` | Busca por id | 200 | 404 |
| POST | `/api/intervencoes` | **Executa** a intervenção no trecho e grava o registro | 201 + `Location` | 400 · 404 trecho/equipe |
| PUT | `/api/intervencoes/{id}` | Corrige equipe e custo do lançamento | 200 | 400 · 404 |
| DELETE | `/api/intervencoes/{id}` | Remove o registro | 204 | 404 |

POST:

```json
{ "idTrecho": 1, "tipo": "ROCADA", "idEquipe": 2 }
```

O service aplica a `RocadaMecanizada`/`Pulverizacao` da Sprint 2 no trecho, calcula o custo
por km, grava nível antes/depois e atualiza o nível do trecho. Sem `idEquipe`, usa a equipe
responsável pelo trecho. Trecho em `NORMAL` não recebe intervenção (400).

PUT — nível antes/depois e tipo são histórico do que aconteceu e não mudam; dá para corrigir só:

```json
{ "idEquipe": 2, "custoEstimado": 7500.00 }
```

### Relatórios — `/api/relatorios`

| Método | Rota | Ação | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/relatorios` | Gera o relatório a partir dos trechos atuais e grava no histórico | 201 + `Location` | |
| GET | `/api/relatorios` | Histórico (mais recentes primeiro) | 200 | |
| GET | `/api/relatorios/{id}` | Um relatório do histórico | 200 | 404 |
| GET | `/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31` | Históricos no período (datas ISO, inclusivo) | 200 | 400 parâmetro ausente, data inválida ou fim antes do início |

O POST faz o mesmo ciclo do `GeradorRelatorio` da Sprint 3:

1. lê os sensores dos trechos que implementam `MonitoravelViaIoT` (trechos úmidos crescem +11 cm);
2. ordena a fila pelo nível de vegetação;
3. classifica cada trecho na sua faixa e executa a intervenção recomendada (gravando em `MT_INTERVENCAO`);
4. grava contagens + resumo em `MT_RELATORIO_PRIORIDADE`.

A resposta do POST inclui a `fila` completa; o histórico guarda só as contagens e o resumo,
como na tabela da Sprint 3.

### Formato de erro

Todo erro passa pelo `GlobalExceptionHandler` (`@RestControllerAdvice`) e sai no mesmo formato:

```json
{
  "timestamp": "2026-10-22T21:45:13",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "caminho": "/api/equipes",
  "campos": {
    "nome": "nome é obrigatório",
    "quantidadeOperadores": "equipe precisa de pelo menos um operador"
  }
}
```

| Status | Quando |
|---|---|
| 200 OK | GET e PUT com sucesso |
| 201 Created | POST com sucesso (com header `Location`) |
| 204 No Content | DELETE com sucesso |
| 400 Bad Request | Bean Validation, regra de negócio, JSON inválido, parâmetro ausente ou com tipo errado |
| 404 Not Found | id inexistente |
| 409 Conflict | duplicidade (nome de equipe, faixa de trecho) ou registro ainda referenciado |

## Exemplos cURL

```bash
# Trechos
curl -i http://localhost:8080/api/trechos
curl -i http://localhost:8080/api/trechos/1
curl -i "http://localhost:8080/api/trechos?nivelMinimo=40"
curl -i -X POST http://localhost:8080/api/trechos -H "Content-Type: application/json" \
     -d '{"rodovia":"SP-280","kmInicial":30.0,"kmFinal":36.5,"nivelVegetacao":52.0,"perfil":"UMIDO","idEquipe":1}'
curl -i -X PUT http://localhost:8080/api/trechos/6 -H "Content-Type: application/json" \
     -d '{"rodovia":"SP-280","kmInicial":30.0,"kmFinal":37.0,"nivelVegetacao":55.0,"perfil":"UMIDO","idEquipe":1}'
curl -i -X DELETE http://localhost:8080/api/trechos/6

# Equipes
curl -i http://localhost:8080/api/equipes
curl -i -X POST http://localhost:8080/api/equipes -H "Content-Type: application/json" \
     -d '{"nome":"Equipe Delta","quantidadeOperadores":5}'
curl -i -X PUT http://localhost:8080/api/equipes/4 -H "Content-Type: application/json" \
     -d '{"nome":"Equipe Delta","quantidadeOperadores":7}'
curl -i -X DELETE http://localhost:8080/api/equipes/4

# Intervenções
curl -i -X POST http://localhost:8080/api/intervencoes -H "Content-Type: application/json" \
     -d '{"idTrecho":1,"tipo":"ROCADA"}'
curl -i "http://localhost:8080/api/intervencoes?tipo=ROCADA"
curl -i -X PUT http://localhost:8080/api/intervencoes/3 -H "Content-Type: application/json" \
     -d '{"idEquipe":2,"custoEstimado":7500.00}'
curl -i -X DELETE http://localhost:8080/api/intervencoes/3

# Relatórios
curl -i -X POST http://localhost:8080/api/relatorios
curl -i http://localhost:8080/api/relatorios
curl -i "http://localhost:8080/api/relatorios/periodo?inicio=2026-10-01&fim=2026-10-31"
```

No PowerShell use `curl.exe` (o `curl` de lá é um alias do `Invoke-WebRequest`) e escape as
aspas do JSON, ou use o Postman.

## Registro das requisições

[`docs/requisicoes.md`](docs/requisicoes.md) tem **28 requisições executadas de verdade** —
CRUD das três entidades, derived queries, os três endpoints de relatório e os casos de erro
(400, 404, 409) — com o comando, o status e o corpo devolvido.

## Queries Spring Data

Nenhum repository tem `@Query` nem SQL: tudo é derived query, gerada pelo nome do método.

| Repository | Método | SQL que o Hibernate gera (resumido) |
|---|---|---|
| `TrechoRodoviaRepository` | `findByNivelVegetacaoGreaterThanEqualOrderByNivelVegetacaoDesc(Double)` | `... WHERE nr_nivel_vegetacao >= ? ORDER BY nr_nivel_vegetacao DESC` |
| | `findByPerfil(String)` | `... WHERE tp_perfil = ?` |
| | `findByIdentificacaoRodoviaIgnoreCase(String)` | `... WHERE UPPER(ds_rodovia) = UPPER(?)` |
| | `countByEquipeResponsavelId(Long)` | `SELECT COUNT(*) ... WHERE id_equipe = ?` |
| `EquipeManutencaoRepository` | `findByNomeIgnoreCase(String)` | `... WHERE UPPER(nm_equipe) = UPPER(?)` |
| | `findByQuantidadeOperadoresGreaterThanEqual(Integer)` | `... WHERE qt_operadores >= ?` |
| `IntervencaoRepository` | `findByTipo(String)` | `... WHERE tp_intervencao = ?` |
| | `findByTrechoIdOrderByDataExecucaoDesc(Long)` | `... WHERE id_trecho = ? ORDER BY dt_execucao DESC` |
| | `findByDataExecucaoBetween(LocalDateTime, LocalDateTime)` | `... WHERE dt_execucao BETWEEN ? AND ?` |
| `RelatorioPrioridadeRepository` | `findByGeradoEmBetweenOrderByGeradoEmDesc(...)` | `... WHERE dt_geracao BETWEEN ? AND ? ORDER BY dt_geracao DESC` |

Com `spring.jpa.show-sql=true` o SQL real aparece no console a cada requisição.

As checagens de "já existe?" usam `countBy...` em vez de `existsBy...`: o Hibernate 7 monta o
`existsBy` com `FETCH FIRST 1 ROWS ONLY`, sintaxe que só existe a partir do Oracle 12c, e o Oracle
da FIAP usado na Sprint 3 é 11g. `COUNT(*)` roda em qualquer versão.

> O campo se chama `nivelVegetacao` desde a Sprint 1, por isso o método é
> `findByNivelVegetacaoGreaterThanEqual...` e não `findByNivelVegetacaoCmGreaterThanEqual`.

## Bônus implementados

- **DTOs** (`dto/`): as entities nunca saem pela API. Requests e responses são `record`s.
- **Bean Validation**: `@Valid` nos controllers, `@NotBlank`, `@NotNull`, `@Positive`,
  `@PositiveOrZero`, `@Size`, `@Pattern` nos requests.
- **`@RestControllerAdvice`**: `GlobalExceptionHandler` com formato único de erro.
- **`@SpringBootTest` + `MockMvc`**: `MotivaApiTest`, 17 cenários cobrindo 200/201/204/400/404/409.

## Boas práticas de Git

- Commits incrementais por camada (setup → entities → repositories → DTOs/erros → services →
  controllers → testes → documentação). Veja `git log --oneline`.
- `.gitignore` cobre `target/`, arquivos de IDE (`.idea/`, `.vscode/`, `*.iml`...) e arquivos de segredo locais.
- Nenhuma senha no repositório: só placeholders e variáveis de ambiente.

## Perguntas de reflexão

**1. Por que o Repository é uma interface e não uma classe? Quem escreve a implementação e quando?**

Porque a gente só declara *o que* quer (salvar, buscar por id, buscar por tipo) e deixa o *como*
para o framework. Ninguém do grupo escreve a implementação: na inicialização da aplicação o
Spring Data cria um proxy em tempo de execução para cada interface que estende `JpaRepository`,
usando a `SimpleJpaRepository` para o CRUD básico e montando as consultas dos métodos derivados
a partir dos nomes. Esse proxy vira um bean e é injetado pelo construtor nos services. Como o
service depende só da interface, dá para trocar a implementação (ou usar um mock nos testes)
sem mexer nele.

**2. O pattern DAO da Sprint 3 "morreu" na migração ou só mudou de forma?**

Mudou de forma. A ideia do DAO — isolar o acesso a dados atrás de uma interface para que o resto
do sistema não saiba que existe SQL — continua exatamente igual: o `TrechoService` chama
`trechoRepository.findById(id)` do mesmo jeito que o `GeradorRelatorio` chamava
`trechoDAO.buscarPorId(id)`. O que sumiu foi o trabalho braçal: os 4 DAOs da Sprint 3 somavam 626 linhas de
`PreparedStatement`, `ResultSet`, conversão coluna→atributo e try-with-resources; os 4
repositories daqui somam 75, quase tudo assinatura de método. O Repository é um DAO que o
framework escreve pra gente.

**3. Por que a validação de `nivelVegetacao >= 0` deve ficar no Service e não no Controller?**

Porque é regra de negócio, não detalhe de HTTP. O Controller é só uma das portas de entrada; o
mesmo `TrechoService` pode ser chamado por outro service (o relatório, por exemplo), por um job
agendado, por uma fila ou por um teste. Se a regra estivesse no Controller, qualquer outro caminho
conseguiria gravar um trecho com nível negativo. No nosso projeto ela está no `TrechoService` e na
própria entity (`TrechoRodovia.atualizarDados`, a mesma regra da Sprint 1), e o banco ainda tem o
`CHECK (nr_nivel_vegetacao >= 0)` como última barreira. O `@PositiveOrZero` do DTO é só um filtro
rápido na entrada para devolver uma mensagem amigável — ele não substitui a regra do domínio.

**4. No JDBC puro vocês escreviam SQL. Onde está o SQL do `findByTipo()`? Quem o gerou?**

Não está em nenhum arquivo do projeto. Na subida da aplicação o Spring Data lê o nome do método,
quebra em partes (`find` + `By` + `Tipo`), confere que `tipo` é um atributo da entity
`Intervencao` e monta uma consulta JPQL equivalente a `SELECT i FROM Intervencao i WHERE i.tipo = ?1`.
Na hora da execução o Hibernate traduz essa JPQL para o SQL do Oracle usando o mapeamento das
anotações (`@Table(name = "MT_INTERVENCAO")`, `@Column(name = "TP_INTERVENCAO")`), gerando algo como
`SELECT ... FROM mt_intervencao i WHERE i.tp_intervencao = ?`. Dá para ver o SQL gerado no
console com `spring.jpa.show-sql=true`. Se o nome do método tiver um atributo que não existe, a
aplicação nem sobe — o erro aparece na inicialização, não em produção.
