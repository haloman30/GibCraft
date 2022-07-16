package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class EntityDamageByEntity implements Listener {

	private ArenaManager am;

	public EntityDamageByEntity(GibCraft plugin) {
		this.am = plugin.getArenaManager();
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		Player damaged = (Player) event.getEntity();

		if (am.isPlayerInArena(damaged)) {
			event.setCancelled(true);
			return;
		}
	}

}
