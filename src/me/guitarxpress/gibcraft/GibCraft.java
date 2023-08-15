package me.guitarxpress.gibcraft;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import me.guitarxpress.gibcraft.Logger.LogLevel;
import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.events.CommandPreprocess;
import me.guitarxpress.gibcraft.events.EditMode;
import me.guitarxpress.gibcraft.events.EntityDamage;
import me.guitarxpress.gibcraft.events.EntityDamageByEntity;
import me.guitarxpress.gibcraft.events.EntityRegainHealth;
import me.guitarxpress.gibcraft.events.FoodLevelChange;
import me.guitarxpress.gibcraft.events.InventoryClick;
import me.guitarxpress.gibcraft.events.ItemDrop;
import me.guitarxpress.gibcraft.events.PacketSend;
import me.guitarxpress.gibcraft.events.PlayerInteract;
import me.guitarxpress.gibcraft.events.PlayerInteractAtEntity;
import me.guitarxpress.gibcraft.events.PlayerJoin;
import me.guitarxpress.gibcraft.events.PlayerMove;
import me.guitarxpress.gibcraft.events.PlayerQuit;
import me.guitarxpress.gibcraft.events.SignEvents;
import me.guitarxpress.gibcraft.events.ToggleFlight;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GUIManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.managers.ItemManager;
import me.guitarxpress.gibcraft.sql.MySQL;
import me.guitarxpress.gibcraft.sql.SQLGetter;
import me.guitarxpress.gibcraft.tasks.SecondTask;
import me.guitarxpress.gibcraft.tasks.TickTask;
import me.guitarxpress.gibcraft.utils.ConfigClass;
import me.guitarxpress.gibcraft.utils.Metrics;
import me.guitarxpress.gibcraft.utils.Utils;

public class GibCraft extends JavaPlugin {

	public static GibCraft instance = null;
	
	private ArenaManager am;
	private GameManager gm;
	private ConfigClass cfg;
	private GUIManager guim;

	private MySQL sql;
	private SQLGetter sqlGetter;
	private FileConfiguration dataCfg;

	private boolean localSave;
	private boolean loadFromSQL;

	public List<PowerUp> powerups = new ArrayList<>();
	public static Map<Player, PowerUp> playerPowerup = new HashMap<>();
	public Map<String, Stats> playerStats = new HashMap<>();

	public List<String> deathMessages1 = new ArrayList<>();
	public List<String> deathMessages2 = new ArrayList<>();

	private ProtocolManager protocolManager;
	
	private SecondTask second_task = null;
	private TickTask tick_task = null;

	public ArenaManager getArenaManager() {
		return this.am;
	}

	public GameManager getGameManager() {
		return this.gm;
	}

	public GUIManager getGUIManager() {
		return this.guim;
	}

	public MySQL getSQL() {
		return this.sql;
	}

	public SQLGetter getSQLGetter() {
		return this.sqlGetter;
	}

	public ConfigClass getCfg() {
		return this.cfg;
	}

	@Override
	public void onEnable() 
	{
		instance = this;
		
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		saveDefaultConfig();

		ItemManager.init();

		sql = new MySQL();
		sqlGetter = new SQLGetter(this);

		gm = new GameManager(this);
		am = new ArenaManager(this);
		guim = new GUIManager(this);
		cfg = new ConfigClass(this);

		dataCfg = ConfigClass.getDataCfg();

		protocolManager = ProtocolLibrary.getProtocolManager();

		loadSQLConfig();
		try {
			sql.connect();
		} catch (ClassNotFoundException | SQLException e) {
			getServer().getConsoleSender()
					.sendMessage("§7[§4Gib§6Craft§7] §cCould not connect to SQL Server. Ignore if you don't have one.");
		}

		getServer().getPluginManager().registerEvents(new EditMode(this), this);
		getServer().getPluginManager().registerEvents(new EntityDamageByEntity(this), this);
		getServer().getPluginManager().registerEvents(new EntityDamage(this), this);
		getServer().getPluginManager().registerEvents(new ItemDrop(this), this);
		getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
		getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
		getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
		getServer().getPluginManager().registerEvents(new SignEvents(this), this);
		getServer().getPluginManager().registerEvents(new EntityRegainHealth(this), this);
		getServer().getPluginManager().registerEvents(new FoodLevelChange(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
		getServer().getPluginManager().registerEvents(new ToggleFlight(this), this);
		getServer().getPluginManager().registerEvents(new CommandPreprocess(this), this);
		getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
		getServer().getPluginManager().registerEvents(guim, this);
		new PacketSend(this);

		getServer().getPluginCommand("gibcraft").setExecutor(new Commands(this));
		getServer().getPluginCommand("gibcraft").setTabCompleter(new TabComplete(this));

		loadData();

		if (sql.isConnected()) {
			getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §aSQL connected.");
			sqlGetter.createPlayerTable();
		}

		createPowerUps();

		new Metrics(this, 15791);
		
		second_task = new SecondTask();
		tick_task = new TickTask();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, second_task, 20L, 20L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, tick_task, 1L, 1L);

		getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §aEnabled");
	}

	@Override
	public void onDisable() 
	{
		endAllArenas();
		saveData();
		sql.disconnect();
		
		Bukkit.getScheduler().cancelTasks(this);
		am.CloseAllStatsBoards();
		
		getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §cDisabled");
	}

	public void loadSQLConfig() {
		sql.setHost(this.getConfig().getString("host"));
		sql.setPort(this.getConfig().getString("port"));
		sql.setDatabase(this.getConfig().getString("database"));
		sql.setUsername(this.getConfig().getString("username"));
		sql.setPassword(this.getConfig().getString("password"));
		sql.setUseSSL(this.getConfig().getBoolean("useSSL"));
	}

	@SuppressWarnings("unchecked")
	public void loadData() {
		localSave = this.getConfig().getBoolean("localSave");
		loadFromSQL = this.getConfig().getBoolean("loadFromSQL");

		if (!loadFromSQL || !sql.isConnected())
			loadPlayers();
		else
			sqlGetter.loadAllPlayerValues();

		
		am.SetDefaultGameTime(getConfig().getInt("gameTime"));
		am.SetDefaultFragsLimit(getConfig().getInt("maxFrags"));
		am.SetDefaultMaxPlayers(getConfig().getInt("maxPlayers", 4));
		am.timeToStart = getConfig().getInt("timeToStart");
		gm.SetDefaultRespawnTime(getConfig().getInt("respawnTime"));
		deathMessages1 = getConfig().getStringList("deathMessages1");
		deathMessages2 = getConfig().getStringList("deathMessages2");

		loadArenas();

		if (dataCfg.get("Signs.Locations") != null) {
			SignEvents.signsLoc = (List<Location>) dataCfg.getList("Signs.Locations");
		}

		if (dataCfg.get("Lobby.Location") != null) {
			am.setLobby(dataCfg.getLocation("Lobby.Location"));
		}
	}

	public ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	public void createPowerUps() {
		PowerUp speed = new PowerUp(0, "Speed", new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, true, false));
		PowerUp fireRate = new PowerUp(1, "Increased Fire Rate",
				new PotionEffect(PotionEffectType.BAD_OMEN, 10 * 20, 0, true, false));
		PowerUp jump = new PowerUp(2, "Jump Boost", new PotionEffect(PotionEffectType.JUMP, 15 * 20, 2, true, false));
		powerups.add(speed);
		powerups.add(fireRate);
		powerups.add(jump);
	}

	public void endAllArenas() {
		for (Arena arena : am.arenas)
			if (arena.getStatus() == Status.STARTING || arena.getStatus() == Status.ONGOING
					|| arena.getStatus() == Status.STARTUP)
				if (arena.getMode() == Mode.DUOS)
					am.endDuos(arena);
				else
					am.end(arena);
	}

	public void saveData() {
		if (localSave || !sql.isConnected() || !loadFromSQL)
			savePlayers();
		saveArenas();

		if (!SignEvents.signsLoc.isEmpty()) {
			dataCfg.set("Signs.Locations", SignEvents.signsLoc);
		}

		if (am.getLobby() != null) {
			dataCfg.set("Lobby.Location", am.getLobby());
		}

		ConfigClass.saveDataCfg();
	}

	public void loadPlayers() {
		cfg.loadPlayerFiles();
		List<String> players = cfg.getPlayerNameList();
		if (players == null)
			return;
		for (String name : players) {
			FileConfiguration pCfg = cfg.getPlayerCfg(name);
			String uuid = pCfg.getString("UUID");
			int kills = pCfg.getInt("Kills");
			int deaths = pCfg.getInt("Deaths");
			int wins = pCfg.getInt("Wins");
			int gamesPlayed = pCfg.getInt("GamesPlayed");
			int losses = pCfg.getInt("Losses");
			int shotsFired = pCfg.getInt("ShotsFired");
			int shotsHit = pCfg.getInt("ShotsHit");
			int headshots = pCfg.getInt("Headshots");
			Stats stats = new Stats(uuid, kills, deaths, wins, gamesPlayed, losses, shotsFired, shotsHit, headshots);
			playerStats.put(name, stats);
		}
	}

	public void savePlayer(String player) {
		if (sql.isConnected()) {
			sqlGetter.updatePlayerValues(Bukkit.getPlayer(player).getUniqueId());
			if (!localSave)
				return;
		}

		cfg.createNewPlayerFiles();
		FileConfiguration pCfg = cfg.getPlayerCfg(player);
		Stats stats = playerStats.get(player);
		pCfg.set("UUID", stats.getUuid());
		pCfg.set("Kills", stats.getKills());
		pCfg.set("Deaths", stats.getDeaths());
		pCfg.set("Wins", stats.getWins());
		pCfg.set("GamesPlayed", stats.getGamesPlayed());
		pCfg.set("Losses", stats.getLosses());
		pCfg.set("ShotsFired", stats.getShotsFired());
		pCfg.set("ShotsHit", stats.getShotsHit());
		pCfg.set("Headshots", stats.getHeadshots());
		cfg.savePlayer(player);
	}

	public void savePlayers() {
		for (Map.Entry<String, Stats> entry : playerStats.entrySet()) {
			savePlayer(entry.getKey());
		}
	}

	public void loadArenas() 
	{
		for (File arena_config_file : cfg.GetArenaFiles()) 
		{
			FileConfiguration aCfg = new YamlConfiguration();
			
			try
			{
				aCfg.load(arena_config_file);
			}
			catch (Exception ex)
			{
				Logger.LogEvent("Failed to load configuration file '" + arena_config_file.getName()
					+ "', skipping: " + ex.getMessage(), LogLevel.WARN);
				continue;
			}
			
			String name = aCfg.getString("Name");
			Status status = Status.fromString(aCfg.getString("Status"));
			Mode mode = Mode.fromString(aCfg.getString("Mode"));
			Location corner1 = aCfg.getLocation("Corner1");
			Location corner2 = aCfg.getLocation("Corner2");
			
			Arena a = new Arena(name, status, mode);
			
			a.arena_config_file = arena_config_file;
			a.arena_config = aCfg;
			
			if (aCfg.contains("maxFrags"))
			{
				a.max_frags_override = true;
				a.max_frags = aCfg.getInt("maxFrags", am.GetMaxFrags(null));
			}
			
			if (aCfg.contains("gameTime"))
			{
				a.game_time_override = true;
				a.game_time = aCfg.getInt("gameTime", am.GetGameTime(null));
			}
			
			if (aCfg.contains("maxPlayers"))
			{
				a.max_players_override = true;
				a.max_players = aCfg.getInt("maxPlayers", am.GetMaxPlayers(null));
			}
			
			if (aCfg.contains("respawnTime"))
			{
				a.respawn_time_override = true;
				a.respawn_time = aCfg.getInt("respawnTime", gm.GetRespawnTime(null));
			}
			
			a.setBoundaries(new Location[] { corner1, corner2 });
			am.arenas.add(a);
			a.arena_timer = am.GetGameTime(a);
		}
	}

	public void saveArena(Arena arena) 
	{
		arena.SaveConfig();
	}

	public void saveArenas() 
	{
		for (Arena arena : am.arenas) 
		{
			saveArena(arena);
		}
	}

	public boolean isLoadFromSQL() 
	{
		return loadFromSQL;
	}

	public void setLoadFromSQL(boolean loadFromSQL) {
		this.loadFromSQL = loadFromSQL;
	}
}
