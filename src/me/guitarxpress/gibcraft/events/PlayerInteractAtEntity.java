package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import me.guitarxpress.gibcraft.GibCraft;

public class PlayerInteractAtEntity implements Listener {

	private GibCraft plugin;
	
	public PlayerInteractAtEntity(GibCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
		Player p = event.getPlayer();
		
		if (!plugin.getArenaManager().isPlayerInArena(p))
			return;
		
		event.setCancelled(true);
		
	}
	
}
