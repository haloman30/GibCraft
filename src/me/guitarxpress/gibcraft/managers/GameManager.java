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
import me.guitarxpress.gibcraft.utils.RepeatingTask;
import me.guitarxpress.gibcraft.utils.Utils;

public class GameManager {

	private GibCraft plugin;

	private int timeToStart = 3;
	private Map<Arena, Integer> timeToStartMap = new HashMap<>();
	public int respawnTime;

	int fadeIn = 2;
	int stay = 1 * 20;
	int fadeOut = 2;

	public Map<Player, Player> knockedBy = new HashMap<>();
	public Map<Player, Long> knockedTimeout = new HashMap<>();

	public GameManager(GibCraft plugin) {
		this.plugin = plugin;
	}

	public void start(Arena arena) {
		arena.setStatus(Status.STARTUP);
		timeToStartMap.put(arena, timeToStart);

		new RepeatingTask(plugin, 0, 1 * 20) {

			@Override
			public void run() {
				int timer = timeToStartMap.get(arena);
				switch (timer) {
				case 3:
					sendStartNotification(arena, "3");
					break;
				case 2:
					sendStartNotification(arena, "2");
					break;
				case 1:
					sendStartNotification(arena, "1");
					break;
				case 0:
					sendStartNotification(arena, "Gib!");
					if (hasEnoughPlayers(arena))
						arena.setStatus(Status.ONGOING);
					break;
				default:
					if (!hasEnoughPlayers(arena))
						arena.setStatus(Status.JOINABLE);
					cancel();
					break;
				}
				timeToStartMap.put(arena, --timer);
			}

		};
	}

	public boolean hasEnoughPlayers(Arena arena) {
		if (arena.getPlayerCount() >= arena.getMode().minPlayers())
			return true;
		return false;
	}

	public void sendStartNotification(Arena arena, String string) {
		for (Player player : arena.getAllPlayers()) {
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
}
