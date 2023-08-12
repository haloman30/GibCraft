package me.guitarxpress.gibcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class TabComplete implements TabCompleter
{

	private ArenaManager am;
	private String cmd = "gib";

	public TabComplete(GibCraft plugin)
	{
		this.am = plugin.getArenaManager();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String lbl, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (command.getName().equalsIgnoreCase("gibcraft") || command.getName().equalsIgnoreCase(cmd))
			{

				if (am.isPlayerInArena(player))
				{
					list.add("leave");
					return list;
				}

				if (am.isSpectating(player))
				{
					list.add("spectate " + am.getPlayerArena(player));
					return list;
				}

				if (args.length == 1)
				{
					list.add("help");
					list.add("info");
					if (player.hasPermission(cmd + ".play"))
					{
						list.add("help");
						list.add("join");
						list.add("leave");
						list.add("stats");
						list.add("spectate");
					}
					if (player.hasPermission(cmd + ".status"))
					{
						list.add("setstatus");
					}
					if (player.hasPermission(cmd + ".admin"))
					{
						list.add("raysize");
						list.add("powerup");
					}
					if (player.hasPermission(cmd + ".create"))
					{
						list.add("create");
						list.add("setlobby");
					}
					if (player.hasPermission(cmd + ".delete"))
					{
						list.add("delete");
					}
					if (player.hasPermission(cmd + ".edit"))
					{
						list.add("edit");
						list.add("settings");
						list.add("tp");
					}
				} else if (args.length == 2)
				{
					if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("spectate")
							|| args[0].equalsIgnoreCase("setstatus") || args[0].equalsIgnoreCase("delete")
							|| args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("settings"))
					{
						if (player.hasPermission(cmd + ".play") || player.hasPermission(cmd + ".spectate")
								|| player.hasPermission(cmd + ".setstatus") || player.hasPermission(cmd + ".delete")
								|| player.hasPermission(cmd + ".edit"))
						{
							for (String name : am.arenaNames)
							{
								list.add(name);
							}
						}
					} else if (args[0].equalsIgnoreCase("create"))
					{
						if (player.hasPermission(cmd + ".create"))
							list.add("name");
					}
				} else if (args.length == 3)
				{
					if (args[0].equalsIgnoreCase("create"))
					{
						if (player.hasPermission(cmd + ".create"))
						{
							list.add("ffa");
							list.add("duos");
						}
					} else if (args[0].equalsIgnoreCase("setstatus"))
					{
						if (player.hasPermission(cmd + ".setstatus"))
						{
							for (Status status : Status.values())
							{
								list.add(Status.valueToString(status));
							}
						}
					}
					else if (args[0].equalsIgnoreCase("settings"))
					{
						if (player.hasPermission(cmd + ".edit"))
						{
							list.add("maxplayers");
							list.add("gametime");
							list.add("respawntime");
							list.add("maxfrags");
						}
					}
				}
			}
		}
		return list;
	}
}