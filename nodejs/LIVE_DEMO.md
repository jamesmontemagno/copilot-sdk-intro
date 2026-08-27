# Copilot SDK Live Demo: Node.js

Run from the repository root:

```powershell
cd nodejs
npm install
npm start
```

## Act One: Hello World

Open `src\index.ts`. It has placeholders for `client`, authentication, `session`, and the first prompt.

### 1. Start the client

Replace `let client: CopilotClient;` with:

```typescript
const client = new CopilotClient();
await client.start();
```

Say: "The client is my Node.js app's connection to the Copilot runtime."

### 2. Replace the auth placeholder

Replace `const isAuthenticated = false;` with:

```typescript
const isAuthenticated = true;
```

Say: "The JavaScript SDK starts against the authenticated Copilot runtime. If this machine is not signed in, run `copilot auth login`."

### 3. Create a streaming session

Replace `let session: CopilotSession;` with:

```typescript
const session = await client.createSession({
  model,
  streaming: true,
});
```

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

## Act Two: Podcast Assistant

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

Replace the prompt with:

```typescript
prompt: `Use get_github_podcast_episode for the episode titled "${selectedEpisode.title}". Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.`,
```

Expected cues: model list, episode list, tool start, approval prompt, tool completion, then streamed launch copy.
