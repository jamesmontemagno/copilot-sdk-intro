package demo;

import com.github.copilot.rpc.ToolDefinition;
import com.github.copilot.tool.Param;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public final class MergeConflictEpisodeTool {
    private static final String FEED_URL = "https://feeds.fireside.fm/mergeconflict/rss";

    private MergeConflictEpisodeTool() {
    }

    public static ToolDefinition episodeTool() {
        return ToolDefinition.from(
                "get_merge_conflict_episode",
                "Gets a Merge Conflict episode from the official RSS feed.",
                Param.of(Integer.class, "episodeNumber", "Optional Merge Conflict episode number. Omit for the latest episode."),
                MergeConflictEpisodeTool::getEpisodeJson);
    }

    public static ToolDefinition latestEpisodesTool() {
        return ToolDefinition.from(
                "get_latest_merge_conflict_episodes",
                "Gets the ten newest Merge Conflict episodes from the official RSS feed.",
                MergeConflictEpisodeTool::getLatestJson);
    }

    public static List<EpisodeBrief> latest() throws Exception {
        var items = items();
        var episodes = new ArrayList<EpisodeBrief>();
        for (int index = 0; index < Math.min(10, items.size()); index++) {
            episodes.add(toEpisodeBrief(items.get(index)));
        }
        return episodes;
    }

    public static EpisodeBrief pick(List<EpisodeBrief> episodes) throws Exception {
        if (episodes.isEmpty()) {
            throw new IllegalStateException("No Merge Conflict episodes are available to select.");
        }

        System.out.println("\nChoose a Merge Conflict episode:");
        for (int index = 0; index < episodes.size(); index++) {
            System.out.println("  " + (index + 1) + ". " + episodes.get(index).title());
        }
        return episodes.get(ModelSelector.readIndex("Episode [1]: ", episodes.size(), 0));
    }

    private static String getLatestJson() throws Exception {
        return latest().toString();
    }

    private static String getEpisodeJson(Integer episodeNumber) throws Exception {
        for (var item : items()) {
            var episode = toEpisodeBrief(item);
            if (episodeNumber == null || episodeNumber.equals(episode.episodeNumber())) {
                return episode.toString();
            }
        }
        throw new IllegalArgumentException("Episode " + episodeNumber + " was not found in the Merge Conflict RSS feed.");
    }

    private static List<org.w3c.dom.Element> items() throws Exception {
        var response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(FEED_URL)).build(),
                HttpResponse.BodyHandlers.ofInputStream());
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.body());
        var nodes = document.getElementsByTagName("item");
        var items = new ArrayList<org.w3c.dom.Element>();
        for (int index = 0; index < nodes.getLength(); index++) {
            items.add((org.w3c.dom.Element) nodes.item(index));
        }
        return items;
    }

    private static EpisodeBrief toEpisodeBrief(org.w3c.dom.Element item) {
        var title = value(item, "title");
        return new EpisodeBrief(
                episodeNumber(title),
                title,
                value(item, "pubDate"),
                valueByLocalName(item, "duration", "Unknown"),
                value(item, "description").replaceAll("<[^>]+>", "").trim(),
                value(item, "link"),
                FEED_URL);
    }

    private static Integer episodeNumber(String title) {
        int separator = title.indexOf(':');
        if (separator <= 0) {
            return null;
        }
        try {
            return Integer.parseInt(title.substring(0, separator));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String value(org.w3c.dom.Element item, String name) {
        var nodes = item.getElementsByTagName(name);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }

    private static String valueByLocalName(org.w3c.dom.Element item, String name, String fallback) {
        var nodes = item.getElementsByTagNameNS("*", name);
        return nodes.getLength() == 0 ? fallback : nodes.item(0).getTextContent().trim();
    }

    public record EpisodeBrief(
            Integer episodeNumber,
            String title,
            String published,
            String duration,
            String description,
            String episodeUrl,
            String sourceFeedUrl) {
    }
}
