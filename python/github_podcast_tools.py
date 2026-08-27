from __future__ import annotations

import html
import re
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import asdict, dataclass

from copilot import define_tool
from pydantic import BaseModel, Field

from model_selector import read_index

FEED_URL = "https://feeds.simplecast.com/ioCY0vfY"


@dataclass(frozen=True)
class EpisodeBrief:
    episode_number: int | None
    title: str
    published: str
    duration: str
    description: str
    episode_url: str
    source_feed_url: str


class EpisodeParams(BaseModel):
    episode_title: str | None = Field(
        default=None,
        description="Optional GitHub Podcast episode title. Omit for the latest episode.",
    )


@define_tool(
    name="get_github_podcast_episode",
    description="Gets a GitHub Podcast episode from the official RSS feed.",
)
def get_github_podcast_episode(params: EpisodeParams) -> dict[str, object]:
    return asdict(get_episode(params.episode_title))


@define_tool(
    name="get_latest_github_podcast_episodes",
    description="Gets the ten newest GitHub Podcast episodes from the official RSS feed.",
)
def get_latest_github_podcast_episodes() -> list[dict[str, object]]:
    return [asdict(episode) for episode in get_latest()]


def get_latest() -> list[EpisodeBrief]:
    return [to_episode_brief(item) for item in get_items()[:10]]


def get_episode(episode_title: str | None) -> EpisodeBrief:
    items = get_items()
    item = items[0] if not episode_title else next(
        (candidate for candidate in items if value(candidate, "title").lower() == episode_title.lower()),
        None,
    )
    if item is None:
        raise ValueError(
            "The GitHub Podcast RSS feed contained no episodes."
            if not episode_title
            else f'Episode "{episode_title}" was not found in the GitHub Podcast RSS feed.'
        )
    return to_episode_brief(item)


def pick_episode(episodes: list[EpisodeBrief]) -> EpisodeBrief:
    if not episodes:
        raise ValueError("No GitHub Podcast episodes are available to select.")

    print("\nChoose a GitHub Podcast episode:")
    for index, episode in enumerate(episodes, start=1):
        print(f"  {index}. {episode.title}")
    return episodes[read_index("Episode [1]: ", len(episodes), 0)]


def get_items() -> list[ET.Element]:
    with urllib.request.urlopen(FEED_URL) as response:
        document = ET.fromstring(response.read())
    return list(document.iter("item"))


def to_episode_brief(item: ET.Element) -> EpisodeBrief:
    return EpisodeBrief(
        episode_number=get_episode_number(item),
        title=value(item, "title"),
        published=value(item, "pubDate"),
        duration=value_by_local_name(item, "duration") or "Unknown",
        description=strip_html(value(item, "description")),
        episode_url=value(item, "link"),
        source_feed_url=FEED_URL,
    )


def get_episode_number(item: ET.Element) -> int | None:
    title = value(item, "title")
    separator = title.find(":")
    if separator <= 0:
        return None
    try:
        return int(title[:separator])
    except ValueError:
        return None


def value(item: ET.Element, name: str) -> str:
    child = item.find(name)
    return (child.text or "").strip() if child is not None else ""


def value_by_local_name(item: ET.Element, name: str) -> str:
    for child in item:
        if child.tag.split("}")[-1].split(":")[-1] == name:
            return (child.text or "").strip()
    return ""


def strip_html(value: str) -> str:
    return html.unescape(re.sub(r"<[^>]+>", "", value)).strip()
