package me.guitarxpress.gibcraft.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.utils.Utils;

public class PlayerJoin implements Listener {

	private GibCraft plugin;

	public PlayerJoin(GibCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();
		
		/*if (!plugin.playerStats.containsKey(player.getName()))
		{
			if (plugin.isLoadFromSQL() && plugin.getSQL().isConnected())
			{
				plugin.getSQLGetter().loadPlayerValues(player.getUniqueId());
			}
			else
			{
				plugin.playerStats.put(player.getName(), new Stats(player.getUniqueId().toString(), 0, 0, 0, 0, 0, 0, 0, 0));
			}
		}*/
		
		if (!plugin.getArenaManager().isPlayerInArena(player) && !player.hasPermission("gib.admin"))
		{
			for (Arena arena : plugin.getArenaManager().arenas)
			{
				if (Utils.playerInArea(arena.getBoundaries()[0], arena.getBoundaries()[1], player))
				{
					plugin.getArenaManager().toLobby(player);
					break;
				}
			}
		}
	}

}
