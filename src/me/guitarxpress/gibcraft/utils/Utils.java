package me.guitarxpress.gibcraft.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Stats;

public class Utils {

	public static boolean playerInArea(Location start, Location end, Player player) {

		int topBlockX = (start.getBlockX() < end.getBlockX() ? end.getBlockX() : start.getBlockX());
		int bottomBlockX = (start.getBlockX() > end.getBlockX() ? end.getBlockX() : start.getBlockX());

		int topBlockY = (start.getBlockY() < end.getBlockY() ? end.getBlockY() : start.getBlockY());
		int bottomBlockY = (start.getBlockY() > end.getBlockY() ? end.getBlockY() : start.getBlockY());

		int topBlockZ = (start.getBlockZ() < end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());
		int bottomBlockZ = (start.getBlockZ() > end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());

		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();

		if (x >= bottomBlockX && x <= topBlockX && y >= bottomBlockY && y <= topBlockY && z >= bottomBlockZ
				&& z <= topBlockZ) {
			return true;
		}
		return false;
	}

	public static boolean hitPlayer(Location start, double maxDistance, World world, Player p) {
		RayTraceResult result = world.rayTrace(start, p.getLocation().getDirection(), maxDistance,
				FluidCollisionMode.NEVER, true, 0.1,
				(e) -> e != null && e instanceof Player && !e.getName().equals(p.getName()));

		if (result == null)
			return false;

		// Uncomment for debug
		if (result.getHitEntity() instanceof Player) {
//			p.getWorld().spawnParticle(Particle.REDSTONE, result.getHitPosition().toLocation(world), 10, 0, 0, 0, 1,
//					dustRed);
//			Bukkit.getServer().getConsoleSender().sendMessage("HIT PLAYER: " + result.getHitEntity().getCustomName());
			return true;
		} else {
//			p.getWorld().spawnParticle(Particle.REDSTONE, result.getHitPosition().toLocation(world), 10, 0, 0, 0, 1,
//					dustAqua);
//			Bukkit.getServer().getConsoleSender().sendMessage("HIT SOMETHING: " + result.toString());
			return false;
		}
	}

	public static RayTraceResult getHitResult(Location start, double maxDistance, World world, Player p) {
		RayTraceResult result = world.rayTrace(start, p.getLocation().getDirection(), maxDistance,
				FluidCollisionMode.NEVER, true, 0.1, (e) -> e != null && !e.getName().equals(p.getName()));
		return result;
	}

	public static void spawnFireworks(Location loc, Color color) {
		Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		fwm.setPower(1);
		fwm.addEffect(FireworkEffect.builder().withColor(color).with(Type.BALL).build());

		fw.setFireworkMeta(fwm);
		fw.detonate();
	}

	public static boolean isGun(ItemStack item) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore())
			if (item.getItemMeta().getLore().get(0).contains("Laser"))
				return true;
		return false;
	}

	public static List<Integer> getSortedPointsLeaderboard(Arena arena) {
		List<Integer> scores = new ArrayList<>();
		List<String> players = new ArrayList<>();

		Map<Integer, String> pScores = new HashMap<>();

		for (Player p : arena.getPlayers()) {
			pScores.put(arena.getScores().get(p), p.getName());
			scores.add(arena.getScores().get(p));
			players.add(p.getName());
		}

		Collections.sort(scores, Collections.reverseOrder());
		return scores;
	}

	public static List<Player> getSortedPlayerLeaderboard(Arena arena, List<Integer> scores) {
		List<Player> sortedPlayers = new ArrayList<>();

		for (Integer score : scores)
			for (Player player : arena.getPlayers())
				if (arena.getScores().get(player) == score)
					sortedPlayers.add(player);

		return sortedPlayers;
	}

	public static void spawnParticlesBetweenLocations(Location start, Location end, ItemStack item,
			Map<String, List<Particle.DustOptions>> particleDustMap) {
		double interval = 1 / 3d;
		double distance = end.distance(start);
		Vector difference = end.toVector().subtract(start.toVector());
		double points = Math.ceil(distance / interval);
		difference.multiply(1d / points);
		Location location = start.clone().add(start.clone().getDirection().multiply(0.5));
		for (int i = 0; i <= points; i++) {
			start.getWorld().spawnParticle(Particle.REDSTONE, location.clone(), 2, 0, 0, 0, 1,
					particleDustMap.get(item.getItemMeta().getLore().get(0)).get(0));
			start.getWorld().spawnParticle(Particle.REDSTONE, location.clone(), 2, 0, 0, 0, 1,
					particleDustMap.get(item.getItemMeta().getLore().get(0)).get(1));
			location.add(difference);
		}
	}

	public static void spawnParticlesBetweenLocations(Location start, Location end, Particle.DustOptions dustOptions) {
		double interval = 1 / 3d;
		double distance = end.distance(start);
		Vector difference = end.toVector().subtract(start.toVector());
		double points = Math.ceil(distance / interval);
		difference.multiply(1d / points);
		Location location = start.clone().add(start.clone().getDirection().multiply(0.5));
		for (int i = 0; i <= points; i++) {
			start.getWorld().spawnParticle(Particle.REDSTONE, location.clone(), 5, 0, 0, 0, 1, dustOptions);
			location.add(difference);
		}
	}

	public static ChatColor intToColor(int i) {
		switch (i) {
		case 0:
			return ChatColor.DARK_RED;
		case 1:
			return ChatColor.BLUE;
		case 2:
			return ChatColor.YELLOW;
		case 3:
			return ChatColor.GREEN;
		}
		return ChatColor.WHITE;
	}

	public static String intToColorString(int i) {
		switch (i) {
		case 0:
			return "Red";
		case 1:
			return "Blue";
		case 2:
			return "Yellow";
		case 3:
			return "Green";
		}
		return "";
	}

	public static String intToColorCode(int i) {
		switch (i) {
		case 0:
			return "§4";
		case 1:
			return "§3";
		case 2:
			return "§e";
		case 3:
			return "§a";
		}
		return "";
	}

	public static String getNameFromString(String string) {
		String s = string;
		if (s.length() > 2)
			s = s.substring(2, s.length()); // Remove "§b" from the line in order to get arena name
		return s;
	}

	public static void addFrag(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseKills();
		statsMap.put(p.getName(), stats);
	}

	public static void addDeath(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseDeaths();
		statsMap.put(p.getName(), stats);
	}

	public static void addGamePlayed(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseGamesPlayed();
		statsMap.put(p.getName(), stats);
	}

	public static void addWin(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseWins();
		statsMap.put(p.getName(), stats);
	}

	public static void addLoss(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseLosses();
		statsMap.put(p.getName(), stats);
	}

	public static void addShotFired(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseShotsFired();
		statsMap.put(p.getName(), stats);
	}

	public static void addShotHit(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseShotsHit();
		statsMap.put(p.getName(), stats);
	}

	public static void addHeadshot(Player p, Map<String, Stats> statsMap) {
		Stats stats = statsMap.get(p.getName());
		stats.increaseHeadshots();
		statsMap.put(p.getName(), stats);
	}

	public static void increaseArenaScore(Player p, Arena arena) {
		int score = arena.getScores().get(p);
		arena.addScore(p, ++score);
	}

}
