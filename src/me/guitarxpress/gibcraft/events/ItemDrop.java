package me.guitarxpress.gibcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import me.guitarxpress.gibcraft.GibCraft;

public class ItemDrop implements Listener {
	
	private GibCraft plugin;
	
	public ItemDrop(GibCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if (plugin.getArenaManager().isPlayerInArena(event.getPlayer()))
			event.setCancelled(true);
	}
	
	
}
