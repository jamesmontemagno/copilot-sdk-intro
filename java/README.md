# The GitHub Podcast Copilot SDK Live Demo: Java

A Java console demo that starts with a GitHub Copilot SDK Hello World, then expands into typed application tools backed by the official [GitHub Podcast RSS feed](https://feeds.simplecast.com/ioCY0vfY).

## Prerequisites

- Java 17 or later
- Apache Maven 3.9 or later
- An active GitHub Copilot subscription
- GitHub Copilot CLI authentication

## Setup

```powershell
cd java
mvn compile
copilot auth login
```

## Run

```powershell
mvn compile exec:java
```

## What It Demonstrates

- Explicit `CopilotClient` lifecycle management
- Authentication status checks
- Session configuration and events
- Typed local tools
- Explicit tool permission policy
- Model and live episode selection
- Responses grounded in current RSS data

Follow [LIVE_DEMO.md](LIVE_DEMO.md) for the complete presenter typing script and talk track.
