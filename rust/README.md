# The GitHub Podcast Copilot SDK Live Demo: Rust

A Rust console demo that starts with a streaming GitHub Copilot SDK Hello World, then expands into typed application tools backed by the official [GitHub Podcast RSS feed](https://feeds.simplecast.com/ioCY0vfY).

## Prerequisites

- Rust 1.94 or later
- An active GitHub Copilot subscription
- GitHub Copilot CLI authentication

## Setup

```powershell
cd rust
cargo fetch --locked
copilot auth login
```

## Run

```powershell
cargo run --locked
```

## What It Demonstrates

- Explicit Copilot client lifecycle management
- Authentication status checks
- Streaming session events
- Schema-derived typed local tools
- Host-controlled tool approval
- A replacement system message that defines the session's complete agent identity
- Model and live episode selection
- Responses grounded in current RSS data

Follow [LIVE_DEMO.md](LIVE_DEMO.md) for the complete presenter typing script and talk track.
