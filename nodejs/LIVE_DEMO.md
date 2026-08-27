# The GitHub Podcast Live Demo: Node.js

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Install dependencies from the repository root:

```powershell
cd nodejs
npm ci
```

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
});
```

Say: "The session is the conversation. I chose the model, enabled streaming, and the event handler below already prints each text fragment as it arrives."

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

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message."

Run:

```powershell
npm start
```

Expected output: a streamed one-sentence answer, followed by the `session.idle` event completing the program.

## Act Two: Turn It Into A Podcast Assistant

After Hello World, use the prewritten helpers in `src` to turn the same session into a grounded podcast workflow.

### 1. Let The Presenter Choose

Add imports:

```typescript
import { selectModel } from "./model-selector.js";
import { episodeTool, latestEpisodesTool, getLatestEpisodes, pickEpisode } from "./github-podcast-tools.js";
import { permissionPrompt } from "./permission-prompt.js";
```

After `await client.start();`, add:

```typescript
const selectedModel = await selectModel(client, model);
const latestEpisodes = await getLatestEpisodes();
const selectedEpisode = await pickEpisode(latestEpisodes);
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes."

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

Say: "The model does not get arbitrary access to my application. I grant two narrow, typed capabilities and remain the approval point before a tool executes."

Say: "The system message uses replace, not append. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

### 3. Replace The Prompt

Replace the prompt with:

```typescript
prompt: `Use get_github_podcast_episode for the episode titled "${selectedEpisode.title}". Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.`,
```

Say: "The agent decides to call the episode tool, I approve the read-only lookup, and its response is grounded in the official feed rather than invented details."

Run the completed flow:

```powershell
npm start
```

Expected milestones: model selection, ten-episode selection, `[Tool call started]`, approval prompt, `[Tool call complete]`, then streamed launch copy.
