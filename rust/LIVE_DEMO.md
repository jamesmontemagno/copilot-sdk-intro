# Copilot SDK Live Demo: Rust

Run from the repository root:

```powershell
cd rust
cargo run
```

## Act One: Hello World

Open `src\main.rs`.

1. Replace the client placeholder with:

```rust
let client = Client::start(ClientOptions::default()).await?;
```

2. Replace `let is_authenticated = false;` with:

```rust
let is_authenticated = true;
```

3. Replace the session placeholder with:

```rust
let mut config = SessionConfig::default();
config.model = Some(MODEL.to_owned());
config.streaming = Some(true);
let session = client.create_session(config).await?;
```

4. Under `// Step 5: Send the first message.`, send:

```rust
session
    .send("Hello world! In one sentence, say what the Copilot SDK helps a Rust app do.")
    .await?;
```

## Act Two: Podcast Assistant

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

Say: "This Rust track keeps the same typed-tool boundary and approval flow; keep the grounding instruction in the prompt if this SDK version does not expose a system-message helper."

Replace the prompt:

```rust
session
    .send(format!(
        "Use get_github_podcast_episode for the episode titled \"{}\". Return exactly a social headline and a sponsor-safe post under 280 characters. Use only facts returned by the tool; do not invent guests, sponsors, topics, or links.",
        selected_episode.title
    ))
    .await?;
```

Expected cues: model list, episode list, tool start, approval, tool completion, then streamed launch copy.
