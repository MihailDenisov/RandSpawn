# Verification

The current automated suite covers annulus sampling, horizontal minimum
distance, inclusive local-search bounds and deterministic ordering, distinct
no-vanilla-spawn and destroyed-block fallback decisions, UUID-key replacement,
nickname-only updates, v1 anchor migration, v2 anchor/claim persistence, claim
expiry, failed-claim retry, and unchanged anchors after position repair.

The exact current-session commands and results belong in the handoff report;
this file does not treat an older build snapshot as current evidence.

The source metadata file under `src/main/templates` contains Gradle placeholders,
so only its generated output is expected to parse as standalone TOML.

## Still requires a real dedicated-server or world-backed GameTest

Compilation and JUnit do not verify Minecraft world behavior. The following
scenarios remain unverified in a running game:

1. Automatic mode first login creates exactly one personal spawn and teleports
   the player; later logins do not create another spawn.
2. Claim mode first login creates only a persistent window, successful
   `/randspawn` claims once, failed searches remain retryable, offline time and a
   restart count toward expiry, and expired claims leave vanilla behavior intact.
3. A changed nickname updates metadata without changing UUID or coordinates.
4. Multiple players satisfy the configured horizontal X/Z minimum distance.
5. Search uses the world surface height and treats `minY`/`maxY` only as filters.
6. Water, lava, dangerous blocks, and insufficient feet/head space are rejected.
7. A valid bed, respawn anchor, or `/spawnpoint` retains priority.
8. RandSpawn is used when no vanilla respawn is configured.
9. A destroyed bed falls back to RandSpawn and the broken vanilla position is
   not copied to the newly respawned player.
10. A roof above an otherwise safe existing RandSpawn does not invalidate it.
11. A physically unsafe existing RandSpawn is repaired at the nearest ordered
    local candidate, including candidates above and below the saved point.
12. Failed local repair preserves the complete previous record and immutable
    anchor, and uses world spawn rather than a distant newly generated point.
13. A missing dimension or exhausted search falls through to world spawn.
14. `/randspawn`, `info`, `tp`, `set`, `regenerate`, `remove`, `list`, and
    `reload` enforce player/permission-level-2 access as documented.
15. A client without RandSpawn can join a server that has it installed.

## Operational risk

Spawn search is synchronous and may generate up to `maxSearchAttempts` chunks.
No performance claim has been made without profiling; optimization remains a
separate architectural task.
