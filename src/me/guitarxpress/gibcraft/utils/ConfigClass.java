package me.guitarxpress.gibcraft.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class ConfigClass {

	private GibCraft plugin;

	public static File dataFile;
	public static FileConfiguration dataCfg;

	private File playerFolder;
	private File arenaFolder;

	public String playerPath = "Players";
	public String arenaPath = "Arenas";

	//private Map<String, FileConfiguration> arenaConfigs = new HashMap<>();
	private Map<String, FileConfiguration> playerConfigs = new HashMap<>();

	//private Map<String, File> arenaFiles = new HashMap<>();
	private Map<String, File> playerFiles = new HashMap<>();

	private ArenaManager am;

	public ConfigClass(GibCraft plugin) {
		this.plugin = plugin;

		am = plugin.getArenaManager();

		arenaFolder = new File(plugin.getDataFolder(), arenaPath);
		playerFolder = new File(plugin.getDataFolder(), playerPath);

		dataFile = new File(plugin.getDataFolder(), "data.yml");

		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender()
						.sendMessage("�c[" + plugin.getName() + "] Failed to create data.yml\n" + "-> " + e);
			}
		}

		dataCfg = YamlConfiguration.loadConfiguration(dataFile);
	}

	public void createNewArenaFiles() 
	{
		if (!arenaFolder.exists())
		{
			arenaFolder.mkdir();
		}

		for (Arena arena : am.arenas) 
		{
			arena.arena_config_file = new File(arenaFolder, arena.getName() + ".yml");
			
			if (!arena.arena_config_file.exists())
			{
				try 
				{
					arena.arena_config_file.createNewFile();
				} 
				catch (IOException e) 
				{
					Bukkit.getServer().getConsoleSender().sendMessage("�c[" + plugin.getName() + "] Failed to create file for arena: " + arena + " �e-> �c" + e);
				}
			}
		}
	}

	public void loadArenaFiles() 
	{
		if (getArenaNameList() == null)
		{
			return;
		}
		
		for (Arena arena : am.arenas) 
		{
			arena.arena_config_file = new File(arenaFolder, arena + ".yml");
			
			if (!arena.arena_config_file.exists())
			{
				try 
				{
					arena.arena_config_file.createNewFile();
				} 
				catch (IOException e) 
				{
					Bukkit.getServer().getConsoleSender().sendMessage(
							"�c[" + plugin.getName() + "] Failed to load file for arena: " + arena + " �e-> �c" + e);
				}
			}
		}
	}

	public void deleteArena(String name) 
	{
		Arena arena = am.getArena(name);
		
		if (arena != null && arena.arena_config_file != null)
		{
			arena.arena_config_file.delete();
		}
	}

	public void createNewPlayerFiles() {
		if (!playerFolder.exists())
			playerFolder.mkdir();

		for (Map.Entry<String, Stats> entry : plugin.playerStats.entrySet()) {
			File playerFile = new File(playerFolder, entry.getKey() + ".yml");
			if (!playerFile.exists())
				try {
					playerFile.createNewFile();
				} catch (IOException e) {
					Bukkit.getServer().getConsoleSender().sendMessage("�c[" + plugin.getName()
							+ "] Failed to file for player: " + entry.getKey() + " �e-> �c" + e);
				}
			playerFiles.put(entry.getKey(), playerFile);
		}
	}

	public void loadPlayerFiles() {
		if (getPlayerNameList() == null)
			return;
		for (String name : getPlayerNameList()) {
			File playerFile = new File(playerFolder, name + ".yml");
			try {
				playerFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage(
						"�c[" + plugin.getName() + "] Failed to file for player: " + name + " �e-> �c" + e);
			}
			playerFiles.put(name, playerFile);
		}
	}

	public File getPlayerFile(String name) {
		return playerFiles.get(name);
	}

	public FileConfiguration getPlayerCfg(String name) {
		if (playerConfigs.containsKey(name))
			return playerConfigs.get(name);
		File playerFile = getPlayerFile(name);
		playerConfigs.put(name, YamlConfiguration.loadConfiguration(playerFile));
		return playerConfigs.get(name);
	}

	public void savePlayer(String name) {
		File playerFile = getPlayerFile(name);
		if (playerFile.exists()) {
			try {
				FileConfiguration playerCfg = getPlayerCfg(name);
				playerCfg.save(playerFile);
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage(
						"�c[" + plugin.getName() + "] Failed to save file for player: " + name + " �e-> �c" + e);
			}
		}

	}
	
	public List<File> GetArenaFiles()
	{
		File[] array = arenaFolder.listFiles();
		
		if (array == null)
		{
			return null;
		}
		
		ArrayList<File> arena_files = new ArrayList<File>();
		
		for (int i = 0; i < array.length; i++) 
		{
			arena_files.add(array[i]);
		}
		
		return arena_files;
	}

	public List<String> getArenaNameList() {
		String[] array = arenaFolder.list();
		if (array == null)
			return null;
		List<String> arenas = new ArrayList<>();
		for (int i = 0; i < array.length; i++) {
			arenas.add(array[i].substring(0, array[i].length() - 4));
		}
		return arenas;
	}

	public List<String> getPlayerNameList() {
		String[] array = playerFolder.list();
		if (array == null)
			return null;
		List<String> players = new ArrayList<>();
		for (int i = 0; i < array.length; i++) {
			players.add(array[i].substring(0, array[i].length() - 4));
		}
		return players;
	}

	public static FileConfiguration getDataCfg() {
		return dataCfg;
	}

	public static void saveDataCfg() {
		try {
			dataCfg.save(dataFile);
		} catch (IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage("�c[GibCraft] Failed to save data.yml");
		}
	}
}
