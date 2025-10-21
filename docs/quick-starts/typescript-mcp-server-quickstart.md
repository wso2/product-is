# Secure MCP servers with TypeScript SDK

This guide shows you how to secure Model Context Protocol (MCP) servers implemented with the TypeScript MCP SDK using Asgardeo authentication.

## What you'll learn

- Create a TypeScript MCP server
- Install Asgardeo MCP authentication packages
- Configure Asgardeo for user authentication
- Define secure MCP tools with authentication

## Prerequisites

Before you begin, ensure you have the following:

- About 20 minutes
- [Asgardeo account](https://wso2.com/asgardeo/docs/get-started/create-asgardeo-account/)
- Node.js 16.x or later installed on your system
- TypeScript knowledge
- A JavaScript package manager (npm, yarn, or pnpm)
- [Claude Desktop](https://claude.ai/download) (for testing)
- Your preferred text editor or IDE

## Sample code

The complete working example for this guide is available in the [TypeScript MCP Auth Quick Start repository](https://github.com/ngsanthosh/typescript-mcp-auth-quickstart).

## Configure an application in Asgardeo

1. Sign in to the [Asgardeo console](https://console.asgardeo.io/).
2. Navigate to **Applications** > **New Application**.
3. Select **Single Page Application** and complete the wizard with the following details:

   - **Name**: TypeScriptMCPServer
   - **Authorized redirect URL**: `http://localhost:47926/oauth/callback`

   !!! note
       The authorized redirect URL determines where Asgardeo sends users after successful authentication. For this guide, use `http://localhost:47926/oauth/callback` as the authorized redirect URL.

4. From the **Protocol** tab of the registered application, copy the **Client ID**.
5. From the **Info** tab, copy the **Token Endpoint** URL.

You'll need these values in later steps.

## Create a TypeScript MCP server

### Initialize the project

Create a new directory for your project and initialize it:

```bash
mkdir typescript-mcp-auth-quickstart
cd typescript-mcp-auth-quickstart
npm init -y
```

### Configure TypeScript

Create a `tsconfig.json` file with the following configuration:

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ES2020",
    "moduleResolution": "node",
    "esModuleInterop": true,
    "strict": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "outDir": "./dist",
    "rootDir": "./src"
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules"]
}
```

### Set up package.json

Update your `package.json` to include the required dependencies and scripts:

```json
{
  "name": "typescript-mcp-auth-quickstart",
  "version": "1.0.0",
  "type": "module",
  "main": "dist/index.js",
  "scripts": {
    "build": "tsc",
    "start": "node dist/index.js",
    "dev": "tsc && node dist/index.js"
  },
  "dependencies": {
    "@asgardeo/mcp-express": "^0.2.1",
    "@modelcontextprotocol/sdk": "^1.20.1",
    "dotenv": "^16.4.7",
    "express": "^4.21.2",
    "zod": "^3.24.1"
  },
  "devDependencies": {
    "@types/express": "^5.0.0",
    "@types/node": "^22.10.5",
    "typescript": "^5.7.3"
  }
}
```

Install the dependencies:

```bash
npm install
```

### Create the server

Create a `src` directory and add an `index.ts` file:

```bash
mkdir src
```

Add the following code to `src/index.ts`:

```typescript
import { randomUUID } from 'node:crypto';
import { McpAuthServer } from '@asgardeo/mcp-express';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StreamableHTTPServerTransport } from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import { isInitializeRequest } from '@modelcontextprotocol/sdk/types.js';
import { config } from 'dotenv';
import express, { Express, Request, Response } from 'express';
import { z } from 'zod';

config();

const app: Express = express();

// Initialize without authentication
app.use(express.json());

// Session management
interface TransportMap {
  [sessionId: string]: {
    lastAccess: number;
    transport: StreamableHTTPServerTransport;
  };
}

const transports: TransportMap = {};
const SESSION_TIMEOUT_MS: number = 30 * 60 * 1000;

const isSessionExpired = (lastAccessTime: number): boolean =>
  Date.now() - lastAccessTime > SESSION_TIMEOUT_MS;

// Clean up expired sessions periodically
setInterval(() => {
  const now = Date.now();
  for (const [sessionId, session] of Object.entries(transports)) {
    if (isSessionExpired(session.lastAccess)) {
      console.log(`Session ${sessionId} expired, cleaning up...`);
      session.transport.close();
      delete transports[sessionId];
    }
  }
}, SESSION_TIMEOUT_MS);

// MCP endpoint without authentication
app.post('/mcp', async (req: Request, res: Response): Promise<void> => {
  try {
    const sessionId: string | undefined = req.headers['mcp-session-id'] as
      | string
      | undefined;
    let transport: StreamableHTTPServerTransport;

    if (sessionId && transports[sessionId]) {
      if (isSessionExpired(transports[sessionId].lastAccess)) {
        console.log(`Session ${sessionId} expired`);
        transports[sessionId].transport.close();
        delete transports[sessionId];

        res.status(401).json({
          jsonrpc: '2.0',
          error: {
            code: -32000,
            message: 'Session expired',
          },
          id: req.body?.id || null,
        });
        return;
      }

      transport = transports[sessionId].transport;
      transports[sessionId].lastAccess = Date.now();
    } else if (!sessionId && isInitializeRequest(req.body)) {
      transport = new StreamableHTTPServerTransport({
        sessionIdGenerator: () => randomUUID(),
        onsessioninitialized: (newSessionId) => {
          transports[newSessionId] = {
            lastAccess: Date.now(),
            transport,
          };
          console.log(`New session created: ${newSessionId}`);
        },
        enableJsonResponse: true,
      });

      transport.onclose = () => {
        if (transport.sessionId && transports[transport.sessionId]) {
          console.log(`Transport closed for session: ${transport.sessionId}`);
          delete transports[transport.sessionId];
        }
      };

      const server: McpServer = new McpServer({
        name: 'typescript-auth-quickstart',
        version: '1.0.0',
      });

      // Define a simple whoami tool
      server.registerTool(
        'whoami',
        {
          title: 'Who Am I',
          description: 'Returns information about the current user',
          inputSchema: {},
          outputSchema: {
            user: z.string(),
            authenticated: z.boolean(),
          },
        },
        async () => {
          const output = {
            user: 'Not authenticated',
            authenticated: false,
          };

          return {
            content: [
              {
                type: 'text' as const,
                text: JSON.stringify(output, null, 2),
              },
            ],
            structuredContent: output,
          };
        }
      );

      await server.connect(transport);
    } else {
      res.status(400).json({
        jsonrpc: '2.0',
        error: {
          code: -32000,
          message: 'Bad Request: No valid session ID provided',
        },
        id: req.body?.id || null,
      });
      return;
    }

    await transport.handleRequest(req, res, req.body);
  } catch (error) {
    console.error('Error handling MCP request:', error);
    if (!res.headersSent) {
      res.status(500).json({
        jsonrpc: '2.0',
        error: {
          code: -32603,
          message: 'Internal server error',
        },
        id: null,
      });
    }
  }
});

const PORT: string | number = process.env.PORT || 3000;
app.listen(PORT, (): void => {
  console.log(`MCP server running on http://localhost:${PORT}/mcp`);
});
```

### Create environment file

Create a `.env.example` file:

```bash
PORT=3000
BASE_URL=https://api.asgardeo.io/t/<your-organization>
```

Copy this to `.env` and update with your Asgardeo organization name.

## Run and test without authentication

Build and start the server:

```bash
npm run dev
```

### Configure Claude Desktop

1. Open Claude Desktop.
2. Click **Claude Desktop** > **Settings** > **Developer**.
3. Click **Edit Config**. This opens the `claude_desktop_config.json` file.
4. Add the following configuration:

```json
{
  "mcpServers": {
    "typescript-auth-server": {
      "command": "npx",
      "args": ["mcp-remote@latest", "http://localhost:3000/mcp"]
    }
  }
}
```

5. Restart Claude Desktop.

### Test the server

In Claude Desktop, use the following prompt:

```
Who am I?
```

You should see the response indicating the user is not authenticated.

## Add authentication to the MCP server

Now, let's secure the server with Asgardeo authentication.

### Update the server code

Replace the content of `src/index.ts` with the following authenticated version:

```typescript
import { randomUUID } from 'node:crypto';
import { McpAuthServer } from '@asgardeo/mcp-express';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StreamableHTTPServerTransport } from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import { isInitializeRequest } from '@modelcontextprotocol/sdk/types.js';
import { config } from 'dotenv';
import express, { Express, Request, Response } from 'express';
import { z } from 'zod';

config();

const app: Express = express();

// Initialize Asgardeo MCP authentication
const mcpAuthServer = new McpAuthServer({
  baseUrl: process.env.BASE_URL as string,
});

app.use(express.json());
app.use(mcpAuthServer.router());

// Session management
interface TransportMap {
  [sessionId: string]: {
    lastAccess: number;
    transport: StreamableHTTPServerTransport;
  };
}

const transports: TransportMap = {};
const SESSION_TIMEOUT_MS: number = 30 * 60 * 1000;

const isSessionExpired = (lastAccessTime: number): boolean =>
  Date.now() - lastAccessTime > SESSION_TIMEOUT_MS;

// Clean up expired sessions periodically
setInterval(() => {
  const now = Date.now();
  for (const [sessionId, session] of Object.entries(transports)) {
    if (isSessionExpired(session.lastAccess)) {
      console.log(`Session ${sessionId} expired, cleaning up...`);
      session.transport.close();
      delete transports[sessionId];
    }
  }
}, SESSION_TIMEOUT_MS);

// Protected MCP endpoint with authentication
app.post(
  '/mcp',
  mcpAuthServer.protect(),
  async (req: Request, res: Response): Promise<void> => {
    try {
      const sessionId: string | undefined = req.headers['mcp-session-id'] as
        | string
        | undefined;
      let transport: StreamableHTTPServerTransport;

      if (sessionId && transports[sessionId]) {
        if (isSessionExpired(transports[sessionId].lastAccess)) {
          console.log(`Session ${sessionId} expired`);
          transports[sessionId].transport.close();
          delete transports[sessionId];

          res.status(401).json({
            jsonrpc: '2.0',
            error: {
              code: -32000,
              message: 'Session expired',
            },
            id: req.body?.id || null,
          });
          return;
        }

        transport = transports[sessionId].transport;
        transports[sessionId].lastAccess = Date.now();
      } else if (!sessionId && isInitializeRequest(req.body)) {
        // Extract bearer token from authorization header
        let bearerToken: string | undefined;
        const authHeader: string | undefined = req.headers
          .authorization as string | undefined;

        if (authHeader && authHeader.toLowerCase().startsWith('bearer ')) {
          bearerToken = authHeader.substring(7);
          console.log('Bearer token captured for new session.');
        }

        transport = new StreamableHTTPServerTransport({
          sessionIdGenerator: () => randomUUID(),
          onsessioninitialized: (newSessionId) => {
            transports[newSessionId] = {
              lastAccess: Date.now(),
              transport,
            };
            console.log(`New session created: ${newSessionId}`);
          },
          enableJsonResponse: true,
        });

        transport.onclose = () => {
          if (transport.sessionId && transports[transport.sessionId]) {
            console.log(`Transport closed for session: ${transport.sessionId}`);
            delete transports[transport.sessionId];
          }
        };

        const server: McpServer = new McpServer({
          name: 'typescript-auth-quickstart',
          version: '1.0.0',
        });

        // Define whoami tool with authentication info
        server.registerTool(
          'whoami',
          {
            title: 'Who Am I',
            description: 'Returns authenticated user information',
            inputSchema: {},
            outputSchema: {
              user: z.string(),
              authenticated: z.boolean(),
              tokenPresent: z.boolean(),
            },
          },
          async () => {
            // In a production environment, you would validate the token
            // and extract user information from it
            const output = {
              user: 'Authenticated user',
              authenticated: true,
              tokenPresent: !!bearerToken,
            };

            return {
              content: [
                {
                  type: 'text' as const,
                  text: JSON.stringify(output, null, 2),
                },
              ],
              structuredContent: output,
            };
          }
        );

        await server.connect(transport);
      } else {
        res.status(400).json({
          jsonrpc: '2.0',
          error: {
            code: -32000,
            message: 'Bad Request: No valid session ID provided',
          },
          id: req.body?.id || null,
        });
        return;
      }

      await transport.handleRequest(req, res, req.body);
    } catch (error) {
      console.error('Error handling MCP request:', error);
      if (!res.headersSent) {
        res.status(500).json({
          jsonrpc: '2.0',
          error: {
            code: -32603,
            message: 'Internal server error',
          },
          id: null,
        });
      }
    }
  }
);

const PORT: string | number = process.env.PORT || 3000;
app.listen(PORT, (): void => {
  console.log(`MCP server with auth running on http://localhost:${PORT}/mcp`);
});
```

### Rebuild and restart

```bash
npm run dev
```

## Test the MCP server with authentication

### Update Claude Desktop configuration

1. Open Claude Desktop.
2. Click **Claude Desktop** > **Settings** > **Developer**.
3. Click **Edit Config**.
4. Update the configuration with your Client ID:

```json
{
  "mcpServers": {
    "typescript-auth-server": {
      "command": "npx",
      "args": [
        "mcp-remote@latest",
        "http://localhost:3000/mcp",
        "--static-oauth-client-info",
        "{ \"client_id\": \"<your-client-id>\"}",
        "--static-oauth-client-metadata",
        "{ \"scope\": \"openid profile email\"}"
      ]
    }
  }
}
```

Replace `<your-client-id>` with the Client ID you copied from Asgardeo.

5. Restart Claude Desktop.

### Authenticate and test

When you open Claude Desktop, it redirects you to the Asgardeo sign-in page.

!!! important
    Create a test user in Asgardeo by following the [Onboard a single user guide](https://wso2.com/asgardeo/docs/guides/users/manage-users/#onboard-single-user) to try the authentication feature.

After successful authentication, you can see the tools exposed by the MCP server.

Test with the following prompt:

```
Who am I?
```

You should now see your authenticated user information with the authentication status set to `true`.

## Summary

You've successfully created a secure TypeScript MCP server with Asgardeo authentication. You learned how to:

- Set up a TypeScript MCP server using the official SDK
- Integrate Asgardeo authentication using the `@asgardeo/mcp-express` package
- Configure Claude Desktop to work with authenticated MCP servers
- Test the authentication flow end-to-end

## What's next?

- Explore [Advanced MCP Authentication Patterns](https://wso2.com/asgardeo/docs/guides/agentic-ai/mcp/mcp-server-authorization/)
- Learn about [MCP Client Registration](https://wso2.com/asgardeo/docs/guides/agentic-ai/mcp/register-mcp-client-app/)
- Implement [Role-Based Access Control for MCP Tools](https://wso2.com/asgardeo/docs/guides/authorization/api-authorization/api-authorization/)
