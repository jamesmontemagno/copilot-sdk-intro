package main

import (
	"bufio"
	"context"
	"encoding/xml"
	"fmt"
	"html"
	"io"
	"net/http"
	"os"
	"strconv"
	"strings"
	"sync/atomic"

	copilot "github.com/github/copilot-sdk/go"
)

const feedURL = "https://feeds.fireside.fm/mergeconflict/rss"

type episodeBrief struct {
	EpisodeNumber *int   `json:"episodeNumber"`
	Title         string `json:"title"`
	Published     string `json:"published"`
	Duration      string `json:"duration"`
	Description   string `json:"description"`
	EpisodeURL    string `json:"episodeUrl"`
	SourceFeedURL string `json:"sourceFeedUrl"`
}

type episodeParams struct {
	EpisodeNumber *int `json:"episodeNumber" jsonschema:"Optional Merge Conflict episode number. Omit for the latest episode."`
}

type rssDocument struct {
	Items []rssItem `xml:"channel>item"`
}

type rssItem struct {
	Title       string `xml:"title"`
	Published   string `xml:"pubDate"`
	Description string `xml:"description"`
	Link        string `xml:"link"`
	Duration    string `xml:"duration"`
}

func createEpisodeTool() copilot.Tool {
	return copilot.DefineTool(
		"get_merge_conflict_episode",
		"Gets a Merge Conflict episode from the official RSS feed.",
		func(params episodeParams, _ copilot.ToolInvocation) (any, error) {
			return getEpisode(params.EpisodeNumber)
		},
	)
}

func createLatestEpisodesTool() copilot.Tool {
	return copilot.DefineTool(
		"get_latest_merge_conflict_episodes",
		"Gets the ten newest Merge Conflict episodes from the official RSS feed.",
		func(_ struct{}, _ copilot.ToolInvocation) (any, error) {
			return getLatestEpisodes()
		},
	)
}

func getLatestEpisodes() ([]episodeBrief, error) {
	items, err := getItems()
	if err != nil {
		return nil, err
	}

	limit := min(10, len(items))
	episodes := make([]episodeBrief, 0, limit)
	for _, item := range items[:limit] {
		episodes = append(episodes, toEpisodeBrief(item))
	}
	return episodes, nil
}

func getEpisode(episodeNumber *int) (episodeBrief, error) {
	items, err := getItems()
	if err != nil {
		return episodeBrief{}, err
	}
	if episodeNumber == nil {
		if len(items) == 0 {
			return episodeBrief{}, fmt.Errorf("the Merge Conflict RSS feed contained no episodes")
		}
		return toEpisodeBrief(items[0]), nil
	}

	for _, item := range items {
		if number := getEpisodeNumber(item); number != nil && *number == *episodeNumber {
			return toEpisodeBrief(item), nil
		}
	}
	return episodeBrief{}, fmt.Errorf("episode %d was not found in the Merge Conflict RSS feed", *episodeNumber)
}

func getItems() ([]rssItem, error) {
	response, err := http.Get(feedURL)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()
	if response.StatusCode < 200 || response.StatusCode >= 300 {
		return nil, fmt.Errorf("failed to fetch Merge Conflict RSS feed: %s", response.Status)
	}

	var document rssDocument
	body, err := io.ReadAll(response.Body)
	if err != nil {
		return nil, err
	}
	if err := xml.Unmarshal(body, &document); err != nil {
		return nil, err
	}
	return document.Items, nil
}

func toEpisodeBrief(item rssItem) episodeBrief {
	duration := item.Duration
	if duration == "" {
		duration = "Unknown"
	}
	return episodeBrief{
		EpisodeNumber: getEpisodeNumber(item),
		Title:         strings.TrimSpace(item.Title),
		Published:     strings.TrimSpace(item.Published),
		Duration:      duration,
		Description:   stripHTML(item.Description),
		EpisodeURL:    strings.TrimSpace(item.Link),
		SourceFeedURL: feedURL,
	}
}

func getEpisodeNumber(item rssItem) *int {
	separator := strings.Index(item.Title, ":")
	if separator <= 0 {
		return nil
	}
	number, err := strconv.Atoi(item.Title[:separator])
	if err != nil {
		return nil
	}
	return &number
}

func stripHTML(value string) string {
	var builder strings.Builder
	insideTag := false
	for _, character := range value {
		switch character {
		case '<':
			insideTag = true
		case '>':
			insideTag = false
		default:
			if !insideTag {
				builder.WriteRune(character)
			}
		}
	}
	return strings.TrimSpace(html.UnescapeString(builder.String()))
}

func pickEpisode(episodes []episodeBrief) (episodeBrief, error) {
	if len(episodes) == 0 {
		return episodeBrief{}, fmt.Errorf("no Merge Conflict episodes are available to select")
	}

	fmt.Println("\nChoose a Merge Conflict episode:")
	for index, episode := range episodes {
		fmt.Printf("  %d. %s\n", index+1, episode.Title)
	}
	selectedIndex := readIndex("Episode [1]: ", len(episodes), 0)
	return episodes[selectedIndex], nil
}

func selectModel(ctx context.Context, client *copilot.Client, preferred string) (string, error) {
	models, err := client.ListModels(ctx)
	if err != nil {
		return "", err
	}
	if len(models) == 0 {
		return "", nil
	}

	defaultIndex := 0
	for index, model := range models {
		if strings.EqualFold(model.ID, preferred) {
			defaultIndex = index
			break
		}
	}

	fmt.Println("Choose a Copilot model:")
	for index, model := range models {
		fmt.Printf("  %d. %s\n", index+1, model.ID)
	}
	return models[readIndex(fmt.Sprintf("Model [%d]: ", defaultIndex+1), len(models), defaultIndex)].ID, nil
}

func readIndex(prompt string, optionCount int, defaultIndex int) int {
	reader := bufio.NewReader(os.Stdin)
	for {
		fmt.Print(prompt)
		answer, _ := reader.ReadString('\n')
		answer = strings.TrimSpace(answer)
		if answer == "" {
			return defaultIndex
		}
		selection, err := strconv.Atoi(answer)
		if err == nil && selection >= 1 && selection <= optionCount {
			return selection - 1
		}
		fmt.Printf("Enter a number from 1 to %d.\n", optionCount)
	}
}

func streamResponse(session *copilot.Session, prompt string) error {
	var receivedDelta atomic.Bool
	unsubscribe := session.On(func(event copilot.SessionEvent) {
		switch data := event.Data.(type) {
		case *copilot.AssistantMessageDeltaData:
			if data.DeltaContent != "" {
				receivedDelta.Store(true)
				fmt.Print(data.DeltaContent)
			}
		case *copilot.ToolExecutionStartData:
			fmt.Printf("\n[Tool call started] %s\n", data.ToolName)
		case *copilot.ToolExecutionCompleteData:
			fmt.Println("\n[Tool call complete]")
		case *copilot.AssistantMessageData:
			if !receivedDelta.Load() {
				fmt.Print(data.Content)
			}
		}
	})
	defer unsubscribe()

	_, err := session.SendAndWait(context.Background(), copilot.MessageOptions{Prompt: prompt})
	fmt.Println()
	return err
}
