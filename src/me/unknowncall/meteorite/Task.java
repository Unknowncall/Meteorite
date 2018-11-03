package me.unknowncall.meteorite;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.connorlinfoot.actionbarapi.ActionBarAPI;

public class Task implements Runnable {

	private int counter;
	private int projOn;
	private Meteorite plugin;

	public Task(Meteorite meteorite) {
		this.plugin = meteorite;
		this.counter = 0;
		this.despawnCounter = 0;
		this.projOn = this.plugin.getSecondsBetweenSpawn() * 20;
		this.changeInX = -1.0;
		this.changeInZ = -1.0;
	}

	private double changeInX;
	private double changeInZ;
	private int despawnCounter;

	@Override
	public void run() {
		if (counter > this.projOn * 2) {
			this.counter = 0;
		}
		counter++;
		despawnCounter++;
		if (this.plugin.getMeteor() != null) {
			if (despawnCounter > this.plugin.getDespawnTime()) {
				this.plugin.getMeteor().getLandingLocation().getBlock().setType(Material.AIR);
				this.plugin.setTotalTimeMined(0);
				Bukkit.broadcastMessage(this.plugin.getMeteorVanished());
				despawnCounter = 0;
				this.plugin.setMeteor(null);
				return;
			}
			if (!this.plugin.getMeteor().isLanded()) {
				if (this.changeInX == -1.0) {
					this.changeInX = this.plugin.getRandomGenerator().nextDouble(this.plugin.getChangeInXMin(),
							this.plugin.getChangeInXMax());
				}
				if (changeInZ == -1.0) {
					this.changeInZ = this.plugin.getRandomGenerator().nextDouble(this.plugin.getChangeInZMin(),
							this.plugin.getChangeInZMax());
				}
				this.plugin.getMeteor().getArmorStand()
						.setVelocity(new Vector(changeInX, this.plugin.getChangeInY(), changeInZ));
			}
			this.plugin.getMeteor().doLanding(this.plugin);
			ActionBarAPI.sendActionBarToAllPlayers(this.getProgressBar());
		} else {
			this.changeInX = -1.0;
			this.changeInZ = -1.0;
			if (counter % this.projOn == 0) {
				if (this.plugin.getMeteor() == null) {
					this.plugin.spawnMeteorite();
					String message = this.plugin.getMeteor_spawned();
					message = message.replace("%mine_amount%", this.plugin.getMeteor().getBreaksRequired() + "");
					Location loc = this.plugin.getMeteor().getLandingLocation();
					message = message.replace("%location%", "X: " + loc.getBlockX() + " Z: " + loc.getBlockZ());
					Bukkit.broadcastMessage(message);
				}
			}
		}
	}

	public String getProgressBar() {
		String message = this.plugin.getActionbartext();
		if (message.contains("%percent_left%")) {
			double percentLeft = ((this.plugin.getTotalTimeMined() + 0.0) / this.plugin.getMeteor().getBreaksRequired())
					* 100;
			DecimalFormat df = new DecimalFormat("00.00");
			message = message.replace("%percent_left%", df.format(percentLeft));
		}
		if (message.contains("%percent_completed%")) {
			double percentLeft = ((this.plugin.getTotalTimeMined() + 0.0) / this.plugin.getMeteor().getBreaksRequired())
					* 100;
			double flipPercent = 100.0 - percentLeft;
			DecimalFormat df = new DecimalFormat("00.00");
			message = message.replace("%percent_completed%", df.format(flipPercent));
		}
		if (message.contains("%blocks_needed%")) {
			message = message.replace("%blocks_needed%", this.plugin.getMeteor().getBreaksRequired() + "");
		}
		if (message.contains("%blocks_mined%")) {
			message = message.replace("%blocks_mined%", this.plugin.getTotalTimeMined() + "");
		}
		if (message.contains("%blocks_remaining_to_mine%")) {
			message = message.replace("%blocks_remaining_to_mine%", (this.plugin.getMeteor().getBreaksRequired() - this.plugin.getTotalTimeMined()) + "");
		}
		if (message.contains("%progress_bar%")) {
			double percentLeft = ((this.plugin.getTotalTimeMined() + 0.0) / this.plugin.getMeteor().getBreaksRequired())
					* 10;
			String progressBar = this.plugin.getProgressBarFront();
			for (int i = 1; i < 11; i++) {
				if (i <= (int) percentLeft) {
					progressBar = progressBar + this.plugin.getProgressBarMiddleCompleted();
				} else {
					progressBar = progressBar + this.plugin.getProgressBarMiddle();
				}
			}
			progressBar = progressBar + this.plugin.getProgressBarBack();
			message = message.replace("%progress_bar%", progressBar);
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

}