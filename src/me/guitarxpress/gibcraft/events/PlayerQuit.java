package me.guitarxpress.gibcraft.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class PlayerQuit implements Listener {

	private ArenaManager am;

	public PlayerQuit(GibCraft plugin) {
		this.am = plugin.getArenaManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();

		if (am.isPlayerInArena(p))
		{
			//Bukkit.dispatchCommand(p, "gib leave");
			
			Arena arena = am.getPlayerArena(p);
			
			if (arena.getSpectators().contains(p)) 
			{
				if (am.getLobby() != null && p.getLocation().distance(am.getLobby()) > 20)
					am.toLobby(p);

				am.removeSpectatorFromArena(p, arena);
			} 
			else 
			{
				am.removePlayerFromArena(p, arena);
			}
		}
	}
}