package me.imdanix.food;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class FoodPlugin extends JavaPlugin implements Listener {

	private Map<String, FoodItem> food;
	private Map<UUID, Eater> eaters;
	private int minClicks, minTime, maxTime;
	private final static PotionEffect slow=new PotionEffect(PotionEffectType.SLOW, 10, 2, true);

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
		eaters=new HashMap<>();
		initConfig();
		startScheduler();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length>0&&!sender.equals(Bukkit.getConsoleSender())&&sender.hasPermission("xfood.give")) {
			args[0]=args[0].toLowerCase();
			FoodItem foodItem=food.get(args[0]);
			if(foodItem==null)
				sender.sendMessage(clr("&e"+args[0]+" &cdo not exist."));
			else
				((Player)sender).getInventory().addItem(foodItem);
			return true;
		}
		if(sender.hasPermission("xfood.reload")) {
			reloadConfig();
			initConfig();
			sender.sendMessage(clr("&exFood &aplugin was successfully reloaded."));
			return true;
		}
		return false;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!e.getPlayer().hasPermission("xfood.eat"))
			return;
		Action act=e.getAction();
		if(act==Action.PHYSICAL)
			return;
		else if(act==Action.LEFT_CLICK_AIR||act==Action.LEFT_CLICK_BLOCK)
			eaters.remove(e.getPlayer().getUniqueId());
		else {
			Player p=e.getPlayer();
			if(act==Action.RIGHT_CLICK_BLOCK&&p.isSneaking())
				return;
			FoodItem foodItem=getFood(p.getInventory().getItemInMainHand());
			if(foodItem==null)
				foodItem=getFood(p.getInventory().getItemInOffHand());
			if(foodItem!=null) {
				if(!p.hasPermission("xfood.eat."+foodItem.getId()))
					return;
				e.setCancelled(true);
				Eater eater=eaters.get(p.getUniqueId());
				if(eater==null)
					eaters.put(p.getUniqueId(), new Eater(p, foodItem.getId()));
				else if(eater.getFood().equals(foodItem.getId())) {
					p.addPotionEffect(slow);
					World w = p.getWorld();
					w.playSound(p.getEyeLocation(), Sound.ENTITY_GENERIC_EAT, 1 , 1);
					w.spawnParticle(Particle.ITEM_CRACK,p.getEyeLocation(), 3,  0.1,0.1,0.1, 0.01, foodItem);
					eater.addClick();
				}
			}
		}
	}

	@EventHandler
	public void onSwitch(PlayerItemHeldEvent e) {
		eaters.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		eaters.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent e) {
		eaters.remove(e.getDamager().getUniqueId());
	}

	private void startScheduler() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for(Eater eater:eaters.values()) {
				Player p=Bukkit.getPlayer(eater.getPlayer());
				if(p==null) {
					eaters.remove(eater);
					return;
				}
				PlayerInventory inv=p.getInventory();
				FoodItem foodItem=getFood(inv.getItemInMainHand());
				EquipmentSlot hand=EquipmentSlot.HAND;
				if(foodItem==null) {
					foodItem=getFood(inv.getItemInOffHand());
					hand=EquipmentSlot.OFF_HAND;
				}
				if(foodItem!=null&&foodItem.equals(food.get(eater.getFood()))) {
					double time=(System.currentTimeMillis()-eater.getStartTime())/1000;
					if(time<=maxTime) {
						if(time>minTime&&eater.getClicks()>=minClicks) {
							if(hand==EquipmentSlot.HAND)
								inv.setItemInMainHand(removeItem(inv.getItemInMainHand()));
							else
								inv.setItemInOffHand(removeItem(inv.getItemInOffHand()));
							foodItem.eat(p);
							p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_BURP, 1 , 1);
							eaters.remove(eater.getPlayer());
						}
						continue;
					}
				}
				eaters.remove(eater.getPlayer());
			}
		}, 5L, 5L);
	}

	private void initConfig() {
		FileConfiguration cfg=getConfig();
		minClicks=cfg.getInt("settings.min_clicks");
		minTime=cfg.getInt("settings.min_time");
		maxTime=cfg.getInt("settings.max_time");
		food=new HashMap<>();
		for(String s:cfg.getConfigurationSection("food").getKeys(false)) {
			s=s.toLowerCase();
			food.put(s, new FoodItem(s, cfg.getConfigurationSection("food."+s)));
		}
	}

	private ItemStack removeItem(ItemStack is) {
		if(is.getAmount()==1)
			return null;
		is.setAmount(is.getAmount()-1);
		return is;
	}

	private FoodItem getFood(ItemStack is) {
		for(FoodItem foodItem:food.values()) {
			if(foodItem.isSimilar(is))
				return foodItem;
		}
		return null;
	}

	static String clr(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	static List<String> clr(List<String> ls) {
		List<String> clred=new ArrayList<>();
		for(String s:ls)
			clred.add(clr(s));
		return clred;
	}
}
