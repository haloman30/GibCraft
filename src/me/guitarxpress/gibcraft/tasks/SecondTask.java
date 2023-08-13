package me.guitarxpress.gibcraft.tasks;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Language;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class SecondTask implements Runnable
{
	@Override
	public void run()
	{
		ArenaManager am = GibCraft.instance.getArenaManager();
		
		// Stats scoreboard removal
		{
			ArrayList<Player> players_to_remove = new ArrayList<Player>();
			
			for (Player player : am.active_stats_boards.keySet())
			{
				int time = am.active_stats_boards.get(player);
				
				if (time <= 0)
				{
					players_to_remove.add(player);
				}
				else
				{
					am.active_stats_boards.put(player, time - 1);
				}
			}
			
			for (Player player : players_to_remove)
			{
				am.active_stats_boards.remove(player);

				Bukkit.getScheduler().scheduleSyncDelayedTask(GibCraft.instance, new Runnable() 
				{
					@Override
					public void run()
					{
						player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
					}
				}, 1L);
			}
		}
		
		// Arena countdown timers
		{
			ArrayList<Arena> arenas_to_remove = new ArrayList<Arena>();
			
			for (Arena arena : am.arenaCountdownTimer.keySet())
			{
				int time = am.arenaCountdownTimer.get(arena);
				
				if ((time % 5 == 0 && time >= 5) || (time > 0 && time < 5)) 
				{
					for (Player player : arena.getPlayers()) 
					{
						player.sendMessage(String.format(Language.arena_starting_in_format, time));
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
					}
				} 
				else if (time <= 0 && arena.getPlayerCount() >= arena.getMode().minPlayers()) 
				{
					am.start(arena);
					arenas_to_remove.add(arena);
				}

				if (arena.getPlayerCount() < arena.getMode().minPlayers()) 
				{
					arenas_to_remove.add(arena);
					arena.setStatus(Status.CANCELLED);
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(GibCraft.instance, new Runnable() 
					{
						@Override
						public void run()
						{
							arena.setStatus(Status.JOINABLE);
						}
					}, 1 * 20);
				}

				am.arenaCountdownTimer.put(arena, time - 1);
			}
			
			for (Arena arena : arenas_to_remove)
			{
				am.arenaCountdownTimer.remove(arena);
			}
		}
		
		
	}
}