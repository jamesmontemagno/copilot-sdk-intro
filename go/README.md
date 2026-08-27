# The GitHub Podcast Copilot SDK Live Demo: Go

A Go console demo that starts with a streaming GitHub Copilot SDK Hello World, then expands into typed application tools backed by the official [GitHub Podcast RSS feed](https://feeds.simplecast.com/ioCY0vfY).

## Prerequisites

- Go 1.24 or later
- An active GitHub Copilot subscription
- GitHub Copilot CLI authentication

## Setup

```powershell
cd go
go mod download
copilot auth login
```

## Run

```powershell
go run .
```

## What It Demonstrates

- Explicit Copilot client lifecycle management
- Authentication status checks
- Streaming session events
- Typed local tools
- Explicit tool permission policy
- A replacement system message that defines the session's complete agent identity
- Model and live episode selection
- Responses grounded in current RSS data

Follow [LIVE_DEMO.md](LIVE_DEMO.md) for the complete presenter typing script and talk track.
