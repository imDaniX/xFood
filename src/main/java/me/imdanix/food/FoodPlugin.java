package me.imdanix.food;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FoodPlugin extends JavaPlugin implements Listener {

	private static Map<String, FoodItem> food;
	private static Map<UUID, Eater> eaters;
	private static int minClicks;
	private static double minTime, maxTime;
	private static boolean debug;
	public final static PotionEffect SLOW_EFFECT=new PotionEffect(PotionEffectType.SLOW, 20, 2, true);

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		initData();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length>0&&!sender.equals(Bukkit.getConsoleSender())&&sender.hasPermission("xfood.give")) {
			args[0]=args[0].toLowerCase();
			FoodItem foodItem=getFood(args[0]);
			if(foodItem==null)
				sender.sendMessage(Utils.clr("&e"+args[0]+" &cdo not exist."));
			else
				((Player)sender).getInventory().addItem(foodItem);
			return true;
		}
		if(sender.hasPermission("xfood.reload")) {
			reloadConfig();
			initData();
			sender.sendMessage(Utils.clr("&exFood &aplugin was successfully reloaded."));
			return true;
		}
		return false;
	}

	private void initData() {
		FileConfiguration cfg=getConfig();
		minClicks=cfg.getInt("settings.min_clicks");
		minTime=cfg.getDouble("settings.min_time")*1000;
		maxTime=cfg.getDouble("settings.max_time")*1000;
		debug=cfg.getBoolean("setiings.debug");
		food=new HashMap<>();
		eaters=new HashMap<>();
		for(String s:cfg.getConfigurationSection("food").getKeys(false)) {
			s=s.toLowerCase();
			food.put(s, new FoodItem(s, cfg.getConfigurationSection("food."+s)));
		}
	}

	static FoodItem getFood(String id) {
		return food.get(id);
	}
	static FoodItem getFood(ItemStack is) {
		if(is==null)
			return null;
		for(FoodItem foodItem:food.values()) {
			if(foodItem.isSimilar(is))
				return foodItem;
		}
		return null;
	}
	static Eater addEater(UUID player, String food) {
		return eaters.put(player, new Eater(food));
	}
	static Eater getEater(UUID player) {
		return eaters.get(player);
	}
	static Eater removeEater(UUID player) {
		return eaters.remove(player);
	}
	static int getMinClicks() {
		return minClicks;
	}
	static double getMinTime() {
		return minTime;
	}
	static double getMaxTime() {
		return maxTime;
	}
	public static void sendDebug(Player p, String s) {
		if(debug&&p.hasPermission("xfood.debug"))
			p.sendMessage(s);
	}
}
