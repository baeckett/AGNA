The most reliable path to make Agna friendly to AI models is to make Agna **discoverable, installable, and immediately usable by an agent**: publish a clear CLI reference, provide an MCP server alongside the CLI, and make the repository/docs easy for both humans and AI systems to retrieve. MCP is designed exactly for this: clients can list tools, inspect their schemas, and invoke them automatically.[[modelcontextprotocol](https://modelcontextprotocol.io/specification/2026-07-28/server/tools.md)][[modelcontextprotocol](https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro.md)]

## Make the CLI agent-friendly

Treat the CLI as a product interface for both people and models:

- Use stable, predictable commands: `agna run`, `agna validate`, `agna explain`, etc.
- Support `--help`, `--version`, `--json`, and machine-readable output; JSON is especially useful because agents can parse it reliably.
- Return useful exit codes and concise errors that state *what failed*, *why*, and *the next command to try*.
- Make common operations work without interactive prompts, so an agent can run them non-interactively.
- Include examples for the 5–10 highest-value tasks directly in the README and docs.

Cloudflare’s agent-oriented CLI is a useful pattern: it exposes searchable commands and schema inspection, and prints JSON for programmatic use.[[developers.cloudflare](https://developers.cloudflare.com/docs-for-agents/)]

## Publish an MCP server

A CLI alone is discoverable only if an agent already knows to run shell commands and find it. An MCP server makes Agna a first-class tool for MCP-capable clients such as Claude, ChatGPT, VS Code/Copilot, and Cursor.[[modelcontextprotocol](https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro.md)]

Provide an MCP server that wraps the same capabilities as the CLI:

| Capability           | MCP tool example | What the model gets                  |
| -------------------- | ---------------- | ------------------------------------ |
| Run an analysis      | `agna_run`       | Structured result plus errors        |
| Validate input       | `agna_validate`  | Pass/fail and actionable diagnostics |
| Explain a command    | `agna_explain`   | Documentation or interpretation      |
| Inspect capabilities | `agna_help`      | Command list, arguments, examples    |

MCP tools should have descriptive names, input schemas, and handlers; clients discover them through `tools/list` and invoke them through `tools/call`.  For a larger tool surface, use progressive discovery: expose a small search/catalog tool first, then load detailed schemas only when needed.[[modelcontextprotocol](https://modelcontextprotocol.io/specification/2026-07-28/server/tools.md)][[developers.cloudflare](https://developers.cloudflare.com/agents/model-context-protocol/protocol/tools/)][[modelcontextprotocol](https://modelcontextprotocol.io/docs/2025-11-25/develop/clients/client-best-practices)]

**Practical recommendation:** ship both. The CLI remains the universal fallback; MCP gives AI clients a safer, more structured integration path.

## Make GitHub discoverable

AI systems often encounter a project through GitHub, package registries, documentation, or web search. Optimize those entry points:

- Put a one-paragraph “What Agna does” description at the very top of the README.
- Add a short **“Use with AI agents”** section: install command, minimal example, JSON output example, and MCP configuration.
- Use a clear repository description, topics/tags, and a good project name in the GitHub “About” field.
- Publish to the relevant package registry—npm, PyPI, crates.io, Homebrew, etc.—with keywords such as `cli`, `ai`, `agent`, `mcp`, and your domain.
- Create a docs site with stable URLs and clean Markdown versions of key pages.
- Add an `llms.txt` file at the root of the docs domain, with links to installation, quickstart, CLI reference, MCP setup, and examples. It is a convention for giving LLMs a concise, curated map of important content.[[specification](https://specification.website/spec/agent-readiness/llms-txt/)][[specification](https://specification.website/llms.txt)]
- Advertise it with `rel="describedby"` in the HTML head or HTTP `Link` header, which improves discovery beyond guessing the `/llms.txt` path.[[specification](https://specification.website/spec/agent-readiness/llms-txt/)]

## Write for the model’s context

The highest-leverage artifact is a compact, unambiguous **agent guide**. Put it in the repository and link it prominently.

Include:

```
# Agna for AI agents

Agna is a CLI for [one-sentence purpose].

## When to use Agna
Use Agna when the user asks to [task 1], [task 2], or [task 3].

## Install
[one canonical install command]

## Quickstart
agna [command] [args]

## Machine-readable output
Use --json for structured output.

## Safety
Do not run destructive commands without user confirmation.
```

Then add:

- A complete command reference generated from the CLI itself, so it cannot drift.
- Copy-pasteable examples with real inputs and expected JSON output.
- A troubleshooting section mapping common errors to corrective commands.
- A “capabilities” section using the words users and agents are likely to use, not only internal terminology.

Avoid vague marketing language in the technical docs. Models select tools based on names, descriptions, and examples; precise descriptions improve both discovery and correct invocation.[[modelcontextprotocol](https://modelcontextprotocol.io/specification/2026-07-28/server/tools.md)][[modelcontextprotocol](https://modelcontextprotocol.io/docs/2025-11-25/develop/clients/client-best-practices)]

## Distribute where agents look

Discovery is not just SEO. Make Agna appear in the places developers and agent builders already search:

- GitHub Topics and a strong README.
- npm/PyPI/crates.io/Homebrew, as applicable.
- MCP server registries/directories, if applicable.
- Awesome lists and developer-tool directories relevant to your domain.
- Technical blog posts and documentation that explicitly say “use Agna with Claude, Cursor, Codex, or your own agent.”
- A public example repository or notebook showing an agent completing a real task with Agna.

## Add safety boundaries

Because agents can execute commands, make safe operation explicit:

- Default to read-only or dry-run behavior where feasible.
- Require an explicit flag for destructive or external-network operations.
- Clearly document filesystem, network, and credential access.
- Never encourage agents to place secrets in command-line arguments; support environment variables or a config file instead.
- Provide a sandboxing or permission guidance section for users running Agna through autonomous agents.

## A strong launch checklist

1. Publish the CLI with stable commands, `--help`, `--json`, and non-interactive operation.
2. Create an MCP server exposing the core operations.
3. Write an “AI agents” quickstart with install, MCP setup, and three end-to-end examples.
4. Generate a complete CLI reference and keep it synchronized with releases.
5. Add `llms.txt`, Markdown docs, structured metadata, and a clear GitHub description.
6. Publish the package and submit it to relevant registries/directories.
7. Test with at least one coding agent: ask it to install Agna, inspect its capabilities, and complete a representative task without human-provided command details.

The core principle: don’t rely on an AI model “knowing” Agna from training data. Make it trivially discoverable at runtime through MCP, searchable documentation, a well-described repository, and a CLI whose help output teaches the model how to use it.[[modelcontextprotocol](https://modelcontextprotocol.io/specification/2026-07-28/server/tools.md)][[modelcontextprotocol](https://modelcontextprotocol.io/docs/2026-07-28/getting-started/intro.md)][[developers.cloudflare](https://developers.cloudflare.com/docs-for-agents/)]