package me.guitarxpress.gibcraft.tasks;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.Language;
import me.guitarxpress.gibcraft.enums.Status;
import me.guitarxpress.gibcraft.events.SignEvents;
import me.guitarxpress.gibcraft.managers.ArenaManager;
import me.guitarxpress.gibcraft.managers.GameManager;
import me.guitarxpress.gibcraft.utils.Utils;

public class SecondTask implements Runnable
{	
	@Override
	public void run()
	{
		ArenaManager am = GibCraft.instance.getArenaManager();
		GameManager gm = GibCraft.instance.getGameManager();
		
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
			for (Arena arena : am.arenas)
			{
				int time = arena.countdown_timer;
				
				if (time >= 0)
				{
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
						arena.countdown_timer = -1;
					}

					if (arena.getPlayerCount() < arena.getMode().minPlayers()) 
					{
						arena.countdown_timer = -1;
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

					arena.countdown_timer = time - 1;
				}
				else 
				{
					arena.countdown_timer = -1;
				}
			}
		}
		
		// In-game arena countdown timer
		{
			ArrayList<Arena> arenas_to_remove = new ArrayList<Arena>();
			
			for (Arena arena : gm.timeToStartMap.keySet())
			{
				int timer = gm.timeToStartMap.get(arena);
				switch (timer) {
				case 3:
					gm.sendStartNotification(arena, "3");
					break;
				case 2:
					gm.sendStartNotification(arena, "2");
					break;
				case 1:
					gm.sendStartNotification(arena, "1");
					break;
				case 0:
					gm.sendStartNotification(arena, "Gib!");
					
					if (gm.hasEnoughPlayers(arena))
					{
						arena.setStatus(Status.ONGOING);
					}
					
					arenas_to_remove.add(arena);
					
					break;
				default:
					if (!gm.hasEnoughPlayers(arena))
					{
						arena.setStatus(Status.JOINABLE);
					}
					
					arenas_to_remove.add(arena);
					
					break;
				}
				
				gm.timeToStartMap.put(arena, --timer);
			}
			
			for (Arena arena : arenas_to_remove)
			{
				gm.timeToStartMap.remove(arena);
			}
		}
		
		// Update/remove arena signs
		UpdateArenaSigns();
		
		// Update arenas
		for (Arena arena : am.arenas)
		{
			arena.OnSecondPass();
		}
	}
	
	
	private void UpdateArenaSigns()
	{
		if (SignEvents.signsLoc == null) 
		{
			return;
		}
		
		ArenaManager am = GibCraft.instance.getArenaManager();
		Sign toRemove = null;
		
		for (Location loc : SignEvents.signsLoc) 
		{
			Sign sign = (Sign) loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getState();
			String s = Utils.getNameFromString(sign.getLine(1));
			
			if (!am.exists(s)) 
			{
				toRemove = sign;
				sign.getBlock().breakNaturally();
			} 
			else 
			{
				SignEvents.updateSign(sign, am, s);
			}
		}
		
		if (toRemove != null)
		{
			SignEvents.signsLoc.remove(toRemove.getLocation());
		}
	}
}