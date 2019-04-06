package me.imdanix.food;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Eater {
	private UUID player;
	private String food;
	private double start;
	private int clicks;

	public Eater(Player player, String food) {
		this(player.getUniqueId(), food);
	}

	public Eater(UUID player, String food) {
		this.player=player;
		this.food=food;
		start=System.currentTimeMillis();
		clicks=1;
	}

	public void addClick() {
		++clicks;
	}
	public int getClicks() {
		return clicks;
	}
	public double getStartTime() {
		return start;
	}
	public UUID getPlayer() {
		return player;
	}
	public String getFood() {
		return food;
	}
}
