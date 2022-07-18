package me.guitarxpress.gibcraft.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Stats;

public class SQLGetter {

	private GibCraft plugin;

	public SQLGetter(GibCraft plugin) {
		this.plugin = plugin;
	}

	public void createPlayerTable() {
		PreparedStatement ps;
		try {
			ps = plugin.getSQL().getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS players "
					+ "(NAME VARCHAR(100), UUID VARCHAR(100), KILLS INT(100), DEATHS INT(100), "
					+ "WINS INT(100), GAMES INT(100), LOSSES INT(100), SHOTSFIRED INT(100), SHOTSHIT INT(100), HS INT(100), PRIMARY KEY (NAME))");
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createPlayer(Player player) {
		UUID uuid = player.getUniqueId();
		try {
			if (!exists(uuid)) {
				PreparedStatement ps = plugin.getSQL().getConnection().prepareStatement(
						"INSERT IGNORE INTO players (NAME, UUID, KILLS, DEATHS, WINS, GAMES, LOSSES, SHOTSFIRED, SHOTSHIT, HS) VALUES (?,?,?,?,?,?,?,?,?,?)");
				ps.setString(1, player.getName());
				ps.setString(2, uuid.toString());
				for (int i = 3; i < 11; i++)
					ps.setInt(i, 0);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean exists(UUID uuid) {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection()
					.prepareStatement("SELECT * FROM players WHERE UUID=?");
			ps.setString(1, uuid.toString());

			ResultSet results = ps.executeQuery();
			if (results.next())
				return true;
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setValue(UUID uuid, int value, String sqlValue) {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection()
					.prepareStatement("UPDATE players SET " + sqlValue + "=? WHERE UUID=?");
			ps.setInt(1, value);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addValue(UUID uuid, int value, String sqlValue) {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection()
					.prepareStatement("UPDATE players SET " + sqlValue + "=? WHERE UUID=?");
			ps.setInt(1, getValue(uuid, sqlValue) + value);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getValue(UUID uuid, String sqlValue) {
		int value = 0;
		try {
			PreparedStatement ps = plugin.getSQL().getConnection()
					.prepareStatement("SELECT " + sqlValue + " FROM players WHERE UUID=?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				value = rs.getInt(sqlValue);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return value;
	}

	public void emptyTable(String table) {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection().prepareStatement("TRUNCATE " + table);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removePlayer(UUID uuid) {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection().prepareStatement("DELETE FROM players WHERE UUID=?");
			ps.setString(1, uuid.toString());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadPlayerValues(UUID uuid) {
		if (exists(uuid)) {
			int kills = getValue(uuid, "KILLS");
			int deaths = getValue(uuid, "DEATHS");
			int wins = getValue(uuid, "WINS");
			int games = getValue(uuid, "GAMES");
			int losses = getValue(uuid, "LOSSES");
			int shotsFired = getValue(uuid, "SHOTSFIRED");
			int shotsHit = getValue(uuid, "SHOTSHIT");
			int headshots = getValue(uuid, "HS");
			Stats stats = new Stats(uuid.toString(), kills, deaths, wins, games, losses, shotsFired, shotsHit,
					headshots);
			plugin.playerStats.put(Bukkit.getOfflinePlayer(uuid).getName(), stats);
		} else {
			createPlayer(Bukkit.getPlayer(uuid));
			Stats stats = new Stats(uuid.toString(), 0, 0, 0, 0, 0, 0, 0, 0);
			plugin.playerStats.put(Bukkit.getOfflinePlayer(uuid).getName(), stats);
		}
	}

	public void updatePlayerValues(UUID uuid) {
		Stats stats = plugin.playerStats.get(Bukkit.getPlayer(uuid).getName());
		setValue(uuid, stats.getKills(), "KILLS");
		setValue(uuid, stats.getDeaths(), "DEATHS");
		setValue(uuid, stats.getWins(), "WINS");
		setValue(uuid, stats.getGamesPlayed(), "GAMES");
		setValue(uuid, stats.getLosses(), "LOSSES");
		setValue(uuid, stats.getShotsFired(), "SHOTSFIRED");
		setValue(uuid, stats.getShotsHit(), "SHOTSHIT");
		setValue(uuid, stats.getHeadshots(), "HS");
	}
	
	public void loadAllPlayerValues() {
		try {
			PreparedStatement ps = plugin.getSQL().getConnection()
					.prepareStatement("SELECT UUID FROM players");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				loadPlayerValues(UUID.fromString(rs.getString("UUID")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
