package me.guitarxpress.gibcraft.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class PlayerQuit implements Listener {

	private ArenaManager am;

	public PlayerQuit(GibCraft plugin) {
		this.am = plugin.getArenaManager();
	}

	@EventHandler
	public void onQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();

		if (am.isPlayerInArena(p))
			if (am.isSpectating(p))
				Bukkit.dispatchCommand(p, "gib spectate " + am.getPlayerArena(p).getName());
			else
				Bukkit.dispatchCommand(p, "gib leave");
	}

}
