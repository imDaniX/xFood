package me.imdanix.food;

public class Eater {
	private String food;
	private double start;
	private int clicks;

	public Eater(String food) {
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
	public String getFood() {
		return food;
	}
}
