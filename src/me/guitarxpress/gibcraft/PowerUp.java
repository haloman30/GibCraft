package me.guitarxpress.gibcraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PowerUp {

	private int id;
	private String name;
	private PotionEffect effect;

	public PowerUp(int id, String name, PotionEffect effect) {
		this.id = id;
		this.name = name;
		this.effect = effect;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PotionEffect getEffect() {
		return effect;
	}

	public void setEffect(PotionEffect effect) {
		this.effect = effect;
	}

	public void applyEffect(Player p) 
	{
		if (p.hasPotionEffect(effect.getType()))
		{
			p.removePotionEffect(effect.getType());
		}
		
		p.addPotionEffect(effect);
		GibCraft.playerPowerup.put(p, this);
		p.setLevel((int) ((double) effect.getDuration() / (double) 20));
		Bukkit.getScheduler().scheduleSyncDelayedTask(GibCraft.instance, () -> {
			clearEffect(p);
		}, effect.getDuration());
	}

	public void clearEffect(Player p) {
		p.removePotionEffect(effect.getType());
		GibCraft.playerPowerup.remove(p);
		p.setLevel(0);
		
		if (GibCraft.instance.getArenaManager().isPlayerInArena(p))
		{
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000 * 20, 0, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000 * 20, 0, true, false));
		}
	}

}
