package me.unknowncall.meteorite.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.unknowncall.meteorite.Meteorite;

public class BlockBreakListener implements Listener {
	
	private Meteorite plugin;

	public BlockBreakListener(Meteorite plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void blockBreakListener(BlockBreakEvent event) {
		if (this.plugin.getMeteor() == null) {
			return;
		} else {
			if (this.plugin.getMeteor().getLandingLocation().equals(event.getBlock().getLocation())) {
				this.plugin.setTotalTimeMined(this.plugin.getTotalTimeMined() + 1);
				if (this.plugin.getTimesMined().get(event.getPlayer()) == null) {
					this.plugin.getTimesMined().put(event.getPlayer(), 0);
				} else {
					int tmp = this.plugin.getTimesMined().get(event.getPlayer());
					this.plugin.getTimesMined().put(event.getPlayer(), tmp + 1);
				}
				if (this.plugin.getMeteor().getBreaksRequired() <= this.plugin.getTotalTimeMined()) {
					Player player = this.plugin.findTopMiner();
					player.getInventory().addItem(this.plugin.getKey());
					String message = this.plugin.getMeteor_mined_broadcast();
					message = message.replace("%player%", player.getName());
					Bukkit.broadcastMessage(message);
					player.sendMessage(this.plugin.getMeteor_mined_self());
					this.plugin.setMeteor(null);
					event.getBlock().setType(Material.AIR);
					event.setCancelled(true);
					this.plugin.setTotalTimeMined(0);
					return;
				}
				event.setCancelled(true);
			}
		}
	}

}