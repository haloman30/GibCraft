package me.guitarxpress.gibcraft.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class SignEvents implements Listener {

	private ArenaManager am;

	public static List<Location> signsLoc;

	public SignEvents(GibCraft plugin) {
		signsLoc = new ArrayList<Location>();
		this.am = plugin.getArenaManager();
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (event.getPlayer().hasPermission("gib.signs")) {
			if (event.getBlock().getType().toString().toLowerCase().contains("sign")) {
				if (!event.getLine(0).equals("[gibcraft]") && !event.getLine(0).equals("[gib]"))
					return;

				if (event.getLine(1).isEmpty()) {
					if (event.getLine(2).equals("leave")) {
						event.setLine(0, "§7[§4Gib§6Craft§7]");
						event.setLine(2, "§cLeave Queue");
						return;
					}
				}

				String aName = event.getLine(1);

				if (am.exists(aName)) {
					World world = event.getBlock().getWorld();
					int x = event.getBlock().getX();
					int y = event.getBlock().getY();
					int z = event.getBlock().getZ();

					event.setLine(0, "§7[§4Gib§6Craft§7]");
					event.setLine(1, "§b" + aName);
					event.setLine(2, "§e" + am.getArena(aName).getPlayerCount() + "/4");

					Location loc = new Location(world, x, y, z);
					signsLoc.add(loc);
				} else {
					event.getPlayer().sendMessage("§7[§4Gib§6Craft§7] §cInvalid Arena.");
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
		}
	}

	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		if (event.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) event.getBlock().getState();
			if (signsLoc.contains(sign.getLocation())) {
				if (event.getPlayer().hasPermission("gib.signs")) {
					signsLoc.remove(sign.getLocation());
				} else {
					event.getPlayer().sendMessage("§7[§4Gib§6Craft§7] §cSorry! You can't break these signs.");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.OAK_WALL_SIGN)
				return;

			Sign sign = (Sign) event.getClickedBlock().getState();

			if (!sign.getLine(0).equals("§7[§4Gib§6Craft§7]"))
				return;

			if (sign.getLine(1).isEmpty()) {
				if (sign.getLine(2).equals("§cLeave Queue")) {
					Bukkit.dispatchCommand(event.getPlayer(), "gib leave");
					if (am.isPlayerInArena(event.getPlayer()))
						updateSign(sign, am, am.getPlayerArena(event.getPlayer()).getName());
					return;
				}
			}
			
			String s = Utils.getNameFromString(sign.getLine(1));
			if (am.exists(s)) {
				Arena arena = am.getArena(s);
				if (arena.getStatus() == Status.JOINABLE || arena.getStatus() == Status.STARTING) {
					Bukkit.dispatchCommand(event.getPlayer(), "gib join " + s);
				} else if (arena.getStatus() == Status.ONGOING) {
					am.addSpectatorToArena(event.getPlayer(), arena);
					event.getPlayer().sendMessage(Commands.prefix() + "§eTo leave use §6/gib spectate " + s + "§e.");
				} else if (arena.getStatus() == Status.SETTING_UP) {
					event.getPlayer().sendMessage(Commands.prefix() + "§cThis arena is being setup.");
				} else if (arena.getStatus() == Status.ENDED || arena.getStatus() == Status.CANCELLED) {
					event.getPlayer().sendMessage(Commands.prefix() + "§cThis arena is restarting.");
				}
				updateSign(sign, am, s);
			}
		}
	}

	public static void updateSign(Sign sign, ArenaManager am, String arena) {
		sign.setLine(0, "§7[§4Gib§6Craft§7]");
		sign.setLine(1, "§b" + arena);
		sign.setLine(2, "§e" + am.getArena(arena).getPlayerCount() + "/4 - " + am.getArena(arena).getMode().toString());
		switch (am.getArena(arena).getStatus()) {
		case SETTING_UP:
			sign.setLine(3, "§6SETTING UP");
			break;
		case STARTING:
			sign.setLine(3, "§aSTARTING");
			break;
		case JOINABLE:
			sign.setLine(3, "§aJOINABLE");
			break;
		case ONGOING:
			sign.setLine(3, "§7SPECTATE");
			break;
		case CANCELLED:
			sign.setLine(3, "§cCANCELLED");
			break;
		case ENDED:
			sign.setLine(3, "§cENDED");
			break;
		case UNAVAILABLE:
			sign.setLine(3, "§cUNAVAILABLE");
			break;
		case STARTUP:
			sign.setLine(3, "§6STARTUP");
			break;
		default:
			sign.setLine(3, "§cCONTACT ADMIN");
			break;
		}
		sign.update();
	}
}
