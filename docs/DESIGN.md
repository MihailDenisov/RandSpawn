# RandSpawn design notes

## Invariants

1. A valid vanilla respawn block always wins.
2. RandSpawn is considered when the player has no configured vanilla respawn,
   or when `DimensionTransition.missingRespawnBlock()` is true. A valid configured
   bed, anchor, or `/spawnpoint` still wins. End-fight return transitions are ignored.
3. Failure to load the configured dimension, find a candidate, or validate a
   stored point leaves vanilla world spawn as the final fallback.
4. Automatic death-time repair is local to the record's immutable anchor. If no
   local candidate exists, the record is left unchanged and the original vanilla
   world-spawn transition remains in effect.
5. A nickname change updates metadata but never moves the spawn.
6. Administrative `regenerate` intentionally chooses a new global point and a
   new anchor; automatic repair never does so.

## Data

`randspawn_spawns.dat` is stored through Overworld `DimensionDataStorage` and
uses data version 2 and contains spawn records plus optional claim records. A
spawn stores UUID, nickname, dimension, current X/Y/Z, and immutable anchor
X/Y/Z. Version-1 records without anchor fields migrate by using their saved
position as the anchor. Claim records store UUID, an absolute epoch-millisecond
deadline, and a consumed flag. Malformed individual records are skipped so one
bad entry does not discard all others.

Mode changes never clear either collection. Automatic mode creates a missing
spawn at login. Claim mode preserves existing spawns; for a player without one,
the first login in that mode creates a window only if no historical window is
stored. Successful claim commits the spawn and consumed flag together after the
search succeeds. Search failure does not consume the claim.

## Search and safety

Candidates are sampled uniformly by area within an annulus. Distance is checked
in X/Z only. The target chunk is loaded/generated, surface Y comes from
`MOTION_BLOCKING_NO_LEAVES`, then collision, fluids, blocked ground, height, and
sky are checked.

Existing spawns use a deliberately weaker validator: collision, fluids and
blocked ground still matter, but open sky does not. This prevents a roof built
later from invalidating a player's permanent home point.

If an existing position is unsafe, local repair enumerates the configured
inclusive X/Y/Z offset box around the original anchor (defaults: -16 through
+16 on every axis, 33 positions per axis). Candidates are ordered by squared
3D Euclidean distance. Equal-distance offsets are ordered by ascending Y, then
X, then Z, making lower positions win the first tie deterministically. The
search is exhaustive within the box, so it can find safe positions above an
obstruction or below a point whose floor disappeared. Local candidates validate
configured height, world build height, world border, full floor support, body
and head collision space, fluids, and configured dangerous blocks, but do not
require open sky.

NeoForge 21.1.250 calls `ServerPlayer.findRespawnPositionAndUseSpawnBlock` before
`PlayerRespawnPositionEvent`. With no configured vanilla position it returns an
ordinary Overworld transition (`missingRespawnBlock=false`); with a configured
but unusable position it returns the missing-block transition. RandSpawn must
therefore test both `player.getRespawnPosition()` and `missingRespawnBlock()`.
The event leaves a valid bed, anchor, modded respawn block, or forced
`/spawnpoint` transition untouched.

## Known draft limitations

- search runs synchronously and may generate up to `maxSearchAttempts` chunks;
- local repair is synchronous and may validate every position in its configured
  offset box;
- blocked-block IDs are parsed during validation rather than cached on config
  load;
- `/randspawn reload` is informational; live config reload is not implemented;
- offline administration resolves only players already stored by RandSpawn;
- Minecraft-aware JUnit covers search math, SavedData round trips and the
  respawn fallback decision, but there are no world-backed NeoForge GameTests yet.

These limitations are explicit follow-up work, not hidden guarantees.
