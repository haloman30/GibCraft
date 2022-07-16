package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import me.guitarxpress.gibcraft.GibCraft;

public class FoodLevelChange implements Listener {
	
	private GibCraft plugin;
	
	public FoodLevelChange(GibCraft plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player p = (Player) event.getEntity();
		
		if (plugin.getArenaManager().isPlayerInArena(p))
			event.setCancelled(true);
	}
	
}
