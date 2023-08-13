package me.guitarxpress.gibcraft.tasks;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class SecondTask implements Runnable
{
	@Override
	public void run()
	{
		ArenaManager am = GibCraft.instance.getArenaManager();
		
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
}