package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.enums.Status;

public class InventoryClick implements Listener {

	private GibCraft plugin;

	public InventoryClick(GibCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		if (plugin.getArenaManager().isPlayerInArena(p)
				&& plugin.getArenaManager().getPlayerArena(p).getStatus() == Status.ONGOING)
			event.setCancelled(true);
	}

}
