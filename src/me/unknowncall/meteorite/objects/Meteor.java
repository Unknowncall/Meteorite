package me.unknowncall.meteorite.objects;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.FlameEffect;
import me.unknowncall.meteorite.Meteorite;

public class Meteor {

	private Location landingLocation;
	private int breaksRequired;
	private ArmorStand armorStand;
	private ItemStack key;
	private EffectManager em;

	public Meteor(Location landingLocation, int breaksRequired, ItemStack key,
			ArmorStand armorStand, Meteorite plugin) {
		this.landingLocation = landingLocation;
		this.breaksRequired = breaksRequired;
		this.key = key;
		this.armorStand = armorStand;
		this.em = new EffectManager(plugin);
	}

	public Location getLandingLocation() {
		return landingLocation;
	}

	public void setLandingLocation(Location landingLocation) {
		this.landingLocation = landingLocation;
	}

	public int getBreaksRequired() {
		return breaksRequired;
	}

	public void setBreaksRequired(int breaksRequired) {
		this.breaksRequired = breaksRequired;
	}

	public void spawn(Meteorite plugin) {
		this.armorStand.setVisible(false);
		armorStand.setHelmet(new ItemStack(plugin.getBlockType()));
		Effect effect = new FlameEffect(em);
		effect.disappearWithOriginEntity = true;
		effect.particleSize = 100.0f;
		effect.particleCount = 100;
		effect.visibleRange = 100000.0f;
		effect.setEntity(this.armorStand);
		effect.start();
	}

	public ArmorStand getArmorStand() {
		return armorStand;
	}

	public void setArmorStand(ArmorStand armorStand) {
		this.armorStand = armorStand;
	}

	public ItemStack getKey() {
		return key;
	}

	public void setKey(ItemStack key) {
		this.key = key;
	}

	public boolean isLanded() {
		return didLanding;
	}
	
	private boolean didLanding = false;
	
	public void doLanding(Meteorite plugin) {
		if (this.armorStand.isOnGround() && didLanding == false) {
			this.landingLocation.getWorld().createExplosion(this.armorStand.getLocation(), 6.0f, true);
			em.dispose();
			Location spawnBlockAt = new Location(this.armorStand.getWorld(), this.armorStand.getLocation().getBlockX(), this.armorStand.getWorld().getHighestBlockYAt(this.armorStand.getLocation().getBlockX(), this.armorStand.getLocation().getBlockZ()), this.armorStand.getLocation().getBlockZ());
			this.armorStand.remove();
			spawnBlockAt.getBlock().setType(plugin.getBlockType());
			this.landingLocation = spawnBlockAt;
			didLanding = true;
		}
	}

	public void minusOneBreak() {
		this.breaksRequired--;
	}

}