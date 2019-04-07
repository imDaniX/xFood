package me.imdanix.food;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	static ItemStack removeItem(ItemStack is) {
		if(is.getAmount()==1)
			return null;
		is.setAmount(is.getAmount()-1);
		return is;
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
