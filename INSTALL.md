# Apply to an existing RandSpawn checkout

This package contains instructions only. It has no Java source, Gradle files,
VS Code settings, existing project documentation, or executable model configuration.

1. Extract into a separate temporary folder, outside the working checkout.
2. Open your current RandSpawn checkout in VS Code/Codex.
3. Give Codex TASK.md and the path to this extracted package.
4. Codex must merge CODEX_AGENT_POLICY.md into the existing AGENTS.md, preserving
   unrelated project rules and existing uncommitted work.
5. The agent-prompts files are role instructions, not executable Codex configuration.
   Codex should use them when creating the corresponding subagents.

Model names in the policy are requested tiers. Use actual runtime-supported IDs.
Do not replace a working .codex/config.toml with an unverified sample. If the
runtime cannot change its own active model, report the requested manual selection.
