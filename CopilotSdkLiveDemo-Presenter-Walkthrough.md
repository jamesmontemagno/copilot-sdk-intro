# Copilot SDK Live Presenter Walkthrough

This is a standalone live-demo script for the `dotnet` sample in this repo, adapted from:

`https://github.com/jamesmontemagno/podcast-metadata-generator/tree/vslive-demo/samples/CopilotSdkLiveDemo`

The walkthrough starts with a small streaming "hello world" and grows it into a grounded podcast assistant that uses typed .NET tools, user approval, and live RSS data from the Merge Conflict podcast.

## Demo Goal

Show that a .NET app can use the GitHub Copilot SDK to:

1. Start and own the Copilot runtime lifecycle.
2. Check whether the user is authenticated.
3. Create a streaming Copilot session.
4. Send a prompt and stream the answer.
5. Add narrow, typed application tools.
6. Keep the host app in control of tool approval.
7. Ground model output in trusted application data.

## Presenter Setup

Before the session:

1. Clone or open this repository.
2. Confirm the sample runs from the repository root:

```powershell
dotnet run --project dotnet
```

3. If authentication is required, run:

```powershell
copilot auth login
```

4. If the SDK cannot download its bundled runtime because of network restrictions, use the local Copilot CLI executable:

```powershell
$env:COPILOT_CLI_BINARY_PATH = (Get-Command copilot.exe).Source
dotnet run --project dotnet
```

## Starting Point

Open `dotnet\Program.cs`.

The starter file intentionally has placeholders:

```csharp
CopilotClient client;
var isAuthenticated = false;
CopilotSession session = null!;
// Step 5: Send the first message.
```

Leave the existing streaming event handler and `await complete.Task;` in place. The event handler is the demo's visual payoff: attendees see tokens, tool calls, and completion events as they happen.

## Act One: Streaming Hello World

Target time: 5 minutes.

### Step 1: Start the Copilot client

At:

```csharp
CopilotClient client;
```

Type:

```csharp
client = new CopilotClient();
await client.StartAsync();
```

Say:

> The client is my .NET app's connection to the Copilot runtime. I start it explicitly, so my app owns when the runtime starts and stops.

### Step 2: Check authentication

Replace:

```csharp
var isAuthenticated = false;
```

With:

```csharp
var isAuthenticated = (await client.GetAuthStatusAsync()).IsAuthenticated;
```

Say:

> Before I create a conversation, I can ask the runtime whether this machine is signed in. If it is not, the app gives the user a clear next step instead of failing later.

### Step 3: Create a streaming session

At:

```csharp
CopilotSession session = null!;
```

Type:

```csharp
session = await client.CreateSessionAsync(new SessionConfig
{
    Model = Model,
    Streaming = true
});
```

Say:

> The session is the conversation. I choose the model, enable streaming, and the event handler below prints each text fragment as it arrives.

### Step 4: Send the first message

Under:

```csharp
// Step 5: Send the first message.
```

Type:

```csharp
await session.SendAsync(new MessageOptions
{
    Prompt = "Hello world! In one sentence, say what the Copilot SDK helps a .NET app do."
});
```

Say:

> That is the basic shape: start a client, check auth, create a session, listen for events, and send a message.

### Run it

```powershell
dotnet run --project dotnet
```

Expected cue:

- The console prints the selected model.
- The assistant streams a one-sentence answer.
- The app exits after `SessionIdleEvent` completes the task.

## Act Two: Turn It Into a Podcast Assistant

Target time: 8-10 minutes.

This act keeps the same session pattern but adds app-owned capabilities. The app fetches current episode data from the official Merge Conflict RSS feed and lets Copilot generate sponsor-safe launch copy from that data only.

### Step 1: Add helper namespaces

At the top of `Program.cs`, add:

```csharp
using CopilotSdkLiveDemo.Helpers;
using CopilotSdkLiveDemo.Tools;
```

Say:

> The helpers are normal .NET code: console selection, permission prompting, and an RSS-backed tool. The model does not own those capabilities; the app does.

### Step 2: Let the presenter choose a model and episode

After authentication succeeds and before creating the session, add:

```csharp
var model = await ModelSelector.PickAsync(client, "gpt-5.4-mini");
var latestEpisodes = await MergeConflictEpisodeTool.GetLatestAsync();
var selectedEpisode = EpisodeSelector.Pick(latestEpisodes);
var selectedEpisodeNumber = selectedEpisode.EpisodeNumber;
```

Then replace:

```csharp
Console.WriteLine($"Using model: {Model}\n");
```

With:

```csharp
Console.WriteLine($"Using model: {model}\n");
```

Say:

> This keeps the demo live. I can choose a model in the room, then choose from the real ten newest Merge Conflict episodes.

### Step 3: Create the app-owned tools

Before creating the session, add:

```csharp
var episodeTool = MergeConflictEpisodeTool.CreateEpisodeTool();
var latestEpisodesTool = MergeConflictEpisodeTool.CreateLatestEpisodesTool();
```

Say:

> These are narrow tools. One gets a specific episode, and one gets the ten newest episodes. The app decides what exists and what data comes back.

### Step 4: Give the session tools and instructions

Replace the Act One `CreateSessionAsync` call with:

```csharp
session = await client.CreateSessionAsync(new SessionConfig
{
    Model = model,
    Streaming = true,
    Tools = [episodeTool, latestEpisodesTool],
    OnPermissionRequest = PermissionPrompt.RequestAsync,
    SystemMessage = new SystemMessageConfig
    {
        Mode = SystemMessageMode.Replace,
        Content = "You are the launch assistant for the Merge Conflict podcast. Use supplied episode facts only."
    }
});
```

Say:

> The model does not get arbitrary access to my application. I grant two typed capabilities, and my host app remains the approval point before a tool executes.

### Step 5: Replace the prompt with a grounded request

Replace the hello-world prompt with:

```csharp
await session.SendAsync(new MessageOptions
{
    Prompt = $"Use get_merge_conflict_episode for episode {selectedEpisodeNumber}. Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."
});
```

Say:

> The agent now has a job that needs trusted source data. It should call the episode tool, I approve the lookup, and the final copy is grounded in the official feed instead of invented details.

### Run it

```powershell
dotnet run --project dotnet
```

Expected cues:

1. The console lists available Copilot models.
2. The console lists the ten newest Merge Conflict episodes.
3. The console prints `[Tool call started]`.
4. The app asks whether to approve the tool.
5. After approval, the console prints `[Tool call complete]`.
6. The assistant streams a social headline and a sponsor-safe post under 280 characters.

## Final Code Shape

By the end, `Program.cs` should have this structure:

```csharp
using CopilotSdkLiveDemo.Helpers;
using CopilotSdkLiveDemo.Tools;
using GitHub.Copilot;

const string Model = "gpt-5.4-mini";

Console.WriteLine("Copilot SDK hello world\n");

var client = new CopilotClient();
await client.StartAsync();

var isAuthenticated = (await client.GetAuthStatusAsync()).IsAuthenticated;

if (!isAuthenticated)
{
    Console.WriteLine("Copilot is not authenticated. Run 'copilot auth login' and try again.");
    return;
}

var model = await ModelSelector.PickAsync(client, Model);
var latestEpisodes = await MergeConflictEpisodeTool.GetLatestAsync();
var selectedEpisode = EpisodeSelector.Pick(latestEpisodes);
var selectedEpisodeNumber = selectedEpisode.EpisodeNumber;

var episodeTool = MergeConflictEpisodeTool.CreateEpisodeTool();
var latestEpisodesTool = MergeConflictEpisodeTool.CreateLatestEpisodesTool();

var complete = new TaskCompletionSource(TaskCreationOptions.RunContinuationsAsynchronously);

var session = await client.CreateSessionAsync(new SessionConfig
{
    Model = model,
    Streaming = true,
    Tools = [episodeTool, latestEpisodesTool],
    OnPermissionRequest = PermissionPrompt.RequestAsync,
    SystemMessage = new SystemMessageConfig
    {
        Mode = SystemMessageMode.Replace,
        Content = "You are the launch assistant for the Merge Conflict podcast. Use supplied episode facts only."
    }
});

session.On<SessionEvent>(evt =>
{
    switch (evt)
    {
        case AssistantMessageDeltaEvent delta:
            Console.Write(delta.Data.DeltaContent);
            break;
        case ToolExecutionStartEvent start:
            Console.WriteLine($"[Tool call started] {start.Data.ToolName} {start.Data.Arguments}");
            break;
        case ToolExecutionCompleteEvent:
            Console.WriteLine("\n[Tool call complete]\n");
            break;
        case SessionErrorEvent error:
            complete.TrySetException(new InvalidOperationException(error.Data.Message));
            break;
        case SessionIdleEvent:
            complete.TrySetResult();
            break;
    }
});

Console.WriteLine($"Using model: {model}\n");

await session.SendAsync(new MessageOptions
{
    Prompt = $"Use get_merge_conflict_episode for episode {selectedEpisodeNumber}. Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."
});

await complete.Task;
```

## Presenter Pacing

| Moment | What to emphasize |
| --- | --- |
| Client startup | The app owns the runtime lifecycle. |
| Auth check | Authentication is explicit and user-friendly. |
| Streaming session | The app receives events, not just a final string. |
| First prompt | Minimal working SDK loop. |
| Model and episode selection | The demo is live and data-driven. |
| Tool definitions | Tools are typed .NET capabilities owned by the host. |
| Permission prompt | The user/app remains in control. |
| Final response | The model output is grounded in trusted RSS data. |

## Recovery Notes

If authentication fails, run:

```powershell
copilot auth login
```

If no models appear, keep the code path safe by returning the preferred model or confirm the Copilot CLI is authenticated.

If the RSS feed is unavailable, explain that the app deliberately fails rather than asking the model to invent episode facts. That is the point of grounding.

If the tool permission prompt appears and the demo needs to continue, type:

```text
y
```

If the model produces extra text, rerun with the final prompt and point out that the app can tighten instructions because it controls both the system message and the user task.

## Closing Line

> This started as a streaming hello world, then became a real .NET workflow: typed tools, explicit approval, live source data, and a Copilot response grounded in facts the application provided.
