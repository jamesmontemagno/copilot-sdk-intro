# Copilot SDK Live Demo: Java

Run from the repository root:

```powershell
cd java
mvn compile exec:java
```

## Act One: Hello World

Open `src\main\java\demo\CopilotSdkLiveDemo.java`.

1. Replace `CopilotClient client;` with:

```java
var client = new CopilotClient();
client.start().get();
```

2. Replace `boolean isAuthenticated = false;` with:

```java
boolean isAuthenticated = true;
```

3. Replace the session placeholder with:

```java
var session = client.createSession(new SessionConfig()
        .setModel(MODEL)
        .setStreaming(true)).get();
```

4. Under `// Step 5: Send the first message.`, type:

```java
session.sendAndWait(new MessageOptions()
        .setPrompt("Hello world! In one sentence, say what the Copilot SDK helps a Java app do."))
        .get();
session.close();
client.close();
```

## Act Two: Podcast Assistant

Add imports for `MessageOptions`, `PermissionHandler`, and `List`.

After the client starts:

```java
String selectedModel = ModelSelector.select(client, MODEL);
var latestEpisodes = MergeConflictEpisodeTool.latest();
var selectedEpisode = MergeConflictEpisodeTool.pick(latestEpisodes);
```

Create the tools:

```java
var episodeTool = MergeConflictEpisodeTool.episodeTool();
var latestEpisodesTool = MergeConflictEpisodeTool.latestEpisodesTool();
```

Create the session:

```java
var session = client.createSession(new SessionConfig()
        .setModel(selectedModel)
        .setStreaming(true)
        .setTools(List.of(episodeTool, latestEpisodesTool))
        .setAvailableTools(List.of("get_merge_conflict_episode", "get_latest_merge_conflict_episodes"))
        .setOnPermissionRequest(PermissionHandler.APPROVE_ALL))
        .get();
```

Say: "This Java track keeps the same typed-tool boundary and approval flow; the grounding instruction stays in the prompt if this SDK version does not expose a system-message helper."

Replace the prompt:

```java
session.sendAndWait(new MessageOptions()
        .setPrompt("Use get_merge_conflict_episode for episode " + selectedEpisode.episodeNumber()
                + ". Return exactly a social headline and a sponsor-safe post under 280 characters. "
                + "Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."))
        .get();
```

Expected cues: model list, episode list, tool start, approval, tool completion, then launch copy.
