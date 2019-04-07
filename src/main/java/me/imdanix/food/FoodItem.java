package me.imdanix.food;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FoodItem extends ItemStack {
	private String id;
	private int food;
	private Set<PotionEffect> effects;

	public FoodItem(String id, ConfigurationSection cfg) {
		this(id,
			 cfg.isString("type")?Material.valueOf(cfg.getString("type").toUpperCase()):Material.BROWN_MUSHROOM,
			 cfg.isInt("data")?cfg.getInt("data"):0,
			 cfg.getString("name"),
			 cfg.getStringList("lore"),
			 cfg.isInt("food")?cfg.getInt("food"):1,
			 cfg.getStringList("effects"));
	}

	public FoodItem(String id, Material type, int data, String name, List<String> lore, int food, List<String> effects) {
		super(type);
		this.id=id;
		super.setDurability((short)data);
		ItemMeta im=super.getItemMeta();
		if(name!=null)
			im.setDisplayName(Utils.clr(name));
		if(lore!=null&&!lore.isEmpty())
			im.setLore(Utils.clr(lore));
		super.setItemMeta(im);
		this.food=food;
		if(effects!=null) {
			this.effects=new HashSet<>();
			for(String e:effects) {
				String[] arr=e.split(":");
				this.effects.add(new PotionEffect(PotionEffectType.getByName(arr[0].toUpperCase()),Integer.valueOf(arr[2])*20, Integer.valueOf(arr[1])-1));
			}
		}
	}

	public boolean isSimilar(ItemStack is) {
		ItemMeta im1=is.getItemMeta();
		ItemMeta im2=super.getItemMeta();
		return is.getType()==super.getType()&&is.getDurability()==super.getDurability()&&
				(!im2.hasDisplayName()||(im1.hasDisplayName()&&im1.getDisplayName().equals(im2.getDisplayName())))&&
				(!im2.hasLore()||(im1.hasLore()&&im1.getLore().equals(im2.getLore())));
	}

	public String getId() {
		return id;
	}

	public void eat(Player p) {
		int foodlvl=p.getFoodLevel()+food;
		if(foodlvl>20) {
			p.setFoodLevel(20);
			p.setSaturation(((float)foodlvl-20)/2);
		} else p.setFoodLevel(foodlvl);
		if(effects!=null)
			p.addPotionEffects(effects);
	}
}
