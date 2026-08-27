# The GitHub Podcast Copilot SDK Live Demo: Node.js

A TypeScript console demo that starts with a streaming GitHub Copilot SDK Hello World, then expands into typed application tools backed by the official [GitHub Podcast RSS feed](https://feeds.simplecast.com/ioCY0vfY).

## Prerequisites

- Node.js 22.12 or later
- An active GitHub Copilot subscription
- GitHub Copilot CLI authentication

## Setup

```powershell
cd nodejs
npm ci
copilot auth login
```

## Run

```powershell
npm start
```

## What It Demonstrates

- Explicit `CopilotClient` startup and shutdown
- Authentication status checks
- A streaming Copilot session and event handling
- Zod-typed local tools
- Host-controlled tool approval
- A replacement system message that defines the session's complete agent identity
- Model and live episode selection
- Responses grounded in current RSS data

Follow [LIVE_DEMO.md](LIVE_DEMO.md) for the complete presenter typing script and talk track.
