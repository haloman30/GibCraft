# GibCraft

GibCraft is a minigame plugin for Minecraft (using the Spigot API), originally created by GuitarXpress/HeyImJ0hn, inspired
by Ratz InstaGib and Quake Live. This fork is for continued development by haloman30, primarily for the [Chaotic United](https://chaoticunited.net/)
Minecraft server.

In this gamemode, all players are given Laserguns which can instantly kill other players. Several powerups spawn randomly on the map.
Currently, two modes are available: Free-For-All and Duos, with Duos being a team of two players. The first player or team to reach 20
kills wins.

** This plugin requires [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) **

## Commands

The base command for all commands is `/gib` or `/gibcraft`.

### Player Commands

| Command | Description | Permission Required |
| --- | --- | --- |
| `/gib help` | Shows available commands | `N/A` |
| `/gib info` | Displays plugin info | `N/A` |
| `/gib join` | Joins the specified arena | `gib.play` |
| `/gib leave` | Leaves current arena/game | `gib.play` |
| `/gib spectate [arena]` | Joins/Leaves spectators for specified arena/game | `gib.play` |
| `/gib stats` | Displays player statistics | `gib.play` |
| `/gib list` / `/gib arenas` | Displays all available arenas and their status | `gib.play` |

### Admin/Staff Commands

| Command | Description | Permission Required |
| --- | --- | --- |
| `/gib setlobby` | Sets the minigame lobby. This is where players get teleported when they join/leave an arena | `gib.create` |
| `/gib create <name>` | Creates a new arena with the specified name | `gib.create` |
| `/gib delete <arena>` | Deletes specified arena | `gib.delete` |
| `/gib edit <arena>` | Toggles edit mode for specified arena | `gib.edit` |
| `/gib setstatus <arena> <status>` | Changes the specified arena status to the new one | `gib.status` |
| `/gib raysize` | Changes the ray size used for lasergun hit detection | `gib.admin` |
| `/gib powerup` | Spawns a new random powerup | `gib.admin` |

## Additional Info

When using the `gib setstatus` command a list will of status will be presented so you can have control of when the arena is playable or not.

For any feedback, suggestion or bugs please open an **issue** on the github's [Issue Tracker](https://github.com/haloman30/GibCraft/issues).