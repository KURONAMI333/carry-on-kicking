# Carry On Kicking

A Carry On addon that lets you charge a kick and launch the mob you are holding. Inspired by Big Walk.

Pick up a mob with Carry On, release the pickup button, then hold right-click to wind up. Release to kick. Charge reaches full strength after one second; releasing before 0.2 seconds simply drops the mob. The kick follows your aim with an upward arc.

Block carrying and player carrying keep their normal Carry On controls.

## Requirements

| Minecraft | Loaders | Carry On |
| --- | --- | --- |
| 1.21.1 | NeoForge, Fabric | 2.2.6.13 |
| 1.20.1 | Forge, Fabric | 2.1.2.7 |
| 1.21.11 | Fabric | 2.9.2 |
| 26.2 | Fabric | 2.11.0 |

- Fabric API on Fabric
- Install the addon on both the client and the server

Download the matching loader version from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/carry-on-kicking).

Bugs and questions: comment on the CurseForge page, or DM [@kuronami333 on X](https://x.com/kuronami333).

## Credits and license

The icon adapts Carry On's wordmark and includes Minecraft visual elements. See [NOTICE.md](NOTICE.md) for attributions and third-party terms.

All Rights Reserved. Free to put in any modpack, on any platform, monetised or not - no permission needed, no credit required. See [LICENSE](LICENSE).

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.

## Building

For Minecraft 1.21.1, use Java 21 and run `./gradlew build`. Loader-specific JARs are written to `neoforge/build/libs` and `fabric/build/libs`.

For Minecraft 1.20.1, use Java 21 and run `./gradlew build` from `mc1201/`. This build produces Java 17-compatible classes. Loader-specific JARs are written to `mc1201/forge/build/libs` and `mc1201/fabric/build/libs`. The build shares the original artwork, sounds, and translations with the 1.21.1 version.

For Minecraft 1.21.11, use Java 21 and run `./gradlew build` from `fabric-1.21.11/`. For Minecraft 26.2, use Java 25 and run the same command from `fabric-26.2/`. Each build writes its JAR to `build/libs` inside that directory and shares the original artwork, sounds, and translations.
