package me.guitarxpress.gibcraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public abstract class Logger
{
	public enum LogLevel
	{
		INFO,
		WARN,
		ERROR,
		SEVERE
	}
	
	private static ConsoleCommandSender console = Bukkit.getConsoleSender();
	
	public static void LogEvent(String message)
	{
		LogEvent(message, LogLevel.INFO);
	}
	
	public static void LogEvent(LogLevel level, String message)
	{
		LogEvent(message, level);
	}
	
	public static void LogEvent(String message, LogLevel level)
	{
		String prefix = "";
		
		switch (level)
		{
		case INFO:
			prefix = "[INFO] ";
			break;
		case WARN:
			prefix = ChatColor.YELLOW + "[WARN] ";
			break;
		case ERROR:
			prefix = ChatColor.RED + "[ERROR] ";
			break;
		case SEVERE:
			prefix = ChatColor.DARK_RED + "[SEVERE] ";
			break;
		default:
			prefix = "[INFO] ";
			break;
		}
		
		console.sendMessage(Language.label + prefix + message);
	}
}
