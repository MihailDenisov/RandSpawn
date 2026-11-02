---
name: randspawn-build-verification
description: Verify RandSpawn Gradle builds, unit tests, produced JARs, and version metadata. Use for build, test, artifact, or release-readiness checks; not for diagnosing unclear NeoForge runtime behavior.
---

# RandSpawn build verification

Use this deterministic sequence only as far as the request requires:

1. Confirm Java 21 with `java -version`.
2. Run `./gradlew test` for test-only requests or `./gradlew build` for a full
   verification. Do not skip failing tests.
3. For artifact checks, inspect `build/libs/randspawn-*.jar` and confirm it
   contains generated `META-INF/neoforge.mods.toml` plus RandSpawn classes.
4. Compare the filename/version with `mod_version` in `gradle.properties`.

Report the exact command, PASS/FAIL, the failing test or file when applicable,
and `escalation required: yes|no`. Escalate after one inconclusive pass. Do not
change respawn/lifecycle logic while performing this skill.
