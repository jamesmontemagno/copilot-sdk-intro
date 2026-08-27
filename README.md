# Copilot SDK Intro

Standalone live-demo samples for the GitHub Copilot SDK.

Each folder contains starter code, a language README, and a `LIVE_DEMO.md` presenter script:

| Language | Overview | Presenter guide | Run |
| --- | --- | --- | --- |
| .NET | [README](dotnet/README.md) | [Live demo](dotnet/LIVE_DEMO.md) | `dotnet run --project dotnet` |
| Node.js | [README](nodejs/README.md) | [Live demo](nodejs/LIVE_DEMO.md) | `cd nodejs; npm ci; npm start` |
| Python | [README](python/README.md) | [Live demo](python/LIVE_DEMO.md) | `cd python; pip install -r requirements.txt; python main.py` |
| Go | [README](go/README.md) | [Live demo](go/LIVE_DEMO.md) | `cd go; go run .` |
| Java | [README](java/README.md) | [Live demo](java/LIVE_DEMO.md) | `cd java; mvn compile exec:java` |
| Rust | [README](rust/README.md) | [Live demo](rust/LIVE_DEMO.md) | `cd rust; cargo run --locked` |

Authenticate first if needed:

```powershell
copilot auth login
```
