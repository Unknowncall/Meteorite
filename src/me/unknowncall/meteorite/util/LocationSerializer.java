package me.unknowncall.meteorite.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Serializes and de-serializes locations.
 * 
 * @author xMrPoi
 */
public class LocationSerializer {
	
	public static String serializeBlockLocation(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		return world + "|" + x + "|" + y + "|" + z;
	}
	
	public static String serializePlayerLocation(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		return world + "|" + x + "|" + y + "|" + z + "|" + yaw + "|" + pitch;
	}
	
	public static Location deserialize(String str) {
		String[] split = str.split("\\|");
		
		if (split.length != 4 && split.length != 6) return null;
		
		World world = Bukkit.getWorld(split[0]);
		
		if (world == null) return null;
		
		double x, y, z;
		try {
			x = Double.valueOf(split[1]);
			y = Double.valueOf(split[2]);
			z = Double.valueOf(split[3]);
		} catch (NumberFormatException exc) {
			return null;
		}
		
		if (split.length == 4) {
			return new Location(world, x, y, z);
		}
		
		float yaw, pitch;
		try {
			yaw = Float.valueOf(split[4]);
			pitch = Float.valueOf(split[5]);
		} catch (NumberFormatException exc) {
			return null;
		}
		
		return new Location(world, x, y, z, yaw, pitch);
	}
}