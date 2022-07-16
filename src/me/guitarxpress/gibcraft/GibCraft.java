package me.guitarxpress.gibcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import me.guitarxpress.gibcraft.events.ItemDrop;
import me.guitarxpress.gibcraft.events.PacketSend;
import me.guitarxpress.gibcraft.events.PlayerInteract;
import me.guitarxpress.gibcraft.events.PlayerInteractAtEntity;
import me.guitarxpress.gibcraft.events.PlayerMove;
import me.guitarxpress.gibcraft.events.PlayerQuit;
import me.guitarxpress.gibcraft.events.SignEvents;
import me.guitarxpress.gibcraft.events.ToggleFlight;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.managers.ItemManager;
import me.guitarxpress.gibcraft.utils.ConfigClass;
import me.guitarxpress.gibcraft.utils.Metrics;
import me.guitarxpress.gibcraft.utils.RepeatingTask;
import me.guitarxpress.gibcraft.utils.Utils;

public class GibCraft extends JavaPlugin {

	private ArenaManager am;
	private GameManager gm;
	private ConfigClass cfg;
	private FileConfiguration dataCfg;

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

	public ConfigClass getCfg() {
		return this.cfg;
	}

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		saveDefaultConfig();

		ItemManager.init();
		gm = new GameManager(this);
		am = new ArenaManager(this);
		cfg = new ConfigClass(this);
		dataCfg = ConfigClass.getDataCfg();
		protocolManager = ProtocolLibrary.getProtocolManager();

		getServer().getPluginManager().registerEvents(new EditMode(this), this);
		getServer().getPluginManager().registerEvents(new EntityDamageByEntity(this), this);
		getServer().getPluginManager().registerEvents(new EntityDamage(this), this);
		getServer().getPluginManager().registerEvents(new ItemDrop(this), this);
		getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
		getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
		getServer().getPluginManager().registerEvents(new SignEvents(this), this);
		getServer().getPluginManager().registerEvents(new EntityRegainHealth(this), this);
		getServer().getPluginManager().registerEvents(new FoodLevelChange(this), this);
		getServer().getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
		getServer().getPluginManager().registerEvents(new ToggleFlight(this), this);
		getServer().getPluginManager().registerEvents(new CommandPreprocess(this), this);

		getServer().getPluginCommand("gibcraft").setExecutor(new Commands(this));
		getServer().getPluginCommand("gibcraft").setTabCompleter(new TabComplete(this));

		new PacketSend(this);
		loadData();
		createPowerUps();
		startGlobalRunnableSecond(this);
		startGlobalRunnableTick(this);

		new Metrics(this, 15791);

		getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §aEnabled");
	}

	@Override
	public void onDisable() {
		saveData();
		getServer().getConsoleSender().sendMessage("§7[§4Gib§6Craft§7] §cDisabled");
	}

	@SuppressWarnings("unchecked")
	public void loadData() {
		loadPlayers();
		loadArenas();

		am.gameTime = getConfig().getInt("gameTime");
		am.maxFrags = getConfig().getInt("maxFrags");
		am.timeToStart = getConfig().getInt("timeToStart");
		gm.respawnTime = getConfig().getInt("respawnTime");
		deathMessages1 = getConfig().getStringList("deathMessages1");
		deathMessages2 = getConfig().getStringList("deathMessages2");

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

	public void saveData() {
		saveArenas();
		savePlayers();

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
		for (String arena : arenas) {
			FileConfiguration aCfg = cfg.getArenaCfg(arena);
			String name = aCfg.getString("Name");
			Status status = Status.fromString(aCfg.getString("Status"));
			Mode mode = Mode.fromString(aCfg.getString("Mode"));
			Location corner1 = aCfg.getLocation("Corner1");
			Location corner2 = aCfg.getLocation("Corner2");
			Arena a = new Arena(name, status, mode);
			a.setBoundaries(new Location[] { corner1, corner2 });
			am.arenas.add(a);
			am.arenaNames.add(name);
			am.arenaTimer.put(a, am.gameTime);
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
		cfg.saveArenaFile(arena.getName());
	}

	public void saveArenas() {
		for (Arena arena : am.arenas) {
			saveArena(arena);
		}
	}

	// Runs every second
	public void startGlobalRunnableSecond(GibCraft plugin) {
		new RepeatingTask(plugin, 0, 1 * 20) {
			int puTimer = 0;

			@Override
			public void run() {
				if (SignEvents.signsLoc != null)
					for (Location loc : SignEvents.signsLoc) {
						Sign sign = (Sign) loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
								.getState();
						String s = Utils.getNameFromString(sign.getLine(1));
						SignEvents.updateSign(sign, am, s);
					}

				if (am.arenas != null)
					for (Arena arena : am.arenas) {
						if (arena.getStatus() == Status.ONGOING) {
							if (puTimer > 25) {
								if (!arena.getPowerups().isEmpty())
									arena.removePowerup(0);
								ArmorStand as = (ArmorStand) arena.getBoundaries()[0].getWorld()
										.spawnEntity(arena.selectRandomSpawn(), EntityType.ARMOR_STAND);
								as.setInvisible(true);
								as.setInvulnerable(true);
								as.setCustomName("§ePOWERUP");
								as.setCustomNameVisible(true);
								as.setCollidable(false);
								as.getEquipment().setItem(EquipmentSlot.HEAD, new ItemStack(Material.GOLD_BLOCK));
								arena.addPowerup(as);
								puTimer = 0;
							} else {
								puTimer++;
							}

							int timer = am.arenaTimer.get(arena);
							if (timer > 0)
								am.arenaTimer.put(arena, --timer);
							else {
								am.arenaTimer.put(arena, 0);
								am.end(arena);
							}

							for (Player p : arena.getPlayers()) {
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
							am.createScoreboard(p);

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

						for (ArmorStand as : arena.getPowerups()) {
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
