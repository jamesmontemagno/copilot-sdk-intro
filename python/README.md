# The GitHub Podcast Copilot SDK Live Demo: Python

A Python console demo that starts with a streaming GitHub Copilot SDK Hello World, then expands into typed application tools backed by the official [GitHub Podcast RSS feed](https://feeds.simplecast.com/ioCY0vfY).

## Prerequisites

- Python 3.11 or later
- An active GitHub Copilot subscription
- GitHub Copilot CLI authentication

## Setup

```powershell
cd python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
copilot auth login
```

## Run

```powershell
python main.py
```

## What It Demonstrates

- Explicit `CopilotClient` lifecycle management
- Authentication status checks
- Streaming session events
- Pydantic-typed local tools
- Host-controlled tool approval
- A replacement system message that defines the session's complete agent identity
- Model and live episode selection
- Responses grounded in current RSS data

Follow [LIVE_DEMO.md](LIVE_DEMO.md) for the complete presenter typing script and talk track.
