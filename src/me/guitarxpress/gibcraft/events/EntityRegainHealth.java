package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import me.guitarxpress.gibcraft.GibCraft;

public class EntityRegainHealth implements Listener {

	private GibCraft plugin;
	
	public EntityRegainHealth(GibCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player p = (Player) event.getEntity();
		
		if (plugin.getArenaManager().isPlayerInArena(p))
			event.setCancelled(true);
	}
	
}
