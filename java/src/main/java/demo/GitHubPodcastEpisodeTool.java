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

public final class GitHubPodcastEpisodeTool {
    private static final String FEED_URL = "https://feeds.simplecast.com/ioCY0vfY";

    private GitHubPodcastEpisodeTool() {
    }

    public static ToolDefinition episodeTool() {
        return ToolDefinition.from(
                "get_github_podcast_episode",
                "Gets a GitHub Podcast episode from the official RSS feed.",
                Param.of(String.class, "episodeTitle", "Optional GitHub Podcast episode title. Omit for the latest episode."),
                GitHubPodcastEpisodeTool::getEpisodeJson);
    }

    public static ToolDefinition latestEpisodesTool() {
        return ToolDefinition.from(
                "get_latest_github_podcast_episodes",
                "Gets the ten newest GitHub Podcast episodes from the official RSS feed.",
                GitHubPodcastEpisodeTool::getLatestJson);
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
            throw new IllegalStateException("No GitHub Podcast episodes are available to select.");
        }

        System.out.println("\nChoose a GitHub Podcast episode:");
        for (int index = 0; index < episodes.size(); index++) {
            System.out.println("  " + (index + 1) + ". " + episodes.get(index).title());
        }
        return episodes.get(ModelSelector.readIndex("Episode [1]: ", episodes.size(), 0));
    }

    private static String getLatestJson() {
        try {
            return latest().toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read The GitHub Podcast RSS feed.", exception);
        }
    }

    private static String getEpisodeJson(String episodeTitle) {
        try {
            for (var item : items()) {
                var episode = toEpisodeBrief(item);
                if (episodeTitle == null || episodeTitle.isBlank() || episodeTitle.equalsIgnoreCase(episode.title())) {
                    return episode.toString();
                }
            }
            throw new IllegalArgumentException("Episode \"" + episodeTitle + "\" was not found in the GitHub Podcast RSS feed.");
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read The GitHub Podcast RSS feed.", exception);
        }
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
