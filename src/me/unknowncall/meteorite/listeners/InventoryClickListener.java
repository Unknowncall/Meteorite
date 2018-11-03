package me.unknowncall.meteorite.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.unknowncall.meteorite.Meteorite;
import me.unknowncall.meteorite.objects.GUIItem;
import net.md_5.bungee.api.ChatColor;

public class InventoryClickListener implements Listener {
	
	private Meteorite plugin;

	public InventoryClickListener(Meteorite plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void inventoryClickListener(InventoryClickEvent e) {
		if (e.getAction() == null) {
			return;
		}
		if (ChatColor.stripColor(e.getInventory().getTitle()).equals(ChatColor.stripColor(this.plugin.getGuiTitle()))) {
			Player player = (Player) e.getWhoClicked();	
			if (!this.plugin.hasKey(player)) {
				e.setCancelled(true);
				return;
			}
			ItemStack keys = this.plugin.getKeys(player);
			int slot = e.getSlot();
			for (GUIItem item : this.plugin.getGuiItems()) {
				if (item.getPos() == slot) {
					if (keys.getAmount() >= item.getCost()) {
						if (keys.getAmount() > 1) {
							keys.setAmount(keys.getAmount() - 1);
							((Player) e.getWhoClicked()).updateInventory();
							e.setCancelled(true);
							String command = item.getCommand();
							command = command.replace("%player%", player.getName());
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
							return;
						} else {
							keys.setAmount(0);
							keys = null;
							e.setCancelled(true);
							String command = item.getCommand();
							command = command.replace("%player%", player.getName());
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
							return;
						}
					} else {
						e.setCancelled(true);
						return;
					}
				}
			}
			return;
		}
	}

}
