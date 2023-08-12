package me.guitarxpress.gibcraft;

public abstract class Language
{
	public static String label = "§7[§6GibCraft§7] §e";
	public static String error_prefix = label + "§c";
	
	public static String help_entry_format = "§e - §6/%s %s §e- §f%s";
	public static String info_entry_format = "§e- %s: §4%s";
	
	public static String arena_status_changed_format = label + "Set §6%s §estatus to §6%s§e.";
	public static String ray_size_changed_format = label + "§aRay size set to §6%s§a.";
	public static String teleported_to_arena_format = label + "Teleported to arena §6%s§a.";
	public static String edit_mode_toggled = label + "Toggled edit mode.";
	public static String arena_removed_format = label + "§aRemoved arena §6%s§a.";
	public static String arena_created_format = label + "§aArena created. §eEdit it with §6/%s§a.";
	public static String lobby_set = label + "Lobby set.";
	public static String left_spectators = label + "You left spectators.";
	public static String left_game = label + "You left the game.";
	public static String available_arenas_header = label + "Available arenas:";
	public static String info_header = label + "Plugin Information:";
	public static String help_header = label + "Available Commands:";
	
	public static String player_joined_format = label + "§6%s§e joined the game. (§b%s§e/§b%s§e)";
	public static String player_left_format = label + "§6%s §eleft the game.";
	public static String player_spectating_format = label + "%s §eis spectating.";
	public static String player_stopped_spectating_format = label + "%s §eis no longer spectating.";
	public static String arena_starting_warning_format = label + "§eArena §6%s §eis starting in §6%s§e seconds. Join now!";
	public static String arena_starting_in_format = label + "§eStarting in §6%s§e.";
	
	public static String duos_winners_message_format = label + "§6%s Team §ewon with §6%s §efrags!";
	public static String duos_losers_message_format = label + "§e2. §6%s §e- §6%s";
	public static String ffa_winner_message_format = label + "§6%s §ewon with §6%s §efrags!";
	public static String ffa_tied_message_format = label + "§6%s §etied with §6%s §efrags!";
	public static String ffa_loser_message_format = label + "§e%s. §6%s §e- §6%s";
	
	public static String displaying_stats = label + "Displaying Stats";
	public static String spectate_leave_information = label + "To leave use §6/gib spectate %s§e.";
	public static String powerup_pickup_format = label + "Picked up PowerUp: §6%s";
	
	public static String edit_mode_set_corner_1 = label + "Set arena corner 1";
	public static String edit_mode_set_corner_2 = label + "Set arena corner 2";
	
	public static String powerup_spawned = label + "Powerup spawned.";
	public static String setting_updated = label + "Changed setting §6%s§e to '%s'.";
	public static String setting_reverted_to_default = label + "Reverted setting  §6%s§e to default value '%s'.";
	
	public static String powerup_title_format = "§ePOWERUP [§6%s§e]";
	
	// - Error Strings -
	
	public static String error_unknown_command = error_prefix + "Invalid Command.";
	public static String error_no_permission = error_prefix + "You do not have permission to do that.";
	public static String error_arena_not_found = error_prefix + "Arena doesn't exist.";
	public static String error_arena_not_found_format = error_prefix + "Arena §6%s §cdoesn't exist.";
	public static String error_arena_already_exists = error_prefix + "Arena already exists.";
	public static String error_cannot_parse_raysize = error_prefix + "Could not parse §6%s §c as a double for ray size.";
	public static String error_game_in_progress = error_prefix + "Please wait for the game to finish.";
	public static String error_missing_args_format = error_prefix + "Missing arguments. §6/%s§c.";
	public static String error_cannot_specate = error_prefix + "Cannot spectate this game right now.";
	public static String error_cannot_do_this_now = error_prefix + "You can't do this right now.";
	public static String error_not_in_game = error_prefix + "You're not in a game.";
	public static String error_already_in_game = error_prefix + "You're already in a game.";
	public static String error_cannot_join_now = error_prefix + "You can't join this arena right now.";
	public static String error_game_started_spectate_format = error_prefix + "That game has already started. Spectate with §6/%s§c.";
	public static String error_game_full_spectate_format = error_prefix + "Game is full. Spectate with §6/%s§c.";
	public static String error_bounds_not_configured = error_prefix + "This arena does not have boundaries set up.";
	public static String error_lobby_not_configured = error_prefix + "Lobby isn't set.";
	public static String error_invalid_mode_header = error_prefix + "Invalid mode. Available modes: ";
	public static String error_stats_no_games = error_prefix + "You haven't played a game yet.";
	public static String error_invalid_setting_name = error_prefix + "Invalid setting name. Available settings: ";
	
	public static String error_arena_being_setup = error_prefix + "This arena is being setup.";
	public static String error_arena_restarting = error_prefix + "This arena is restarting.";
	public static String error_team_full = error_prefix + "That team is full.";
	public static String error_command_blocked = error_prefix + "You can't do that inside the arena.";

	public static String error_spectate_already_in_game = error_prefix + "You cannot spectate while already in a game.";
	public static String error_powerup_spawn_raycast_fail = error_prefix + "Could not find a valid location to summon powerup.";
	public static String error_setting_parse_fail_int = error_prefix + "Could not parse '%s' as a valid integer. Value has not been updated.";
}
