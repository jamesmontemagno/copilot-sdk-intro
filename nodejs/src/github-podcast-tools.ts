import { defineTool } from "@github/copilot-sdk";
import { z } from "zod";

const feedUrl = "https://feeds.simplecast.com/ioCY0vfY";

export type EpisodeBrief = {
  episodeNumber: number | null;
  title: string;
  published: string;
  duration: string;
  description: string;
  episodeUrl: string;
  sourceFeedUrl: string;
};

export const episodeTool = defineTool("get_github_podcast_episode", {
  description: "Gets a GitHub Podcast episode from the official RSS feed.",
  parameters: z.object({
    episodeTitle: z.string().optional().describe("Optional GitHub Podcast episode title. Omit for the latest episode."),
  }),
  handler: async ({ episodeTitle }) => getEpisode(episodeTitle),
});

export const latestEpisodesTool = defineTool("get_latest_github_podcast_episodes", {
  description: "Gets the ten newest GitHub Podcast episodes from the official RSS feed.",
  parameters: z.object({}),
  handler: async () => getLatestEpisodes(),
});

export async function getLatestEpisodes(): Promise<EpisodeBrief[]> {
  const items = await getItems();
  return items.slice(0, 10).map(toEpisodeBrief);
}

export async function getEpisode(episodeTitle?: string): Promise<EpisodeBrief> {
  const items = await getItems();
  const item = !episodeTitle
    ? items[0]
    : items.find((candidate) => value(candidate, "title").toLowerCase() === episodeTitle.toLowerCase());

  if (!item) {
    throw new Error(!episodeTitle
      ? "The GitHub Podcast RSS feed contained no episodes."
      : `Episode "${episodeTitle}" was not found in the GitHub Podcast RSS feed.`);
  }

  return toEpisodeBrief(item);
}

export async function pickEpisode(episodes: EpisodeBrief[]): Promise<EpisodeBrief> {
  if (episodes.length === 0) {
    throw new Error("No GitHub Podcast episodes are available to select.");
  }

  console.log("\nChoose a GitHub Podcast episode:");
  episodes.forEach((episode, index) => console.log(`  ${index + 1}. ${episode.title}`));

  const { readIndex } = await import("./model-selector.js");
  return episodes[await readIndex("Episode [1]: ", episodes.length, 0)]!;
}

async function getItems(): Promise<string[]> {
  const response = await fetch(feedUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch The GitHub Podcast RSS feed: ${response.status}`);
  }

  const xml = await response.text();
  return [...xml.matchAll(/<item\b[\s\S]*?<\/item>/g)].map((match) => match[0]);
}

function toEpisodeBrief(item: string): EpisodeBrief {
  return {
    episodeNumber: getEpisodeNumber(item),
    title: value(item, "title"),
    published: value(item, "pubDate"),
    duration: valueByLocalName(item, "duration") || "Unknown",
    description: stripHtml(value(item, "description")),
    episodeUrl: value(item, "link"),
    sourceFeedUrl: feedUrl,
  };
}

function getEpisodeNumber(item: string): number | null {
  const title = value(item, "title");
  const separator = title.indexOf(":");
  if (separator <= 0) {
    return null;
  }

  const parsed = Number.parseInt(title.slice(0, separator), 10);
  return Number.isInteger(parsed) ? parsed : null;
}

function value(item: string, name: string): string {
  const match = item.match(new RegExp(`<${name}[^>]*>([\\s\\S]*?)<\\/${name}>`));
  return decodeXml(match?.[1]?.trim() ?? "");
}

function valueByLocalName(item: string, localName: string): string {
  const match = item.match(new RegExp(`<(?:[^:>]+:)?${localName}[^>]*>([\\s\\S]*?)<\\/(?:[^:>]+:)?${localName}>`));
  return decodeXml(match?.[1]?.trim() ?? "");
}

function stripHtml(value: string): string {
  return decodeXml(value.replace(/<[^>]+>/g, "")).trim();
}

function decodeXml(value: string): string {
  return value
    .replaceAll("<![CDATA[", "")
    .replaceAll("]]>", "")
    .replaceAll("&amp;", "&")
    .replaceAll("&lt;", "<")
    .replaceAll("&gt;", ">")
    .replaceAll("&quot;", '"')
    .replaceAll("&#39;", "'");
}
