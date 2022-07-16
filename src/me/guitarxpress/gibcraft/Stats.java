package me.guitarxpress.gibcraft;

public class Stats {

	private String uuid;
	private int kills;
	private int deaths;
	private int wins;
	private int gamesPlayed;
	private int losses;
	private int shotsFired;
	private int shotsHit;
	private int headshots;

	public Stats(String uuid) {
		this.uuid = uuid;
		this.kills = 0;
		this.deaths = 0;
		this.wins = 0;
		this.gamesPlayed = 0;
		this.losses = 0;
		this.shotsFired = 0;
		this.shotsHit = 0;
		this.headshots = 0;
	}
	
	public Stats(String uuid, int kills, int deaths, int wins, int gamesPlayed, int losses, int shotsFired,
			int shotsHit, int headshots) {
		this.uuid = uuid;
		this.kills = kills;
		this.deaths = deaths;
		this.wins = wins;
		this.gamesPlayed = gamesPlayed;
		this.losses = losses;
		this.shotsFired = shotsFired;
		this.shotsHit = shotsHit;
		this.headshots = headshots;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public void increaseKills() {
		this.kills++;
	}

	public void decreaseKills() {
		this.kills--;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public void increaseDeaths() {
		this.deaths++;
	}

	public void decreaseDeaths() {
		this.deaths--;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void increaseWins() {
		this.wins++;
	}

	public void decreaseWins() {
		this.wins--;
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public void increaseGamesPlayed() {
		this.gamesPlayed++;
	}

	public void decreaseGamesPlayed() {
		this.gamesPlayed--;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public void increaseLosses() {
		this.losses++;
	}

	public void decreaseLosses() {
		this.losses--;
	}

	public double getKd() {
		return ((deaths == 0) ? kills : ((double) kills / (double) deaths));
	}

	public double getWinPercent() {
		return ((gamesPlayed == 0) ? 0 : ((2 * (double) wins) / (2 * (double) gamesPlayed)) * 100);
	}

	public int getShotsFired() {
		return shotsFired;
	}

	public void setShotsFired(int shotsFired) {
		this.shotsFired = shotsFired;
	}

	public void increaseShotsFired() {
		this.shotsFired++;
	}

	public int getShotsHit() {
		return shotsHit;
	}

	public void increaseShotsHit() {
		this.shotsHit++;
	}

	public void setShotsHit(int shotsHit) {
		this.shotsHit = shotsHit;
	}

	public double getAccuracy() {
		return shotsFired == 0 ? 0 : (((double) shotsHit / (double) shotsFired) * 100);
	}

	public int getHeadshots() {
		return headshots;
	}

	public void setHeadshots(int headshots) {
		this.headshots = headshots;
	}

	public void increaseHeadshots() {
		this.headshots++;
	}
	
	public double getHeadshotPercentage() {
		return shotsHit == 0 ? 0 : (((double) headshots / (double) shotsHit) * 100);
	}

}
