from __future__ import annotations

from copilot import CopilotClient


async def select_model(client: CopilotClient, preferred_model: str) -> str | None:
    models = await client.list_models()
    if not models:
        return None

    default_index = next(
        (index for index, model in enumerate(models) if model.id.lower() == preferred_model.lower()),
        0,
    )

    print("Choose a Copilot model:")
    for index, model in enumerate(models, start=1):
        print(f"  {index}. {model.id}")

    selected_index = read_index(f"Model [{default_index + 1}]: ", len(models), default_index)
    return models[selected_index].id


def read_index(prompt: str, option_count: int, default_index: int) -> int:
    while True:
        answer = input(prompt).strip()
        if not answer:
            return default_index
        try:
            selection = int(answer)
        except ValueError:
            selection = 0
        if 1 <= selection <= option_count:
            return selection - 1
        print(f"Enter a number from 1 to {option_count}.")
