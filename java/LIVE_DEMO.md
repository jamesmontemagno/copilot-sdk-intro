# The GitHub Podcast Live Demo: Java

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Compile dependencies from the repository root:

```powershell
cd java
mvn compile
```

## Demo Pitch

Say: "We are building a Podcast Agent for The GitHub Podcast. It will let us choose a real episode, retrieve verified metadata from the official RSS feed, and turn those facts into sponsor-safe social copy."

Say: "We will begin with the smallest possible Copilot SDK conversation, then give it a purpose, an identity, and application-owned tools."

## Act One: Hello World

Start with `src\main\java\demo\CopilotSdkLiveDemo.java`. It deliberately has named placeholders for `client`, `isAuthenticated`, and `session`.

### 1. Start The Client

Replace `CopilotClient client;` with:

```java
var client = new CopilotClient();
client.start().get();
```

Say: "The client is my connection to the Copilot runtime. I start it explicitly, so the application owns its lifecycle."

### 2. Check Authentication

Replace `boolean isAuthenticated = false;` with:

```java
boolean isAuthenticated = client.getAuthStatus().get().isAuthenticated();
```

Say: "Before creating a session, I can ask the runtime whether this machine is signed in."

### 3. Create The Session

Replace the session placeholder with:

```java
var session = client.createSession(new SessionConfig()
        .setModel(MODEL)
        .setStreaming(true)
        .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)).get();
```

Add imports for `com.github.copilot.rpc.MessageOptions` and `com.github.copilot.rpc.PermissionHandler`.

Say: "The session is the conversation. I chose the model and enabled streaming."

Say: "The handler is here even though Hello World has no tools: the runtime asks the application to decide on every permission request, and a session with no handler leaves them pending, so the run stalls instead of failing. Approve-all is the honest choice for an act with nothing to approve."

### 4. Send Hello World

Under `// Step 5: Send the first message.`, type:

```java
var response = session.sendAndWait(new MessageOptions()
        .setPrompt("Hello world! In one sentence, say what the Copilot SDK helps a Java app do."))
        .get();
if (response != null) {
    System.out.println(response.getData().content());
}
session.close();
client.close();
```

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message. Once this loop works, we can evolve it into our Podcast Agent."

Run this Hello World checkpoint now from the `java` folder:

```powershell
mvn compile exec:java
```

Expected output: a one-sentence answer returned by `sendAndWait`.

## Act Two: Turn It Into A Podcast Agent

After Hello World, use the prewritten helper classes to turn the same session into a grounded podcast workflow.

Say: "The conversation works. Now we will turn it into our Podcast Agent: a focused assistant that can research a selected GitHub Podcast episode and prepare launch copy without inventing facts."

### 1. Let The Presenter Choose

Add imports for `com.github.copilot.rpc.SystemMessageConfig`, `com.github.copilot.SystemMessageMode`,
and `java.util.List`. The `PermissionHandler` import is no longer used once `PermissionPrompt` replaces it below.

After the client starts:

```java
String selectedModel = ModelSelector.select(client, MODEL);
var latestEpisodes = GitHubPodcastEpisodeTool.latest();
var selectedEpisode = GitHubPodcastEpisodeTool.pick(latestEpisodes);
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes. That selection becomes the Podcast Agent's assignment."

### 2. Give The Session Capabilities

Create the tools:

```java
var episodeTool = GitHubPodcastEpisodeTool.episodeTool();
var latestEpisodesTool = GitHubPodcastEpisodeTool.latestEpisodesTool();
```

Create the session:

```java
var session = client.createSession(new SessionConfig()
        .setModel(selectedModel)
        .setStreaming(true)
        .setTools(List.of(episodeTool, latestEpisodesTool))
        .setAvailableTools(List.of("get_github_podcast_episode", "get_latest_github_podcast_episodes"))
        .setOnPermissionRequest(PermissionPrompt.HANDLER)
        .setSystemMessage(new SystemMessageConfig()
                .setMode(SystemMessageMode.REPLACE)
                .setContent("You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.")))
        .get();
```

Say: "The model does not get arbitrary access to my application. I grant two narrow, typed capabilities, allowlist them by name, and swap Hello World's approve-all handler for `PermissionPrompt`, which denies anything that is not one of these tools and asks me on stdin before one runs."

Say: "These tools are what make this an agent rather than a generic chatbot: it can take action against a trusted data source that my application controls."

Say: "The system message uses REPLACE, not APPEND. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

### 3. Replace The Prompt

Replace the prompt:

```java
var response = session.sendAndWait(new MessageOptions()
        .setPrompt("Use get_github_podcast_episode for the episode titled \"" + selectedEpisode.title() + "\""
                + ". Return exactly a social headline and a sponsor-safe post under 280 characters. "
                + "Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."))
        .get();
if (response != null) {
    System.out.println(response.getData().content());
}
```

Say: "The agent decides to call the episode tool, I approve the read-only lookup, and its response is grounded in the official feed rather than invented details."

Run the completed Podcast Agent now from the `java` folder:

```powershell
mvn compile exec:java
```

Expected milestones: model selection, ten-episode selection, tool execution, approval prompt, then grounded launch copy.
