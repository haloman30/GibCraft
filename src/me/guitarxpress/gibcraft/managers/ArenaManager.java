package me.guitarxpress.gibcraft.managers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.Commands;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.events.EditMode;
import me.guitarxpress.gibcraft.utils.RepeatingTask;
import me.guitarxpress.gibcraft.utils.Utils;

public class ArenaManager {

	private GibCraft plugin;

	public List<Arena> arenas;
	public List<String> arenaNames;
	private Location lobby;
	private Map<Player, Arena> playerInArena;
	public Map<Arena, Integer> arenaCountdownTimer = new HashMap<>();
	public Map<Arena, Integer> arenaTimer = new HashMap<>();
	public int gameTime; // Time in seconds
	public int maxFrags;
	public int timeToStart;
	private GameManager gm;
	private Map<Player, ItemStack[]> oldInventory = new HashMap<>();
	private Map<Player, Float> oldExp = new HashMap<>();
	private Map<Player, GameMode> oldMode = new HashMap<>();
	private Map<Player, Integer> oldLevel = new HashMap<>();

	public ArenaManager(GibCraft plugin) {
		this.plugin = plugin;
		arenas = new ArrayList<>();
		arenaNames = new ArrayList<>();
		playerInArena = new HashMap<>();
		this.gm = plugin.getGameManager();

		for (Arena arena : arenas) {
			arenaTimer.put(arena, gameTime);
		}

	}

	public Location getLobby() {
		return lobby;
	}

	public void setLobby(Location loc) {
		this.lobby = loc;
	}

	public boolean isLobbySet() {
		return lobby != null;
	}

	/*
	 * @return false if lobby doesn't exist.
	 */
	public boolean toLobby(Player player) {
		if (!isLobbySet())
			return false;
		player.teleport(lobby);
		return true;
	}

	public Arena getPlayerArena(Player player) {
		return playerInArena.containsKey(player) ? playerInArena.get(player) : null;
	}

	public boolean isPlayerInArena(Player player) {
		return playerInArena.containsKey(player) ? true : false;
	}

	public void addPlayerToArena(Player player, Arena arena) {
		if (!plugin.playerStats.containsKey(player.getName()))
			plugin.playerStats.put(player.getName(),
					new Stats(player.getUniqueId().toString(), 0, 0, 0, 0, 0, 0, 0, 0));

		arena.addPlayer(player);
		arena.addToArena(player);
		arena.addScore(player, 0);
		playerInArena.put(player, arena);

		for (Player p : arena.getAllPlayers()) {
			p.sendMessage(Commands.prefix() + "§6" + player.getName() + "§e joined the game. (§b"
					+ arena.getPlayerCount() + "§e/" + "§b" + arena.getMode().maxPlayers() + "§e)");
		}

		if (arena.getPlayerCount() >= arena.getMode().minPlayers()) {
			if (arena.getStatus() != Status.STARTING) {
				arena.setStatus(Status.STARTING);
				startTimer(arena);
			}
		}

		if (lobby != null && player.getLocation().distance(lobby) > 20)
			toLobby(player);
	}

	public void removePlayerFromArena(Player player, Arena arena) {
		arena.removeFromArena(player);
		arena.removePlayer(player);
		arena.removeScore(player);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP);
		playerInArena.remove(player);

		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		if (arena.getStatus() == Status.ONGOING) {
			Stats stats = plugin.playerStats.get(player.getName());
			stats.increaseLosses();
			plugin.playerStats.put(player.getName(), stats);

			if (arena.getPlayerCount() < arena.getMode().minPlayers()) {
				for (Player p : arena.getAllPlayers()) {
					if (arena.getMode() == Mode.FFA) {
						p.sendMessage(Commands.prefix() + "§eEveryone left, you won!");
					} else {
						p.sendMessage(Commands.prefix() + "§eNot enough players. Game has been cancelled");
					}
				}
				player.getInventory().setContents(oldInventory.get(player));
				oldInventory.remove(player);
				player.setLevel(oldLevel.get(player));
				oldLevel.remove(player);
				player.setGameMode(oldMode.get(player));
				oldMode.remove(player);
				player.setExp(oldExp.get(player));
				oldExp.remove(player);
				cancel(arena);
				toLobby(player);
				return;
			}
		}

		if (arena.getStatus() != Status.ENDED) {
			for (Player p : arena.getAllPlayers()) {
				p.sendMessage(Commands.prefix() + "§6" + player.getName() + " §eleft the game.");
			}
		}

		if (oldInventory.containsKey(player)) {
			player.getInventory().setContents(oldInventory.get(player));
			oldInventory.remove(player);
		}

		if (oldMode.containsKey(player)) {
			player.setGameMode(oldMode.get(player));
			oldMode.remove(player);
		}

		if (oldLevel.containsKey(player)) {
			player.setLevel(oldLevel.get(player));
			oldLevel.remove(player);
		}

		toLobby(player);
	}

	public void addSpectatorToArena(Player player, Arena arena) {
		arena.addSpectator(player);
		playerInArena.put(player, arena);
		createScoreboard(player);
		for (Player p : arena.getAllPlayers()) {
			p.sendMessage(Commands.prefix() + player.getName() + " §eis spectating.");
		}
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(arena.getPlayers().get(0));
	}

	public void removeSpectatorFromArena(Player player, Arena arena) {
		arena.removeFromArena(player);
		arena.removeSpectator(player);
		playerInArena.remove(player);

		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		for (Player p : arena.getAllPlayers()) {
			p.sendMessage(Commands.prefix() + player.getName() + " §eis no longer spectating.");
		}

		if (oldMode.containsKey(player)) {
			player.setGameMode(oldMode.get(player));
			oldMode.remove(player);
		}
		toLobby(player);
	}

	public boolean isSpectating(Player player) {
		for (Arena arena : arenas)
			for (Player p : arena.getSpectators())
				if (p.getName().equalsIgnoreCase(player.getName()))
					return true;
		return false;
	}

	public void setStatus(Arena arena, Status status) {
		arena.setStatus(status);
	}

	public void toggleEditMode(Player player, String arena) {
		Arena a = getArena(arena);
		EditMode.toggleEditMode(player, a);
		plugin.saveArena(a);
	}

	public void createArena(String name, Mode mode) {
		Arena arena = new Arena(name, Status.SETTING_UP, mode);
		arenas.add(arena);
		arenaNames.add(name);
		arenaTimer.put(arena, gameTime);
		plugin.saveArena(arena);
	}

	/*
	 * @return true if removed, false if it doesn't exist
	 */
	public boolean removeArena(String arena) {
		if (!exists(arena))
			return false;

		Arena a = getArena(arena);

		arenas.remove(a);
		arenaNames.remove(arena);
		plugin.getCfg().deleteArena(arena);
		return true;
	}

	/*
	 * @param arena name
	 * 
	 * @return arena if exists, null if otherwise
	 */
	public Arena getArena(String name) {
		for (Arena arena : arenas)
			if (arena.getName().equals(name))
				return arena;
		return null;
	}

	/*
	 * @param arena name
	 * 
	 * @return true if exists
	 */
	public boolean exists(String arena) {
		for (String name : arenaNames)
			if (name.equals(arena))
				return true;
		return false;
	}

	/*
	 * @return true if arena started
	 */
	public boolean start(Arena arena) {
		int i = 0;
		for (Player p : arena.getPlayers()) {
			p.teleport(arena.selectRandomSpawn());
			oldInventory.put(p, p.getInventory().getContents());
			p.getInventory().clear();
			p.getInventory().addItem(ItemManager.guns[i++]);
			Stats stats = plugin.playerStats.get(p.getName());
			stats.increaseGamesPlayed();
			plugin.playerStats.put(p.getName(), stats);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000 * 20, 0, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000 * 20, 0, true, false));
			p.setHealth(20);
			p.setFoodLevel(20);
			oldMode.put(p, p.getGameMode());
			oldExp.put(p, p.getExp());
			oldLevel.put(p, p.getLevel());
			p.setLevel(0);
			p.setExp(0);
			p.setGameMode(GameMode.ADVENTURE);
			createScoreboard(p);
		}

		arenaTimer.put(arena, gameTime);

		gm.start(arena);
		return true;
	}

	public void startTimer(Arena arena) {
		if (arena.getPlayerCount() < arena.getMode().maxPlayers())
			Bukkit.broadcastMessage(Commands.prefix() + "§eArena §6" + arena.getName() + " §eis starting in §6"
					+ timeToStart + "§e seconds. Join now!");
		arenaCountdownTimer.put(arena, timeToStart);
		new RepeatingTask(plugin, 0, 1 * 20) {
			@Override
			public void run() {
				int time = arenaCountdownTimer.get(arena);
				if ((time % 5 == 0 && time >= 5) || (time > 0 && time < 5)) {
					for (Player player : arena.getPlayers()) {
						player.sendMessage(Commands.prefix() + "§eStarting in §6" + time + "§e.");
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
					}
				} else if (time <= 0 && arena.getPlayerCount() >= arena.getMode().minPlayers()) {
					start(arena);
					cancel();
				} else if (arena.getPlayerCount() < arena.getMode().minPlayers()) {
					cancel();
					arena.setStatus(Status.CANCELLED);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
						arena.setStatus(Status.JOINABLE);
					}, 1 * 20);
				}
				arenaCountdownTimer.put(arena, time - 1);
			}
		};
	}

	/*
	 * @return true if ended. False if arena doesn't exist or hasn't started.
	 */
	public boolean end(Arena arena) {
		arena.setStatus(Status.ENDED);
		List<Player> scoreboard = Utils.getSortedPlayers(arena);
		Player winner = scoreboard.get(0);

		List<Player> toRemove = new ArrayList<>();

		for (Player p : arena.getAllPlayers()) {
			Stats stats = plugin.playerStats.get(p.getName());

			if (p.getName().equals(winner.getName()))
				stats.increaseWins();
			else if (!arena.getSpectators().contains(p))
				stats.increaseLosses();

			plugin.playerStats.put(p.getName(), stats);

			p.sendMessage(Commands.prefix() + "§6" + winner.getName() + " §ewon with §6" + arena.getScores().get(winner)
					+ " §ekills!");
			for (int j = 2; j <= arena.getPlayerCount(); j++) {
				p.sendMessage(Commands.prefix() + "§e" + j + ". §6" + scoreboard.get(j - 1).getName() + " §e- " + "§6"
						+ arena.getScores().get(scoreboard.get(j - 1)));
			}

			p.setExp(oldExp.get(p));
			oldExp.remove(p);

			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);

			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

			toRemove.add(p);
			plugin.savePlayer(p.getName());
		}

		for (Player player : toRemove) {
			if (isSpectating(player))
				removeSpectatorFromArena(player, arena);
			else
				removePlayerFromArena(player, arena);
		}

		toRemove.clear();
		arena.removeAllPowerups();
		arena.getAllPlayers().clear();
		arena.getPlayers().clear();
		arena.getSpectators().clear();
		arena.clearScores();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			arena.setStatus(Status.JOINABLE);
		}, 2 * 20);

		return true;
	}

	public void cancel(Arena arena) {
		for (Player p : arena.getAllPlayers()) {
			Stats stats = plugin.playerStats.get(p.getName());
			if (arena.getMode() == Mode.FFA) {
				stats.increaseWins();
			} else {
				stats.decreaseGamesPlayed();
			}
			plugin.playerStats.put(p.getName(), stats);

			if (oldInventory.containsKey(p)) {
				p.getInventory().setContents(oldInventory.get(p));
				oldInventory.remove(p);
			}
			toLobby(p);
		}

		arena.removeAllPowerups();
		arena.clearScores();
		arena.getAllPlayers().clear();
		arena.getPlayers().clear();
		arena.getSpectators().clear();
		arena.setStatus(Status.CANCELLED);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			arena.setStatus(Status.JOINABLE);
		}, 2 * 20);
	}

	public void createScoreboard(Player p) {
		Arena arena = getPlayerArena(p);
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective("Scoreboard", "dummy", "§6Time Left: §e" + arenaTimer.get(arena));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		Team fragLimit = board.registerNewTeam("fragLimit");
		fragLimit.addEntry("§6Frag Limit: ");
		Score score = obj.getScore("§6Frag Limit: ");
		score.setScore(maxFrags);

		int playerCount = arena.getPlayerCount();
		for (int i = playerCount - 1; i >= 0; i--) {
			String t = "";
			ChatColor color = null;
			switch (i) {
			case 0:
				t = "Red";
				color = ChatColor.DARK_RED;
				break;
			case 1:
				t = "Blue";
				color = ChatColor.BLUE;
				break;
			case 2:
				t = "Yellow";
				color = ChatColor.YELLOW;
				break;
			case 3:
				t = "Green";
				color = ChatColor.GREEN;
				break;
			}
			Team team = board.registerNewTeam(t);
			team.setColor(color);
			team.addEntry(p.getName());
			score = obj.getScore(p.getName());
			score.setScore(arena.getScores().get(arena.getPlayers().get(i)));
		}
		p.setScoreboard(board);
	}

	public void createStatsBoard(Player p) {
		DecimalFormat df = new DecimalFormat(" #,##0.00");
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective("Statsboard", "dummy", "§4" + p.getName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		if (!plugin.playerStats.containsKey(p.getName())) {
			p.sendMessage(Commands.prefix() + "§cYou haven't played a game yet.");
			return;
		}
		Stats stats = plugin.playerStats.get(p.getName());
		Score score = obj.getScore("§6Kills: §e" + stats.getKills());
		score.setScore(11);
		Score score1 = obj.getScore("§6Deaths: §e" + stats.getDeaths());
		score1.setScore(10);
		Score score2 = obj.getScore("§6KD: §e" + df.format(stats.getKd()));
		score2.setScore(9);
		Score score3 = obj.getScore("§6Games Played: §e" + stats.getGamesPlayed());
		score3.setScore(8);
		Score score4 = obj.getScore("§6Wins: §e" + stats.getWins());
		score4.setScore(7);
		Score score5 = obj.getScore("§6Losses: §e" + stats.getLosses());
		score5.setScore(6);
		Score score6 = obj.getScore("§6Win Rate: §e" + df.format(stats.getWinPercent()) + "%");
		score6.setScore(5);
		Score score7 = obj.getScore("§6Shots Fired: §e" + stats.getShotsFired());
		score7.setScore(4);
		Score score8 = obj.getScore("§6Shots Hit: §e" + stats.getShotsHit());
		score8.setScore(3);
		Score score9 = obj.getScore("§6Accuracy: §e" + df.format(stats.getAccuracy()) + "%");
		score9.setScore(2);
		Score score10 = obj.getScore("§6Headshots: §e" + stats.getHeadshots());
		score10.setScore(1);
		Score score11 = obj.getScore("§6Headshot Accuracy: §e" + df.format(stats.getHeadshotPercentage()) + "%");
		score11.setScore(0);

		p.setScoreboard(board);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			p.setScoreboard(manager.getNewScoreboard());
		}, 10 * 20);
	}
}
