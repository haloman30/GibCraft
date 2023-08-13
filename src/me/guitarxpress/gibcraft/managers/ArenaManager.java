package me.guitarxpress.gibcraft.managers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
import me.guitarxpress.gibcraft.Language;
import me.guitarxpress.gibcraft.Logger;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.events.EditMode;
import me.guitarxpress.gibcraft.sql.SQLGetter;
import me.guitarxpress.gibcraft.utils.RepeatingTask;
import me.guitarxpress.gibcraft.utils.Utils;

public class ArenaManager {

	private GibCraft plugin;

	public List<Arena> arenas;
	public List<String> arenaNames;
	private Location lobby;
	private Map<Player, Arena> playerInArena;
	private Map<Arena, Integer> arenaCountdownTimer = new HashMap<>();
	public Map<Arena, Integer> arenaTimer = new HashMap<>();
	private int gameTime; // Time in seconds
	private int maxFrags;
	private int maxPlayers;
	public int timeToStart;
	private GameManager gm;
	private SQLGetter data;

	private Map<Player, ItemStack[]> oldInventory = new HashMap<>();
	private Map<Player, Float> oldExp = new HashMap<>();
	private Map<Player, GameMode> oldMode = new HashMap<>();
	private Map<Player, Integer> oldLevel = new HashMap<>();

	public ArenaManager(GibCraft plugin) {
		this.plugin = plugin;
		this.data = plugin.getSQLGetter();

		arenas = new ArrayList<>();
		arenaNames = new ArrayList<>();
		playerInArena = new HashMap<>();
		this.gm = plugin.getGameManager();

		for (Arena arena : arenas) {
			arenaTimer.put(arena, GetGameTime(arena));
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
		return isPlayerInArena(player) ? playerInArena.get(player) : null;
	}

	public boolean isPlayerInArena(Player player) {
		return playerInArena.containsKey(player);
	}

	public void addPlayerToArena(Player player, Arena arena) {
		if (plugin.getSQL().isConnected())
			data.loadPlayerValues(player.getUniqueId());
		else if (!plugin.playerStats.containsKey(player.getName()))
			plugin.playerStats.put(player.getName(),
					new Stats(player.getUniqueId().toString(), 0, 0, 0, 0, 0, 0, 0, 0));

		arena.addPlayer(player);
		arena.addToArena(player);
		arena.addScore(player, 0);
		playerInArena.put(player, arena);

		for (Player p : arena.getAllPlayers())
		{
			p.sendMessage(String.format(Language.player_joined_format, player.getName(), arena.getPlayerCount(), arena.getMode().maxPlayers(arena)));
		}

		if (arena.getPlayerCount() >= arena.getMode().minPlayers())
			if (arena.getStatus() != Status.STARTING)
				startTimer(arena);

		if (lobby != null && player.getLocation().distance(lobby) > 20)
			toLobby(player);
	}

	public void addPlayerToArena(Player player, Arena arena, String team) {
		if (plugin.getSQL().isConnected())
			data.loadPlayerValues(player.getUniqueId());
		else if (!plugin.playerStats.containsKey(player.getName()))
			plugin.playerStats.put(player.getName(),
					new Stats(player.getUniqueId().toString(), 0, 0, 0, 0, 0, 0, 0, 0));

		if (!arena.teamExists(team))
			arena.createTeam(team);
		arena.addToTeam(team, player);

		arena.addPlayer(player);
		arena.addToArena(player);
		playerInArena.put(player, arena);

		for (Player p : arena.getAllPlayers())
		{
			p.sendMessage(String.format(Language.player_joined_format, player.getName(), arena.getPlayerCount(), arena.getMode().maxPlayers(arena)));
		}

		if (arena.getPlayerCount() >= arena.getMode().minPlayers())
			if (arena.getStatus() != Status.STARTING)
				startTimer(arena);

		if (lobby != null && player.getLocation().distance(lobby) > 20)
			toLobby(player);
	}

	public void removePlayerFromArena(Player player, Arena arena) {
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		arena.removeFromArena(player);
		arena.removePlayer(player);
		arena.removeScore(player);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP);
		playerInArena.remove(player);

		if (arena.getMode() == Mode.DUOS)
			arena.removeFromTeam(arena.getPlayerTeam(player), player);

		if (arena.getStatus() == Status.ONGOING) {
			Utils.addLoss(player, plugin.playerStats);
			if (arena.getPlayerCount() < arena.getMode().minPlayers()) {
				end(arena);
			}
		}

		if (arena.getStatus() != Status.ENDED) {
			for (Player p : arena.getAllPlayers()) {
				p.sendMessage(String.format(Language.player_left_format, player.getName()));
			}
		}

		if (oldInventory.containsKey(player)) {
			player.getInventory().setContents(oldInventory.get(player));
			oldInventory.remove(player);
			player.setLevel(oldLevel.get(player));
			oldLevel.remove(player);
			player.setGameMode(oldMode.get(player));
			oldMode.remove(player);
			player.setExp(oldExp.get(player));
			oldExp.remove(player);
		}

		if (plugin.getSQL().isConnected())
			data.updatePlayerValues(player.getUniqueId());
		plugin.savePlayer(player.getName());

		if (lobby != null && player.getLocation().distance(lobby) > 20)
			toLobby(player);
	}

	public void addSpectatorToArena(Player player, Arena arena) {
		arena.addSpectator(player);
		arena.addToArena(player);
		playerInArena.put(player, arena);
		if (arena.getMode() == Mode.FFA)
			createScoreboardFFA(player);
		else
			createScoreboardDuos(player);

		for (Player p : arena.getAllPlayers())
			p.sendMessage(String.format(Language.player_spectating_format, player.getName()));

		oldMode.put(player, player.getGameMode());
		player.setGameMode(GameMode.SPECTATOR);

		player.teleport(arena.getPlayers().get(0));
	}

	public void removeSpectatorFromArena(Player player, Arena arena) {
		arena.removeFromArena(player);
		arena.removeSpectator(player);
		playerInArena.remove(player);
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		for (Player p : arena.getAllPlayers())
			p.sendMessage(String.format(Language.player_stopped_spectating_format, player.getName()));

		toLobby(player);
		
		player.setGameMode(oldMode.get(player));
		oldMode.remove(player);
	}

	/*
	 * @return true if player is spectating.
	 */
	public boolean isSpectating(Player player) {
		for (Arena arena : arenas)
			if (arena.getSpectators().contains(player))
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

	/*
	 * @return true if arena was created.
	 */
	public boolean createArena(String name, Mode mode) {
		if (exists(name))
			return false;

		Arena arena = new Arena(name, Status.SETTING_UP, mode);
		arenas.add(arena);
		arenaNames.add(name);
		arenaTimer.put(arena, GetGameTime(arena));
		plugin.saveArena(arena);
		return true;
	}

	/*
	 * @return true if arena was removed.
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
	 * @return arena if exists, null if otherwise
	 */
	public Arena getArena(String name) {
		for (Arena arena : arenas)
			if (arena.getName().equals(name))
				return arena;
		return null;
	}

	/*
	 * @return true if exists
	 */
	public boolean exists(String arena) {
		for (String name : arenaNames)
			if (name.equals(arena))
				return true;
		return false;
	}

	public void start(Arena arena) 
	{
		int i = 0;
		for (Player p : arena.getPlayers()) {
			oldInventory.put(p, p.getInventory().getContents());
			oldMode.put(p, p.getGameMode());
			oldExp.put(p, p.getExp());
			oldLevel.put(p, p.getLevel());

			p.getInventory().clear();
			if (arena.getMode() == Mode.FFA)
				p.getInventory().addItem(ItemManager.guns[i++]);
			else {
				if (arena.getPlayerTeam(p).equals("Red")) {
					p.getInventory().setHelmet(ItemManager.redHat);
					p.getInventory().setChestplate(ItemManager.redTop);
					p.getInventory().setLeggings(ItemManager.redLegs);
					p.getInventory().setBoots(ItemManager.redBoots);
					p.getInventory().addItem(ItemManager.gunRed);
				} else {
					p.getInventory().setHelmet(ItemManager.blueHat);
					p.getInventory().setChestplate(ItemManager.blueTop);
					p.getInventory().setLeggings(ItemManager.blueLegs);
					p.getInventory().setBoots(ItemManager.blueBoots);
					p.getInventory().addItem(ItemManager.gunBlue);
				}
			}

			p.setHealth(20);
			p.setFoodLevel(20);
			p.setLevel(0);
			p.setExp(0);
			p.setGameMode(GameMode.ADVENTURE);

			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000 * 20, 0, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000 * 20, 0, true, false));

			Utils.addGamePlayed(p, plugin.playerStats);

			if (arena.getMode() == Mode.FFA)
				createScoreboardFFA(p);
			else
				createScoreboardDuos(p);

			p.teleport(arena.selectRandomSpawn());
		}

		arenaTimer.put(arena, GetGameTime(arena));

		gm.start(arena);
	}

	public void startTimer(Arena arena) {
		arena.setStatus(Status.STARTING);

		if (arena.getPlayerCount() < arena.getMode().maxPlayers(arena))
			Bukkit.broadcastMessage(String.format(Language.arena_starting_warning_format, arena.getName(), timeToStart));

		arenaCountdownTimer.put(arena, timeToStart);
		new RepeatingTask(plugin, 0, 1 * 20) {

			@Override
			public void run() {
				int time = arenaCountdownTimer.get(arena);
				if ((time % 5 == 0 && time >= 5) || (time > 0 && time < 5)) {
					for (Player player : arena.getPlayers()) {
						player.sendMessage(String.format(Language.arena_starting_in_format, time));
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
					}
				} else if (time <= 0 && arena.getPlayerCount() >= arena.getMode().minPlayers()) {
					start(arena);
					cancel();
				}

				if (arena.getPlayerCount() < arena.getMode().minPlayers()) {
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

	public void endDuos(Arena arena) {
		arena.setStatus(Status.ENDED);

		if (arena.getPlayerCount() != 0) {
			String winners = Utils.getWinningTeam(arena.getTeamScores());
			String losers = Utils.getLosingTeam(arena.getTeamScores());

			List<Player> toRemove = new ArrayList<>();

			for (Player p : arena.getAllPlayers()) {

				if (arena.getPlayerTeam(p) == winners)
					Utils.addWin(p, plugin.playerStats);
				else if (!arena.getSpectators().contains(p))
					Utils.addLoss(p, plugin.playerStats);

				p.sendMessage(String.format(Language.duos_winners_message_format, winners, arena.getTeamScore(winners)));
				p.sendMessage(String.format(Language.duos_losers_message_format, losers, arena.getTeamScore(losers)));

				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

				toRemove.add(p);
				plugin.savePlayer(p.getName());
			}

			for (Player player : toRemove) {
				if (isSpectating(player))
					removeSpectatorFromArena(player, arena);
				else
					removePlayerFromArena(player, arena);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
				createStatsBoard(player);
			}
			toRemove.clear();
		}

		arena.removeAllPowerups();
		arena.getAllPlayers().clear();
		arena.getPlayers().clear();
		arena.getSpectators().clear();
		arena.clearScores();

//		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			arena.setStatus(Status.JOINABLE);
//		}, 2 * 20);
	}

	public void end(Arena arena) 
	{
		arena.setStatus(Status.ENDED);

		if (arena.getPlayerCount() != 0) 
		{
			List<Integer> scores = Utils.getSortedPointsLeaderboard(arena);
			List<Player> players = Utils.getSortedPlayerLeaderboard(arena, scores);
			
			Player winner = players.get(0);
			int winner_score = scores.get(0);
			
			String tied_winner_names = "";
			ArrayList<Player> tied_players = new ArrayList<Player>();
			
			// Build tied players list/message
			{
				tied_players.add(winner);
				
				for (int i = 1; i < players.size(); i++)
				{
					int score = scores.get(i);
					
					if (score == winner_score)
					{
						tied_players.add(players.get(i));
					}
				}
				
				if (tied_players.size() > 1)
				{
					for (int i = 0; i < tied_players.size(); i++)
					{
						tied_winner_names += tied_players.get(i).getName();
						
						if (i != tied_players.size() - 1)
						{
							if (tied_players.size() > 2)
							{
								tied_winner_names += ", ";
							}
							else
							{
								tied_winner_names += " ";
							}
						}
						
						if (i == tied_players.size() - 2)
						{
							tied_winner_names += "and ";
						}
					}
				}
			}

			List<Player> toRemove = new ArrayList<>();

			for (Player p : arena.getAllPlayers()) 
			{
				if (tied_players.size() > 1)
				{
					if (tied_players.contains(p))
					{
						Utils.addWin(p, plugin.playerStats);
					}
					else if (!arena.getSpectators().contains(p))
					{
						Utils.addLoss(p, plugin.playerStats);
					}
					
					p.sendMessage(String.format(Language.ffa_tied_message_format, tied_winner_names, winner_score));
					
					for (int j = tied_players.size(); j <= arena.getPlayerCount() - 1; j++)
					{
						p.sendMessage(String.format(Language.ffa_loser_message_format, j + 1, players.get(j).getName(), 
							arena.getScores().get(players.get(j))));
					}
				}
				else
				{
					if (p.getName().equals(winner.getName()))
					{
						Utils.addWin(p, plugin.playerStats);
					}
					else if (!arena.getSpectators().contains(p))
					{
						Utils.addLoss(p, plugin.playerStats);
					}
					
					p.sendMessage(String.format(Language.ffa_winner_message_format, winner.getName(), winner_score));
					
					for (int j = 1; j <= arena.getPlayerCount() - 1; j++)
					{
						p.sendMessage(String.format(Language.ffa_loser_message_format, j + 1, players.get(j).getName(), 
							arena.getScores().get(players.get(j))));
					}
				}

				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

				toRemove.add(p);
			}

			for (Player player : toRemove) 
			{
				if (isSpectating(player))
				{
					removeSpectatorFromArena(player, arena);
				}
				else
				{
					removePlayerFromArena(player, arena);
				}
				
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
				createStatsBoard(player);
			}

			toRemove.clear();
		}

		arena.removeAllPowerups();
		arena.getAllPlayers().clear();
		arena.getPlayers().clear();
		arena.getSpectators().clear();
		arena.clearScores();

//		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			arena.setStatus(Status.JOINABLE);
//		}, 2 * 20);
	}

	public void createScoreboardFFA(Player p) {
		Arena arena = getPlayerArena(p);
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective("Scoreboard", "dummy", "§6Time Left: §e" + arenaTimer.get(arena));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		Team fragLimit = board.registerNewTeam("fragLimit");
		fragLimit.addEntry("§6Frag Limit: ");
		Score score = obj.getScore("§6Frag Limit: ");
		score.setScore(maxFrags);

		for (int i = 0; i < arena.getPlayerCount(); i++) {
			Score sc = obj.getScore(Utils.intToColorCode(i) + arena.getPlayers().get(i).getName());
			sc.setScore(arena.getScores().get(arena.getPlayers().get(i)));
		}

		p.setScoreboard(board);
	}

	public void createScoreboardDuos(Player p) {
		Arena arena = getPlayerArena(p);
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective("Scoreboard", "dummy", "§6Time Left: §e" + arenaTimer.get(arena));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		Team fragLimit = board.registerNewTeam("fragLimit");
		fragLimit.addEntry("§6Frag Limit: ");
		Score score = obj.getScore("§6Frag Limit: ");
		score.setScore(maxFrags);

		Score sc1 = obj.getScore("§4Red Team");
		sc1.setScore(arena.getTeamScore("Red"));
		Score sc2 = obj.getScore("§3Blue Team");
		sc2.setScore(arena.getTeamScore("Blue"));

		p.setScoreboard(board);
	}

	public void createStatsBoard(Player p) {
		if (!plugin.playerStats.containsKey(p.getName())) {
			p.sendMessage(Language.error_stats_no_games);
			return;
		}

		p.sendMessage(Language.displaying_stats);

		DecimalFormat df = new DecimalFormat(" #,##0.00");

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective obj = board.registerNewObjective("Statsboard", "dummy", "§4" + p.getName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

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
	
	public int GetGameTime(Arena arena)
	{
		if (arena != null && arena.game_time_override)
		{
			return arena.game_time;
		}

		return gameTime;
	}
	
	public int GetMaxFrags(Arena arena)
	{
		if (arena != null && arena.max_frags_override)
		{
			return arena.max_frags;
		}

		return maxFrags;
	}
	
	public int GetMaxPlayers(Arena arena)
	{
		if (arena != null && arena.max_players_override)
		{
			return arena.max_players;
		}

		return maxPlayers;
	}
	
	public void SetDefaultGameTime(int game_time)
	{
		gameTime = game_time;
	}
	
	public void SetDefaultFragsLimit(int frag_limit)
	{
		maxFrags = frag_limit;
	}
	
	public void SetDefaultMaxPlayers(int max_players)
	{
		maxPlayers = max_players;
	}
}
