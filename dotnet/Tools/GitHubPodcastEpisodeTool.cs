using System.ComponentModel;
using System.Text;
using System.Xml.Linq;
using GitHub.Copilot;
using Microsoft.Extensions.AI;

namespace CopilotSdkLiveDemo.Tools;

internal static class GitHubPodcastEpisodeTool
{
    private const string FeedUrl = "https://feeds.simplecast.com/ioCY0vfY";

    internal static AIFunction CreateEpisodeTool() =>
        CopilotTool.DefineTool(
            ([Description("Optional GitHub Podcast episode title. Omit for the latest episode.")] string? episodeTitle) =>
                GetAsync(episodeTitle),
            factoryOptions: new AIFunctionFactoryOptions
            {
                Name = "get_github_podcast_episode",
                Description = "Gets a GitHub Podcast episode from the official RSS feed."
            });

    internal static AIFunction CreateLatestEpisodesTool() =>
        CopilotTool.DefineTool(
            () => GetLatestAsync(),
            factoryOptions: new AIFunctionFactoryOptions
            {
                Name = "get_latest_github_podcast_episodes",
                Description = "Gets the ten newest GitHub Podcast episodes from the official RSS feed."
            });

    internal static async Task<IReadOnlyList<EpisodeBrief>> GetLatestAsync()
    {
        var items = await GetItemsAsync();
        return items.Take(10).Select(ToEpisodeBrief).ToList();
    }

    private static async Task<EpisodeBrief> GetAsync(string? episodeTitle)
    {
        var items = await GetItemsAsync();

        var item = string.IsNullOrWhiteSpace(episodeTitle)
            ? items.FirstOrDefault()
            : items.FirstOrDefault(candidate =>
                string.Equals(Value(candidate, "title"), episodeTitle, StringComparison.OrdinalIgnoreCase));

        if (item is null)
        {
            throw new InvalidOperationException(string.IsNullOrWhiteSpace(episodeTitle)
                ? "The GitHub Podcast RSS feed contained no episodes."
                : $"Episode \"{episodeTitle}\" was not found in the GitHub Podcast RSS feed.");
        }

        return ToEpisodeBrief(item);
    }

    private static async Task<List<XElement>> GetItemsAsync()
    {
        using var httpClient = new HttpClient();
        var feed = await httpClient.GetStringAsync(FeedUrl);
        var document = XDocument.Parse(feed);
        return document.Descendants("item").ToList();
    }

    private static EpisodeBrief ToEpisodeBrief(XElement item) =>
        new(
            GetEpisodeNumber(item),
            Value(item, "title"),
            Value(item, "pubDate"),
            item.Elements().FirstOrDefault(element => element.Name.LocalName == "duration")?.Value ?? "Unknown",
            StripHtml(Value(item, "description")),
            Value(item, "link"),
            FeedUrl);

    private static int? GetEpisodeNumber(XElement item)
    {
        var title = Value(item, "title");
        var separator = title.IndexOf(':');
        return separator > 0 && int.TryParse(title[..separator], out var episodeNumber)
            ? episodeNumber
            : null;
    }

    private static string Value(XElement item, string name) => item.Element(name)?.Value.Trim() ?? string.Empty;

    private static string StripHtml(string value)
    {
        var builder = new StringBuilder(value.Length);
        var insideTag = false;

        foreach (var character in value)
        {
            if (character == '<')
            {
                insideTag = true;
            }
            else if (character == '>')
            {
                insideTag = false;
            }
            else if (!insideTag)
            {
                builder.Append(character);
            }
        }

        return System.Net.WebUtility.HtmlDecode(builder.ToString()).Trim();
    }
}

internal sealed record EpisodeBrief(
    int? EpisodeNumber,
    string Title,
    string Published,
    string Duration,
    string Description,
    string EpisodeUrl,
    string SourceFeedUrl);
