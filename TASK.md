# Codex task: agent policy and RandSpawn verification

Continue in the CURRENT RandSpawn repository, not in an older draft archive.
The existing checkout is authoritative. Preserve user changes and any fixes
already made by Codex. This instruction package does not supply replacement code.

## First apply the policy

1. Read the existing AGENTS.md, Git status, README.md, and relevant design and
   verification documentation. If a named file is absent, report it and continue
   with available evidence rather than restoring an older version.
2. Merge the model/subagent section in CODEX_AGENT_POLICY.md into AGENTS.md.
   Replace the superseded model/subagent section only; preserve other rules.
3. Inspect existing .codex configuration and available model/agent capabilities.
   Reuse working roles. Adapt or add roles using agent-prompts only if the current
   Codex version supports them and their configuration format is verified.
   Do not copy outdated model IDs or invent unsupported TOML keys.
4. Baseline: GPT-5.6 Sol Medium. GPT-6 Sol Medium is pre-authorized for architecture
   and difficult questions. Use available cheaper Luna/Terra workers for bounded
   research, file reading, documentation, and deterministic verification.
   A policy file alone does not change the active model. Report unavailable tiers
   or capabilities honestly; do not claim a switch or delegation that did not occur.

## Then continue development from actual repository state

Target: Minecraft 1.21.1, NeoForge 21.1.x, Java 21, server-side RandSpawn.
Check current implementation before changing it; do not redo completed work.
Keep UUID identity, mutable nickname metadata, persistent storage, and valid
vanilla bed/anchor or forced /spawnpoint priority over RandSpawn fallback.
Do not implement RandSpawn by overwriting the vanilla respawn position.

Verify these previously requested features, repairing confirmed defects:

- Unsafe personal spawn: look for the nearest safe position within configurable
  local bounds, default offsets -16 through +16 on X/Y/Z. This is 33 block
  positions per axis including the center, not literally a 32-block cube.
  Validate floor, body space, fluids, dangerous blocks, build limits and border.
  Support locations above obstructed points and below points with missing ground.
  Define and document distance ordering and deterministic tie-breaking.
- Preserve the original anchor to prevent gradual drift. Do not assign a distant
  new personal spawn after a death. If no local candidate exists, use the vanilla
  world-spawn fallback without destroying the personal record. Existing points
  under roofs must remain valid if physically safe.
- Optional mode: first login records a persistent one-time claim window, default
  15 minutes of wall-clock time including offline time. Do not automatically
  allocate a personal point. /randspawn may claim it once before the deadline;
  successful search assigns it and teleports the player. Failed searches must
  not consume the claim. Deadline expiry survives restart and leaves vanilla
  respawn behavior intact. Existing valid vanilla respawn points retain priority.
- Automatic mode remains configurable. Preserve existing player data; inspect
  migration and mode-switch behavior before making changes to storage semantics.
- Verify the no-vanilla-spawn case separately from a destroyed bed. Do not assume
  missingRespawnBlock() alone distinguishes every fallback case; inspect the
  actual NeoForge/Minecraft 1.21.1 implementation and test the event behavior.

## Validation and handoff

- Verify JDK 21, run ./gradlew test and ./gradlew build, and inspect the generated
  JAR metadata/classes. Report network or environment blockers as BLOCKED.
- Use focused tests for local search ordering/bounds, claim expiry, persistence,
  failed-claim retry, and unchanged anchor. Use GameTests or manual-server cases
  for runtime-dependent behavior; never equate compilation with game verification.
- Check command permissions, localization parity, JSON/TOML validity, and
  git diff --check. Update documentation from confirmed behavior only.
- The parent reviews evidence and final integration. Lightweight workers report
  commands, PASS/FAIL/BLOCKED, failing files/tests, and escalation needs.
- Do not push, publish, or release without a separate user instruction.
- Finish with changed files, performed checks, actual model/delegation usage,
  remaining blockers, and required manual-server scenarios.
