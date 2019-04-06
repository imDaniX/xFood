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
	private int minClicks;
	private double minTime, maxTime;
	private final static PotionEffect slow=new PotionEffect(PotionEffectType.SLOW, 20, 2, true);

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
		eaters=new HashMap<>();
		initConfig();
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
		if(act==Action.LEFT_CLICK_AIR||act==Action.LEFT_CLICK_BLOCK)
			eaters.remove(e.getPlayer().getUniqueId());
		else {
			Player p=e.getPlayer();
			if(act==Action.RIGHT_CLICK_BLOCK&&p.isSneaking())
				return;
			Eater eater=eaters.get(p.getUniqueId());
			EquipmentSlot hand=EquipmentSlot.HAND;
			FoodItem foodItem=getFood(p.getInventory().getItemInMainHand());
			if(foodItem==null) {
				foodItem=getFood(p.getInventory().getItemInMainHand());
				if(foodItem==null)
					return;
				hand=EquipmentSlot.OFF_HAND;
			}
			e.setCancelled(true);
			if(eater!=null) {
				if(foodItem.getId().equals(eater.getFood())) {
					double time=(System.currentTimeMillis()-eater.getStartTime());
					if(time<minTime) {
						p.addPotionEffect(slow);
						World w = p.getWorld();
						w.playSound(p.getEyeLocation(), Sound.ENTITY_GENERIC_EAT, 1 , 1);
						w.spawnParticle(Particle.ITEM_CRACK,p.getEyeLocation(), 6,  0.1,0.1,0.1, 0.01, foodItem);
						eater.addClick();
						return;
					} else
					if(eater.getClicks()>=minClicks) {
						PlayerInventory inv=p.getInventory();
						if(hand==EquipmentSlot.HAND)
							inv.setItemInMainHand(removeItem(inv.getItemInMainHand()));
						else
							inv.setItemInOffHand(removeItem(inv.getItemInOffHand()));
						foodItem.eat(p);
						p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_BURP, 1 , 1);
					} else
					if(time<maxTime)
						return;
				}
				eaters.remove(e.getPlayer().getUniqueId());
			} else
				eaters.put(p.getUniqueId(), new Eater(foodItem.getId()));
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

	private void initConfig() {
		FileConfiguration cfg=getConfig();
		minClicks=cfg.getInt("settings.min_clicks");
		minTime=cfg.getDouble("settings.min_time")*1000;
		maxTime=cfg.getDouble("settings.max_time")*1000;
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
		if(is==null)
			return null;
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
		ls.forEach(s->clred.add(clr(s)));
		return clred;
	}
}
