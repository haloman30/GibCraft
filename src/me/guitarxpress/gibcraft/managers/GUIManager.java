package me.guitarxpress.gibcraft.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.utils.Utils;

public class GUIManager implements Listener {

	private GibCraft plugin;

	private Inventory teamsGUI;

	public GUIManager(GibCraft plugin) {
		this.plugin = plugin;
	}

	public void createTeamsGUI(String arenaName) {
		teamsGUI = Bukkit.createInventory(null, 9, "§0" + arenaName + " - Choose Team");
		for (int i = 0; i < teamsGUI.getSize(); i++) {
			switch (i) {
			case 3:
				teamsGUI.setItem(i, ItemManager.redTeam);
				break;
			case 5:
				teamsGUI.setItem(i, ItemManager.blueTeam);
				break;
			default:
				teamsGUI.setItem(i, ItemManager.filler);
				break;
			}
		}
	}

	public void openTeamsGUI(Player p, String arenaName) {
		createTeamsGUI(arenaName);
		p.openInventory(teamsGUI);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		if (event.getClickedInventory() == null)
			return;

		if (!event.getClickedInventory().equals(teamsGUI))
			return;

		event.setCancelled(true);

		String arenaName = Utils.getArenaNameFromString(event.getView().getTitle());

		ItemStack item = event.getCurrentItem();

		if (item == null || item.equals(ItemManager.filler))
			return;

		if (item.equals(ItemManager.redTeam)) {
			if (plugin.getArenaManager().getArena(arenaName).getTeamPlayers("Red").size() < 2)
				plugin.getArenaManager().addPlayerToArena(p, plugin.getArenaManager().getArena(arenaName), "Red");
			else
				p.sendMessage(Commands.prefix() + "§cThat team is full.");
		} else if (item.equals(ItemManager.blueTeam)) {
			if (plugin.getArenaManager().getArena(arenaName).getTeamPlayers("Red").size() < 2)
				plugin.getArenaManager().addPlayerToArena(p, plugin.getArenaManager().getArena(arenaName), "Blue");
			else
				p.sendMessage(Commands.prefix() + "§cThat team is full.");
		}
		p.closeInventory();
	}

}
