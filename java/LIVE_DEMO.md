# The GitHub Podcast Live Demo: Java

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Compile dependencies from the repository root:

```powershell
cd java
mvn compile
```

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
        .setStreaming(true)).get();
```

Say: "The session is the conversation. I chose the model and enabled streaming."

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

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message."

Run:

```powershell
mvn compile exec:java
```

Expected output: a one-sentence answer returned by `sendAndWait`.

## Act Two: Turn It Into A Podcast Assistant

After Hello World, use the prewritten helper classes to turn the same session into a grounded podcast workflow.

### 1. Let The Presenter Choose

Add imports for `com.github.copilot.rpc.MessageOptions`, `com.github.copilot.rpc.PermissionHandler`,
`com.github.copilot.rpc.SystemMessageConfig`, `com.github.copilot.SystemMessageMode`, and `java.util.List`.

After the client starts:

```java
String selectedModel = ModelSelector.select(client, MODEL);
var latestEpisodes = GitHubPodcastEpisodeTool.latest();
var selectedEpisode = GitHubPodcastEpisodeTool.pick(latestEpisodes);
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes."

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
        .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
        .setSystemMessage(new SystemMessageConfig()
                .setMode(SystemMessageMode.REPLACE)
                .setContent("You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.")))
        .get();
```

Say: "The model gets two narrow, typed application capabilities. This Java sample uses the SDK's approve-all handler for those known local tools; replace it with a custom handler when the host must prompt for each call."

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

Say: "The agent decides to call the episode tool, and its response is grounded in the official feed rather than invented details."

Run the completed flow:

```powershell
mvn compile exec:java
```

Expected milestones: model selection, ten-episode selection, tool execution, then grounded launch copy.
