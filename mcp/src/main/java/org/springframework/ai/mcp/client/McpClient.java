/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.mcp.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.McpSchema.ClientCapabilities;
import org.springframework.ai.mcp.spec.McpSchema.CreateMessageRequest;
import org.springframework.ai.mcp.spec.McpSchema.CreateMessageResult;
import org.springframework.ai.mcp.spec.McpSchema.Implementation;
import org.springframework.ai.mcp.spec.McpSchema.Root;
import org.springframework.ai.mcp.spec.McpTransport;
import org.springframework.ai.mcp.util.Assert;

/**
 * Factory class for creating Model Context Protocol (MCP) clients. MCP is a protocol that enables
 * AI models to interact with external tools and resources through a standardized interface.
 * 
 * <p>This class serves as the main entry point for establishing connections with MCP servers,
 * implementing the client-side of the MCP specification. The protocol follows a client-server
 * architecture where:
 * <ul>
 * <li>The client (this implementation) initiates connections and sends requests
 * <li>The server responds to requests and provides access to tools and resources
 * <li>Communication occurs through a transport layer (e.g., stdio, SSE) using JSON-RPC 2.0
 * </ul>
 *
 * <p>The class provides factory methods to create either:
 * <ul>
 * <li>{@link McpAsyncClient} for non-blocking operations with CompletableFuture responses
 * <li>{@link McpSyncClient} for blocking operations with direct responses
 * </ul>
 *
 * <p>Example of creating a basic client:
 * <pre>{@code
 * McpClient.using(transport)
 *     .requestTimeout(Duration.ofSeconds(5))
 *     .sync(); // or .async()
 * }</pre>
 *
 * <p>Example with advanced configuration:
 * <pre>{@code
 * McpClient.using(transport)
 *     .requestTimeout(Duration.ofSeconds(10))
 *     .capabilities(new ClientCapabilities(...))
 *     .clientInfo(new Implementation("My Client", "1.0.0"))
 *     .roots(new Root("file://workspace", "Workspace Files"))
 *     .toolsChangeConsumer(tools -> System.out.println("Tools updated: " + tools))
 *     .async();
 * }</pre>
 *
 * <p>The client supports:
 * <ul>
 * <li>Tool discovery and invocation
 * <li>Resource access and management
 * <li>Prompt template handling
 * <li>Real-time updates through change consumers
 * <li>Custom sampling strategies
 * </ul>
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 * @see McpAsyncClient
 * @see McpSyncClient
 * @see McpTransport
 */
public interface McpClient {

	/**
	 * Start building an MCP client with the specified transport layer.
	 * The transport layer handles the low-level communication between
	 * client and server using protocols like stdio or Server-Sent Events (SSE).
	 *
	 * @param transport The transport layer implementation for MCP communication.
	 *                  Common implementations include {@code StdioClientTransport}
	 *                  for stdio-based communication and {@code SseClientTransport}
	 *                  for SSE-based communication.
	 * @return A new builder instance for configuring the client
	 * @throws IllegalArgumentException if transport is null
	 */
	public static Builder using(McpTransport transport) {
		return new Builder(transport);
	}

	/**
	 * Builder class for creating and configuring MCP clients. This class follows
	 * the builder pattern to provide a fluent API for setting up clients with
	 * custom configurations.
	 *
	 * <p>The builder supports configuration of:
	 * <ul>
	 * <li>Transport layer for client-server communication
	 * <li>Request timeouts for operation boundaries
	 * <li>Client capabilities for feature negotiation
	 * <li>Client implementation details for version tracking
	 * <li>Root URIs for resource access
	 * <li>Change notification handlers for tools, resources, and prompts
	 * <li>Custom message sampling logic
	 * </ul>
	 */
	public static class Builder {

		private final McpTransport transport;

		private Duration requestTimeout = Duration.ofSeconds(20); // Default timeout

		private ClientCapabilities capabilities;

		private Implementation clientInfo = new Implementation("Spring AI MCP Client", "0.3.1");

		private Map<String, Root> roots = new HashMap<>();

		private List<Consumer<List<McpSchema.Tool>>> toolsChangeConsumers = new ArrayList<>();

		private List<Consumer<List<McpSchema.Resource>>> resourcesChangeConsumers = new ArrayList<>();

		private List<Consumer<List<McpSchema.Prompt>>> promptsChangeConsumers = new ArrayList<>();

		private Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

		private Builder(McpTransport transport) {
			Assert.notNull(transport, "Transport must not be null");
			this.transport = transport;
		}

		/**
		 * Sets the duration to wait for server responses before timing out requests.
		 * This timeout applies to all requests made through the client, including
		 * tool calls, resource access, and prompt operations.
		 *
		 * @param requestTimeout The duration to wait before timing out requests.
		 *                       Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if requestTimeout is null
		 */
		public Builder requestTimeout(Duration requestTimeout) {
			Assert.notNull(requestTimeout, "Request timeout must not be null");
			this.requestTimeout = requestTimeout;
			return this;
		}

		/**
		 * Sets the client capabilities that will be advertised to the server during
		 * connection initialization. Capabilities define what features the client supports,
		 * such as tool execution, resource access, and prompt handling.
		 *
		 * @param capabilities The client capabilities configuration. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if capabilities is null
		 */
		public Builder capabilities(ClientCapabilities capabilities) {
			Assert.notNull(capabilities, "Capabilities must not be null");
			this.capabilities = capabilities;
			return this;
		}

		/**
		 * Sets the client implementation information that will be shared with the server
		 * during connection initialization. This helps with version compatibility and
		 * debugging.
		 *
		 * @param clientInfo The client implementation details including name and version.
		 *                   Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if clientInfo is null
		 */
		public Builder clientInfo(Implementation clientInfo) {
			Assert.notNull(clientInfo, "Client info must not be null");
			this.clientInfo = clientInfo;
			return this;
		}

		/**
		 * Sets the root URIs that this client can access. Roots define the base URIs
		 * for resources that the client can request from the server. For example,
		 * a root might be "file://workspace" for accessing workspace files.
		 *
		 * @param roots A list of root definitions. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if roots is null
		 */
		public Builder roots(List<Root> roots) {
			Assert.notNull(roots, "Roots must not be null");
			for (Root root : roots) {
				this.roots.put(root.uri(), root);
			}
			return this;
		}

		/**
		 * Sets the root URIs that this client can access, using a varargs parameter
		 * for convenience. This is an alternative to {@link #roots(List)}.
		 *
		 * @param roots An array of root definitions. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if roots is null
		 * @see #roots(List)
		 */
		public Builder roots(Root... roots) {
			Assert.notNull(roots, "Roots must not be null");
			for (Root root : roots) {
				this.roots.put(root.uri(), root);
			}
			return this;
		}

		/**
		 * Sets a custom sampling handler for processing message creation requests.
		 * The sampling handler can modify or validate messages before they are sent
		 * to the server, enabling custom processing logic.
		 *
		 * @param samplingHandler A function that processes message requests and returns
		 *                        results. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if samplingHandler is null
		 */
		public Builder sampling(Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {
			Assert.notNull(samplingHandler, "Sampling handler must not be null");
			this.samplingHandler = samplingHandler;
			return this;
		}

		/**
		 * Adds a consumer to be notified when the available tools change. This allows
		 * the client to react to changes in the server's tool capabilities, such as
		 * tools being added or removed.
		 *
		 * @param toolsChangeConsumer A consumer that receives the updated list of
		 *                           available tools. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolsChangeConsumer is null
		 */
		public Builder toolsChangeConsumer(Consumer<List<McpSchema.Tool>> toolsChangeConsumer) {
			Assert.notNull(toolsChangeConsumer, "Tools change consumer must not be null");
			this.toolsChangeConsumers.add(toolsChangeConsumer);
			return this;
		}

		/**
		 * Adds a consumer to be notified when the available resources change. This allows
		 * the client to react to changes in the server's resource availability, such as
		 * files being added or removed.
		 *
		 * @param resourcesChangeConsumer A consumer that receives the updated list of
		 *                               available resources. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourcesChangeConsumer is null
		 */
		public Builder resourcesChangeConsumer(Consumer<List<McpSchema.Resource>> resourcesChangeConsumer) {
			Assert.notNull(resourcesChangeConsumer, "Resources change consumer must not be null");
			this.resourcesChangeConsumers.add(resourcesChangeConsumer);
			return this;
		}

		/**
		 * Adds a consumer to be notified when the available prompts change. This allows
		 * the client to react to changes in the server's prompt templates, such as
		 * new templates being added or existing ones being modified.
		 *
		 * @param promptsChangeConsumer A consumer that receives the updated list of
		 *                             available prompts. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if promptsChangeConsumer is null
		 */
		public Builder promptsChangeConsumer(Consumer<List<McpSchema.Prompt>> promptsChangeConsumer) {
			Assert.notNull(promptsChangeConsumer, "Prompts change consumer must not be null");
			this.promptsChangeConsumers.add(promptsChangeConsumer);
			return this;
		}

		/**
		 * Builds a synchronous MCP client that provides blocking operations.
		 * Synchronous clients wait for each operation to complete before returning,
		 * making them simpler to use but potentially less performant for
		 * concurrent operations.
		 *
		 * @return A new instance of {@link McpSyncClient} configured with this
		 *         builder's settings
		 */
		public McpSyncClient sync() {
			return new McpSyncClient(async());
		}

		/**
		 * Builds an asynchronous MCP client that provides non-blocking operations.
		 * Asynchronous clients return CompletableFuture objects immediately,
		 * allowing for concurrent operations and reactive programming patterns.
		 *
		 * @return A new instance of {@link McpAsyncClient} configured with this
		 *         builder's settings
		 */
		public McpAsyncClient async() {
			return new McpAsyncClient(transport, requestTimeout, clientInfo, capabilities, roots, toolsChangeConsumers,
					resourcesChangeConsumers, promptsChangeConsumers, samplingHandler);
		}

	}

}
