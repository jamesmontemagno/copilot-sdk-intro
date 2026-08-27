# The GitHub Podcast Live Demo: Go

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Download dependencies from the repository root:

```powershell
cd go
go mod tidy
```

## Demo Pitch

Say: "We are building a Podcast Agent for The GitHub Podcast. It will let us choose a real episode, retrieve verified metadata from the official RSS feed, and turn those facts into sponsor-safe social copy."

Say: "We will begin with the smallest possible Copilot SDK conversation, then give it a purpose, an identity, and application-owned tools."

## Act One: Hello World

Start with `main.go`. It deliberately has named placeholders for `client`, `isAuthenticated`, and `session`. Leave the event handler in place.

### 1. Start The Client

Replace the client placeholder with:

```go
client := copilot.NewClient(&copilot.ClientOptions{LogLevel: "error"})
if err := client.Start(context.Background()); err != nil {
	panic(err)
}
defer client.Stop()
```

Say: "The client is my connection to the Copilot runtime. I start it explicitly, so the application owns its lifecycle."

### 2. Check Authentication

Replace `isAuthenticated := false` with:

```go
authStatus, err := client.GetAuthStatus(context.Background())
if err != nil {
	panic(err)
}
isAuthenticated := authStatus.IsAuthenticated
```

Say: "Before creating a session, I can ask the runtime whether this machine is signed in."

### 3. Create The Session

Replace the session placeholder with:

```go
session, err := client.CreateSession(context.Background(), &copilot.SessionConfig{
	Model:     preferredModel,
	Streaming: copilot.Bool(true),
})
if err != nil {
	panic(err)
}
defer session.Disconnect()
```

Say: "The session is the conversation. I chose the model, enabled streaming, and the event handler below prints each text fragment as it arrives."

### 4. Send Hello World

Under `// Step 5: Send the first message.`, type:

```go
if err := streamResponse(session, "Hello world! In one sentence, say what the Copilot SDK helps a Go app do."); err != nil {
	panic(err)
}
```

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message. Once this loop works, we can evolve it into our Podcast Agent."

Run this Hello World checkpoint now from the `go` folder:

```powershell
go run .
```

Expected output: a streamed one-sentence answer, followed by `SendAndWait` completing the turn.

## Act Two: Turn It Into A Podcast Agent

After Hello World, use the prewritten helpers in `helpers.go` to turn the same session into a grounded podcast workflow.

Say: "The conversation works. Now we will turn it into our Podcast Agent: a focused assistant that can research a selected GitHub Podcast episode and prepare launch copy without inventing facts."

### 1. Let The Presenter Choose

After starting the client:

```go
selectedModel, err := selectModel(context.Background(), client, preferredModel)
if err != nil {
	panic(err)
}
latestEpisodes, err := getLatestEpisodes()
if err != nil {
	panic(err)
}
selectedEpisode, err := pickEpisode(latestEpisodes)
if err != nil {
	panic(err)
}
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes. That selection becomes the Podcast Agent's assignment."

### 2. Give The Session Capabilities

Create the tools:

```go
episodeTool := createEpisodeTool()
latestEpisodesTool := createLatestEpisodesTool()
```

Create the session:

```go
session, err := client.CreateSession(context.Background(), &copilot.SessionConfig{
	Model:               selectedModel,
	Streaming:           copilot.Bool(true),
	Tools:               []copilot.Tool{episodeTool, latestEpisodesTool},
	AvailableTools:      []string{"get_github_podcast_episode", "get_latest_github_podcast_episodes"},
	OnPermissionRequest: copilot.PermissionHandler.ApproveAll,
	SystemMessage: &copilot.SystemMessageConfig{
		Mode:    "replace",
		Content: "You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.",
	},
})
if err != nil {
	panic(err)
}
defer session.Disconnect()
```

Say: "The model gets two narrow, typed application capabilities. This Go sample uses the SDK's approve-all handler for those known local tools; replace it with a custom handler when the host must prompt for each call."

Say: "These tools are what make this an agent rather than a generic chatbot: it can take action against a trusted data source that my application controls."

Say: "The system message uses replace, not append. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

### 3. Replace The Prompt

Replace the prompt:

```go
prompt := fmt.Sprintf("Use get_github_podcast_episode for the episode titled %q. Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.", selectedEpisode.Title)
if err := streamResponse(session, prompt); err != nil {
	panic(err)
}
```

Say: "The agent decides to call the episode tool, and its response is grounded in the official feed rather than invented details."

Run the completed Podcast Agent now from the `go` folder:

```powershell
go run .
```

Expected milestones: model selection, ten-episode selection, `[Tool call started]`, `[Tool call complete]`, then streamed launch copy.
