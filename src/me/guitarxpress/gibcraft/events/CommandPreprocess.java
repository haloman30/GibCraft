package me.guitarxpress.gibcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Language;
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
		
		String[] valid_commands = {
			"/gibcraft spectate",
			"/gibcraft leave",
			"/gib spectate",
			"/gib leave"
		};
		
		boolean command_permitted = false;
		
		for (String valid_command : valid_commands)
		{
			if (event.getMessage().toLowerCase().startsWith(valid_command))
			{
				command_permitted = true;
			}
		}
		
		if (!command_permitted)
		{
			p.sendMessage(Language.error_command_blocked);
			event.setCancelled(true);
		}
	}

}