package me.guitarxpress.gibcraft.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {
	public static ItemStack gunRed;
	public static ItemStack gunBlue;
	public static ItemStack gunYellow;
	public static ItemStack gunGreen;

	public static ItemStack quitEdit;
	public static ItemStack boundariesItem;

	public static ItemStack[] editMode = new ItemStack[9];
	public static ItemStack[] guns = new ItemStack[4];

	public static void init() {
		createGunRed();
		createGunBlue();
		createGunYellow();
		createGunGreen();
		createQuitEdit();
		createBoundariesItem();
		editMode[0] = boundariesItem;
		editMode[8] = quitEdit;
		guns[0] = gunRed;
		guns[1] = gunBlue;
		guns[2] = gunYellow;
		guns[3] = gunGreen;
	}

	private static void createBoundariesItem() {
		ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Set Boundaries");
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("§6Left Click §7to set corner 1.");
		lore.add("§6Right Click §7to set corner 2.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		boundariesItem = item;
	}

	private static void createQuitEdit() {
		ItemStack item = new ItemStack(Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§4Exit Edit Mode");
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("§6Right Click §7to exit edit mode.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		quitEdit = item;
	}

	private static void createGunRed() {
		ItemStack item = new ItemStack(Material.IRON_HOE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§4Lasergun");
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("Red Laser");
		lore.add("Red");
		meta.setLore(lore);
		item.setItemMeta(meta);
		gunRed = item;
	}

	private static void createGunBlue() {
		ItemStack item = new ItemStack(Material.IRON_HOE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§3Lasergun");
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("Blue Laser");
		lore.add("Blue");
		meta.setLore(lore);
		item.setItemMeta(meta);
		gunBlue = item;
	}

	private static void createGunYellow() {
		ItemStack item = new ItemStack(Material.IRON_HOE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§eLasergun");
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("Yellow Laser");
		lore.add("Yellow");
		meta.setLore(lore);
		item.setItemMeta(meta);
		gunYellow = item;
	}

	private static void createGunGreen() {
		ItemStack item = new ItemStack(Material.IRON_HOE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§2Lasergun");
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		List<String> lore = new ArrayList<>();
		lore.add("Green Laser");
		lore.add("Green");
		meta.setLore(lore);
		item.setItemMeta(meta);
		gunGreen = item;
	}

}
