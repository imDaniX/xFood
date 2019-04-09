package me.imdanix.food;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

import static me.imdanix.food.FoodPlugin.*;

public class PlayerListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!e.getPlayer().hasPermission("xfood.eat"))
			return;
		Action act=e.getAction();
		if(act==Action.PHYSICAL)
			return;
		if(act==Action.LEFT_CLICK_AIR||act==Action.LEFT_CLICK_BLOCK)
			removeEater(e.getPlayer().getUniqueId());
		else {
			Player p=e.getPlayer();
			if(act==Action.RIGHT_CLICK_BLOCK) {
				if(p.isSneaking())
					return;
				e.setCancelled(true);
			}
			Eater eater=getEater(p.getUniqueId());
			EquipmentSlot hand=EquipmentSlot.HAND;
			FoodItem foodItem=getFood(p.getInventory().getItemInMainHand());
			if(foodItem==null) {
				foodItem=getFood(p.getInventory().getItemInMainHand());
				if(foodItem==null)
					return;
				hand=EquipmentSlot.OFF_HAND;
			}
			if(eater!=null) {
				sendDebug(p, "Eater found");
				if(foodItem.getId().equals(eater.getFood())) {
					double time=(System.currentTimeMillis()-eater.getStartTime());
					if(time<getMaxTime()) {
						sendDebug(p, "Eating");
						p.addPotionEffect(SLOW_EFFECT);
						World w = p.getWorld();
						w.playSound(p.getEyeLocation(), Sound.ENTITY_GENERIC_EAT, 1 , 1);
						w.spawnParticle(Particle.ITEM_CRACK,p.getEyeLocation(), 6,  0.1,0.1,0.1, 0.01, foodItem);
						eater.addClick();
						if(time>getMinTime()&&eater.getClicks()>=getMinClicks()) {
							sendDebug(p, "Eating finished");
							PlayerInventory inv=p.getInventory();
							if(hand==EquipmentSlot.HAND)
								inv.setItemInMainHand(Utils.removeItem(inv.getItemInMainHand()));
							else
								inv.setItemInOffHand(Utils.removeItem(inv.getItemInOffHand()));
							foodItem.eat(p);
							p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_BURP, 1 , 1);
						} else return;
					}
				}
				sendDebug(p, "Removing eater");
				removeEater(e.getPlayer().getUniqueId());
			} else {
				sendDebug(p, "New eater");
				addEater(p.getUniqueId(), foodItem.getId());
			}
		}
	}

	@EventHandler
	public void onSwitch(PlayerItemHeldEvent e) {
		removeEater(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removeEater(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent e) {
		removeEater(e.getDamager().getUniqueId());
	}

}
