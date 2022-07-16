package me.guitarxpress.gibcraft.events;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.PowerUp;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class PlayerMove implements Listener {

	private GibCraft plugin;
	private GameManager gm;
	private ArenaManager am;

//	private Map<Player, Long> dashCooldown = new HashMap<Player, Long>();

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
			if (gm.knockedBy.containsKey(p)) {
				if (p.getWorld().getBlockAt(p.getLocation().subtract(new Vector(0, 1, 0))).getType() != Material.AIR) {
					gm.knockedBy.remove(p);
				}
			}

			// Player goes outside bounds
			if (!Utils.playerInArea(arena.getBoundaries()[0], arena.getBoundaries()[1], p)) {
				if (p.getGameMode() == GameMode.SPECTATOR) {
					p.teleport(arena.selectRandomSpawn());
				} else {
					// Check if player was knocked outside bounds
					if (gm.knockedBy.containsKey(p) && !gm.knockedBy.get(p).getName().equals(p.getName())) {
						Player killer = gm.knockedBy.get(p);
						int score = arena.getScores().get(killer);
						arena.addScore(killer, ++score);
						Stats killerStats = plugin.playerStats.get(killer.getName());
						killerStats.increaseKills();
						plugin.playerStats.put(killer.getName(), killerStats);
						for (Player player : arena.getAllPlayers()) {
							player.sendMessage("§f" + killer.getName() + " knocked " + p.getName() + " off the arena!");
						}
						gm.knockedBy.remove(p);
					} else {
						for (Player player : arena.getAllPlayers()) {
							player.sendMessage("§f" + p.getName() + " fell off. Literally.");
						}
					}
					p.setGameMode(GameMode.SPECTATOR);
					spawnFireworks(p.getLocation().clone().add(new Vector(0, 1.5, 0)), Color.WHITE);
					p.sendTitle("", "§eRespawning in §6" + gm.respawnTime + " §eseconds.", 2, 20, 2);
					
					Stats damagedStats = plugin.playerStats.get(p.getName());
					damagedStats.increaseDeaths();
					plugin.playerStats.put(p.getName(), damagedStats);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
						if (arena.getStatus() == Status.ONGOING)
							gm.respawnPlayer(p, arena);
					}, gm.respawnTime * 20);
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
			if (p.getLocation().distance(as.getLocation()) <= 1) {
				int r = rnd.nextInt(plugin.powerups.size());
				PowerUp pu = plugin.powerups.get(r);
				toRemove = as;
				pu.applyEffect(p);
				p.sendMessage(Commands.prefix() + "§ePicked up PowerUp: §6" + pu.getName());
				p.playSound(as.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50f, 1f);
			}
		}
		if (toRemove != null)
			arena.removePowerup(toRemove);
	}

	private void spawnFireworks(Location loc, Color color) {
		Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		fwm.setPower(1);
		fwm.addEffect(FireworkEffect.builder().withColor(color).with(Type.BALL).build());

		fw.setFireworkMeta(fwm);
		fw.detonate();
	}

}
