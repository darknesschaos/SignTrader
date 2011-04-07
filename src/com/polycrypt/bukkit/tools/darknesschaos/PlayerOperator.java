package com.polycrypt.bukkit.tools.darknesschaos;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerOperator {
	
	public static String playerStockErr = "You do not have enough of the required item in hand.";

	public static void givePlayerItem(int amount, int type, int damage, Player p) {
		//TODO: get working with player.getInventory().addItem(item);
		//Catch to stop creation of non-existent items.
		if( Material.getMaterial(type) == null )
		    return; 
	    int amt = 0;
		int maxItemStack = ChestOperator.getItemMaxStack(type);
		
		ItemStack item = null;
		
		for(int amtToDrop = 0; amtToDrop < amount;){
			if ((amtToDrop + maxItemStack) <= amount)
				amt = maxItemStack;
			else
				amt = amount - amtToDrop;
			
			item = new ItemStack(type,amt,(short)damage);
			p.getWorld().dropItemNaturally(p.getLocation(), item);
			amtToDrop += amt;
		}
	}
	
	public static boolean playerHasEnough(int amount, int type, int damage, Player p) {
		if (p.getItemInHand().getTypeId() == type)
			return (p.getItemInHand().getAmount() >= amount && p.getItemInHand().getDurability() == (short)damage);
		return false;
	}
	
	public static void removeFromPlayer(int amount, int type, int damage, Player p) {
		ItemStack item = p.getItemInHand();

		if (item.getTypeId() == type && item.getDurability() == (short)damage)
			if((item.getAmount()-amount) < 1)
				p.setItemInHand(null);
			else
				item.setAmount(item.getAmount() - amount);
	}
}
