# The GitHub Podcast Live Demo: Node.js

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Install dependencies from the repository root:

```powershell
cd nodejs
npm ci
```

## Demo Pitch

Say: "We are building a Podcast Agent for The GitHub Podcast. It will let us choose a real episode, retrieve verified metadata from the official RSS feed, and turn those facts into sponsor-safe social copy."

Say: "We will begin with the smallest possible Copilot SDK conversation, then give it a purpose, an identity, and application-owned tools."

## Act One: Hello World

Open `src\index.ts`. It has placeholders for `client`, authentication, `session`, and the first prompt.

### 1. Start the client

Replace `let client: CopilotClient;` with:

```typescript
const client = new CopilotClient();
await client.start();
```

Say: "The client is my connection to the Copilot runtime. I start it explicitly, so the application owns its lifecycle."

### 2. Check Authentication

Replace `const isAuthenticated = false;` with:

```typescript
const isAuthenticated = (await client.getAuthStatus()).isAuthenticated;
```

Say: "Before creating a session, I can ask the runtime whether this machine is signed in."

### 3. Create a streaming session

Replace `let session: CopilotSession;` with:

```typescript
const session = await client.createSession({
  model,
  streaming: true,
  onPermissionRequest: approveAll,
});
```

Add `approveAll` to the SDK import at the top of the file:

```typescript
import { CopilotClient, approveAll, type CopilotSession } from "@github/copilot-sdk";
```

Say: "The session is the conversation. I chose the model, enabled streaming, and the event handler below already prints each text fragment as it arrives."

Say: "The handler is here even though Hello World has no tools: the runtime asks the application to decide on every permission request, and a session with no handler leaves them pending, so the run stalls instead of failing. Approve-all is the honest choice for an act with nothing to approve."

### 4. Send hello world

Under `// Step 5: Send the first message.`, type:

```typescript
await new Promise<void>((resolve, reject) => {
  const unsubscribe = session.on((event) => {
    if (event.type === "session.idle") {
      unsubscribe();
      resolve();
    } else if (event.type === "session.error") {
      reject(new Error(event.data.message));
    }
  });

  void session.send({
    prompt: "Hello world! In one sentence, say what the Copilot SDK helps a Node.js app do.",
  }).catch(reject);
});

await session.disconnect();
await client.stop();
```

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message. Once this loop works, we can evolve it into our Podcast Agent."

Run this Hello World checkpoint now from the `nodejs` folder:

```powershell
npm start
```

Expected output: a streamed one-sentence answer, followed by the `session.idle` event completing the program.

## Act Two: Turn It Into A Podcast Agent

After Hello World, use the prewritten helpers in `src` to turn the same session into a grounded podcast workflow.

Say: "The conversation works. Now we will turn it into our Podcast Agent: a focused assistant that can research a selected GitHub Podcast episode and prepare launch copy without inventing facts."

### 1. Let The Presenter Choose

Add imports:

```typescript
import { selectModel } from "./model-selector.js";
import { episodeTool, latestEpisodesTool, getLatestEpisodes, pickEpisode } from "./github-podcast-tools.js";
import { permissionPrompt } from "./permission-prompt.js";
```

Drop `approveAll` from the SDK import, since `permissionPrompt` replaces it below:

```typescript
import { CopilotClient, type CopilotSession } from "@github/copilot-sdk";
```

After `await client.start();`, add:

```typescript
const selectedModel = await selectModel(client, model);
const latestEpisodes = await getLatestEpisodes();
const selectedEpisode = await pickEpisode(latestEpisodes);
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes. That selection becomes the Podcast Agent's assignment."

### 2. Give The Session Capabilities

Replace the session config with:

```typescript
const session = await client.createSession({
  model: selectedModel,
  streaming: true,
  tools: [episodeTool, latestEpisodesTool],
  availableTools: ["get_github_podcast_episode", "get_latest_github_podcast_episodes"],
  onPermissionRequest: permissionPrompt,
  systemMessage: {
    mode: "replace",
    content: "You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.",
  },
});
```

Say: "The model does not get arbitrary access to my application. I grant two narrow, typed capabilities, allowlist them by name, and swap Hello World's approve-all handler for one that prompts me, so I remain the approval point before a tool executes."

Say: "These tools are what make this an agent rather than a generic chatbot: it can take action against a trusted data source that my application controls."

Say: "The system message uses replace, not append. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

### 3. Replace The Prompt

Replace the prompt with:

```typescript
prompt: `Use get_github_podcast_episode for the episode titled "${selectedEpisode.title}". Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.`,
```

Say: "The agent decides to call the episode tool, I approve the read-only lookup, and its response is grounded in the official feed rather than invented details."

Run the completed Podcast Agent now from the `nodejs` folder:

```powershell
npm start
```

Expected milestones: model selection, ten-episode selection, `[Tool call started]`, approval prompt, `[Tool call complete]`, then streamed launch copy.
