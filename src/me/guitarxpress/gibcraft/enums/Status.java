package me.guitarxpress.gibcraft.enums;

public enum Status {
	SETTING_UP, JOINABLE, STARTING, ONGOING, CANCELLED, ENDED, UNAVAILABLE, STARTUP;
	
	public static Status fromString(String status) {
		switch (status) {
		case "cancelled":
			return CANCELLED;
		case "ended":
			return ENDED;
		case "joinable":
			return JOINABLE;
		case "ongoing":
			return ONGOING;
		case "settingup":
			return SETTING_UP;
		case "starting":
			return STARTING;
		case "startup":
			return STARTUP;
		case "unavailable":
			return UNAVAILABLE;
		default:
			return SETTING_UP;
		}
	}
	
	public static String valueToString(Status status) {
		switch (status) {
		case CANCELLED:
			return "cancelled";
		case ENDED:
			return "ended";
		case JOINABLE:
			return "joinable";
		case ONGOING:
			return "ongoing";
		case SETTING_UP:
			return "settingup";
		case STARTING:
			return "starting";
		case STARTUP:
			return "startup";
		case UNAVAILABLE:
			return "unavailable";
		default:
			return "unavailable";
		}
	}
	
	public String display() {
		switch (this) {
		case CANCELLED:
			return "§cCancelled";
		case ENDED:
			return "§cEnded";
		case JOINABLE:
			return "§aJoinable";
		case ONGOING:
			return "§6Ongoing";
		case SETTING_UP:
			return "§cSetting Up";
		case STARTING:
			return "§aStarting";
		case STARTUP:
			return "§6Startup";
		case UNAVAILABLE:
			return "§cUnavailable";
		default:
			return "§cUnavailable";
		}
	}
	
}
