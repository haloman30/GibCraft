package me.guitarxpress.gibcraft.events;

import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.PowerUp;
import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class PlayerMove implements Listener {

	private GibCraft plugin;
	private GameManager gm;
	private ArenaManager am;

	private Random rnd;

	public PlayerMove(GibCraft plugin) {
		this.plugin = plugin;
		this.gm = plugin.getGameManager();
		this.am = plugin.getArenaManager();
		rnd = new Random();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if (!am.isPlayerInArena(p))
			return;

		Arena arena = am.getPlayerArena(p);

		// Freeze player when game is starting
		if (arena.getStatus() == Status.STARTUP) {
			p.teleport(new Location(event.getFrom().getWorld(), event.getFrom().getX(), event.getFrom().getY(),
					event.getFrom().getZ(), event.getTo().getYaw(), event.getTo().getPitch()));
			return;
		}
		//////////

		if (arena.getStatus() == Status.ONGOING) {
			// Check if player got knocked but fell without dying
			if (gm.knockedBy.containsKey(p) && System.currentTimeMillis() >= gm.knockedTimeout.get(p))
				if (p.getWorld().getBlockAt(p.getLocation().subtract(new Vector(0, 1, 0))).getType() != Material.AIR)
					gm.knockedBy.remove(p);

			// Player goes outside bounds
			if (!Utils.playerInArea(arena.getBoundaries()[0], arena.getBoundaries()[1], p)) {
				if (p.getGameMode() == GameMode.SPECTATOR) {
					p.teleport(arena.selectRandomSpawn());
				} else {
					// Check if player was knocked outside bounds
					if (gm.knockedBy.containsKey(p) && !gm.knockedBy.get(p).getName().equals(p.getName())) {
						Player killer = gm.knockedBy.get(p);
						if (arena.getMode() == Mode.FFA)
							Utils.increasePlayerScore(killer, arena);
						else
							Utils.increaseTeamScore(arena, arena.getPlayerTeam(killer));

						Utils.addFrag(killer, plugin.playerStats);
						for (Player player : arena.getAllPlayers()) {
							player.sendMessage("�f" + killer.getName() + " knocked " + p.getName() + " off the arena!");
						}
						gm.knockedBy.remove(p);
					} else {
						for (Player player : arena.getAllPlayers()) {
							player.sendMessage("�f" + p.getName() + " fell off. Literally.");
						}
					}
					p.setGameMode(GameMode.SPECTATOR);
					
					Color firework_color = Color.WHITE;
					
					try
					{
						if (p.getInventory().getItemInMainHand() != null)
						{
							firework_color = Utils.colorFromString(p.getInventory().getItemInMainHand().getItemMeta().getLore().get(0));
						}
					}
					catch (Exception ex) {}
					
					Utils.spawnFireworks(p.getLocation().clone().add(new Vector(0, 1.5, 0)), firework_color);
					
					gm.respawnPlayer(p, arena);
				}
			}
		}
		/////////////////

		// Pickup PowerUp
		if (p.getGameMode() != GameMode.ADVENTURE)
			return;

		List<ArmorStand> powerups = arena.getPowerups();
		ArmorStand toRemove = null;
		for (ArmorStand as : powerups) {
			if (p.getLocation().distance(as.getLocation()) <= 1.5) {
				int r = rnd.nextInt(plugin.powerups.size());
				PowerUp pu = plugin.powerups.get(r);
				toRemove = as;
				pu.applyEffect(p);
				p.sendMessage(Commands.prefix() + "�ePicked up PowerUp: �6" + pu.getName());
//				p.playSound(as.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
				p.playSound(as.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f);
			}
		}
		if (toRemove != null)
			arena.removePowerup(toRemove);
		/////////////////
	}
}
