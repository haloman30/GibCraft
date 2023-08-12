package me.guitarxpress.gibcraft.enums;

import me.guitarxpress.gibcraft.Arena;

public enum Mode {
	FFA, DUOS;

	public int maxPlayers(Arena arena) 
	{
		if (arena != null && arena.max_players_override)
		{
			return arena.max_players;
		}
		
		switch (this) 
		{
		case DUOS:
			return 4;
		case FFA:
			return 4;
		default:
			return 0;
		}
	}

	public int minPlayers() {
		switch (this) {
		case DUOS:
			return 4;
		case FFA:
			return 2;
		default:
			return 0;
		}
	}

	public int maxPerTeam(Arena arena) 
	{
		if (arena != null && arena.max_players_override)
		{
			return arena.max_players / 2;
		}
		
		switch (this) {
		case DUOS:
			return 2;
		case FFA:
			return 1;
		default:
			return 0;
		}
	}

	public static Mode fromString(String mode) {
		if (mode.equalsIgnoreCase("ffa"))
			return FFA;
		else
			return DUOS;
	}
}
