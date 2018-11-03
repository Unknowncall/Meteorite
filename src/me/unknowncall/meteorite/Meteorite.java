package me.unknowncall.meteorite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SplittableRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.unknowncall.meteorite.listeners.BlockBreakListener;
import me.unknowncall.meteorite.listeners.InventoryClickListener;
import me.unknowncall.meteorite.objects.GUIItem;
import me.unknowncall.meteorite.objects.Meteor;
import me.unknowncall.meteorite.util.ItemStackSerializer;
import me.unknowncall.meteorite.util.LocationSerializer;
import net.md_5.bungee.api.ChatColor;

public class Meteorite extends JavaPlugin {

	private int minBreaksNeeded;
	private int maxBreaksNeeded;
	private Location minLocation;
	private Location maxLocation;
	private ItemStack key;
	private SplittableRandom randomGenerator;
	private int spawnHeight;
	private HashMap<Player, Integer> timesMined;
	private int totalTimeMined;
	private Meteor meteor;
	private int secondsBetweenSpawn;
	private Material blockType;
	private double changeInXMin;
	private double changeInXMax;
	private double changeInZMin;
	private double changeInY;
	private double changeInZMax;
	private String actionbartext;
	private String progressBarFront;
	private String progressBarBack;
	private String progressBarMiddle;
	private String progressBarMiddleCompleted;
	private String meteor_spawned;
	private String meteor_mined_broadcast;
	private String meteor_mined_self;
	private String noPermission;
	private String meteorActive;
	private String sendingMeteor;
	private String setPos;
	private int despawnTime;
	private String meteorVanished;
	private ArrayList<GUIItem> guiItems;
	private int guiSize;
	private String guiTitle;

	public void onEnable() {
		saveDefaultConfig();
		this.timesMined = new HashMap<Player, Integer>();
		this.loadVariables();
		this.randomGenerator = new SplittableRandom();
		this.getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
		this.getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
		this.getServer().getScheduler().runTaskTimer(this, new Task(this), 0, 1);
	}

	public void loadVariables() {
		this.minBreaksNeeded = this.getConfig().getInt("breaks.min");
		this.maxBreaksNeeded = this.getConfig().getInt("breaks.max");
		this.minLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.min"));
		this.maxLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.max"));
		this.spawnHeight = this.getConfig().getInt("spawn_location.height");
		this.key = ItemStackSerializer.deserialize(this.getConfig().getString("key"));
		this.secondsBetweenSpawn = this.getConfig().getInt("spawn_time");
		this.totalTimeMined = 0;
		this.blockType = Material.getMaterial(this.getConfig().getString("block"));
		this.changeInXMin = this.getConfig().getDouble("fall_direction.change_in_x.min");
		this.changeInXMax = this.getConfig().getDouble("fall_direction.change_in_x.max");
		this.changeInZMin = this.getConfig().getDouble("fall_direction.change_in_z.min");
		this.changeInZMax = this.getConfig().getDouble("fall_direction.change_in_z.max");
		this.changeInY = this.getConfig().getDouble("fall_direction.change_in_y");
		this.actionbartext = this.getConfig().getString("action_bar.text");
		this.progressBarFront = this.getConfig().getString("action_bar.progress_bar.first_edge");
		this.progressBarBack = this.getConfig().getString("action_bar.progress_bar.last_edge");
		this.progressBarMiddle = this.getConfig().getString("action_bar.progress_bar.middle");
		this.progressBarMiddleCompleted = this.getConfig().getString("action_bar.progress_bar.middle_completed");
		this.despawnTime = this.getConfig().getInt("despawn_time") * 20;
		// messages
		this.meteor_spawned = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.meteor_spawned"));
		this.meteor_mined_broadcast = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.meteor_mined_broadcast"));
		this.meteor_mined_self = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.meteor_mined_self"));
		this.noPermission = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.no_permission"));
		this.meteorActive = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.meteor_active"));
		this.sendingMeteor = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.sending_meteor"));
		this.setPos = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.set_pos"));
		this.meteorVanished = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("messages.meteor_vanished"));
		// gui
		this.guiSize = this.getConfig().getInt("gui.size");
		this.guiTitle = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("gui.title"));
		this.guiItems = new ArrayList<GUIItem>();
		for (String s : this.getConfig().getConfigurationSection("gui.items").getKeys(false)) {
			ItemStack displayItem = ItemStackSerializer
					.deserialize(this.getConfig().getString("gui.items." + s + ".display_item"));
			int pos = this.getConfig().getInt("gui.items." + s + ".pos");
			int cost = this.getConfig().getInt("gui.items." + s + ".cost");
			String command = this.getConfig().getString("gui.items." + s + ".command");
			this.guiItems.add(new GUIItem(displayItem, pos, cost, command));
		}
	}

	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("spawn")) {
				if (!sender.hasPermission("meteor.spawn")) {
					sender.sendMessage(this.noPermission);
					return true;
				}
				if (this.getMeteor() != null) {
					sender.sendMessage(this.meteorActive);
					return true;
				}
				this.spawnMeteorite();
				sender.sendMessage(this.sendingMeteor);
				String message = this.getMeteor_spawned();
				message = message.replace("%mine_amount%", this.getMeteor().getBreaksRequired() + "");
				Location loc = this.getMeteor().getLandingLocation();
				message = message.replace("%location%", "X: " + loc.getBlockX() + " Z: " + loc.getBlockZ());
				Bukkit.broadcastMessage(message);
				return true;
			}
			if (args[0].equalsIgnoreCase("pos1")) {
				if (!sender.hasPermission("meteor.setpos")) {
					sender.sendMessage(this.noPermission);
					return true;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
					return true;
				}
				Location p1 = ((Player) sender).getLocation();
				Location p2 = this.maxLocation;
				// Sort coordinates
				if (p1.getX() > p2.getX()) {
					double x = p1.getX();
					p1.setX(p2.getX());
					p2.setX(x);
				}
				if (p1.getY() > p2.getY()) {
					double y = p1.getY();
					p1.setY(p2.getY());
					p2.setY(y);
				}
				if (p1.getZ() > p2.getZ()) {
					double z = p1.getZ();
					p1.setZ(p2.getZ());
					p2.setZ(z);
				}
				this.getConfig().set("spawn_location.min", LocationSerializer.serializeBlockLocation(p1));
				this.getConfig().set("spawn_location.max", LocationSerializer.serializeBlockLocation(p2));

				this.saveConfig();
				this.minLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.min"));
				this.maxLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.max"));
				sender.sendMessage(this.setPos);
				return true;
			}
			if (args[0].equalsIgnoreCase("pos2")) {
				if (!sender.hasPermission("meteor.setpos")) {
					sender.sendMessage(this.noPermission);
					return true;
				}
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
					return true;
				}
				Location p1 = ((Player) sender).getLocation();
				Location p2 = this.minLocation;
				// Sort coordinates
				if (p1.getX() > p2.getX()) {
					double x = p1.getX();
					p1.setX(p2.getX());
					p2.setX(x);
				}
				if (p1.getY() > p2.getY()) {
					double y = p1.getY();
					p1.setY(p2.getY());
					p2.setY(y);
				}
				if (p1.getZ() > p2.getZ()) {
					double z = p1.getZ();
					p1.setZ(p2.getZ());
					p2.setZ(z);
				}
				this.getConfig().set("spawn_location.min", LocationSerializer.serializeBlockLocation(p1));
				this.getConfig().set("spawn_location.max", LocationSerializer.serializeBlockLocation(p2));
				this.saveConfig();
				this.minLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.min"));
				this.maxLocation = LocationSerializer.deserialize(this.getConfig().getString("spawn_location.max"));
				sender.sendMessage(this.setPos);
				return true;
			}
		}
		if (command.getName().equals("villager")) {
			((Player) sender).openInventory(this.buildInventory());
			return true;
		}

		return true;
	}

	public Inventory buildInventory() {
		Inventory inv = Bukkit.createInventory(null, (this.guiSize * 9), this.guiTitle);
		for (GUIItem item : this.guiItems) {
			inv.setItem(item.getPos(), item.getDisplay());
		}
		return inv;
	}

	public boolean hasKey(Player player) {
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null) {
				if (is.isSimilar(this.key)) {
					return true;
				}
			}
		}
		return false;
	}

	public ItemStack getKeys(Player player) {
		for (ItemStack is : player.getInventory().getContents()) {
			if (is.isSimilar(this.key)) {
				return is;
			}
		}
		return null;
	}

	// villager
	// commands
	// place villager
	// despawn system...

	public void spawnMeteorite() {
		int randomX = randomGenerator.nextInt(minLocation.getBlockX(), maxLocation.getBlockX() + 1);
		int randomZ = randomGenerator.nextInt(minLocation.getBlockZ(), maxLocation.getBlockZ() + 1);
		Location landingLocation = new Location(this.minLocation.getWorld(), randomX, this.spawnHeight, randomZ);
		ArmorStand as = (ArmorStand) landingLocation.getWorld().spawnEntity(
				new Location(landingLocation.getWorld(), randomX, this.spawnHeight, randomZ), EntityType.ARMOR_STAND);
		this.meteor = new Meteor(landingLocation, this.getBreaksNeeded(), key, as, this);
		this.meteor.spawn(this);

	}

	public int getBreaksNeeded() {
		return randomGenerator.nextInt(minBreaksNeeded, maxBreaksNeeded + 1);
	}

	public Meteor getMeteor() {
		return meteor;
	}

	public void setMeteor(Meteor meteor) {
		this.meteor = meteor;
	}

	public HashMap<Player, Integer> getTimesMined() {
		return timesMined;
	}

	public void setTimesMined(HashMap<Player, Integer> timesMined) {
		this.timesMined = timesMined;
	}

	public Player findTopMiner() {
		Player topMiner = null;
		int topMined = -1;
		for (Entry<Player, Integer> entry : this.timesMined.entrySet()) {
			if (topMiner == null) {
				topMiner = entry.getKey();
				topMined = entry.getValue();
			} else {
				if (topMined < entry.getValue()) {
					topMiner = entry.getKey();
					topMined = entry.getValue();
				}
			}
		}
		return topMiner;
	}

	public int getSecondsBetweenSpawn() {
		return secondsBetweenSpawn;
	}

	public void setSecondsBetweenSpawn(int secondsBetweenSpawn) {
		this.secondsBetweenSpawn = secondsBetweenSpawn;
	}

	public int getTotalTimeMined() {
		return totalTimeMined;
	}

	public void setTotalTimeMined(int totalTimeMined) {
		this.totalTimeMined = totalTimeMined;
	}

	public String getActionbartext() {
		return actionbartext;
	}

	public void setActionbartext(String actionbartext) {
		this.actionbartext = actionbartext;
	}

	public ItemStack getKey() {
		return this.key;
	}

	public int getMinBreaksNeeded() {
		return minBreaksNeeded;
	}

	public void setMinBreaksNeeded(int minBreaksNeeded) {
		this.minBreaksNeeded = minBreaksNeeded;
	}

	public int getMaxBreaksNeeded() {
		return maxBreaksNeeded;
	}

	public void setMaxBreaksNeeded(int maxBreaksNeeded) {
		this.maxBreaksNeeded = maxBreaksNeeded;
	}

	public Location getMinLocation() {
		return minLocation;
	}

	public void setMinLocation(Location minLocation) {
		this.minLocation = minLocation;
	}

	public Location getMaxLocation() {
		return maxLocation;
	}

	public void setMaxLocation(Location maxLocation) {
		this.maxLocation = maxLocation;
	}

	public SplittableRandom getRandomGenerator() {
		return randomGenerator;
	}

	public void setRandomGenerator(SplittableRandom randomGenerator) {
		this.randomGenerator = randomGenerator;
	}

	public int getSpawnHeight() {
		return spawnHeight;
	}

	public void setSpawnHeight(int spawnHeight) {
		this.spawnHeight = spawnHeight;
	}

	public Material getBlockType() {
		return blockType;
	}

	public void setBlockType(Material blockType) {
		this.blockType = blockType;
	}

	public void setKey(ItemStack key) {
		this.key = key;
	}

	public double getChangeInXMin() {
		return changeInXMin;
	}

	public void setChangeInXMin(double changeInXMin) {
		this.changeInXMin = changeInXMin;
	}

	public double getChangeInXMax() {
		return changeInXMax;
	}

	public void setChangeInXMax(double changeInXMax) {
		this.changeInXMax = changeInXMax;
	}

	public double getChangeInZMin() {
		return changeInZMin;
	}

	public void setChangeInZMin(double changeInZMin) {
		this.changeInZMin = changeInZMin;
	}

	public double getChangeInY() {
		return changeInY;
	}

	public void setChangeInY(double changeInY) {
		this.changeInY = changeInY;
	}

	public double getChangeInZMax() {
		return changeInZMax;
	}

	public void setChangeInZMax(double changeInZMax) {
		this.changeInZMax = changeInZMax;
	}

	public String getProgressBarFront() {
		return progressBarFront;
	}

	public void setProgressBarFront(String progressBarFront) {
		this.progressBarFront = progressBarFront;
	}

	public String getProgressBarBack() {
		return progressBarBack;
	}

	public void setProgressBarBack(String progressBarBack) {
		this.progressBarBack = progressBarBack;
	}

	public String getProgressBarMiddle() {
		return progressBarMiddle;
	}

	public void setProgressBarMiddle(String progressBarMiddle) {
		this.progressBarMiddle = progressBarMiddle;
	}

	public String getProgressBarMiddleCompleted() {
		return progressBarMiddleCompleted;
	}

	public void setProgressBarMiddleCompleted(String progressBarMiddleCompleted) {
		this.progressBarMiddleCompleted = progressBarMiddleCompleted;
	}

	public String getMeteor_spawned() {
		return meteor_spawned;
	}

	public void setMeteor_spawned(String meteor_spawned) {
		this.meteor_spawned = meteor_spawned;
	}

	public String getMeteor_mined_broadcast() {
		return meteor_mined_broadcast;
	}

	public void setMeteor_mined_broadcast(String meteor_mined_broadcast) {
		this.meteor_mined_broadcast = meteor_mined_broadcast;
	}

	public String getMeteor_mined_self() {
		return meteor_mined_self;
	}

	public void setMeteor_mined_self(String meteor_mined_self) {
		this.meteor_mined_self = meteor_mined_self;
	}

	public String getNoPermission() {
		return noPermission;
	}

	public void setNoPermission(String noPermission) {
		this.noPermission = noPermission;
	}

	public String getMeteorActive() {
		return meteorActive;
	}

	public void setMeteorActive(String meteorActive) {
		this.meteorActive = meteorActive;
	}

	public String getSendingMeteor() {
		return sendingMeteor;
	}

	public void setSendingMeteor(String sendingMeteor) {
		this.sendingMeteor = sendingMeteor;
	}

	public String getSetPos() {
		return setPos;
	}

	public void setSetPos(String setPos) {
		this.setPos = setPos;
	}

	public int getDespawnTime() {
		return despawnTime;
	}

	public void setDespawnTime(int despawnTime) {
		this.despawnTime = despawnTime;
	}

	public String getMeteorVanished() {
		return meteorVanished;
	}

	public void setMeteorVanished(String meteorVanished) {
		this.meteorVanished = meteorVanished;
	}

	public ArrayList<GUIItem> getGuiItems() {
		return guiItems;
	}

	public void setGuiItems(ArrayList<GUIItem> guiItems) {
		this.guiItems = guiItems;
	}

	public int getGuiSize() {
		return guiSize;
	}

	public void setGuiSize(int guiSize) {
		this.guiSize = guiSize;
	}

	public String getGuiTitle() {
		return guiTitle;
	}

	public void setGuiTitle(String guiTitle) {
		this.guiTitle = guiTitle;
	}

}