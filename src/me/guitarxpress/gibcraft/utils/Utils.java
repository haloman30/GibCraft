package me.guitarxpress.gibcraft.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.RayTraceResult;

import me.guitarxpress.gibcraft.Arena;

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
	
	// Ray Trace
	public static boolean hitPlayer(Location start, double maxDistance, World world, Player p) {
		RayTraceResult result = world.rayTrace(start, p.getLocation().getDirection(), maxDistance,
				FluidCollisionMode.NEVER, true, 0.1,
				(e) -> e != null && e instanceof Player && !e.getName().equals(p.getName()));

		if (result == null)
			return false;

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

	public static boolean isSecondary(ItemStack item) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore())
			if (item.getItemMeta().getLore().get(0).contains("Blast"))
				return true;
		return false;
	}
	
	public static List<Player> getSortedPlayers(Arena arena) {
		Map<String, Integer> leaderboard = getSortedLeaderboard(arena);
		List<Player> sortedPlayers = new ArrayList<>(); 
		for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
			sortedPlayers.add(Bukkit.getPlayer(entry.getKey()));
		}
		return sortedPlayers;
	}
	
	public static Map<String, Integer> getSortedLeaderboard(Arena arena) {
		List<Integer> scores = new ArrayList<>();
		Map<Integer, String> pScores = new HashMap<>();
		
		for (Player p : arena.getPlayers()) {
			pScores.put(arena.getScores().get(p), p.getName());
			scores.add(arena.getScores().get(p));
		}
		
		Collections.sort(scores);

		Map<String, Integer> sortedLeaderboard = new HashMap<>();
		
		for (Map.Entry<Integer, String> score : pScores.entrySet()) {
			sortedLeaderboard.put(score.getValue(), score.getKey());
		}
		return sortedLeaderboard;
	}
	
	public static Player getWinner(Arena arena) {
		return getSortedPlayers(arena).get(0);
	}
	
}
