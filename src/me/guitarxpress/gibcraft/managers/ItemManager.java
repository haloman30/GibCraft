package me.guitarxpress.gibcraft.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemManager {
	public static ItemStack gunRed;
	public static ItemStack gunBlue;
	public static ItemStack gunYellow;
	public static ItemStack gunGreen;

	public static ItemStack quitEdit;
	public static ItemStack boundariesItem;

	public static ItemStack[] editMode = new ItemStack[9];
	public static ItemStack[] guns = new ItemStack[4];

	public static ItemStack redTeam;
	public static ItemStack blueTeam;

	public static ItemStack filler;

	public static ItemStack redHat;
	public static ItemStack redTop;
	public static ItemStack redLegs;
	public static ItemStack redBoots;

	public static ItemStack blueHat;
	public static ItemStack blueTop;
	public static ItemStack blueLegs;
	public static ItemStack blueBoots;

	public static ItemStack[] redOutfit = new ItemStack[4];
	public static ItemStack[] blueOutfit = new ItemStack[4];

	public static void init() {
		createGunRed();
		createGunBlue();
		createGunYellow();
		createGunGreen();
		createQuitEdit();
		createBoundariesItem();
		createRedTeam();
		createBlueTeam();
		createFiller();

		createRedHat();
		createRedTop();
		createRedLegs();
		createRedBoots();

		createBlueHat();
		createBlueTop();
		createBlueLegs();
		createBlueBoots();
		
		redOutfit[0] = redHat;
		redOutfit[1] = redTop;
		redOutfit[2] = redLegs;
		redOutfit[3] = redBoots;
		
		blueOutfit[0] = blueHat;
		blueOutfit[1] = blueTop;
		blueOutfit[2] = blueLegs;
		blueOutfit[3] = blueBoots;

		editMode[0] = boundariesItem;
		editMode[8] = quitEdit;
		guns[0] = gunRed;
		guns[1] = gunBlue;
		guns[2] = gunYellow;
		guns[3] = gunGreen;
	}
	
	private static void createBlueHat() {
		ItemStack item = new ItemStack(Material.CYAN_WOOL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§4Blue Hat");
		item.setItemMeta(meta);
		blueHat = item;
	}

	private static void createBlueTop() {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.TEAL);
		meta.setDisplayName("§4Blue Top");
		item.setItemMeta(meta);
		blueTop = item;
	}

	private static void createBlueLegs() {
		ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.TEAL);
		meta.setDisplayName("§4Blue Legs");
		item.setItemMeta(meta);
		blueLegs = item;
	}

	private static void createBlueBoots() {
		ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.TEAL);
		meta.setDisplayName("§4Blue Boots");
		item.setItemMeta(meta);
		blueBoots = item;
	}

	private static void createRedHat() {
		ItemStack item = new ItemStack(Material.RED_WOOL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§4Red Hat");
		item.setItemMeta(meta);
		redHat = item;
	}

	private static void createRedTop() {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.RED);
		meta.setDisplayName("§4Red Top");
		item.setItemMeta(meta);
		redTop = item;
	}

	private static void createRedLegs() {
		ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.RED);
		meta.setDisplayName("§4Red Legs");
		item.setItemMeta(meta);
		redLegs = item;
	}

	private static void createRedBoots() {
		ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.RED);
		meta.setDisplayName("§4Red Boots");
		item.setItemMeta(meta);
		redBoots = item;
	}

	private static void createFiller() {
		ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(" ");
		item.setItemMeta(meta);
		filler = item;
	}

	private static void createRedTeam() {
		ItemStack item = new ItemStack(Material.RED_WOOL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§4Join Red Team");
		List<String> lore = new ArrayList<>();
		lore.add("§7Press to join Red team.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		redTeam = item;
	}

	private static void createBlueTeam() {
		ItemStack item = new ItemStack(Material.CYAN_WOOL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§3Join Blue Team");
		List<String> lore = new ArrayList<>();
		lore.add("§7Press to join Blue team.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		blueTeam = item;
	}

	private static void createBoundariesItem() {
		ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Set Boundaries");
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
		List<String> lore = new ArrayList<>();
		lore.add("Red Laser");
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
		List<String> lore = new ArrayList<>();
		lore.add("Blue Laser");
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
		List<String> lore = new ArrayList<>();
		lore.add("Yellow Laser");
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
		List<String> lore = new ArrayList<>();
		lore.add("Green Laser");
		meta.setLore(lore);
		item.setItemMeta(meta);
		gunGreen = item;
	}

}
