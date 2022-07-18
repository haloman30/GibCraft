package me.guitarxpress.gibcraft;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class Commands implements CommandExecutor {

	private GibCraft plugin;
	private ArenaManager am;
	private String cmd = "gib";

	public Commands(GibCraft plugin) {
		this.plugin = plugin;
		this.am = plugin.getArenaManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;

		Player p = (Player) sender;

		if (command.getName().equalsIgnoreCase("gibcraft") || command.getName().equalsIgnoreCase(cmd)) {
			switch (args.length) {
			case 0:
				Bukkit.dispatchCommand(p, "gib info");
				break;
			case 1:
				// Help
				if (args[0].equalsIgnoreCase("help")) {
					p.sendMessage(prefix() + "§eAvailable Commands: \n"
							+ "§e - §6/gib help §e- §fDisplays available commands.\n"
							+ "§e - §6/gib info §e- §fDisplays plugin info.");
					if (p.hasPermission(cmd + ".play")) {
						p.sendMessage("§e - §6/gib join <arena> §e- §fJoins the specified arena.\n"
								+ "§e - §6/gib leave §e- §fLeaves the current arena.\n"
								+ "§e - §6/gib spectate <arena> §e- §fSpectate the specified arena/game.\n"
								+ "§e - §6/gib stats §e- §fDisplays player statistics."
								+ "§e - §6/gib arenas §e- §fDisplays all available arenas.");
					}
					if (p.hasPermission(cmd + ".create")) {
						p.sendMessage("§e - §6/gib setlobby §e- §fSets minigame lobby.\n"
								+ "§e - §6/gib create <name> §e- §fCreates a new arena with specified name.\n");
					}
					if (p.hasPermission(cmd + ".delete")) {
						p.sendMessage("§e - §6/gib delete <arena> §e- §fDeletes specified arena.");
					}
					if (p.hasPermission(cmd + ".edit")) {
						p.sendMessage("§e - §6/gib edit <arena> §e- §fToggle edit mode for specified arena.");
					}
					if (p.hasPermission(cmd + ".status")) {
						p.sendMessage(
								"§e - §6/gib setstatus <arena> <status> §e- §fChanges the specified arena status to the new one.");
					}

					// Info
				} else if (args[0].equalsIgnoreCase("info")) {
					p.sendMessage(prefix() + "§ePlugin being developed by §4GuitarXpress§e." + "\n- Plugin Version: §4"
							+ plugin.getDescription().getVersion() + "\n§e- API Version: §4"
							+ plugin.getDescription().getAPIVersion()
							+ "\n§ePlease to submit any feedback or bugs on discord.\n"
							+ "\n§cNote that your stats may be reset during the beta phase.§e"
							+ "\nFor help use §6/gib help§e.");

					// List
				} else if (args[0].equalsIgnoreCase("list")) {
					if (p.hasPermission(cmd + ".play")) {
						p.sendMessage(prefix() + "§eAvailable arenas:");
						for (Arena arena : am.arenas) {
							p.sendMessage(" §e- §6" + arena.getName() + "§e - " + arena.getStatus().display());
						}
					}

					// Create
				} else if (args[0].equalsIgnoreCase("create")) {
					if (p.hasPermission(cmd + ".create")) {
						p.sendMessage(prefix() + "§cMissing arguments. §6/" + cmd + " create <name> + §c.");
					}

					// Leave
				} else if (args[0].equalsIgnoreCase("leave")) {
					if (p.hasPermission(cmd + ".play")) {
						if (am.isPlayerInArena(p)) {
							Arena arena = am.getPlayerArena(p);
							if (p.getGameMode() != GameMode.SPECTATOR) {
								am.removePlayerFromArena(p, arena);
								p.sendMessage(prefix() + "§eYou left the game.");
							} else {
								am.removeSpectatorFromArena(p, arena);
								p.sendMessage(prefix() + "§eYou left spectators.");
							}
						} else {
							p.sendMessage(prefix() + "§cYou're not in a game.");
						}
					}

					// Setlobby
				} else if (args[0].equalsIgnoreCase("setlobby")) {
					if (p.hasPermission(cmd + ".create")) {
						am.setLobby(p.getLocation());
						p.sendMessage(prefix() + "§eLobby set.");
					}

					// Stats
				} else if (args[0].equalsIgnoreCase("stats")) {
					if (p.hasPermission(cmd + ".play")) {
						am.createStatsBoard(p);
					}
				} else {
					p.sendMessage(prefix() + "§cInvalid Command.");
				}
				break;
			case 2:
				// Create
				if (args[0].equalsIgnoreCase("create")) {
					if (p.hasPermission(cmd + ".create")) {
						if (!am.exists(args[1])) {
							am.createArena(args[1], Mode.FFA);
							p.sendMessage(prefix() + "§aArena created. §eEdit it with §6/" + cmd + " edit " + args[1]
									+ "§a.");
						} else {
							p.sendMessage(prefix() + "§cArena already exists.");
						}
					}

					// Delete
				} else if (args[0].equalsIgnoreCase("delete")) {
					if (p.hasPermission(cmd + ".delete")) {
						if (am.exists(args[1])) {
							am.removeArena(args[1]);
							p.sendMessage(prefix() + "§aRemoved arena §6" + args[1] + "§a.");
						} else {
							p.sendMessage(prefix() + "§cArena §6" + args[1] + " §cdoesn't exist.");
						}
					}

					// Join
				} else if (args[0].equalsIgnoreCase("join")) {
					if (p.hasPermission(cmd + ".play")) {
						if (am.isLobbySet()) {
							if (am.exists(args[1])) {
								Arena arena = am.getArena(args[1]);
								if (!arena.isFull()) {
									if (arena.getStatus() == Status.JOINABLE || arena.getStatus() == Status.STARTING) {
										if (!am.isPlayerInArena(p)) {
											if (arena.getMode() == Mode.DUOS) {
												plugin.getGUIManager().openTeamsGUI(p, arena.getName());
											} else {
												am.addPlayerToArena(p, arena);
											}
										} else {
											p.sendMessage(prefix() + "§cYou're already in a game.");
										}
									} else if (arena.getStatus() == Status.ONGOING
											|| arena.getStatus() == Status.STARTUP) {
										p.sendMessage(prefix() + "§cThat game has already started. Spectate with §6/"
												+ cmd + " spectate " + args[1] + "§c.");
									} else {
										p.sendMessage(prefix() + "§cYou can't join this arena right now.");
									}
								} else {
									p.sendMessage(prefix() + "§cGame is full. Spectate with §6/" + cmd + " spectate "
											+ args[1] + "§c.");
								}
							} else {
								p.sendMessage(prefix() + "§cArena doesn't exist.");
							}
						} else {
							p.sendMessage(prefix() + "§cLobby isn't set.");
						}
					}

					// Spectate
				} else if (args[0].equalsIgnoreCase("spectate")) {
					if (p.hasPermission(cmd + ".play")) {
						if (am.exists(args[1])) {
							if (!am.isPlayerInArena(p)) {
								Arena arena = am.getArena(args[1]);
								if (arena.getStatus() == Status.ONGOING) {
									if (am.isSpectating(p))
										am.removeSpectatorFromArena(p, arena);
									else
										am.addSpectatorToArena(p, arena);
								} else {
									p.sendMessage(prefix() + "§cCannot spectate this game right now.");
								}
							} else {
								p.sendMessage(prefix() + "§cYou can't do this right now.");
							}
						} else {
							p.sendMessage(prefix() + "§cArena doesn't exist.");
						}
					}

					// Edit
				} else if (args[0].equalsIgnoreCase("edit")) {
					if (p.hasPermission(cmd + ".edit")) {
						if (am.exists(args[1])) {
							Arena arena = am.getArena(args[1]);
							if (arena.getStatus() != Status.ONGOING) {
								am.toggleEditMode(p, args[1]);
								p.sendMessage(prefix()
										+ "§eConsider updating the arena status before and after you edit with:\n"
										+ "§6/" + cmd + " setstatus «arena» «state»§e.");
							} else {
								p.sendMessage(prefix() + "§cPlease wait for the game to finish.");
							}
						}
					}

				} else {
					p.sendMessage(prefix() + "§cInvalid Command.");
				}
				break;
			case 3:
				if (args[0].equalsIgnoreCase("create")) {
					if (p.hasPermission(cmd + ".create")) {
						if (!am.exists(args[1])) {
							if (args[2].equalsIgnoreCase("ffa") || args[2].equalsIgnoreCase("duos")) {
								am.createArena(args[1], Mode.fromString(args[2]));
								p.sendMessage(prefix() + "§aArena created. §eEdit it with §6/" + cmd + " edit "
										+ args[1] + "§a.");
							} else {
								p.sendMessage(prefix() + "§cInvalid mode. Available modes: ");
								for (int i = 0; i < Mode.values().length; i++) {
									p.sendMessage("§6 - " + Mode.values()[i]);
								}
							}
						} else {
							p.sendMessage(prefix() + "§cArena already exists.");
						}
					}

					// Setstatus
				} else if (args[0].equalsIgnoreCase("setstatus")) {
					if (p.hasPermission(cmd + ".status")) {
						if (am.exists(args[1])) {
							Arena arena = am.getArena(args[1]);
							am.setStatus(arena, Status.fromString(args[2]));
							p.sendMessage(prefix() + "§eSet §6" + args[1] + " §estatus to §6"
									+ Status.fromString(args[2]) + "§e.");
						} else {
							p.sendMessage(prefix() + "§cArena doesn't exist.");
						}
					}

				} else {
					p.sendMessage(prefix() + "§cInvalid Command.");
				}
				break;
			default:
				p.sendMessage(prefix() + "§cInvalid Command.");
				break;
			}
		}
		return true;
	}

	public static String prefix() {
		return "§7[§4Gib§6Craft§7] ";
	}
}
