package me.guitarxpress.gibcraft.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.utils.Utils;

public class GameManager {

	private GibCraft plugin;

	private int timeToStart = 3;
	public Map<Arena, Integer> timeToStartMap = new HashMap<>();
	private int respawnTime;

	int fadeIn = 2;
	int stay = 1 * 20;
	int fadeOut = 2;

	public Map<Player, Player> knockedBy = new HashMap<>();
	public Map<Player, Long> knockedTimeout = new HashMap<>();

	public GameManager(GibCraft plugin) {
		this.plugin = plugin;
	}

	public void start(Arena arena) 
	{
		arena.setStatus(Status.STARTUP);
		timeToStartMap.put(arena, timeToStart);
	}

	public boolean hasEnoughPlayers(Arena arena) {
		return arena.getPlayerCount() >= arena.getMode().minPlayers();
	}

	public void sendStartNotification(Arena arena, String string) {
		for (Player player : arena.GetPlayersAndSpectators()) {
			player.sendTitle("§6" + string, "", fadeIn, stay, fadeOut);
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, string.equals("Gib!") ? 2f : 1f);
		}
	}

	public void respawnPlayer(Player player, Arena arena) {
		Utils.addDeath(player, plugin.playerStats);
		player.sendTitle("", "§eRespawning in §6" + respawnTime + " §eseconds.", fadeIn, stay, fadeOut);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (arena.getStatus() == Status.ONGOING) {
				player.setHealth(20);
				player.setFoodLevel(20);
				player.teleport(arena.selectRandomSpawn());
				player.setGameMode(GameMode.ADVENTURE);
			}
		}, respawnTime * 20);
	}
	
	public int GetRespawnTime(Arena arena)
	{
		if (arena == null || !arena.respawn_time_override)
		{
			return respawnTime;
		}
		
		return arena.respawn_time;
	}
	
	public void SetDefaultRespawnTime(int respawn_time)
	{
		respawnTime = respawn_time;
	}
}
