import asyncio

from copilot import CopilotClient
from copilot.session_events import (
    AssistantMessageDeltaData,
    SessionErrorData,
    SessionIdleData,
    ToolExecutionCompleteData,
    ToolExecutionStartData,
)

MODEL = "gpt-5.4-mini"


async def main() -> None:
    print("Copilot SDK hello world\n")

    # Step 1: Start the Copilot client.
    client: CopilotClient

    # Step 2: Check that Copilot is authenticated.
    is_authenticated = False

    if not is_authenticated:
        print("Copilot is not authenticated. Run 'copilot auth login' and try again.")
        return

    done = asyncio.Event()
    error: RuntimeError | None = None

    # Step 3: Create a streaming session.
    session = None

    # Step 4: Stream events from the assistant.
    def on_event(event) -> None:
        nonlocal error
        match event.data:
            case AssistantMessageDeltaData(delta_content=delta) if delta:
                print(delta, end="", flush=True)
            case ToolExecutionStartData(tool_name=name):
                print(f"[Tool call started] {name}")
            case ToolExecutionCompleteData():
                print("\n[Tool call complete]\n")
            case SessionErrorData(message=message):
                error = RuntimeError(message)
                done.set()
            case SessionIdleData():
                done.set()

    session.on(on_event)  # type: ignore[union-attr]

    print(f"Using model: {MODEL}\n")

    # Step 5: Send the first message.

    await done.wait()
    if error is not None:
        raise error


if __name__ == "__main__":
    asyncio.run(main())
