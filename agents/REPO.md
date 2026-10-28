# Repo rules — mob-info-common

Repo-specific rules and notes. The org-wide `AGENTS.md` beside this file is
maintained in [`neoforge-mod-template`](https://github.com/HexaHexes/neoforge-mod-template)
and pulled in verbatim - **never hand-edit it.** Everything in this file is this
repo's own and is edited freely; keep it aligned with the org rules rather than
restating them.

**Repo:** `HexaHexes/mob-info-common` · **Mod ID:** `mob_info_common` · **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 · **Gradle:** wrapper committed
**Jar:** `build/libs/mob_info_common-<version>.jar`

Shared mob-information logic for the Bitwise Madness Tensura addons:
variant-qualified mob names across four mods, and Tensura's analysis gating.
Ships nothing visible of its own; **Pseudocore** and **Tensura Jade** are the
consumers (both declare it a required dependency and load nothing of this
logic themselves any more).

**Dependencies** (all optional at runtime - the library loads and resolves
nothing without them)
- Tensura (`tensura`) - variant enums, analysis attributes, energy helper.
- ManasCore (`manascore`) - present whenever Tensura is; its module jars are
  extracted for compilation (`extractManascoreModules`).
- Mobs of Mythology (`mobs_of_mythology`) - drake/sporeling variants.
- Mysticism (`mysticism`) - okami variants.
- Ice and Fire needs no compile dependency at all: its branches are pure
  NBT-and-lang lookups.

**Test:** `./gradlew runGameTestServer` — `MobInfoCommonGameTests`
(**5** tests; `expectedTests = 5` in `build.gradle` must match the `@GameTest`
count). The dev closure carries Tensura, so the GameTests exercise the real
enums and attributes; the JUnit suite (`src/test`) must stay free of Minecraft
classes - the test source set carries no NeoForge artifact.

**Gotchas**
- **The access-class pattern is the whole design.** `VariantBridge` and
  `AnalysisGating` name no foreign type; the access classes
  (`TensuraAccess`, `MythologyAccess`, `MysticismAccess`,
  `TensuraAnalysisAccess`) do, and load only past a ModList gate. Never
  reference an access class from outside its package, and never let a foreign
  type into a class that loads unconditionally.
- **Tensura lookups take the caller's gate as a parameter** (`variantOf`'s
  `BooleanSupplier` overload). A static switch would let Pseudocore's
  `tensuraIntegration` config suppress Tensura Jade's names in the same
  process. Do not "simplify" it back into static state.
- **`VariantText` must stay Minecraft-free** - it is what the JUnit suite can
  test. Pure string shaping goes there, not into `VariantHelper`.
- **The Tensura compile pin is 2.0.1.3** (`8793360`), the version both
  consumers ship against. Bump deliberately, never implicitly.
- `shortenedNumber` is a **transcription** of Tensura's private recipe, not a
  call: loading Tensura's `RenderHelper` on a dedicated server aborts the
  game. Keep the transcription byte-for-byte.

## Naming - this repo

- Repo and folder: `mob-info-common`; `mod_id`: `mob_info_common`; Java base
  package `org.bitwisemadness.mobinfocommon`, with the Maven `group` in
  `build.gradle` matching.

## CI & release model - this repo

- `ci.yml` runs the template's three checks (junit, gametest, version-bump).
- `release.yml` + `publish.yml`: a push to `main` cuts the GitHub release for
  the current `mod_version` (pre-releases gated by the `ALPHA_RELEASES`
  repository variable), and publishing that release uploads the Maven artifact
  consumers resolve.
