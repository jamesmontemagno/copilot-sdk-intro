use std::io::{self, Write};
use std::sync::Arc;

use async_trait::async_trait;
use github_copilot_sdk::tool::{ToolHandler, schema_for};
use github_copilot_sdk::types::{Tool, ToolInvocation};
use github_copilot_sdk::{Client, Error, ToolResult};
use quick_xml::de::from_str;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

const FEED_URL: &str = "https://feeds.simplecast.com/ioCY0vfY";

#[derive(Debug, Deserialize)]
struct Rss {
    channel: Channel,
}

#[derive(Debug, Deserialize)]
struct Channel {
    #[serde(rename = "item")]
    items: Vec<Item>,
}

#[derive(Debug, Deserialize)]
struct Item {
    title: Option<String>,
    #[serde(rename = "pubDate")]
    published: Option<String>,
    description: Option<String>,
    link: Option<String>,
    #[serde(rename = "duration")]
    duration: Option<String>,
}

#[derive(Debug, Serialize, Clone)]
pub struct EpisodeBrief {
    pub episode_number: Option<i32>,
    pub title: String,
    pub published: String,
    pub duration: String,
    pub description: String,
    pub episode_url: String,
    pub source_feed_url: String,
}

#[derive(Deserialize, JsonSchema)]
struct EpisodeParams {
    /// Optional GitHub Podcast episode title. Omit for the latest episode.
    episode_title: Option<String>,
}

#[derive(Deserialize, JsonSchema)]
struct EmptyParams {}

struct EpisodeTool;

#[async_trait]
impl ToolHandler for EpisodeTool {
    async fn call(&self, invocation: ToolInvocation) -> Result<ToolResult, Error> {
        let params: EpisodeParams = serde_json::from_value(invocation.arguments)?;
        let episode = get_episode(params.episode_title).await?;
        Ok(ToolResult::Text(serde_json::to_string(&episode)?))
    }
}

struct LatestEpisodesTool;

#[async_trait]
impl ToolHandler for LatestEpisodesTool {
    async fn call(&self, _invocation: ToolInvocation) -> Result<ToolResult, Error> {
        let episodes = get_latest_episodes().await?;
        Ok(ToolResult::Text(serde_json::to_string(&episodes)?))
    }
}

pub fn episode_tool() -> Tool {
    Tool::new("get_github_podcast_episode")
        .with_description("Gets a GitHub Podcast episode from the official RSS feed.")
        .with_parameters(schema_for::<EpisodeParams>())
        .with_handler(Arc::new(EpisodeTool))
}

pub fn latest_episodes_tool() -> Tool {
    Tool::new("get_latest_github_podcast_episodes")
        .with_description("Gets the ten newest GitHub Podcast episodes from the official RSS feed.")
        .with_parameters(schema_for::<EmptyParams>())
        .with_handler(Arc::new(LatestEpisodesTool))
}

pub async fn get_latest_episodes() -> Result<Vec<EpisodeBrief>, Error> {
    let items = get_items().await?;
    Ok(items.iter().take(10).map(to_episode_brief).collect())
}

pub async fn get_episode(episode_title: Option<String>) -> Result<EpisodeBrief, Error> {
    let items = get_items().await?;
    let item = if let Some(title) = episode_title.filter(|title| !title.trim().is_empty()) {
        items.iter().find(|item| item.title.as_deref().is_some_and(|candidate| candidate.eq_ignore_ascii_case(&title)))
    } else {
        items.first()
    };

    item.map(to_episode_brief)
        .ok_or_else(|| std::io::Error::other("Episode was not found in the GitHub Podcast RSS feed").into())
}

pub fn pick_episode(episodes: &[EpisodeBrief]) -> Result<EpisodeBrief, Box<dyn std::error::Error>> {
    if episodes.is_empty() {
        return Err(std::io::Error::other("No GitHub Podcast episodes are available to select.").into());
    }

    println!("\nChoose a GitHub Podcast episode:");
    for (index, episode) in episodes.iter().enumerate() {
        println!("  {}. {}", index + 1, episode.title);
    }

    Ok(episodes[read_index("Episode [1]: ", episodes.len(), 0)?].clone())
}

pub async fn select_model(client: &Client, preferred_model: &str) -> Result<Option<String>, Box<dyn std::error::Error>> {
    let models = client.list_models().await?;
    if models.is_empty() {
        return Ok(None);
    }

    let default_index = models
        .iter()
        .position(|model| model.id.eq_ignore_ascii_case(preferred_model))
        .unwrap_or(0);

    println!("Choose a Copilot model:");
    for (index, model) in models.iter().enumerate() {
        println!("  {}. {}", index + 1, model.id);
    }

    Ok(Some(models[read_index(&format!("Model [{}]: ", default_index + 1), models.len(), default_index)?].id.clone()))
}

async fn get_items() -> Result<Vec<Item>, Error> {
    let response = reqwest::get(FEED_URL)
        .await
        .map_err(|error| std::io::Error::other(error.to_string()))?;
    let xml = response
        .text()
        .await
        .map_err(|error| std::io::Error::other(error.to_string()))?;
    let rss: Rss = from_str(&xml).map_err(|error| std::io::Error::other(error.to_string()))?;
    Ok(rss.channel.items)
}

fn to_episode_brief(item: &Item) -> EpisodeBrief {
    EpisodeBrief {
        episode_number: episode_number_from_title(item),
        title: item.title.clone().unwrap_or_default(),
        published: item.published.clone().unwrap_or_default(),
        duration: item.duration.clone().unwrap_or_else(|| "Unknown".to_owned()),
        description: strip_html(&item.description.clone().unwrap_or_default()),
        episode_url: item.link.clone().unwrap_or_default(),
        source_feed_url: FEED_URL.to_owned(),
    }
}

fn episode_number_from_title(item: &Item) -> Option<i32> {
    item.title
        .as_ref()?
        .split_once(':')?
        .0
        .parse::<i32>()
        .ok()
}

fn strip_html(value: &str) -> String {
    let mut result = String::new();
    let mut inside_tag = false;
    for character in value.chars() {
        match character {
            '<' => inside_tag = true,
            '>' => inside_tag = false,
            _ if !inside_tag => result.push(character),
            _ => {}
        }
    }
    result.trim().to_owned()
}

fn read_index(prompt: &str, option_count: usize, default_index: usize) -> Result<usize, Box<dyn std::error::Error>> {
    loop {
        print!("{prompt}");
        io::stdout().flush()?;
        let mut answer = String::new();
        io::stdin().read_line(&mut answer)?;
        let answer = answer.trim();
        if answer.is_empty() {
            return Ok(default_index);
        }
        if let Ok(selection) = answer.parse::<usize>() {
            if (1..=option_count).contains(&selection) {
                return Ok(selection - 1);
            }
        }
        println!("Enter a number from 1 to {option_count}.");
    }
}
