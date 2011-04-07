package com.polycrypt.bukkit.tools.darknesschaos;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.polycrypt.bukkit.plugin.darknesschaos.SignTrader.SignTrader;

public class ChestOperator {

	private final static int chestSize = 27;
	public final static String notEnoughErr = "The chest doesn't have enough stock.";
	public static String notEnoughSpaceErr = "The chest doesn't have enough space.";
	
	//Thanks @YOGODA!
	private static Chest getDoubleChest(Chest chest){
        if(chest.getBlock().getRelative(BlockFace.NORTH).getType() == Material.CHEST)
             return (Chest)chest.getBlock().getRelative(BlockFace.NORTH).getState();
        else if(chest.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
             return (Chest)chest.getBlock().getRelative(BlockFace.SOUTH).getState();
        else if(chest.getBlock().getRelative(BlockFace.EAST).getType() == Material.CHEST)
             return (Chest)chest.getBlock().getRelative(BlockFace.EAST).getState();
        else if(chest.getBlock().getRelative(BlockFace.WEST).getType() == Material.CHEST)
             return (Chest)chest.getBlock().getRelative(BlockFace.WEST).getState();
        return null;
    }
	
	public static void removeFromChestStock(int amount, int type, int damage, Chest chest) {
		int count = amount;
		count = removeRunner(chest, count, damage, type);
		if (count < 1)
			return;
		
		Chest chestTwo = getDoubleChest(chest);
		if (chestTwo != null)
			removeRunner(chestTwo, count, damage, type);
	}

	private static int removeRunner(Chest chest, int amt, int damage, int type) {
		int count = amt;
		Inventory inv = chest.getInventory();
		ItemStack item = null;
		for (int i = 0; i < chestSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == damage){
				if (item.getAmount() - count < 1){
					count -= item.getAmount();
					inv.clear(i);
				}
				else{
					item.setAmount(item.getAmount() - count);
					return 0;
				}
					
			}
		}
		return count;
	}
	
	public static void addToChestStock(int amount, int type, int damage, Chest chest) {
		int count = amount;
		count = addRunner(count, type, damage, chest);
		
		if (count < 1)
			return;
		
		Chest chestTwo = getDoubleChest(chest);
		if (chestTwo != null)
			addRunner(count, type, damage, chestTwo);
	}
	
	private static int addRunner(int amount, int type, int damage, Chest chest) {
		int count = amount;
		int maxStack = getItemMaxStack(type);
		Inventory inv = chest.getInventory();
		ItemStack item = null;
		ItemStack stacker = new ItemStack(type);
		stacker.setDurability((short)damage);
		
		for (int i = 0; i < chestSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == damage){
				if (item.getAmount() < maxStack){
					if (item.getAmount() + count > maxStack){
						count -= maxStack - item.getAmount();
						item.setAmount(maxStack);
					}
					else {
						item.setAmount(item.getAmount() + count);
						return 0;
					}
				}
			}
			else if (item.getType() == Material.AIR){
				if (count >= maxStack){
					stacker.setAmount(maxStack);
					inv.setItem(i, stacker);
					count -= maxStack;
					if (count == 0)
						return 0;
				}
				else if (count < maxStack){
					stacker.setAmount(count);
					inv.setItem(i, stacker);
					return 0;
				}
			}
		}
		return count;
		
	}

	static int getItemMaxStack(int type) {
		if (SignTrader.itemMaxIdStack.containsKey(type))
			return SignTrader.itemMaxIdStack.get(type);
		return 64;
	}
	
	public static boolean containsEnough(int amount, int type, int damage, Chest chest) {
		int count = amount;
		count = containsRunner(count, type, damage, chest);
		if (count < 1)
			return true;
		
		Chest chestTwo = getDoubleChest(chest);
		if (chestTwo != null){
			count = containsRunner(count, type, damage, chestTwo);
			if (count < 1)
				return true;
		}
		return false;
	}
	
	private static int containsRunner(int amount, int type, int damage, Chest chest){
		int count = amount;
		Inventory inv = chest.getInventory();
		ItemStack item = null;
		for (int i = 0; i < chestSize; i++){
			item = inv.getItem(i);
			if (item.getTypeId() == type && item.getDurability() == (short)damage){
				count -= item.getAmount();
				if (count < 1)
					return 0;
			}
		}
		return count;
	}
	
	public static boolean hasEnoughSpace(int amount, int type, int damage, Chest chest) {
		int count = amount;
		count = emptyRunner(count, type, damage, chest);
		if (count < 1)
			return true;
		
		Chest chestTwo = getDoubleChest(chest);
		if (chestTwo != null){
			count = emptyRunner(count, type, damage, chestTwo);
			if (count < 1)
				return true;
		}
		return false;
	}
	
	private static int emptyRunner(int amount, int type, int damage, Chest chest) {
		int count = amount;
		int maxStack = getItemMaxStack(type);
		Inventory inv = chest.getInventory();
		ItemStack item = null;
		for (int i = 0; i < chestSize; i++){
			item = inv.getItem(i);
			if (item.getType() == Material.AIR){
				count -= maxStack;
				if (count < 1)
					return 0;
			}
			else if (item.getTypeId() == type && item.getDurability() == (short)damage) {
				count -= maxStack - item.getAmount();
				if (count < 1)
					return 0;
			}
		}
		return count;
	}
	
}
