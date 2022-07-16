package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class ToggleFlight implements Listener {
	
	private ArenaManager am;
	
	public ToggleFlight(GibCraft plugin) {
		this.am = plugin.getArenaManager();
	}
	
	@EventHandler
	public void onToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();
		
		if (!am.isPlayerInArena(p))
			return;
					
		if (am.isSpectating(p))
			return;
		
		if (am.getPlayerArena(p).getStatus() != Status.ONGOING)
			return;
		
		event.setCancelled(true);
	}

}
