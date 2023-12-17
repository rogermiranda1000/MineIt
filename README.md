# MineIt ![version](https://badges.spiget.org/resources/version/Version-green-69161.svg) ![downloads](https://badges.spiget.org/resources/downloads/Downloads-blue-69161.svg) ![rating](https://badges.spiget.org/resources/rating/Rating-blue-69161.svg) [![CodeFactor](https://www.codefactor.io/repository/github/rogermiranda1000/mineit/badge)](https://www.codefactor.io/repository/github/rogermiranda1000/mineit)
[Spigot plugin page](https://www.spigotmc.org/resources/mine-it.69161/)

## Dependencies
- spigot 1.16.5
- [VersionController](https://github.com/rogermiranda1000/Spigot-VersionController) (link library with .jar)
- Maven's `org.jetbrains:annotations:LATEST`
- [MineableGems](https://www.spigotmc.org/resources/mineablegems-1-8-8-1-18-create-and-customize-your-own-drops.83807/) recompiled with [MineItxMineableGems](https://www.spigotmc.org/resources/mineit-x-mineablegems.103464/)

### Testing dependencies
- [WatchWolf Tester](https://github.com/rogermiranda1000/WatchWolf-Tester) and all its dependencies (`org.junit.jupiter:junit-jupiter-engine:5.8.1`, `org.junit.jupiter:junit-jupiter-params:5.8.1` and `org.yaml:snakeyaml:1.21`)

## Build
Run `mvn clean install -DskipTests=true`

## Run integration tests

Note: you'll need [WatchWolf](https://watchwolf.dev/) running **locally**.

Run `mvn test -Dmaven.test.skip=false`