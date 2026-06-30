# Sistema de Hospedagem 🏝️

Trabalho Prático da disciplina **Programação Modular** — Bacharelado em Engenharia de Software (PUC Minas).

Sistema de Informação modular com **API REST** para gerenciamento de hospedagens em Maraú-BA: residências, quartos, clientes, reservas, aluguéis, pagamentos e recibos.

---

## 🧱 Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3 |
| Persistência | Spring Data JPA / Hibernate |
| Banco (produção) | MySQL |
| Banco (testes) | H2 em memória |
| Build | Maven (via Maven Wrapper `mvnw`) |
| Documentação da API | OpenAPI / Swagger UI |
| Segurança de senha | BCrypt |
| Testes | JUnit 5 + Mockito |

---

## 🏗️ Arquitetura em camadas

```
controller  ->  service  ->  repository  ->  model (entidades JPA)
     |             |
    dto         regras de negócio (cálculo de diárias, disponibilidade)
```

- **`model`** — entidades de domínio (`Residencia`, `Quarto`, `Cliente`, `Aluguel`, `Pagamento`, `Reserva`) e enums.
- **`repository`** — interfaces Spring Data (padrão *Repository*).
- **`service`** — regras de negócio. Núcleo: `CalculadoraDiaria`, `DisponibilidadeService`.
- **`controller`** — endpoints REST.
- **`dto`** — objetos de transferência (records) de entrada/saída.
- **`exception`** — exceções de negócio + tratamento global (`GlobalExceptionHandler`).
- **`recibo`** — geração do recibo/formulário de aluguel.

### Padrões de projeto aplicados

| Padrão | Onde |
|---|---|
| **Strategy** | `AdicionalDiaria` e implementações (`AdicionalArCondicionado`, `AdicionalHidromassagem`) — cada item adicional é uma estratégia de cálculo plugável. |
| **Builder** | `Recibo.Builder` — construção do recibo/formulário de aluguel. |
| **Factory** | `QuartoFactory` — criação de quartos por tipo. |
| **Repository** | Interfaces `*Repository` (Spring Data JPA). |
| **DTO** | Pacote `dto` — isola a API das entidades. |
| **Singleton** | Beans gerenciados pelo container Spring (`@Service`, `@Component`, `@Bean`). |

---

## 📐 Regras de negócio implementadas

1. **Cálculo de diárias (início às 12h)** — `CalculadoraDiaria.calcularNumeroDiarias`:
   - conta a diferença em dias entre entrada e saída;
   - **saída após as 12h adiciona uma diária**;
   - **entrada após as 12h conta como diária completa** (mínimo de 1 diária).
2. **Valor da diária não é informado diretamente** — é calculado: `valor base do quarto + adicionais` (ar-condicionado e/ou hidromassagem). Os valores dos adicionais são configuráveis em `application.properties`.
3. **Quarto não pode ser alugado se ocupado no período** — `DisponibilidadeService` verifica sobreposição com aluguéis e reservas ativas.
4. **Reservas futuras** — entidade `Reserva` com status (PENDENTE, CONFIRMADA, CANCELADA, CONCLUIDA).
5. **Todo aluguel gera um pagamento associado** — criado junto com o aluguel (status PENDENTE).
6. **Histórico de aluguéis por residência** — `GET /api/alugueis/residencia/{id}`.
7. **Formulário/recibo de aluguel** — `GET /api/alugueis/{id}/recibo`.

---

## ▶️ Como executar

> Não é necessário instalar o Maven — use o wrapper `./mvnw` (Linux/Mac) ou `mvnw.cmd` (Windows).

### 1. Banco de dados (MySQL)

Tenha um MySQL rodando em `localhost:3306`. O schema `hospedagem` é criado automaticamente. Ajuste usuário/senha em [`application.properties`](src/main/resources/application.properties) ou via variáveis de ambiente:

```bash
# Windows (PowerShell)
$env:DB_USERNAME="root"; $env:DB_PASSWORD="suasenha"
```

### 2. Rodar a aplicação

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.
Documentação interativa: **http://localhost:8080/swagger-ui.html**

> Dica: defina `hospedagem.seed.enabled=true` para popular o banco com dados de exemplo na primeira execução.

### 3. Rodar os testes

```bash
mvnw.cmd test      # Windows
./mvnw test        # Linux / Mac
```

Os testes usam **H2 em memória** — não exigem o MySQL.

---

## 🌐 Principais endpoints

| Recurso | Método | Rota |
|---|---|---|
| Clientes | POST / GET / PUT / DELETE | `/api/clientes` |
| Autenticação | POST | `/api/auth/login` |
| Residências | POST / GET / PUT / DELETE | `/api/residencias` |
| Quartos | POST / GET / PUT / DELETE | `/api/quartos` |
| Disponibilidade de quarto | GET | `/api/quartos/{id}/disponibilidade?dataEntrada=...&dataSaida=...` |
| Reservas | POST / GET | `/api/reservas` (+ `/{id}/confirmar`, `/{id}/cancelar`) |
| Aluguéis | POST / GET / DELETE | `/api/alugueis` |
| Recibo | GET | `/api/alugueis/{id}/recibo` |
| Histórico por residência | GET | `/api/alugueis/residencia/{id}` |
| Pagamentos | GET | `/api/pagamentos` (+ `/{id}/pagar`, `/{id}/cancelar`) |

### Exemplo — realizar um aluguel

```http
POST /api/alugueis
Content-Type: application/json

{
  "quartoId": 1,
  "clienteId": 1,
  "dataEntrada": "2026-07-01T14:00:00",
  "dataSaida": "2026-07-03T11:00:00"
}
```

Datas no formato **ISO-8601** (`yyyy-MM-ddTHH:mm:ss`).

---

## 📂 Estrutura do projeto

```
src/main/java/br/com/pucminas/hospedagem
├── HospedagemApplication.java
├── config/         # PasswordEncoder, OpenAPI, seed de dados
├── controller/     # endpoints REST
├── dto/            # records de request/response
├── exception/      # exceções + handler global
├── model/          # entidades JPA + enums
├── recibo/         # recibo (Builder)
├── repository/     # repositórios Spring Data
└── service/        # regras de negócio
    ├── calculo/    # cálculo de diárias (Strategy)
    └── factory/    # criação de quartos (Factory)
```
