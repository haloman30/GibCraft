# GibCraft
GibCraft Minigame Plugin for Spigot/Paper 1.18/1.19 by GuitarXpress (HeyImJ0hn), inspired by Ratz InstaGib and Quake Live.

**This plugin requires [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) **

## Commands:
Command prefix is **/gibcraft** or **/gib**
- **/gib help** - Shows available commands;
- **/gib info** - Displays plugin info;
- **/gib join** - Joins the specified arena;
- **/gib leave** - Leaves current arena/game;
- **/gib spectate <arena>** - Joins/Leaves spectators for specified arena/game;
- **/gib stats** - Displays player statistics;
- **/gib arenas** - Displays all available arenas and their status;
- **/gib setlobby** - Sets the minigame lobby. This is where players get teleported when they join/leave an arena;
- **/gib create <name>** - Creates a new arena with the specified name;
- **/gib delete <arena>** - Deletes specified arena;
- **/gib edit <arena>** - Toggles edit mode for specified arena;
- **/gib setstatus <arena> <status>** - Changes the specified arena status to the new one.

## Permission nodes:
- **"gib.play"** - For regular players - Allows players to play the game, aswell as spectate and check their stats;
- **"gib.create"** - For staff - Allows players to create an arena;
- **"gib.delete"** - For staff - Allows players to delete an arena;
- **"gib.edit"** - For staff - Allows players to toggle edit mode for an arena;
- **"gib.status"** - For staff - Allows players to set the status of an arena;
- **"gib.admin"** - For staff - Allows players to use regular commands while inside a game/arena.

## Additional Info:
When creating an arena, the default status will be "SETTINGUP". Players can only join an arena if the status is "JOINABLE", so make sure to after editing an arena and setting up the lobby to run `/gib setstatus <arena name> joinable` to allow players to play.

When using the `gib setstatus` command a list will of status will be presented so you can have control of when the arena is playable or not.

For any feedback, suggestion or bugs please open an **issue** on the github's [Issue Tracker](https://github.com/HeyImJ0hn/GibCraft/issues).

## Future Features
For now the only planned feature is a new mode, the **Duos Mode**.