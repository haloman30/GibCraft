package me.guitarxpress.gibcraft.events;

import org.bukkit.Sound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.guitarxpress.gibcraft.GibCraft;
import me.guitarxpress.gibcraft.managers.ArenaManager;

public class PacketSend {

	ArenaManager am;

	public PacketSend(GibCraft plugin) {
		am = plugin.getArenaManager();

		plugin.getProtocolManager().addPacketListener(
				new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (am.isPlayerInArena(event.getPlayer()))
							if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT)
								for (Sound sound : event.getPacket().getSoundEffects().getValues())
									if (sound.name().equalsIgnoreCase("ENTITY_FIREWORK_ROCKET_LAUNCH"))
										event.setCancelled(true);
					}
				});
	}

}
