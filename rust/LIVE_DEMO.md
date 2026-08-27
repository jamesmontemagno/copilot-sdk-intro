# The GitHub Podcast Live Demo: Rust

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Download and check dependencies from the repository root:

```powershell
cd rust
cargo check --locked
```

## Demo Pitch

Say: "We are building a Podcast Agent for The GitHub Podcast. It will let us choose a real episode, retrieve verified metadata from the official RSS feed, and turn those facts into sponsor-safe social copy."

Say: "We will begin with the smallest possible Copilot SDK conversation, then give it a purpose, an identity, and application-owned tools."

## Act One: Hello World

Start with `src\main.rs`. It deliberately has named placeholders for `client`, `is_authenticated`, and the session.

Add this import:

```rust
use github_copilot_sdk::types::{MessageOptions, SystemMessageConfig};
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

Say: "That is the basic shape: start a client, create a session, subscribe to events, and send a message. Once this loop works, we can evolve it into our Podcast Agent."

Run this Hello World checkpoint now from the `rust` folder:

```powershell
cargo run
```

Expected output: a one-sentence answer while the session emits its streaming events.

## Act Two: Turn It Into A Podcast Agent

After Hello World, use the prewritten code in `src\workshop.rs` to turn the same session into a grounded podcast workflow.

Say: "The conversation works. Now we will turn it into our Podcast Agent: a focused assistant that can research a selected GitHub Podcast episode and prepare launch copy without inventing facts."

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

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes. That selection becomes the Podcast Agent's assignment."

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
config = config.with_system_message(
    SystemMessageConfig::new()
        .with_mode("replace")
        .with_content(
            "You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.",
        ),
);
let session = client.create_session(config).await?;
```

Say: "The model gets two narrow, typed application capabilities. Add a `PermissionHandler` with `with_permission_handler` when the host must prompt for each tool call."

Say: "These tools are what make this an agent rather than a generic chatbot: it can take action against a trusted data source that my application controls."

Say: "The system message uses replace, not append. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

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

Run the completed Podcast Agent now from the `rust` folder:

```powershell
cargo run
```

Expected milestones: model selection, ten-episode selection, tool execution, then grounded launch copy.
