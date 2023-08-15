package me.guitarxpress.gibcraft.tasks;

import me.guitarxpress.gibcraft.Arena;
import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class TickTask implements Runnable
{
	@Override
	public void run()
	{
		ArenaManager am = GibCraft.instance.getArenaManager();
		
		for (Arena arena : am.arenas)
		{
			arena.OnTickPass();
		}
	}
}