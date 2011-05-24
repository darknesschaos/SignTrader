package com.polycrypt.bukkit.tools.darknesschaos;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerOperator {
	
	public static String playerStockErr = "You do not have enough of the required item.";
	public static String playerSpaceErr = "You do not have enough free space in your inventory.";

	public static boolean playerHasEnoughSpace(int amount, int type, int damage, Player p){
		int total = 0;
		Inventory inv = p.getInventory();
		int invSize = inv.getSize();
		ItemStack item = null;
		for (int i = 0; i < invSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == (short)damage){
				total += (item.getType().getMaxStackSize() - item.getAmount());
			}
			if (item.getType() == Material.AIR)
				total += Material.getMaterial(type).getMaxStackSize();
			
			if (total >= amount)
				return true;
		}
		return false;
	}
	
	public static void givePlayerItem(int amount, int type, int damage, Player p) {
		if( Material.getMaterial(type) == null ) return; // prevents giving the player illegal items 
	    int total = amount;
		int maxItemStack = Material.getMaterial(type).getMaxStackSize();
		Inventory inv = p.getInventory();
		int invSize = inv.getSize();
		ItemStack item = null;
		ItemStack setItem = new ItemStack(Material.getMaterial(type), maxItemStack, (short)damage);
		
		for (int i = 0; i < invSize; i++){
			item = inv.getItem(i);
			if ((item.getTypeId() == type) && (item.getDurability() == (short)damage) && (item.getAmount() < maxItemStack)){
				if ((item.getAmount() + total) >= maxItemStack){
					total -= (maxItemStack - item.getAmount());
					item.setAmount(maxItemStack);
				}
				else {
					item.setAmount(item.getAmount() + total);
					return;
				}
			}
			else if (item.getType() == Material.AIR){
				if (total > maxItemStack){
					total -= maxItemStack;
					setItem.setAmount(maxItemStack);
					inv.setItem(i, setItem);
				}
				else {
					setItem.setAmount(total);
					inv.setItem(i, setItem);
					return;
				}
			}
		}
	}
	
	public static boolean playerHasEnough(int amount, int type, int damage, Player p) {
		int total = 0;
		Inventory inv = p.getInventory();
		int invSize = inv.getSize();
		ItemStack item = null;
		for (int i = 0; i < invSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == (short)damage){
				total += item.getAmount();
				if (total >= amount)
					return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static void removeFromPlayer(int amount, int type, int damage, Player p) {
		int total = amount;
		Inventory inv = p.getInventory();
		int invSize = inv.getSize();
		ItemStack item = null;
		
		for (int i = 0; i < invSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == (short)damage){
				if (total > item.getAmount()){
					total -= item.getAmount();
					inv.clear(i);
				}
				else {
					if (item.getAmount() == total)
						inv.clear(i);
					else
						item.setAmount(item.getAmount() - total);
					p.updateInventory();
					return;
				}
			}
		}
	}
}

