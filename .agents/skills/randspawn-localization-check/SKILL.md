---
name: randspawn-localization-check
description: Check RandSpawn Minecraft language JSON files for valid JSON and identical translation-key sets. Use after command/message localization edits; not for rewriting translations without a request.
---

# RandSpawn localization check

For every JSON file under `src/main/resources/assets/randspawn/lang`:

1. Parse it as strict JSON.
2. Compare its keys with `en_us.json`.
3. Report missing and extra keys per locale.
4. Search Java sources for `Component.translatable` keys and report any key not
   present in `en_us.json`.

Do not judge prose style or rewrite translations unless requested. Report the
exact command or check, PASS/FAIL, the failing file when applicable, and
`escalation required: yes|no`. Escalate after one inconclusive pass.
