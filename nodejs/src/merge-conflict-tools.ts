import { defineTool } from "@github/copilot-sdk";
import { z } from "zod";

const feedUrl = "https://feeds.fireside.fm/mergeconflict/rss";

export type EpisodeBrief = {
  episodeNumber: number | null;
  title: string;
  published: string;
  duration: string;
  description: string;
  episodeUrl: string;
  sourceFeedUrl: string;
};

export const episodeTool = defineTool("get_merge_conflict_episode", {
  description: "Gets a Merge Conflict episode from the official RSS feed.",
  parameters: z.object({
    episodeNumber: z.number().int().optional().describe("Optional Merge Conflict episode number. Omit for the latest episode."),
  }),
  handler: async ({ episodeNumber }) => getEpisode(episodeNumber),
});

export const latestEpisodesTool = defineTool("get_latest_merge_conflict_episodes", {
  description: "Gets the ten newest Merge Conflict episodes from the official RSS feed.",
  parameters: z.object({}),
  handler: async () => getLatestEpisodes(),
});

export async function getLatestEpisodes(): Promise<EpisodeBrief[]> {
  const items = await getItems();
  return items.slice(0, 10).map(toEpisodeBrief);
}

export async function getEpisode(episodeNumber?: number): Promise<EpisodeBrief> {
  const items = await getItems();
  const item = episodeNumber === undefined
    ? items[0]
    : items.find((candidate) => getEpisodeNumber(candidate) === episodeNumber);

  if (!item) {
    throw new Error(episodeNumber === undefined
      ? "The Merge Conflict RSS feed contained no episodes."
      : `Episode ${episodeNumber} was not found in the Merge Conflict RSS feed.`);
  }

  return toEpisodeBrief(item);
}

export async function pickEpisode(episodes: EpisodeBrief[]): Promise<EpisodeBrief> {
  if (episodes.length === 0) {
    throw new Error("No Merge Conflict episodes are available to select.");
  }

  console.log("\nChoose a Merge Conflict episode:");
  episodes.forEach((episode, index) => console.log(`  ${index + 1}. ${episode.title}`));

  const { readIndex } = await import("./model-selector.js");
  return episodes[await readIndex("Episode [1]: ", episodes.length, 0)]!;
}

async function getItems(): Promise<string[]> {
  const response = await fetch(feedUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch Merge Conflict RSS feed: ${response.status}`);
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
