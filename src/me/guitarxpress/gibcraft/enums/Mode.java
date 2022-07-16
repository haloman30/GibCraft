package me.guitarxpress.gibcraft.enums;

public enum Mode {
	FFA, 
	DUOS;
	
	public int maxPlayers() {
		switch(this) {
		case DUOS:
			return 4;
		case FFA:
			return 4;
		default:
			return 0;
		}
	}
	
	public int minPlayers() {
		switch(this) {
		case DUOS:
			return 4;
		case FFA:
			return 2;
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
