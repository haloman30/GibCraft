package me.guitarxpress.gibcraft.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.utils.RepeatingTask;

public class GameManager {

	private GibCraft plugin;

	private int timeToStart = 3;
	Map<Arena, Integer> timeToStartMap = new HashMap<>();
	public int respawnTime;

	int fadeIn = 2;
	int stay = 20;
	int fadeOut = 2;

	public Map<Player, Integer> kills = new HashMap<>();
	public Map<Player, Player> knockedBy = new HashMap<>();

	public Snowball projectile;
	public Snowball secProjectile;
	public int projectileId;
	public int secProjectileId;
	public Map<Player, Boolean> dashed = new HashMap<>();

	public GameManager(GibCraft plugin) {
		this.plugin = plugin;
	}

	public void start(Arena arena) {
		timeToStartMap.put(arena, timeToStart);
		arena.setStatus(Status.STARTUP);
		new RepeatingTask(plugin, 0, 1 * 20) {
			@Override
			public void run() {
				int timer = timeToStartMap.get(arena);
				switch (timer) {
				case 3:
					if (checkPlayers(arena)) {
						sendStartNotification(arena, "3");
					}
					break;
				case 2:
					if (checkPlayers(arena)) {
						sendStartNotification(arena, "2");
					}
					break;
				case 1:
					if (checkPlayers(arena)) {
						sendStartNotification(arena, "1");
					}
					break;
				case 0:
					if (checkPlayers(arena)) {
						sendStartNotification(arena, "Gib!");
						arena.setStatus(Status.ONGOING);
					}
					break;
				default:
					if (!checkPlayers(arena)) {
						arena.setStatus(Status.JOINABLE);
					}
					timeToStartMap.put(arena, timeToStart);
					cancel();
					break;
				}
				timeToStartMap.put(arena, --timer);
			}
		};
	}

	public boolean checkPlayers(Arena arena) {
		if (arena.getPlayerCount() >= arena.getMode().minPlayers())
			return true;
		return false;
	}

	public void sendStartNotification(Arena arena, String string) {
		for (Player player : arena.getAllPlayers()) {
			player.sendTitle("§6" + string, null, fadeIn, stay, fadeOut);
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
		}
	}

	public void respawnPlayer(Player player, Arena arena) {
		player.setHealth(20);
		player.setFoodLevel(20);
		player.teleport(arena.selectRandomSpawn());
		player.setGameMode(GameMode.ADVENTURE);
	}

//	public List<Player> getSortedPlayers(Arena arena) {
//		Map<Player, Integer> sortedMap = sortByValue(arena.getScores());
//		List<Player> scoreboard = new ArrayList<>();
//		for (Map.Entry<Player, Integer> entry : sortedMap.entrySet()) {
//			scoreboard.add(entry.getKey());
//		}
//		return scoreboard;
//	}
//
//	// I stole this method cant lie
//	private HashMap<Player, Integer> sortByValue(Map<Player, Integer> hm) {
//		// Create a list from elements of HashMap
//		List<Map.Entry<Player, Integer>> list = new LinkedList<Map.Entry<Player, Integer>>(hm.entrySet());
//
//		// Sort the list
//		Collections.sort(list, new Comparator<Map.Entry<Player, Integer>>() {
//			public int compare(Map.Entry<Player, Integer> o1, Map.Entry<Player, Integer> o2) {
//				return (o2.getValue()).compareTo(o1.getValue());
//			}
//		});
//
//		// put data from sorted list to hashmap
//		HashMap<Player, Integer> temp = new LinkedHashMap<Player, Integer>();
//		for (Map.Entry<Player, Integer> aa : list) {
//			temp.put(aa.getKey(), aa.getValue());
//		}
//		return temp;
//	}

	

}
