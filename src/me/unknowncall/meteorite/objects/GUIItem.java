package me.unknowncall.meteorite.objects;

import org.bukkit.inventory.ItemStack;

public class GUIItem {

	private ItemStack display;
	private int pos;
	private int cost;
	private String command;

	public GUIItem(ItemStack display, int pos, int cost, String command) {
		this.display = display;
		this.pos = pos;
		this.cost = cost;
		this.command = command;
	}

	public ItemStack getDisplay() {
		return display;
	}

	public void setDisplay(ItemStack display) {
		this.display = display;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
