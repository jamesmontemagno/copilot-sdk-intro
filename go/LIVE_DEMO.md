# Copilot SDK Live Demo: Go

Run from the repository root:

```powershell
cd go
go mod tidy
go run .
```

## Act One: Hello World

Open `main.go`.

1. Replace the client placeholder with:

```go
client := copilot.NewClient(&copilot.ClientOptions{LogLevel: "error"})
if err := client.Start(context.Background()); err != nil {
	panic(err)
}
defer client.Stop()
```

2. Replace `isAuthenticated := false` with:

```go
isAuthenticated := true
```

Say: "The Go SDK starts against the signed-in Copilot runtime. If this machine is not signed in, run `copilot auth login`."

3. Replace the session placeholder with:

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

4. Under `// Step 5: Send the first message.`, type:

```go
if err := streamResponse(session, "Hello world! In one sentence, say what the Copilot SDK helps a Go app do."); err != nil {
	panic(err)
}
```

## Act Two: Podcast Assistant

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
```

Replace the prompt:

```go
prompt := fmt.Sprintf("Use get_github_podcast_episode for the episode titled %q. Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.", selectedEpisode.Title)
if err := streamResponse(session, prompt); err != nil {
	panic(err)
}
```

Expected cues: model list, episode list, tool start, approval, tool completion, then streamed launch copy.
