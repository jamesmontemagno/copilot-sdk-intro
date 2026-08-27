# The GitHub Podcast Live Demo: Python

## Before The Session

1. Run `copilot auth login` if this machine is not already authenticated.
2. Create an environment and install dependencies:

```powershell
cd python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

## Act One: Hello World

Start with `main.py`. It deliberately has named placeholders for `client`, `is_authenticated`, and `session`. Leave the streaming event handler and completion wait in place.

### 1. Start The Client

Replace `client: CopilotClient` with:

```python
client = CopilotClient()
await client.__aenter__()
```

Say: "The client is my connection to the Copilot runtime. I start it explicitly, so the application owns its lifecycle."

### 2. Check Authentication

Replace `is_authenticated = False` with:

```python
is_authenticated = (await client.get_auth_status()).isAuthenticated
```

Say: "Before creating a session, I can ask the runtime whether this machine is signed in."

### 3. Create The Session

Replace `session = None` with:

```python
session = await client.create_session(model=MODEL, streaming=True)
await session.__aenter__()
```

Say: "The session is the conversation. I chose the model, enabled streaming, and the event handler below already prints each text fragment as it arrives."

### 4. Send Hello World

Under `# Step 5: Send the first message.`, type:

```python
await session.send(
    "Hello world! In one sentence, say what the Copilot SDK helps a Python app do."
)
```

After waiting for completion, add cleanup:

```python
await session.__aexit__(None, None, None)
await client.__aexit__(None, None, None)
```

Say: "That is the basic shape: start a client, create a session, listen for events, and send a message."

Run:

```powershell
python main.py
```

Expected output: a streamed one-sentence answer, followed by `SessionIdleData` completing the program.

## Act Two: Turn It Into A Podcast Assistant

After Hello World, use the prewritten helpers to turn the same session into a grounded podcast workflow.

### 1. Let The Presenter Choose

Add imports:

```python
from github_podcast_tools import (
    get_latest,
    get_latest_github_podcast_episodes,
    get_github_podcast_episode,
    pick_episode,
)
from model_selector import select_model
from permission_prompt import permission_prompt
```

After the auth check:

```python
model = await select_model(client, MODEL)
latest_episodes = get_latest()
selected_episode = pick_episode(latest_episodes)
```

Say: "This keeps the demo live. I can choose a model in the room, then choose from the real ten newest GitHub Podcast episodes."

### 2. Give The Session Capabilities

Create the session with tools:

```python
session = await client.create_session(
    model=model,
    streaming=True,
    tools=[get_github_podcast_episode, get_latest_github_podcast_episodes],
    available_tools=["get_github_podcast_episode", "get_latest_github_podcast_episodes"],
    on_permission_request=permission_prompt,
    system_message={
        "mode": "replace",
        "content": "You are the launch assistant for The GitHub Podcast. Use supplied episode facts only.",
    },
)
```

Say: "The model does not get arbitrary access to my application. I grant two narrow, typed capabilities and remain the approval point before a tool executes."

Say: "The system message uses replace, not append. My application supplies the complete agent identity and grounding rule for this session instead of inheriting the default prompt."

### 3. Replace The Prompt

Replace the prompt with:

```python
await session.send(
    f"Use get_github_podcast_episode for the episode titled \"{selected_episode.title}\". "
    "Return exactly a social headline and a sponsor-safe post under 280 characters. "
    "Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."
)
```

Say: "The agent decides to call the episode tool, I approve the read-only lookup, and its response is grounded in the official feed rather than invented details."

Run the completed flow:

```powershell
python main.py
```

Expected milestones: model selection, ten-episode selection, `[Tool call started]`, approval prompt, `[Tool call complete]`, then streamed launch copy.
