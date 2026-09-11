# Carry On Kick

A Carry On addon that lets you charge a kick and launch the mob you are holding. Inspired by Big Walk.

Pick up a mob with Carry On, release the pickup button, then hold right-click to wind up. Release to kick. Charge reaches full strength after one second; releasing before 0.2 seconds simply drops the mob. The kick follows your aim with an upward arc.

Block carrying and player carrying keep their normal Carry On controls.

## Requirements

- Minecraft 1.21.1 with NeoForge or Fabric
- Carry On 2.2.6.13
- Fabric API on Fabric
- Install the addon on both the client and the server

This is a development prototype. Visuals and game feel are still being verified.

## Building

Use Java 21 and run `./gradlew build`. Loader-specific JARs are written to `neoforge/build/libs` and `fabric/build/libs`.
