package me.guitarxpress.gibcraft.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import me.guitarxpress.gibcraft.enums.Mode;
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

	public static Map<Player, Long> cooldowns = new HashMap<Player, Long>();

	private Map<String, List<Particle.DustOptions>> particleDust = new HashMap<>();

	public PlayerInteract(GibCraft plugin) {
		this.plugin = plugin;
		this.am = plugin.getArenaManager();
		this.gm = plugin.getGameManager();
		particleDust.put("Red Laser", new ArrayList<>(Arrays.asList(dustRed, dustDarkRed)));
		particleDust.put("Blue Laser", new ArrayList<>(Arrays.asList(dustAqua, dustTeal)));
		particleDust.put("Yellow Laser", new ArrayList<>(Arrays.asList(dustYellow, dustOrange)));
		particleDust.put("Green Laser", new ArrayList<>(Arrays.asList(dustLime, dustGreen)));
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
				// Increase shots fired
				Utils.addShotFired(p, plugin.playerStats);

				Arena arena = am.getPlayerArena(p);

				// Put player on cooldown
				if (GibCraft.playerPowerup.containsKey(p) && GibCraft.playerPowerup.get(p).getId() == 1) {
					p.setCooldown(Material.IRON_HOE, 8);
					cooldowns.put(p, System.currentTimeMillis() + 400);
				} else {
					p.setCooldown(Material.IRON_HOE, 16);
					cooldowns.put(p, System.currentTimeMillis() + 800);
				}

				// Play shooting sound
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, .35f, 2f);
				p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, .35f, 2f);

				// Get hit data
				Location startLoc = p.getLocation().add(new Vector(0, 1.5, 0));
				RayTraceResult result = Utils.getHitResult(startLoc, 100, p.getLocation().getWorld(), p);
				Location hitLoc = null;

				hitLoc = (result != null) ? (result.getHitPosition().clone().toLocation(startLoc.getWorld()))
						: (startLoc.clone().add(startLoc.clone().getDirection().multiply(50)));

				// Spawn particles from the player to the hit location
				Utils.spawnParticlesBetweenLocations(startLoc, hitLoc, item, particleDust);

				// Check if shooter hit a player
				if (Utils.hitPlayer(p.getLocation().add(new Vector(0, 1.5, 0)), 100, p.getLocation().getWorld(), p)) {
					// Get player hit
					Player hit = (Player) result.getHitEntity();

					if (hit.getGameMode() == GameMode.SPECTATOR)
						return;

					if (arena.getMode() == Mode.DUOS
							&& (arena.getPlayerTeam(hit) == arena.getPlayerTeam(p) && arena.getPlayerTeam(p) != null))
						return;

					// Increase shots hit
					Utils.addShotHit(p, plugin.playerStats);

					// Check if the shot was a headshot
					Location hitPos = result.getHitPosition().toLocation(p.getWorld());
					double eyeY = hit.getLocation().getY() + hit.getEyeHeight();
					if (hitPos.getY() >= eyeY - 0.3) {
						p.sendTitle("", "§4HEADSHOT", 2, 20, 2);
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
						Utils.addHeadshot(p, plugin.playerStats);
					}

					// Spawn a firework where player got hit
					Utils.spawnFireworks(hit.getLocation().clone().add(new Vector(0, 1.5, 0)),
							Utils.colorFromString(item.getItemMeta().getLore().get(0)));
					p.getWorld().playSound(hit.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 10f, 1f);

					Random random = new Random();

					// Get arena score and increase shooter's score by 1
					if (arena.getMode() == Mode.FFA)
						arena.increaseScore(p);
					else
						arena.increaseTeamScore(arena.getPlayerTeam(p));

					Utils.addFrag(p, plugin.playerStats);
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

					// Check if it was last kill needed to end game
					if (arena.getMode() == Mode.FFA) {
						if (arena.getScores().get(p) >= am.maxFrags)
							am.arenaTimer.put(arena, 0);
					} else {
						if (arena.getTeamScore("Red") >= am.maxFrags || arena.getTeamScore("Blue") >= am.maxFrags) {
							am.arenaTimer.put(arena, 0);
						}
					}

					// Send death message to all in game players
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
											hit.getName(), p.getName())));
							if (arena.getMode() == Mode.FFA)
								am.createScoreboardFFA(p);
							else
								am.createScoreboardDuos(p);
						}

					// Change player gamemode to spectator instead of actually killing them
					hit.setGameMode(GameMode.SPECTATOR);

					gm.respawnPlayer(hit, am.getPlayerArena(hit));
				} else {

					// If shot hit a block instead of a player, push back the players nearby
					for (Entity e : hitLoc.getWorld().getNearbyEntities(hitLoc, 2, 2, 2)) {
						if (!(e instanceof Player))
							continue;

						Player ePlayer = (Player) e;

						if (arena.getPlayerTeam(p) != null && arena.getPlayerTeam(ePlayer) == arena.getPlayerTeam(p)
								&& ePlayer.getName() != p.getName())
							return;

						gm.knockedBy.put(ePlayer, p);
						gm.knockedTimeout.put(ePlayer, System.currentTimeMillis() + 1000);

						Vector difference = startLoc.toVector().subtract(hitLoc.toVector());
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
