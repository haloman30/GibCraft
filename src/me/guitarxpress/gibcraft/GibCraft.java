package me.guitarxpress.gibcraft;

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
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
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
import me.guitarxpress.gibcraft.utils.ConfigClass;
import me.guitarxpress.gibcraft.utils.Metrics;
import me.guitarxpress.gibcraft.utils.RepeatingTask;
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
		startGlobalRunnableSecond(this);
		startGlobalRunnableTick(this);

		new Metrics(this, 15791);

		getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §aEnabled");
	}

	@Override
	public void onDisable() {
		endAllArenas();
		saveData();
		sql.disconnect();
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

	public void loadArenas() {
		cfg.loadArenaFiles();
		List<String> arenas = cfg.getArenaNameList();
		if (arenas == null)
			return;
		for (String arena : arenas) {
			FileConfiguration aCfg = cfg.getArenaCfg(arena);
			String name = aCfg.getString("Name");
			Status status = Status.fromString(aCfg.getString("Status"));
			Mode mode = Mode.fromString(aCfg.getString("Mode"));
			Location corner1 = aCfg.getLocation("Corner1");
			Location corner2 = aCfg.getLocation("Corner2");
			
			Arena a = new Arena(name, status, mode);
			
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
			am.arenaNames.add(name);
			am.arenaTimer.put(a, am.GetGameTime(a));
		}
	}

	public void saveArena(Arena arena) {
		cfg.createNewArenaFiles();
		FileConfiguration aCfg = cfg.getArenaCfg(arena.getName());
		aCfg.set("Name", arena.getName());
		aCfg.set("Status", Status.valueToString(arena.getStatus()));
		aCfg.set("Mode", arena.getMode().toString());
		aCfg.set("Corner1", arena.getBoundaries()[0]);
		aCfg.set("Corner2", arena.getBoundaries()[1]);
		
		if (arena.game_time_override)
		{
			aCfg.set("gameTime", arena.game_time);
		}
		else
		{
			aCfg.set("gameTime", null);
		}
		
		if (arena.max_frags_override)
		{
			aCfg.set("maxFrags", arena.max_frags);
		}
		else
		{
			aCfg.set("maxFrags", null);
		}
		
		if (arena.respawn_time_override)
		{
			aCfg.set("respawnTime", arena.respawn_time);
		}
		else
		{
			aCfg.set("respawnTime", null);
		}
		
		if (arena.max_players_override)
		{
			aCfg.set("maxPlayers", arena.max_players);
		}
		else
		{
			aCfg.set("maxPlayers", null);
		}
		
		cfg.saveArenaFile(arena.getName());
	}

	public void saveArenas() {
		for (Arena arena : am.arenas) {
			saveArena(arena);
		}
	}

	public boolean isLoadFromSQL() {
		return loadFromSQL;
	}

	public void setLoadFromSQL(boolean loadFromSQL) {
		this.loadFromSQL = loadFromSQL;
	}

	// Runs every second
	public void startGlobalRunnableSecond(GibCraft plugin) {
		new RepeatingTask(plugin, 0, 1 * 20) {
			int puTimer = 0;

			@Override
			public void run() {
				if (SignEvents.signsLoc != null) {
					Sign toRemove = null;
					for (Location loc : SignEvents.signsLoc) {
						Sign sign = (Sign) loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
								.getState();
						String s = Utils.getNameFromString(sign.getLine(1));
						if (!am.exists(s)) {
							toRemove = sign;
							sign.getBlock().breakNaturally();
						} else {
							SignEvents.updateSign(sign, am, s);
						}
					}
					if (toRemove != null)
						SignEvents.signsLoc.remove(toRemove.getLocation());
				}

				if (am.arenas != null)
					for (Arena arena : am.arenas) {
						if (arena.getStatus() == Status.ONGOING) 
						{
							if (puTimer > 25) 
							{
								arena.AddNewRandomPowerup();
								puTimer = 0;
							} 
							else 
							{
								puTimer++;
							}
							
							arena.UpdatePowerups();

							int timer = am.arenaTimer.get(arena);
							if (timer > 0)
								am.arenaTimer.put(arena, --timer);
							else {
								am.arenaTimer.put(arena, 0);
								if (arena.getMode() == Mode.FFA)
									am.end(arena);
								else
									am.endDuos(arena);
							}

							for (Player p : arena.getPlayers()) 
							{
								if (arena.getMode() == Mode.FFA)
									am.createScoreboardFFA(p);
								else
									am.createScoreboardDuos(p);

								if (playerPowerup.containsKey(p)) {
									if (p.getLevel() > 0)
										p.setLevel(p.getLevel() - 1);
								}
							}
							
							for (Player p : arena.getSpectators()) 
							{
								if (arena.getMode() == Mode.FFA)
									am.createScoreboardFFA(p);
								else
									am.createScoreboardDuos(p);

								if (playerPowerup.containsKey(p)) {
									if (p.getLevel() > 0)
										p.setLevel(p.getLevel() - 1);
								}
							}
						}
					}

			}
		};
	}

	// Runs every tick
	public void startGlobalRunnableTick(GibCraft plugin) {
		new RepeatingTask(plugin, 0, 1) {
			int asRotation = 0;

			@Override
			public void run() {
				for (Arena arena : am.arenas) {
					if (arena.getStatus() == Status.ONGOING) {
						for (Player p : arena.getAllPlayers()) {
							if (am.isPlayerInArena(p) && !am.isSpectating(p)) {
								if (PlayerInteract.cooldowns.containsKey(p)) {
									long millis = System.currentTimeMillis() - PlayerInteract.cooldowns.get(p);
									double cooldown = 800;
									if (playerPowerup.containsKey(p) && playerPowerup.get(p).getId() == 1)
										cooldown = 400;
									if (millis < 0) {
										p.setExp((float) ((millis + cooldown) / cooldown));
									} else {
										p.setExp(1);
									}
								}
							}

						}

						for (ArmorStand as : arena.powerups.keySet()) 
						{
							as.setHeadPose(new EulerAngle(0, Math.toRadians(asRotation), 0));
							as.getWorld().spawnParticle(Particle.REDSTONE,
									as.getLocation().add(new Location(as.getWorld(), 0, 1.8, 0)).clone(), 2, .5, .5, .5,
									1, new Particle.DustOptions(Color.YELLOW, (float) 1.0));
						}
					}
					asRotation++;
				}

			}
		};
	}

}
