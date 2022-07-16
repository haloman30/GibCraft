package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class CommandPreprocess implements Listener {

	private ArenaManager am;

	public CommandPreprocess(GibCraft plugin) {
		this.am = plugin.getArenaManager();
	}

	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();

		if (p.hasPermission("gib.admin"))
			return;

		if (!am.isPlayerInArena(p))
			return;

		if (am.isSpectating(p)) {
			if (!event.getMessage().equalsIgnoreCase("/gib spectate " + am.getPlayerArena(p).getName())
					&& !event.getMessage().equalsIgnoreCase("/gibcraft spectate " + am.getPlayerArena(p).getName())) {
				p.sendMessage(Commands.prefix() + "§cYou can't do that inside the arena.");
				event.setCancelled(true);
			}
		} else {
			if (!event.getMessage().equalsIgnoreCase("/gib leave")
					&& !event.getMessage().equalsIgnoreCase("/gibcraft leave")) {
				p.sendMessage(Commands.prefix() + "§cYou can't do that inside the arena.");
				event.setCancelled(true);
			}
		}

	}

}