package me.guitarxpress.gibcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;

public class PlayerJoin implements Listener {

	private GibCraft plugin;

	public PlayerJoin(GibCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!plugin.playerStats.containsKey(event.getPlayer().getName()))
			if (plugin.isLoadFromSQL() && plugin.getSQL().isConnected())
				plugin.getSQLGetter().loadPlayerValues(event.getPlayer().getUniqueId());
			else
				plugin.playerStats.put(event.getPlayer().getName(),
						new Stats(event.getPlayer().getUniqueId().toString(), 0, 0, 0, 0, 0, 0, 0, 0));
			
	}

}
