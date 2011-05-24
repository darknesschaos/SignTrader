package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.polycrypt.bukkit.tools.darknesschaos.ChestProtectionHandler;
import com.polycrypt.bukkit.tools.darknesschaos.EconomyHandler;

public class SignOperator {

	private static String noProtErr = "There is no chest protection system.";
	private static String notOwnerErr = "The sign owner does not own the chest.";
	private static String noChestErr = "Could not find the chest linked to the sign.";
	private static String oddErr = "Something odd happened.";
	private static String needOwnerErr = "The chest linked needs an owner first.";
	private static String itemTypeErr = "No item of that type exists.";
	private static String formattingErr = "The sign is in an unexpected format.";
	
	public static String getSignType(String str) {
		if (str.compareToIgnoreCase("global") == 0 ||
				str.compareToIgnoreCase("[Global]") == 0)
			return "global";
		else if (str.length() > 0)
			return "personal";
		
		return null;
	}
	
	public static boolean isSign(Block b) {
		if (!(b.getState() instanceof Sign))
			return false;
		
		String location = b.getX() + ":" + b.getY() + ":" + b.getZ() + ":" + b.getWorld().getName();
		
		return SignTrader.signLocs.containsKey(location);
	}
	
	public static boolean isSign(Sign s) {
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ() + ":" + s.getWorld().getName();
		if(!SignTrader.signLocs.containsKey(location))
			return false;
		
		return true;
	}
	
	public static boolean isBlackListed (int type) {
		return !SignTrader.itemIdName.containsKey(type);
	}
	
	public static boolean isBlackListed (String type) {
		return !SignTrader.itemNameId.containsKey(type);
	}
	
	public static int[] getTransFormat(String str) {
		//Returns int,int,int for amount : type : damage
		//Returns 0,0,0 if Free
		//Returns int,-1,0 if it is using money
		//Returns -2,0.0 if there is no economy system
		//Returns -3,0,0 if the item type specified does not exist.
		//Returns null if there is a formatting error.
		int[] retInt = new int[3];
		retInt[0] = 0;
		retInt[1] = 0;
		retInt[2] = 0; // if no damage set, defaults to 0
		
		if(str.startsWith("Free")) {
			return retInt;	
		}
		else {
			String[] s = str.split(":");
			if (s.length == 2){
				if (str.endsWith("$")){
					if(!EconomyHandler.currencyEnabled){
						retInt[0] = -2;
						return retInt;
					}
					try{
						retInt[0] = Integer.parseInt(s[0]);
						retInt[1] = -1;
						return retInt;
					}
					catch(Exception e){
						return null;
					}
				}
				else{
					try{
						retInt[0] = Integer.parseInt(s[0]);
						retInt[1] = Integer.parseInt(s[1]);
						if(Material.getMaterial(retInt[1]) == null){
							retInt[0] = -3;
						}
						return retInt;
					}
					catch(Exception e){
						return null;
					}
				}
			}
			//for handling itemdata
			else if (s.length == 3){
				try{
					retInt[0] = Integer.parseInt(s[0]);
					retInt[1] = Integer.parseInt(s[1]);
					retInt[2] = Integer.parseInt(s[2]);
					if(Material.getMaterial(retInt[1]) == null){
						retInt[0] = -3;
					}
					return retInt;
				}
				catch(Exception e){
					return null;
				}
			}
		}
		return null;
	}
	
	public static void displaySignInfo(Sign s, Player p) {
		String signType = getSignType(s.getLine(0));
		if(signType == null)
			return;
		
		int[] lOneData = getTransFormat(s.getLine(1));
		int[] lTwoData = getTransFormat(s.getLine(2));
		if ((lOneData == null)||(lTwoData == null))
			return;
		
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ() + ":" + s.getWorld().getName();
		String separator = "-----------------------------------";
		
		p.sendMessage(separator);
		if (!SignTrader.signLocs.containsKey(location)){
			p.sendMessage(ChatColor.YELLOW + "The sign has not been activated.");
			p.sendMessage(ChatColor.YELLOW + "Activate by replacing the sign.");
		}
		
		if(signType.compareToIgnoreCase("global") == 0)
			p.sendMessage("This is a global sign.");
		else if (signType.compareToIgnoreCase("personal") == 0){
			if(getChest(location, s) == null) {
				p.sendMessage(ChatColor.AQUA + "There is no chest linked with this sign.");
				p.sendMessage("This is a personal sign owned by " + ChatColor.RED + SignTrader.signLocs.get(location) + ChatColor.WHITE +  ".");
			}
			else 
				p.sendMessage("This is a personal sign owned by " + ChatColor.RED + SignTrader.signLocs.get(location) + ChatColor.WHITE +  ".");
		}
		
		if(lOneData[0] == -2 || lTwoData[0] == 2){
			p.sendMessage("There is no Economy System.");
			p.sendMessage(separator);
			return;
		}
		if (lOneData[0] == -3)
			p.sendMessage("The first line has an innapropriate item type.");
		if (lTwoData[0] == -3)
			p.sendMessage("The second line has an innapropriate item type.");
		if (lOneData[0] == -3 || lTwoData[0] == -3){
			p.sendMessage(separator);
			return;
		}
		
		int getAmount = lOneData[0];
		int giveAmount = lTwoData[0];
		int getType = lOneData[1];
		int giveType = lTwoData[1];
		int getDamage = lOneData[2];
		int giveDamage = lTwoData[2];
		
		String getTypeText = "";
		if (getType == 35)
			getTypeText = woolText(getDamage);
		else if (getType == -1){
			getTypeText = EconomyHandler.getCurrencyName();
		}
		else {
			getTypeText = Material.getMaterial(getType).toString().toLowerCase(Locale.ENGLISH);
			if (getDamage != 0)
				getTypeText += ", with " + getDamage + " damage,";
		}
		
		String giveTypeText = "";
		if (giveType == 35)
			giveTypeText = woolText(giveDamage);
		else if (giveType == -1){
			giveTypeText = EconomyHandler.getCurrencyName();
		}
		else {
			giveTypeText = Material.getMaterial(giveType).toString().toLowerCase(Locale.ENGLISH);
			if (giveDamage != 0)
				giveTypeText += ", with " + giveDamage + " damage,";
		}
		//if the sign is handing away freebies.
		if(lOneData[0] == 0 || lOneData[1] == 0){
			if (lTwoData[1] > 0)
				p.sendMessage("Using the sign will get you " + ChatColor.RED + giveAmount +  ChatColor.WHITE + " of "
						+ ChatColor.RED + giveTypeText + ChatColor.WHITE + " for free.");
			
		}
		//if the sign is taking donations
		else if(getAmount == 0 || giveAmount == 0){
			if(signType.compareToIgnoreCase("global")==0)
				p.sendMessage("This sign can't take donations.");
			else
				p.sendMessage("This sign is taking donations of " + ChatColor.RED + getAmount + ChatColor.WHITE + " of "
						+ ChatColor.RED + getTypeText + ChatColor.WHITE + ".");
		}
		//if the sign is selling items.
		else if(lOneData[1] == -1){
			if (lTwoData[1] > 0)
				p.sendMessage("Using the sign will get you " + ChatColor.RED + giveAmount + ChatColor.WHITE + " of "
						+ ChatColor.RED + giveTypeText + ChatColor.WHITE + " at the cost of " + ChatColor.RED + getAmount + " " + ChatColor.WHITE + getTypeText + ".");
		}
		//if the sign is buying items.
		else if(lTwoData[1] == -1){
			p.sendMessage("Using the sign will get you " + ChatColor.RED + giveAmount 
					+ " " + ChatColor.WHITE + giveTypeText + " at the cost of " + ChatColor.RED + getAmount
					+ " " + getTypeText + ChatColor.WHITE + "(s).");
		}
		//if the sign is trading for items.
		else{
			p.sendMessage("This sign is trading " + ChatColor.RED + giveAmount + ChatColor.WHITE + " of " 
					+ ChatColor.RED + giveTypeText + ChatColor.WHITE + ", for " 
					+ ChatColor.RED + getAmount + " " + getTypeText + ChatColor.WHITE + "(s).");
		}
		p.sendMessage(separator);
	}

	private static String woolText(int damage) {
		if (damage == 0)
			return "White Wool";
		if (damage == 1)
			return "Orange Wool";
		if (damage == 2)
			return "Magenta Wool";
		if (damage == 3)
			return "Light Blue Wool";
		if (damage == 4)
			return "Yellow Wool";
		if (damage == 5)
			return "Lime Green Wool";
		if (damage == 6)
			return "Pink Wool";
		if (damage == 7)
			return "Gray Wool";
		if (damage == 8)
			return "Light Gray Wool";
		if (damage == 9)
			return "Cyan Wool";
		if (damage == 10)
			return "Purple Wool";
		if (damage == 11)
			return "Blue Wool";
		if (damage == 12)
			return "Brown Wool";
		if (damage == 13)
			return "Green Wool";
		if (damage == 14)
			return "Red Wool";
		if (damage == 15)
			return "Black Wool";
		
		return null;
	}

	public static Chest getChest(String location, Sign s) {
		if(SignTrader.SignChest.containsKey(location)) {
			String[] chestLoc = SignTrader.SignChest.get(location).split(":");
			try {
				int x = Integer.parseInt(chestLoc[0]);
				int y = Integer.parseInt(chestLoc[1]);
				int z = Integer.parseInt(chestLoc[2]);
				
				Block c = s.getWorld().getBlockAt(x, y, z);
				if (c.getState() instanceof Chest){
					return (Chest)c.getState();
				}
			}
			catch (Exception e) {
				System.out.println("[SignTrader] Sign locations have been inapropriately stored.");
			}
		}
		else {
			Block c = s.getBlock().getRelative(BlockFace.DOWN);
			if (c.getState() instanceof Chest)
				return (Chest)c.getState();
		}
		return null;
	}

	public static int findChest(String location, Sign s, String pName) {
		// Format for chest location is x:y:z
		// Returns 1 if chest is the sign owners.
		// Returns 0 if there is no chest protection.
		// Returns -1 if the person is not the chest owner.
		// Returns -2 if the chest stored has been removed.
		// Returns -3 if something odd happened.
		// Returns -4 if the chest has no owner.
		// returns -5 if there is no chest.
		if(SignTrader.SignChest.containsKey(location)) {
			String[] chestLoc = SignTrader.SignChest.get(location).split(":");
			try {
				int x = Integer.parseInt(chestLoc[0]);
				int y = Integer.parseInt(chestLoc[1]);
				int z = Integer.parseInt(chestLoc[2]);
				
				Block c = s.getWorld().getBlockAt(x, y, z);
				if (c.getState() instanceof Chest){
					String getOwner = ChestProtectionHandler.getChestOwner(c);
					int length = pName.length();
					if(length > 15) length = 15;
					
					if (getOwner.compareToIgnoreCase("-noprotection") == 0){
						return 0;
					}
					else if (getOwner.compareToIgnoreCase("-NoOwner") == 0){
						return -4;
					}
					else if(getOwner.equals(pName.substring(0, length))){
						return 1;
					}
					else
						return -1;
				}
				else {
					SignTrader.SignChest.remove(location);
					return -2;
				}
			}
			catch (Exception e) {
				return -3;
			}
		}
		else {
			Block c = s.getBlock().getRelative(BlockFace.DOWN);
			if (c.getState() instanceof Chest){
				String getOwner = ChestProtectionHandler.getChestOwner(c);
				int length = pName.length();
				if(length > 15) length = 15;
				if (getOwner.compareToIgnoreCase("-noprotection") == 0){
					return 0;
				}
				else if (getOwner.compareToIgnoreCase("-NoOwner") == 0){
					return -4;
				}
				else if(getOwner.equals(pName.substring(0, length))){
					return 1;
				}
				else
					return -1;
			}
			else {
				return -5;
			}
		}
	}

	public static String sendChestErr(int errorNum) {
		if (errorNum == 0) {
			return noProtErr;
		}
		else if (errorNum == -1) {
			return notOwnerErr;
		}
		else if (errorNum == -2) {
			return noChestErr;
		}
		else if (errorNum == -3) {
			return oddErr;
		}
		else if (errorNum == -4) {
			return needOwnerErr;
		}
		else if (errorNum == -5) {
			return noChestErr;
		}
		return "Could not find error code.";
	}
	
	public static String sendTransFormatErr(int errorNum) {
		
		if (errorNum == -2) {
			return EconomyHandler.noConomyErr;
		}
		else if (errorNum == -3) {
			return itemTypeErr;
		}
		else if (errorNum == -4) {
			return formattingErr;
		}
		return "Could not find error code.";
	}
	
	public static boolean isSignOwner(Sign s, String str){
		
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ() + ":" + s.getWorld().getName();
		if(SignTrader.signLocs.containsKey(location))
			if (SignTrader.signLocs.get(location).compareToIgnoreCase(str) == 0)
				return true;
		return false;
	}

	public static boolean canBreakSign(Sign s, Player p) {
		if (!isSign(s))
			return false;
		
		boolean bool = false;
		bool = isSignOwner(s,p.getName());
		if (!bool)
			bool = p.isOp();
		
		return bool;
	}

	public static Sign[] getAttachedSigns(Block b) {
		Sign[] signs = new Sign[5]; 
		
		Block check = b.getRelative(BlockFace.UP);
		if (check instanceof Sign &&
				check.getType() == Material.SIGN_POST)
			signs[0] = (Sign)check;
		
		check = b.getRelative(BlockFace.NORTH);
		if (check instanceof Sign &&
				check.getType() == Material.WALL_SIGN &&
				check.getData() == (byte)4)
			signs[1] = (Sign)check;
		
		check = b.getRelative(BlockFace.EAST);
		if (check instanceof Sign &&
				check.getType() == Material.WALL_SIGN &&
				check.getData() == (byte)2)
			signs[2] = (Sign)check;
		
		check = b.getRelative(BlockFace.SOUTH);
		if (check instanceof Sign &&
				check.getType() == Material.WALL_SIGN &&
				check.getData() == (byte)5)
			signs[3] = (Sign)check;
		
		check = b.getRelative(BlockFace.WEST);
		if (check instanceof Sign &&
				check.getType() == Material.WALL_SIGN &&
				check.getData() == (byte)3)
			signs[4] = (Sign)check;
		
		return signs;
	}
}
