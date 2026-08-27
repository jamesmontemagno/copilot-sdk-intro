# Copilot SDK Live Demo: Python

Run from the repository root:

```powershell
cd python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python main.py
```

## Act One: Hello World

Open `main.py`.

1. Replace `client: CopilotClient` with:

```python
client = CopilotClient()
await client.__aenter__()
```

2. Replace `is_authenticated = False` with:

```python
is_authenticated = True
```

Say: "The Python SDK connects to the signed-in Copilot runtime. If this machine is not signed in, run `copilot auth login`."

3. Replace `session = None` with:

```python
session = await client.create_session(model=MODEL, streaming=True)
await session.__aenter__()
```

4. Under `# Step 5: Send the first message.`, type:

```python
await session.send(
    "Hello world! In one sentence, say what the Copilot SDK helps a Python app do."
)
```

5. After waiting for completion, add cleanup:

```python
await session.__aexit__(None, None, None)
await client.__aexit__(None, None, None)
```

## Act Two: Podcast Assistant

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

Replace the prompt with:

```python
await session.send(
    f"Use get_github_podcast_episode for the episode titled \"{selected_episode.title}\". "
    "Return exactly a social headline and a sponsor-safe post under 280 characters. "
    "Use only facts returned by the tool; do not invent guests, sponsors, topics, or links."
)
```

Expected cues: model list, episode list, tool start, approval prompt, tool completion, then streamed launch copy.
