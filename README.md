# MCP Java SDK
[![Build Status](https://github.com/modelcontextprotocol/java-sdk/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/modelcontextprotocol/java-sdk/actions/workflows/continuous-integration.yml)

Set of projects that provide Java SDK integration for the [Model Context Protocol](https://modelcontextprotocol.org/docs/concepts/architecture). 
It enables Java applications to interact with AI models and tools through a standardized interface, supporting both synchronous and asynchronous communication patterns.

## 📚 Reference Documentation

For comprehensive guides and API documentation, visit the [MCP Java SDK Reference Documentation](docs/docs/overview.mdx).

<img src="docs/images/java-mcp-client-architecture.jpg" width="600">

<img src="docs/images/java-mcp-server-architecture.jpg" width="600">

## Projects

### MCP 

Java implementation of the Model Context Protocol specification. It includes:
- Synchronous and asynchronous [MCP Client](docs/docs/mcp-client.mdx) and [MCP Server](docs/docs/mcp-server.mdx) implementations
- Standard MCP operations support (tool discovery, resource management, prompt handling, structured logging). Support for request and notification handling.
- [Stdio](https://spec.modelcontextprotocol.io/specification/2024-11-05/basic/transports/#stdio) and [SSE](https://spec.modelcontextprotocol.io/specification/2024-11-05/basic/transports/#http-with-sse) transport implementations. 

##### Core Transports
- Stdio-based (`StdioClientTransport`, `StdioServerTransport`) for process-based communication
- Java HttpClient-based SSE client (`HttpClientSseClientTransport`) for HTTP streaming
- Servlet-based SSE server (`HttpServletSseServerTransport`) for HTTP SSE Server streaming using traditional Servlet API


### MCP Spring

Optional SSE Transports implementations usin gthe Spring Webflux or WebMvc libraries.

- [WebFlux SSE Transport](https://github.com/modelcontextprotocol/java-sdk/tree/main/mcp-spring/mcp-spring-webflux) - Reactive HTTP streaming with Spring WebFlux (Client & Server)
- [WebMvc SSE Transport](https://github.com/modelcontextprotocol/java-sdk/tree/main/mcp-spring/mcp-spring-webmvc) - Spring MVC based HTTP SSE transport (Server only). 
You can use the core `HttpClientSseClientTransport` transport as a SSE client.


## Installation

Add the following dependencies to your Maven project:

```xml
<!-- Core MCP -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
</dependency>

<!-- Optional: WebFlux SSE transport -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webflux</artifactId>
</dependency>

<!-- Optional: WebMVC SSE transport -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webmvc</artifactId>
</dependency>
```

## Example Demos

Explore these MCP examples in the [spring-ai-examples/model-context-protocol](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol) repository:

- [SQLite Simple](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/sqlite/simple) - Demonstrates LLM integration with a database
- [SQLite Chatbot](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/sqlite/chatbot) - Interactive chatbot with SQLite database interaction
- [Filesystem](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/filesystem) - Enables LLM interaction with local filesystem folders and files
- [Brave](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/brave) - Enables natural language interactions with Brave Search, allowing you to perform internet searches.
- [Theme Park API Example](https://github.com/habuma/spring-ai-examples/tree/main/spring-ai-mcp) - Shows how to create an MCP server and client with Spring AI, exposing Theme Park API tools
- [Http SSE Client + WebMvc SSE Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-webmvc-server) - Showcases how to create and use MCP WebMvc servers and HttpClient clients with different capabilities.
- [WebFlux SSE Client + WebFlux SSE Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-webflux-server) - Showcases how to create and use MCP WebFlux servers and clients with different capabilities
- [HttpClient SSE Client + Servlet SSE Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-servlet-server) - Showcases how to create and use MCP Servlet SSE Server and HttpClient SSE Client with different capabilities


## Development

- Building from Source

```bash
mvn clean install
```

- Running Tests

```bash
mvn test
```


## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Submit a Pull Request

## Team

- Christian Tzolov
- Dariusz Jędrzejczyk

## Links

- [GitHub Repository](https://github.com/modelcontextprotocol/java-sdk)
- [Issue Tracker](https://github.com/modelcontextprotocol/java-sdk/issues)
- [CI/CD](https://github.com/modelcontextprotocol/java-sdk/actions)

## License

This project is licensed under the [MIT](LICENSE).
