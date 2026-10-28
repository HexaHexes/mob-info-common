# Mob Info Common

The shared mob-information library behind **Pseudocore** and **Tensura Jade**.
It ships no gameplay of its own: installing it alone changes nothing you can
see in game. The pack installs it once, and both addons read the same logic.

## What it provides

- **Variant-qualified mob names** - the logic that turns a mob's stored
  variant into readable words, so a captured direwolf reads *Red Fang
  Direwolf* and a dragon *Emerald Fire Dragon*. Covers Tensura, Ice and Fire,
  Mobs of Mythology and Mysticism, and degrades to the plain species name when
  a variant cannot be read - including for mobs whose mod was removed after
  they were captured.
- **Analysis gating** - the same rules Tensura's own analysis overlay uses:
  who may see analysed information, and how high EP figures blur beyond the
  reader's grasp. Both addons gate with these rules so neither becomes a way
  around Tensura's balance.

Every mod the library reads is optional at runtime. Without them it simply
resolves nothing, and the addons that depend on it keep working with the
features those mods would have fed.

## Dependencies

| | Mod | Why |
|---|---|---|
| Required | Minecraft | - |
| Required | NeoForge | - |
| Optional | Tensura: Reincarnated | Variant enums and the analysis rules |
| Optional | ManasCore | Tensura's library; present whenever Tensura is |
| Optional | Mobs of Mythology | Drake and sporeling variant names |
| Optional | Mysticism | Okami variant names |

## Using this library

Mods in this suite consume it as a Maven dependency from the org's GitHub
Packages registry:

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/HexaHexes/mob-info-common')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN') // read:packages
        }
    }
}

dependencies {
    compileOnly 'org.bitwisemadness.mobinfocommon:mob_info_common:<version>'
}
```

Reads on GitHub Packages always require authentication. In CI the token needs
`packages: read`; the workflow token of one repository cannot read a package
owned by another, so a read token is supplied as a secret. A pack that ships
Pseudocore or Tensura Jade needs this library installed alongside them.

Publication happens when a GitHub release is published: the `Publish` workflow
builds and uploads the artifact under the version in `gradle.properties`.

## Credits

Bitwise Madness.

## License

GNU Lesser General Public License v2.1 - see `LICENSE`.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
