package me.guitarxpress.gibcraft.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class PlayerInteract implements Listener {

	private GibCraft plugin;
	private ArenaManager am;
	private GameManager gm;

	Particle.DustOptions dustRed = new Particle.DustOptions(Color.RED, (float) 1.0);
	Particle.DustOptions dustDarkRed = new Particle.DustOptions(Color.MAROON, (float) 1.0);
	Particle.DustOptions dustAqua = new Particle.DustOptions(Color.AQUA, (float) 1.0);
	Particle.DustOptions dustTeal = new Particle.DustOptions(Color.TEAL, (float) 1.0);
	Particle.DustOptions dustYellow = new Particle.DustOptions(Color.YELLOW, (float) 1.0);
	Particle.DustOptions dustOrange = new Particle.DustOptions(Color.ORANGE, (float) 1.0);
	Particle.DustOptions dustLime = new Particle.DustOptions(Color.LIME, (float) 1.0);
	Particle.DustOptions dustGreen = new Particle.DustOptions(Color.GREEN, (float) 1.0);
	Particle.DustOptions dustWhite = new Particle.DustOptions(Color.WHITE, (float) 1.0);
	Particle.DustOptions dustBlack = new Particle.DustOptions(Color.BLACK, (float) 1.0);

	public static Map<Player, Long> cooldowns = new HashMap<Player, Long>();

	private Map<String, List<Particle.DustOptions>> particleDust = new HashMap<>();

//	List<String> deathMessages1 = Arrays.asList("§f%s died to %s. ", "§f%s was destroyed by %s! ",
//			"§f%s was killed by %s! ", "§f%s was obliterated by %s! ", "§f%s has fallen to %s. ", "§f%s gibbed %s. ");
//	List<String> deathMessages2 = Arrays.asList("Perhaps they should stick to building.", "Aim diff.", "Sit.",
//			"Clearly they struggle with clicking.", "Someone tell them to turn on their monitor.",
//			"Are they even trying?", "Yikes.", "I think they might need glasses.", "Lmao.", "Hold that L.");

	public PlayerInteract(GibCraft plugin) {
		this.plugin = plugin;
		this.am = plugin.getArenaManager();
		this.gm = plugin.getGameManager();
		particleDust.put("Red", new ArrayList<>(Arrays.asList(dustRed, dustDarkRed)));
		particleDust.put("Blue", new ArrayList<>(Arrays.asList(dustAqua, dustTeal)));
		particleDust.put("Yellow", new ArrayList<>(Arrays.asList(dustYellow, dustOrange)));
		particleDust.put("Green", new ArrayList<>(Arrays.asList(dustLime, dustGreen)));
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		if (!am.isPlayerInArena(p))
			return;

		if (am.getPlayerArena(p).getStatus() != Status.ONGOING)
			return;

		ItemStack item = event.getItem();

		if (Utils.isGun(item)) {
			event.setCancelled(true);

			if (p.getGameMode() == GameMode.SPECTATOR)
				return;

			if (!cooldowns.containsKey(p) || System.currentTimeMillis() >= cooldowns.get(p)) {
				String killer = p.getName();
				Stats damagerStats = plugin.playerStats.get(killer);
				damagerStats.increaseShotsFired();

				if (GibCraft.playerPowerup.containsKey(p) && GibCraft.playerPowerup.get(p).getId() == 1) {
					p.setCooldown(Material.IRON_HOE, 8);
					cooldowns.put(p, System.currentTimeMillis() + 400);
				} else {
					p.setCooldown(Material.IRON_HOE, 16);
					cooldowns.put(p, System.currentTimeMillis() + 800);
				}
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, .35f, 2f);
//				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, .5f, 2f);
				p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, .35f, 2f);
				Location startLoc = p.getLocation().add(new Vector(0, 1.5, 0));
				RayTraceResult result = Utils.getHitResult(startLoc, 100, p.getLocation().getWorld(), p);

				Location hitLoc = null;
				if (result != null) {
					hitLoc = result.getHitPosition().clone().toLocation(startLoc.getWorld());
				} else {
					hitLoc = startLoc.clone().add(startLoc.clone().getDirection().multiply(50));
				}
				double interval = 1 / 3d;
				double distance = hitLoc.distance(startLoc);
				Vector difference = hitLoc.toVector().subtract(startLoc.toVector());
				double points = Math.ceil(distance / interval);
				difference.multiply(1d / points);

				Location location = startLoc.clone().add(startLoc.clone().getDirection().multiply(0.5));
				for (int i = 0; i <= points; i++) {
					p.getWorld().spawnParticle(Particle.REDSTONE, location.clone(), 5, 0, 0, 0, 1,
							particleDust.get(item.getItemMeta().getLore().get(1)).get(0));
					p.getWorld().spawnParticle(Particle.REDSTONE, location.clone(), 5, 0, 0, 0, 1,
							particleDust.get(item.getItemMeta().getLore().get(1)).get(1));
					location.add(difference);
				}
				if (Utils.hitPlayer(p.getLocation().add(new Vector(0, 1.5, 0)), 100, p.getLocation().getWorld(), p)) {
					damagerStats.increaseShotsHit();

					Player hit = (Player) result.getHitEntity();
					Stats damagedStats = plugin.playerStats.get(hit.getName());

					Location hitPos = result.getHitPosition().toLocation(p.getWorld());
					double eyeY = hit.getLocation().getY() + hit.getEyeHeight();

					if (hitPos.getY() >= eyeY - 0.3) {
						p.sendTitle("", "§4HEADSHOT", 2, 20, 2);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						damagerStats.increaseHeadshots();
					}

					Utils.spawnFireworks(hit.getLocation().clone().add(new Vector(0, 1.5, 0)), Color.WHITE);
					p.getWorld().playSound(hit.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 10f, 1f);
					Random random = new Random();

					Arena arena = am.getPlayerArena(p);

					System.out.println("deathMessages1: " + Arrays.deepToString(plugin.deathMessages1.toArray()));
					System.out.println("deathMessages2: " + Arrays.deepToString(plugin.deathMessages2.toArray()));

					if (!plugin.deathMessages1.isEmpty() && !plugin.deathMessages2.isEmpty())
						for (Player player : arena.getAllPlayers()) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&',
									String.format(
											plugin.deathMessages1.get(random.nextInt(plugin.deathMessages1.size()))
													+ " "
													+ ((random.nextInt(2) == 1)
															? plugin.deathMessages2
																	.get(random.nextInt(plugin.deathMessages2.size()))
															: ""),
											hit.getName(), killer)));
						}

					int score = arena.getScores().get(p);
					arena.getScores().put(p, ++score);

					damagedStats.increaseDeaths();
					damagerStats.increaseKills();
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
					plugin.playerStats.put(hit.getName(), damagedStats);
					plugin.playerStats.put(killer, damagerStats);
					if (arena.getScores().get(p) >= am.maxFrags)
						am.arenaTimer.put(arena, 0);

					hit.setGameMode(GameMode.SPECTATOR);
					hit.sendTitle("", "§eRespawning in §6" + gm.respawnTime + " §eseconds.", 2, 20, 2);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
						if (arena.getStatus() == Status.ONGOING)
							gm.respawnPlayer(hit, am.getPlayerArena(hit));
					}, gm.respawnTime * 20);
				} else {
					for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 2, 2, 2)) {
						if (!(e instanceof Player))
							continue;

						gm.knockedBy.put((Player) e, p);

						Vector dir;
						if (e.getLocation().distance(hitLoc) != 0)
							dir = e.getLocation().subtract(hitLoc).toVector().normalize().multiply(2);
						else
							dir = new Vector(0, 1, 0).add(difference).multiply(2);

						if (dir.getY() < 0) {
							e.setVelocity(new Vector(dir.getX(), 0, dir.getZ()));
						} else {
							e.setVelocity(new Vector(dir.getX(), 1.5, dir.getZ()));
						}
					}
				}
			}
		}
	}
}
