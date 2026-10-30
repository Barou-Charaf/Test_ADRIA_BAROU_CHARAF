# Chatbot Service — Parts E1 and E2

Spring AI chatbot backed by the OpenAI API and connected to the banking services through MCP.

## Prerequisites

Inject the API key in the terminal that will start `chatbot-service`:

```bash
export OPENAI_API_KEY="your-api-key"
export OPENAI_MODEL="gpt-4o-mini"
mvn spring-boot:run
```

`OPENAI_MODEL` is optional and defaults to `gpt-4o-mini`. The API key is read from the process
environment and is never copied into source code or centralized configuration.

## API

`POST /api/chat`

```json
{
  "message": "Hello"
}
```

## MCP banking tools

The chatbot discovers tools from two Streamable HTTP MCP servers:

- `account-servie` at `http://localhost:8081/mcp`
- `transaction-service` at `http://localhost:8082/mcp`

Available tools consult accounts and balances, list account transactions, create a transaction,
and perform a transfer. Every tool delegates to the existing business service layer, so the same
amount, status, currency and balance rules apply to REST, GraphQL and MCP.

Start `account-servie` and `transaction-service` before `chatbot-service`. The MCP endpoints are
unauthenticated and are intended only for local/internal use in the current assignment phase.
After adding or changing MCP configuration, restart both business services so `/mcp` is registered.

Telegram integration is intentionally deferred to the next approved Part E step.
