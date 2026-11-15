# Algebraic Equation Solver

A Spring Boot REST API that stores algebraic equations as **postfix (expression) trees**, lists them back as readable text, and evaluates them for any variable values.

Storage is in memory, so everything resets when the app restarts.

## What it does

1. **Store** an equation like `3x + 2y - z`. It is converted to postfix order (`3 x * 2 y * + z -`) and kept as a tree where every operator is a parent and the numbers/variables are its children.
2. **List** the stored equations. The text is rebuilt by walking each tree.
3. **Evaluate** a stored equation by passing a value for each variable.

## Tech

- Java 17, Spring Boot 3.3
- springdoc-openapi (Swagger UI)
- JUnit 5 + MockMvc for tests
- Maven

## Run it

You need JDK 17+ and Maven.

```bash
mvn spring-boot:run
```

The app starts on http://localhost:8080 (set the `PORT` environment variable to change it).

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Run the tests

```bash
mvn test
```

### Run with Docker

```bash
docker build -t equation-solver .
docker run -p 8080:8080 equation-solver
```

## API

### Store an equation

`POST /api/equations/store` -> `201 Created`

```json
{ "equation": "3x + 2y - z" }
```

```json
{
  "message": "Equation stored successfully",
  "equationId": "1",
  "postfix": "3 x * 2 y * + z -"
}
```

### List equations

`GET /api/equations` -> `200 OK`

```json
{
  "equations": [
    { "equationId": "1", "equation": "3x + 2y - z", "postfix": "3 x * 2 y * + z -" },
    { "equationId": "2", "equation": "x^2 + y^2 - 4", "postfix": "x 2 ^ y 2 ^ + 4 -" }
  ]
}
```

### Evaluate an equation

`POST /api/equations/{equationId}/evaluate` -> `200 OK`

```json
{ "variables": { "x": 2, "y": 3, "z": 1 } }
```

```json
{
  "equationId": "1",
  "equation": "3x + 2y - z",
  "variables": { "x": 2, "y": 3, "z": 1 },
  "result": 11
}
```

(3*2 + 2*3 - 1 = 11. The example in the assignment PDF shows 10, which looks like a typo.)

### Errors

Every error has the same shape:

```json
{ "status": 400, "error": "Invalid equation", "message": "Unexpected character '$' at position 3" }
```

| Situation | Status |
|---|---|
| Bad syntax: empty equation, unknown character, unbalanced brackets, two operators in a row, missing operand | 400 |
| Missing variable values, bad variable name, a value that is not a number, malformed JSON | 400 |
| Equation id does not exist | 404 |
| Division by zero, result is not a real number (e.g. `(-8)^0.5`), result too large | 422 |

## How it works

The code is split into small pieces, each with one job:

| Class | Job |
|---|---|
| `Tokenizer` | Splits the text into numbers, variables, operators and brackets |
| `InfixToPostfixConverter` | Shunting-yard algorithm: reorders tokens to postfix and validates the structure |
| `ExpressionTreeBuilder` | Reads the postfix tokens with a stack and builds the tree |
| `NumberNode`, `VariableNode`, `UnaryNode`, `BinaryNode` | The tree nodes. Each knows how to evaluate itself and print itself |
| `EquationService` | Keeps equations in memory, hands out ids, checks variable values |
| `EquationController` | The three REST endpoints |
| `GlobalExceptionHandler` | Turns exceptions into JSON errors with the right status |

The parsing and tree classes do not use Spring, so they are tested with plain unit tests.

**Evaluating** a tree is a recursive walk: a number returns itself, a variable looks up its value, and an operator node evaluates its children first and then applies the operator.

**Rebuilding the text** walks the tree too. Brackets are added only where the tree shape needs them, so `(a+b)*c` comes back with brackets but `a+(b*c)` comes back as `a + b * c`.

## Assumptions

- Supported operators: `+  -  *  /  ^` and brackets. `^` is right-associative (`2^3^2` = 2^9) and binds tighter than unary minus, so `-x^2` means -(x^2).
- Variables are **single letters** (case-sensitive). Because of that, `3x` means 3 times x and `xy` means x times y. Multiplication can be written with or without `*`, including `2(x+1)`.
- Numbers can have decimals (`1.5`, `.5`). Scientific notation (`1e5`) is not supported.
- The stored text shown by `GET /api/equations` is rebuilt from the tree, so spacing is normalised (`3x+2y-z` comes back as `3x + 2y - z`).
- Extra variables in an evaluate request are ignored. Missing ones are an error, and all missing names are reported together.
- Equations are limited to 500 characters.
- Only expressions are accepted, not equations with `=`.
- The store is thread-safe, but data is lost on restart (in-memory storage was part of the requirements).

## Testing

`mvn test` runs:

- **Converter tests** - precedence, associativity, brackets, unary minus, hidden multiplication, and every kind of invalid input
- **Tree tests** - tree shape, evaluation, rebuilding infix text, edge cases like division by zero and overflow
- **Service tests** - ids, storage order, missing/invalid variables, unknown ids
- **Controller tests** - the real HTTP requests and responses with MockMvc

A Postman collection is in `postman/equation-solver.postman_collection.json`. Import it and set the `baseUrl` variable.

## Deploying (Render)

1. Push this project to a public GitHub repo.
2. On Render: New -> Web Service -> pick the repo, set Runtime to **Docker**.
3. Deploy. Render sets `PORT` automatically and the app picks it up.
4. Open the service URL, it redirects to the Swagger UI.
