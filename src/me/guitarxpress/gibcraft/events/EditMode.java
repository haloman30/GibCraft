package me.guitarxpress.gibcraft.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.ItemManager;

public class EditMode implements Listener {

	@SuppressWarnings("unused")
	private GibCraft plugin;
	private ArenaManager am;

	public static Map<Player, ItemStack[]> oldInventory = new HashMap<Player, ItemStack[]>();

	public EditMode(GibCraft plugin) {
		this.plugin = plugin;
		this.am = plugin.getArenaManager();
	}

	public static void toggleEditMode(Player player, Arena arena) {
		ItemStack[] editMode = new ItemStack[ItemManager.editMode.length];

		for (int i = 0; i < ItemManager.editMode.length; i++) {
			if (ItemManager.editMode[i] != null) {
				ItemMeta meta = ItemManager.editMode[i].getItemMeta();
				List<String> lore = meta.getLore();
				lore.add(arena.getName());
				meta.setLore(lore);
				ItemStack item = ItemManager.editMode[i].clone();
				item.setItemMeta(meta);
				editMode[i] = item;
			}
		}

		if (!oldInventory.containsKey(player)) {
			oldInventory.put(player, player.getInventory().getContents());
			player.getInventory().setContents(editMode);
		} else {
			player.getInventory().setContents(oldInventory.get(player));
			oldInventory.remove(player);
		}
	}

	/////////// EVENTS ///////////

	@EventHandler
	public void onPlayerInteraction(PlayerInteractEvent event) {
		if (event.getItem() == null)
			return;

		ItemStack item = event.getItem();
		Player p = event.getPlayer();

		if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
			if (item.getItemMeta().getLore().get(0).equals("§6Right Click §7to exit edit mode.")) {
				event.setCancelled(true);
				toggleEditMode(p,
						am.getArena(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)));
				p.sendMessage(Commands.prefix() + "§eRemember to set a lobby and change arena status.");
			} else if (item.getItemMeta().getLore().get(0).equals("§6Left Click §7to set corner 1.")) {
				event.setCancelled(true);
				Arena arena = am.getArena(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1));
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					Location[] bounds = { event.getClickedBlock().getLocation(), arena.getBoundaries()[1] };
					arena.setBoundaries(bounds);
					p.sendMessage(Commands.prefix() + "§eSet arena corner 1");
				} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Location[] bounds = { arena.getBoundaries()[0], event.getClickedBlock().getLocation() };
					arena.setBoundaries(bounds);
					p.sendMessage(Commands.prefix() + "§eSet arena corner 2");
				}
			}

		}

	}

}
