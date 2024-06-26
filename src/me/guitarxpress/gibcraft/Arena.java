package me.guitarxpress.gibcraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import me.guitarxpress.gibcraft.Logger.LogLevel;
import me.guitarxpress.gibcraft.enums.Mode;
import me.guitarxpress.gibcraft.enums.PowerUpPointType;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.events.PlayerInteract;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.utils.ConfigClass;
import me.guitarxpress.gibcraft.utils.Utils;

public class Arena 
{
	public class PowerUpPoint
	{
		public Location location;
		public PowerUpPointType type;
	}
	
	// -- Arena Configuration --
	
	public FileConfiguration arena_config = null;
	public File arena_config_file = null;

	private String name;
	private Mode mode;
	private Status status;
	private Location[] boundaries = new Location[2];
	public ArrayList<Location> spawn_points;
	public ArrayList<PowerUpPoint> powerup_points;
	public boolean game_time_override = false;
	public boolean respawn_time_override = false;
	public boolean max_frags_override = false;
	public boolean max_players_override = false;
	public int game_time = -1;
	public int respawn_time = 3;
	public int max_frags = 20;
	public int max_players = 4;
	
	// -- Game Session Data --
	
	private ArrayList<Player> players = new ArrayList<>();
	private ArrayList<Player> spectators = new ArrayList<>();
	private HashMap<Player, Integer> scores = new HashMap<>();
	public HashMap<ArmorStand, Integer> powerups = new HashMap<ArmorStand, Integer>();

	private HashMap<String, List<Player>> teams = new HashMap<>();
	private HashMap<String, Integer> teamScore = new HashMap<>();
	private int powerup_timer = 0;
	int armor_stand_rotation = 0;
	
	public int arena_timer = 0;
	public int countdown_timer = 0;

	public Arena(String _name, Status _status, Mode _mode) 
	{
		name = _name;
		status = _status;
		mode = _mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public void addPlayer(Player player) {
		if (players.size() >= mode.maxPlayers(this))
			return;
		players.add(player);
	}

	public void removePlayer(Player player) {
		if (players.contains(player))
			players.remove(player);
	}

	public List<Player> getSpectators() {
		return spectators;
	}

	public void setSpectators(ArrayList<Player> spectators) {
		this.spectators = spectators;
	}

	public void addSpectator(Player player) {
		spectators.add(player);
	}

	public void removeSpectator(Player player) {
		spectators.remove(player);
	}

	/*public void addToArena(Player player) {
		allPlayers.add(player);
	}

	public void removeFromArena(Player player) {
		allPlayers.remove(player);
	}

	public List<Player> getAllPlayers() {
		return allPlayers;
	}*/

	public Location[] getBoundaries() {
		return boundaries;
	}

	public void setBoundaries(Location[] boundaries) {
		this.boundaries = boundaries;
	}

	public Map<Player, Integer> getScores() {
		return scores;
	}

	public void setScores(HashMap<Player, Integer> scores) {
		this.scores = scores;
	}

	public void addScore(Player player, int kills) {
		scores.put(player, kills);
	}

	public void removeScore(Player player) {
		scores.remove(player);
	}

	public void increaseScore(Player player) {
		int score = scores.get(player);
		scores.put(player, ++score);
	}

	public void decreaseScore(Player player) {
		int score = scores.get(player);
		scores.put(player, --score);
	}

	public void clearScores() {
		scores.clear();
	}

	public boolean isEmpty() {
		return players.size() == 0;
	}

	public boolean isFull() 
	{
		return players.size() == mode.maxPlayers(this);
	}

	public int getPlayerCount() {
		return players.size();
	}
	
	public void AddPowerup(Location location)
	{
		int lifetime = 25;
		
		ArmorStand as = (ArmorStand)boundaries[0].getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		{
			as.setInvisible(true);
			as.setInvulnerable(true);
			as.setCustomName(String.format(Language.powerup_title_format, lifetime));
			as.setCustomNameVisible(true);
			as.setCollidable(false);
			as.getEquipment().setItem(EquipmentSlot.HEAD, new ItemStack(Material.GOLD_BLOCK));
		}
		
		addPowerup(as, lifetime);
	}
	
	public void AddNewRandomPowerup()
	{
		AddPowerup(selectRandomSpawn());
	}

	public void addPowerup(ArmorStand as, int lifetime) 
	{
		this.powerups.put(as, lifetime);
	}

	public void removePowerup(ArmorStand as) {
		this.powerups.remove(as);
		as.remove();
	}

	public void UpdatePowerups() 
	{
		ArrayList<ArmorStand> items_to_remove = new ArrayList<ArmorStand>();
		
		for (ArmorStand powerup : powerups.keySet())
		{
			int powerup_time = powerups.get(powerup);
			
			if (powerup_time <= 0)
			{
				items_to_remove.add(powerup);
			}
			else
			{
				int new_lifetime = powerup_time - 1;
				
				powerup.setCustomName(String.format(Language.powerup_title_format, new_lifetime));
				powerups.put(powerup, new_lifetime);
			}
		}
		
		for (ArmorStand powerup : items_to_remove)
		{
			removePowerup(powerup);
		}
	}

	public void removeAllPowerups() 
	{
		Set<ArmorStand> items_to_remove = powerups.keySet();
		
		for (ArmorStand powerup : items_to_remove)
		{
			removePowerup(powerup);
		}
	}

	public Map<String, List<Player>> getTeams() {
		return this.teams;
	}

	public void setTeams(HashMap<String, List<Player>> teams) {
		this.teams = teams;
	}

	public void addToTeam(String team, Player player) {
		List<Player> teamPlayers = teams.get(team);
		teamPlayers.add(player);
		teams.put(team, teamPlayers);
	}

	public void addToTeam(String team, List<Player> players) {
		teams.put(team, players);
	}

	public void removeFromTeam(String team, Player player) {
		List<Player> teamPlayers = teams.get(team);
		teamPlayers.remove(player);
		teams.put(team, teamPlayers);
	}

	public void removeTeam(String team) {
		teams.remove(team);
	}

	public boolean teamExists(String team) {
		return teams.containsKey(team);
	}

	public List<Player> getTeamPlayers(String team) {
		return teams.get(team);
	}

	public void createTeam(String team) {
		teams.put(team, new ArrayList<>());
		teamScore.put(team, 0);
	}

	public void increaseTeamScore(String team) {
		int score = teamScore.get(team);
		teamScore.put(team, ++score);
	}

	/*
	 * @return team if player is on a team. Null otherwise.
	 */
	public String getPlayerTeam(Player p) {
		for (Map.Entry<String, List<Player>> entry : teams.entrySet()) {
			if (entry.getValue().contains(p))
				return entry.getKey();
		}
		return null;
	}

	public Map<String, Integer> getTeamScores() {
		return teamScore;
	}

	public int getTeamScore(String team) {
		return teamScore.get(team);
	}

	public void setTeamScore(HashMap<String, Integer> teamScore) {
		this.teamScore = teamScore;
	}

	public int getTeamPlayerCount(String team) {
		return teamExists(team) ? getTeamPlayers(team).size() : 0;
	}
	
	public boolean areBoundariesSet() {
		return (boundaries[0] != null && boundaries[1] != null) ? true : false;
	}

	/*
	 * @return random spawn if any was found. Null otherwise. Should never return
	 * null as long as the arena is built correctly.
	 */
	public Location selectRandomSpawn() 
	{
		if (spawn_points != null && spawn_points.size() > 0)
		{
			Random random = new Random();
			return spawn_points.get(random.nextInt(spawn_points.size()));
		}
		
		World w = boundaries[0].getWorld();
		Location start = boundaries[0];
		Location end = boundaries[1];

		int topBlockX = (start.getBlockX() < end.getBlockX() ? end.getBlockX() : start.getBlockX());
		int bottomBlockX = (start.getBlockX() > end.getBlockX() ? end.getBlockX() : start.getBlockX());

		int topBlockY = (start.getBlockY() < end.getBlockY() ? end.getBlockY() : start.getBlockY());
		int bottomBlockY = (start.getBlockY() > end.getBlockY() ? end.getBlockY() : start.getBlockY());

		int topBlockZ = (start.getBlockZ() < end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());
		int bottomBlockZ = (start.getBlockZ() > end.getBlockZ() ? end.getBlockZ() : start.getBlockZ());

		int blocks = (topBlockX - bottomBlockX) * (topBlockY - bottomBlockY) * (topBlockZ - bottomBlockZ);

		boolean found = false;
		Random r = new Random();
		int i = 0;
		while (!found) {
			Location loc = null;
			boolean safeSpawn = true;
			int x = r.nextInt(topBlockX - bottomBlockX) + bottomBlockX;
			int y = r.nextInt((topBlockY - 1) - bottomBlockY) + bottomBlockY;
			int z = r.nextInt(topBlockZ - bottomBlockZ) + bottomBlockZ;
			if (i < blocks) {
				if (!Utils.validTypes.contains(w.getBlockAt(x, y, z).getType())) {
					if (Utils.isValidSpawn(w, x, y, z)) {
						loc = new Location(w, x + .5, y + 1, z + .5);
						for (Entity e : w.getNearbyEntities(loc, 8, 8, 8)) {
							if (e instanceof Player) {
								safeSpawn = false;
								break;
							}
						}
						if (safeSpawn)
							return loc;
					}
				}
				i++;
			} else {
				return loc;
			}
		}
		return null;
	}
	
	public void BroadcastMessage(String message)
	{
		for (Player player : players)
		{
			player.sendMessage(message);
		}
		
		for (Player player : spectators)
		{
			player.sendMessage(message);
		}
	}
	
	public ArrayList<Player> GetPlayersAndSpectators()
	{
		ArrayList<Player> combined_player_list = new ArrayList<Player>();
		
		combined_player_list.addAll(players);
		combined_player_list.addAll(spectators);
		
		return combined_player_list;
	}
	
	public void OnSecondPass()
	{
		if (status == Status.ONGOING)
		{
			ArenaManager am = GibCraft.instance.getArenaManager();
			
			// Update Powerups
			{
				if (powerup_timer > 25) 
				{
					AddNewRandomPowerup();
					powerup_timer = 0;
				} 
				else 
				{
					powerup_timer++;
				}
				
				UpdatePowerups();
			}
			
			// Update Arena Timer
			{
				if (arena_timer > 0)
				{
					arena_timer--;
					
					if (arena_timer == 60 || arena_timer == 30 || arena_timer == 10)
					{
						BroadcastMessage(String.format(Language.game_ending_warning, arena_timer));
						
						for (Player player : GetPlayersAndSpectators())
						{
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
						}
					}
				}
				else 
				{
					arena_timer = 0;
					
					if (getMode() == Mode.FFA)
					{
						am.end(this);
					}
					else
					{
						am.endDuos(this);
					}
				}
			}
			
			// Update Scoreboards
			{
				for (Player p : GetPlayersAndSpectators()) 
				{
					if (getMode() == Mode.FFA)
					{
						am.createScoreboardFFA(p);
					}
					else
					{
						am.createScoreboardDuos(p);
					}

					if (GibCraft.playerPowerup.containsKey(p)) 
					{
						if (p.getLevel() > 0)
						{
							p.setLevel(p.getLevel() - 1);
						}
					}
				}
			}
		}
	}
	
	public void OnTickPass()
	{
		if (status == Status.ONGOING) 
		{
			ArenaManager am = GibCraft.instance.getArenaManager();
			
			for (Player p : players) 
			{
				if (am.isPlayerInArena(p) && !am.isSpectating(p)) 
				{
					if (PlayerInteract.cooldowns.containsKey(p)) 
					{
						long millis = System.currentTimeMillis() - PlayerInteract.cooldowns.get(p);
						double cooldown = 800;
						
						if (GibCraft.playerPowerup.containsKey(p) && GibCraft.playerPowerup.get(p).getId() == 1)
						{
							cooldown = 400;
						}
						
						if (millis < 0) 
						{
							p.setExp((float) ((millis + cooldown) / cooldown));
						} 
						else 
						{
							p.setExp(1);
						}
					}
				}
			}

			for (ArmorStand as : powerups.keySet()) 
			{
				as.setHeadPose(new EulerAngle(0, Math.toRadians(armor_stand_rotation), 0));
				as.getWorld().spawnParticle(Particle.REDSTONE,
						as.getLocation().add(new Location(as.getWorld(), 0, 1.8, 0)).clone(), 2, .5, .5, .5,
						1, new Particle.DustOptions(Color.YELLOW, (float) 1.0));
			}
		}
		
		armor_stand_rotation++;
	}
	
	public void SaveConfig()
	{
		if (arena_config == null)
		{
			arena_config = new YamlConfiguration();
		}
		
		arena_config.set("Name", name);
		arena_config.set("Status", Status.valueToString(status));
		arena_config.set("Mode", mode.toString());
		arena_config.set("Corner1", boundaries[0]);
		arena_config.set("Corner2", boundaries[1]);
		
		if (game_time_override)
		{
			arena_config.set("gameTime", game_time);
		}
		else
		{
			arena_config.set("gameTime", null);
		}
		
		if (max_frags_override)
		{
			arena_config.set("maxFrags", max_frags);
		}
		else
		{
			arena_config.set("maxFrags", null);
		}
		
		if (respawn_time_override)
		{
			arena_config.set("respawnTime", respawn_time);
		}
		else
		{
			arena_config.set("respawnTime", null);
		}
		
		if (max_players_override)
		{
			arena_config.set("maxPlayers", max_players);
		}
		else
		{
			arena_config.set("maxPlayers", null);
		}
		
		try
		{
			if (arena_config_file == null)
			{
				arena_config_file = new File(ConfigClass.arenaFolder, name + ".yml");
			}
			
			arena_config.save(arena_config_file);
			Logger.LogEvent("Saved configuration file for arena '" + name + "'");
		}
		catch (Exception ex)
		{
			Logger.LogEvent("Failed to save file for arena '" + name + "': ", LogLevel.ERROR);
			ex.printStackTrace();
		}
	}
	
	public void DeleteConfig()
	{
		if (arena_config_file != null)
		{
			arena_config_file.delete();
		}
	}
}
