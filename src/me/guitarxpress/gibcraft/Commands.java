package me.guitarxpress.gibcraft;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class Commands implements CommandExecutor {

	private GibCraft plugin = null;
	private ArenaManager am = null;
	private String cmd = "gib";

	public Commands(GibCraft _plugin) 
	{
		plugin = _plugin;
		am = plugin.getArenaManager();
	}
	
	private void ShowHelpEntry(Player player, String subcommand, String description)
	{
		player.sendMessage(String.format(Language.help_entry_format, cmd, subcommand, description));
	}
	
	private void ShowInfoEntry(Player player, String name, String content)
	{
		player.sendMessage(String.format(Language.info_entry_format, name, content));
	}
	
	private void HelpSubcommand(Player p, String[] args)
	{
		p.sendMessage(Language.help_header);
		
		ShowHelpEntry(p, "help", "Displays available commands.");
		ShowHelpEntry(p, "info", "Displays plugin info.");
		
		if (p.hasPermission(cmd + ".play")) 
		{
			ShowHelpEntry(p, "join <arena>", "Joins the specified arena.");
			ShowHelpEntry(p, "leave", "Leaves the current arena.");
			ShowHelpEntry(p, "spectate <arena>", "Spectate the specified arena/game.");
			ShowHelpEntry(p, "stats", "Displays player statistics.");
			ShowHelpEntry(p, "list", "Displays all available arenas.");
		}
		
		if (p.hasPermission(cmd + ".create")) 
		{
			ShowHelpEntry(p, "setlobby", "Sets minigame lobby.");
			ShowHelpEntry(p, "create <name>", "Creates a new arena with specified name.");
		}
		
		if (p.hasPermission(cmd + ".delete")) 
		{
			ShowHelpEntry(p, "delete", "Deletes specified arena.");
		}
		
		if (p.hasPermission(cmd + ".edit")) 
		{
			ShowHelpEntry(p, "edit <arena>", "Toggle edit mode for specified arena.");
			ShowHelpEntry(p, "tp <arena>", "Teleports to an arena.");
			ShowHelpEntry(p, "settings <arena>", "Adjust specific settings for an arena.");
		}
		
		if (p.hasPermission(cmd + ".status")) 
		{
			ShowHelpEntry(p, "setstatus <arena> <status>", "Changes the specified arena status to the new one.");
		}
		
		if (p.hasPermission(cmd + ".admin")) 
		{
			ShowHelpEntry(p, "raysize <size>", "Changes the ray size used for lasergun hit detection.");
			ShowHelpEntry(p, "powerup", "Spawns a new random powerup.");
		}
	}
	
	private void InfoSubcommand(Player p, String[] args)
	{
		p.sendMessage(Language.info_header);
		ShowInfoEntry(p, "Plugin Version", plugin.getDescription().getVersion());
		ShowInfoEntry(p, "API Version", plugin.getDescription().getAPIVersion());
		
		p.sendMessage("§6Based on version 1.0.0 of the GibCraft plugin by GuitarXpress.");
		p.sendMessage("§ePlease to submit any feedback or bugs on Github or discord.");
		p.sendMessage("§eFor help use §6/gib help§e.");
	}
	
	private void ListSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".play")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		p.sendMessage(Language.available_arenas_header);
		
		for (Arena arena : am.arenas) 
		{
			p.sendMessage(" §e- §6" + arena.getName() + "§e - " + arena.getStatus().display());
		}
	}
	
	private void JoinSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".play"))
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (!am.isLobbySet())
		{
			p.sendMessage(Language.error_lobby_not_configured);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " join <name>"));
			return;
		}
		
		if (!am.exists(args[1]))
		{
			p.sendMessage(Language.error_arena_not_found);
			return;
		}
		
		Arena arena = am.getArena(args[1]);
		
		if (!arena.areBoundariesSet())
		{
			p.sendMessage(Language.error_bounds_not_configured);
			return;
		}
		
		if (arena.isFull())
		{
			p.sendMessage(String.format(Language.error_game_full_spectate_format, cmd + " spectate " + args[1]));
			return;
		}
		
		if (arena.getStatus() != Status.JOINABLE && arena.getStatus() != Status.STARTING)
		{
			if (arena.getStatus() == Status.ONGOING || arena.getStatus() == Status.STARTUP)
			{
				p.sendMessage(String.format(Language.error_game_started_spectate_format, cmd + " spectate " + args[1]));
			} 
			else
			{
				p.sendMessage(Language.error_cannot_join_now);
			}
			
			return;
		}
		
		if (am.isPlayerInArena(p))
		{
			p.sendMessage(Language.error_already_in_game);
			return;
		}
		
		if (arena.getMode() == Mode.DUOS)
		{
			plugin.getGUIManager().openTeamsGUI(p, arena.getName());
		} 
		else
		{
			am.addPlayerToArena(p, arena);
		}
	}
	
	private void LeaveSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".play")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (am.isPlayerInArena(p)) 
		{
			Arena arena = am.getPlayerArena(p);
			
			if (arena.getSpectators().contains(p)) 
			{
				am.removeSpectatorFromArena(p, arena);
				p.sendMessage(Language.left_spectators);
			} 
			else 
			{
				am.removePlayerFromArena(p, arena);
				p.sendMessage(Language.left_game);
			}
		} 
		else 
		{
			p.sendMessage(Language.error_not_in_game);
		}
	}
	
	private void SpectateSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".play")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " spectate <name>"));
			return;
		}
		
		if (!am.exists(args[1])) 
		{
			p.sendMessage(Language.error_arena_not_found);
			return;
		}
		
		if (am.isPlayerInArena(p)) 
		{
			p.sendMessage(Language.error_spectate_already_in_game);
			return;
		}
		
		Arena arena = am.getArena(args[1]);
		
		if (arena.getStatus() != Status.ONGOING) 
		{
			p.sendMessage(Language.error_cannot_specate);
			return;
		}
		
		if (am.isSpectating(p))
		{
			am.removeSpectatorFromArena(p, arena);
		}
		else
		{
			am.addSpectatorToArena(p, arena);
		}
	}
	
	private void StatsSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".play")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		am.createStatsBoard(p);
	}
	
	private void SetLobbySubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".create")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		am.setLobby(p.getLocation());
		p.sendMessage(Language.lobby_set);
	}
	
	private void CreateSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".create")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " create <name>"));
			return;
		}
		
		if (!am.exists(args[1])) 
		{
			if (args.length >= 3)
			{
				if (args[2].equalsIgnoreCase("ffa") || args[2].equalsIgnoreCase("duos")) 
				{
					am.createArena(args[1], Mode.fromString(args[2]));
					p.sendMessage(String.format(Language.arena_created_format, cmd + " edit " + args[1]));
				} 
				else 
				{
					p.sendMessage(Language.error_invalid_mode_header);
					
					for (int i = 0; i < Mode.values().length; i++) 
					{
						p.sendMessage("§6 - " + Mode.values()[i]);
					}
				}
			}
			else
			{
				am.createArena(args[1], Mode.FFA);
				p.sendMessage(String.format(Language.arena_created_format, cmd + " edit " + args[1]));
			}
		} 
		else 
		{
			p.sendMessage(Language.error_arena_already_exists);
		}
	}
	
	private void DeleteSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".delete")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " delete <name>"));
			return;
		}
		
		if (am.exists(args[1])) 
		{
			am.removeArena(args[1]);
			p.sendMessage(String.format(Language.arena_removed_format, args[1]));
		} 
		else 
		{
			p.sendMessage(String.format(Language.error_arena_not_found_format, args[1]));
		}
	}
	
	private void EditSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".edit")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (!am.exists(args[1])) 
		{
			p.sendMessage(Language.error_arena_not_found);
			return;
		}
		
		Arena arena = am.getArena(args[1]);
		
		if (arena.getStatus() != Status.ONGOING) 
		{
			am.toggleEditMode(p, args[1]);
			p.sendMessage(Language.edit_mode_toggled);
		} 
		else 
		{
			p.sendMessage(Language.error_game_in_progress);
		}
	}
	
	private void TeleportSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".edit")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " tp <name>"));
			return;
		}
		
		if (am.exists(args[1])) 
		{
			Arena arena = am.getArena(args[1]);
			p.teleport(arena.selectRandomSpawn());
			p.sendMessage(String.format(Language.teleported_to_arena_format, args[1]));
		} 
		else 
		{
			p.sendMessage(String.format(Language.error_arena_not_found_format, args[1]));
		}
	}
	
	private void RaySizeSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".admin")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 2)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, cmd + " raysize <size>"));
			return;
		}
		
		try
		{
			double ray_size = Double.valueOf(args[1]);
			Utils.ray_size = ray_size;
			p.sendMessage(String.format(Language.ray_size_changed_format, String.valueOf(ray_size)));
		}
		catch (Exception ex)
		{
			p.sendMessage(String.format(Language.error_cannot_parse_raysize, args[1]));
		}
	}
	
	private void PowerupSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".admin")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (!am.isPlayerInArena(p))
		{
			p.sendMessage(Language.error_not_in_game);
			return;
		}
		
		Arena arena = am.getPlayerArena(p);
		RayTraceResult result = p.rayTraceBlocks(10, FluidCollisionMode.NEVER);
		
		if (result == null)
		{
			p.sendMessage(Language.error_powerup_spawn_raycast_fail);
			return;
		}
		
		Location location = result.getHitBlock().getLocation().add(0, 2, 0);
		arena.AddPowerup(location);
		
		p.sendMessage();
	}
	
	private void SetStatusSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".status")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (!am.exists(args[1])) 
		{
			p.sendMessage(Language.error_arena_not_found);
			return;
		}
		
		Arena arena = am.getArena(args[1]);
		Status new_status = Status.fromString(args[2]);
		
		am.setStatus(arena, new_status);
		p.sendMessage(String.format(Language.arena_status_changed_format, args[1], new_status));
	}
	
	private void SettingsSubcommand(Player p, String[] args)
	{
		if (!p.hasPermission(cmd + ".edit")) 
		{
			p.sendMessage(Language.error_no_permission);
			return;
		}
		
		if (args.length < 4)
		{
			p.sendMessage(String.format(Language.error_missing_args_format, "gib settings <arena> <option> <value|reset>"));
			return;
		}
		
		if (!am.exists(args[1])) 
		{
			p.sendMessage(Language.error_arena_not_found);
			return;
		}
		
		Arena arena = am.getArena(args[1]);
		String setting_name = args[2].toLowerCase();
		String setting_value = args[3].toLowerCase();
		
		switch (setting_name)
		{
		case "maxplayers":
			if (setting_value.equalsIgnoreCase("reset"))
			{
				arena.max_players_override = false;
				p.sendMessage(String.format(Language.setting_reverted_to_default, "maxplayers", am.GetMaxPlayers(null)));
			}
			else
			{
				try
				{
					arena.max_players = Integer.parseInt(setting_value);
					arena.max_players_override = true;
					p.sendMessage(String.format(Language.setting_updated, "maxplayers", setting_value));
				}
				catch (Exception ex)
				{
					p.sendMessage(String.format(Language.error_setting_parse_fail_int, setting_value));
				}
			}
			
			break;
		case "gametime":
			if (setting_value.equalsIgnoreCase("reset"))
			{
				arena.game_time_override = false;
				p.sendMessage(String.format(Language.setting_reverted_to_default, "gametime", am.GetGameTime(null)));
			}
			else
			{
				try
				{
					arena.game_time = Integer.parseInt(setting_value);
					arena.game_time_override = true;
					p.sendMessage(String.format(Language.setting_updated, "gametime", setting_value));
				}
				catch (Exception ex)
				{
					p.sendMessage(String.format(Language.error_setting_parse_fail_int, setting_value));
				}
			}
			
			break;
		case "maxfrags":
			if (setting_value.equalsIgnoreCase("reset"))
			{
				arena.max_frags_override = false;
				p.sendMessage(String.format(Language.setting_reverted_to_default, "maxfrags", am.GetMaxFrags(null)));
			}
			else
			{
				try
				{
					arena.max_frags = Integer.parseInt(setting_value);
					arena.max_frags_override = true;
					p.sendMessage(String.format(Language.setting_updated, "maxfrags", setting_value));
				}
				catch (Exception ex)
				{
					p.sendMessage(String.format(Language.error_setting_parse_fail_int, setting_value));
				}
			}
			
			break;
		case "respawntime":
			if (setting_value.equalsIgnoreCase("reset"))
			{
				arena.respawn_time_override = false;
				p.sendMessage(String.format(Language.setting_reverted_to_default, "respawntime", GibCraft.instance.getGameManager().GetRespawnTime(null)));
			}
			else
			{
				try
				{
					arena.respawn_time = Integer.parseInt(setting_value);
					arena.respawn_time_override = true;
					p.sendMessage(String.format(Language.setting_updated, "respawntime", setting_value));
				}
				catch (Exception ex)
				{
					p.sendMessage(String.format(Language.error_setting_parse_fail_int, setting_value));
				}
			}
			
			break;
		default:
			p.sendMessage(Language.error_invalid_setting_name + "maxplayers, gametime, maxfrags, respawntime");
			break;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if (!(sender instanceof Player))
		{
			return true;
		}

		Player p = (Player)sender;

		if (command.getName().equalsIgnoreCase("gibcraft") || command.getName().equalsIgnoreCase(cmd)) 
		{
			switch (args[0].toLowerCase())
			{
			case "help":
				HelpSubcommand(p, args);
				break;
			case "info":
				InfoSubcommand(p, args);
				break;
			case "arenas":
			case "list":
				ListSubcommand(p, args);
				break;
			case "create":
				CreateSubcommand(p, args);
				break;
			case "delete":
				DeleteSubcommand(p, args);
				break;
			case "join":
				JoinSubcommand(p, args);
				break;
			case "leave":
				LeaveSubcommand(p, args);
				break;
			case "setlobby":
				SetLobbySubcommand(p, args);
				break;
			case "stats":
				StatsSubcommand(p, args);
				break;
			case "tp":
				TeleportSubcommand(p, args);
				break;
			case "raysize":
				RaySizeSubcommand(p, args);
				break;
			case "spectate":
				SpectateSubcommand(p, args);
				break;
			case "edit":
				EditSubcommand(p, args);
				break;
			case "setstatus":
				SetStatusSubcommand(p, args);
				break;
			case "powerup":
				PowerupSubcommand(p, args);
				break;
			case "settings":
				SettingsSubcommand(p, args);
				break;
			default:
				p.sendMessage(Language.error_unknown_command);
				break;
			}
		}
		return true;
	}
}
