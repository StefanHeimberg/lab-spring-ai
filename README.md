# Lab - Spring AI

https://docs.spring.io/spring-ai/reference/index.html

https://github.com/spring-ai-community/awesome-spring-ai

## Setup

```bash
export OPENAI_API_BASE=<api-base-url>
export OPENAI_API_KEY=<api-key>
```

## MCP Tools auflisten

|                             | SSE                        | Streamable HTTP                           |
| --------------------------- | -------------------------- | ----------------------------------------- |
| MCP-Transport               | älter                      | neuer                                     |
| Verbindung                  | dauerhafte SSE-Verbindung  | normale HTTP-Requests, optional Streaming |
| Client → Server             | separater HTTP-Endpoint    | derselbe MCP-Endpoint                     |
| Server → Client             | SSE                        | HTTP Response / SSE                       |
| Session                     | typischerweise `sessionId` | kann ebenfalls Session verwenden          |
| Firewall/Proxy              | manchmal problematischer   | meist einfacher                           |
| Stateless möglich           | eher unpraktisch           | **ja**                                    |
| Empfehlung für neue Systeme | ❌                          | **✅**                                     |


### Transport über SSE

**SSE-Verbindung öffnen**

```bash
curl -N http://localhost:3001/sse
```

```bash
event: endpoint
data: /message?sessionId=abc123
```

Die sessionId bzw. den kompletten /message?...-Pfad brauchst du für den nächsten Schritt.

**MCP initialisieren**

Session offen lassen und in zweitem Terminal:

```bash
curl -X POST "http://localhost:3001/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "curl",
        "version": "1.0"
      }
    }
  }'
```

Die Antwort erscheint in Terminal 1

Initialisieren bestätigen

```bash
curl -X POST "http://localhost:3001/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "notifications/initialized"
  }'
```

**Tools auflisten**

Die Antwort erscheint in Terminal 1

```bash
curl -X POST "http://localhost:3001/message?sessionId=abc123" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

### Transport über Streamable-HTTP

**Streamable-HTTP-Verbindung öffnen**

```bash
curl -i -X POST http://localhost:3002/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-06-18",
      "capabilities": {},
      "clientInfo": {
        "name": "curl",
        "version": "1.0"
      }
    }
  }'
```

MCP Session ID ist nun im HTTP Header enthalten:

```
Mcp-Session-Id: abc123
```

**MCP initialisieren**

anders als bei SSE muss hier nur noch die initialisierung bestätigt werden

```bash
curl -i -X POST http://localhost:3002/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: abc123" \
  -d '{
    "jsonrpc": "2.0",
    "method": "notifications/initialized"
  }'
```

Sollte mit HTTP/1.1 202 Accepted bestätig werden

**Tools auflisten**

```bash
curl -i -X POST http://localhost:3002/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: abc123" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```