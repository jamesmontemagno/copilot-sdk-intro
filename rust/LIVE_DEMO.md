# The GitHub Podcast Live Demo: Rust

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Download and check dependencies from the repository root:

```powershell
cd rust
cargo check --locked
```

## Act One: Hello World

Start with `src\main.rs`. It deliberately has named placeholders for `client`, `is_authenticated`, and the session.

Add this import:

```rust
use github_copilot_sdk::types::MessageOptions;
```

### 1. Start The Client

Replace the client placeholder with:

```rust
let client = Client::start(ClientOptions::default()).await?;
```

Say: "The client is my connection to the Copilot runtime. I start it explicitly, so the application owns its lifecycle."

### 2. Check Authentication

Replace `let is_authenticated = false;` with:

```rust
let is_authenticated = client.get_auth_status().await?.is_authenticated;
```

Say: "Before creating a session, I can ask the runtime whether this machine is signed in."

### 3. Create The Session

Replace the session placeholder with:

```rust
let mut config = SessionConfig::default();
config.model = Some(MODEL.to_owned());
config.streaming = Some(true);
let session = client.create_session(config).await?;
```

Say: "The session is the conversation. I chose the model and enabled streaming."

### 4. Send Hello World

Under `// Step 5: Send the first message.`, send:

```rust
let response = session
    .send_and_wait(
        MessageOptions::new(
            "Hello world! In one sentence, say what the Copilot SDK helps a Rust app do.",
        ),
    )
    .await?;
if let Some(event) = response {
    if let Some(content) = event.data.get("content").and_then(|value| value.as_str()) {
        println!("{content}");
    }
}
```

Say: "That is the basic shape: start a client, create a session, subscribe to events, and send a message."

Run:

```powershell
cargo run
```

Expected output: a one-sentence answer while the session emits its streaming events.

## Act Two: Turn It Into A Podcast Assistant

After Hello World, use the prewritten code in `src\workshop.rs` to turn the same session into a grounded podcast workflow.

### 1. Let The Presenter Choose

Add:

```rust
mod workshop;
```

After starting the client:

```rust
let selected_model = workshop::select_model(&client, MODEL).await?;
let latest_episodes = workshop::get_latest_episodes().await?;
let selected_episode = workshop::pick_episode(&latest_episodes)?;
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes."

### 2. Give The Session Capabilities

Create the session:

```rust
let mut config = SessionConfig::default();
config.model = selected_model;
config.streaming = Some(true);
config.tools = Some(vec![workshop::episode_tool(), workshop::latest_episodes_tool()]);
config.available_tools = Some(vec![
    "get_github_podcast_episode".to_owned(),
    "get_latest_github_podcast_episodes".to_owned(),
]);
let session = client.create_session(config).await?;
```

Say: "The model gets two narrow, typed application capabilities. Add a `PermissionHandler` with `with_permission_handler` when the host must prompt for each tool call."

### 3. Replace The Prompt

Replace the prompt:

```rust
let response = session
    .send_and_wait(MessageOptions::new(format!(
        "Use get_github_podcast_episode for the episode titled \"{}\". Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.",
        selected_episode.title,
    )))
    .await?;
if let Some(event) = response {
    if let Some(content) = event.data.get("content").and_then(|value| value.as_str()) {
        println!("{content}");
    }
}
```

Say: "The agent decides to call the episode tool, and its response is grounded in the official feed rather than invented details."

Run the completed flow:

```powershell
cargo run
```

Expected milestones: model selection, ten-episode selection, tool execution, then grounded launch copy.
