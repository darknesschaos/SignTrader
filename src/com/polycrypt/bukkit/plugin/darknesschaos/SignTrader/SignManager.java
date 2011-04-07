package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.polycrypt.bukkit.tools.darknesschaos.ChestOperator;
import com.polycrypt.bukkit.tools.darknesschaos.ChestProtectionHandler;
import com.polycrypt.bukkit.tools.darknesschaos.EconomyHandler;
import com.polycrypt.bukkit.tools.darknesschaos.PermissionsHandler;
import com.polycrypt.bukkit.tools.darknesschaos.PlayerOperator;

public class SignManager {
	
	private SignTrader plugin;
	private String activeErr = "This sign has not been activated.";
	private String blacklistErr = "That item type cannot be sold or bought.";
	private String similarTypeErr = "The items defined are the same type, not trading.";
	private String formatErr = "The sign is in an unexpected format.";
	private String plugName;
	private String globalStr = "global";
	private String personalStr = "personal";
	private int maxDist = 30; // max x or z dist away (to keep from putting signs far away in a different chunk.)

	public SignManager (SignTrader st) {
		this.plugin = st;
		plugName = "" + ChatColor.BLUE + "[" + st.name + "] " + ChatColor.WHITE;
	}

	public void setSign(Sign s, Player p) {
		signSetter(s.getLines(),p,s.getBlock());
	}
	
	public void setSign(String[] lines, Player p, Block s) {
		signSetter(lines, p, s);
	}
	
	private void signSetter(String[] lines, Player p, Block s) {
		String type = SignOperator.getSignType(lines[0]);
		if (type == null)
			return;
		int[] lOneData = SignOperator.getTransFormat(lines[1]);
		if (lOneData == null)
			return;
		int[] lTwoData = SignOperator.getTransFormat(lines[2]);
		if (lTwoData == null)
			return;
		
		if (lTwoData[1] == lOneData[1]){
			p.sendMessage(plugName + similarTypeErr);
			return;
		}
		
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ()  + ":" + s.getWorld().getName();
		String pName = p.getName();
		
		if (type.compareToIgnoreCase("global") == 0){
			if (!PermissionsHandler.canSetGlobalSign(p)){
				p.sendMessage(PermissionsHandler.permissionErr);
				return;
			}
			p.sendMessage(plugName + "You have added the sign to the sign list.");
			SignTrader.signLocs.put(location, pName);
			plugin.fileIO.saveGlobalSigns();
			lines[3] = ChatColor.AQUA + "[Active]";
		}
		else if(type.compareToIgnoreCase("personal")==0){
			if (!PermissionsHandler.canSetPersonalSign(p)){
				p.sendMessage(PermissionsHandler.permissionErr);
				return;
			}
			p.sendMessage(plugName + "You have added the sign to the sign list.");
			SignTrader.signLocs.put(location, pName);
			lines[0] = p.getName();
			lines[3] = ChatColor.GREEN + "[Active]";
			plugin.fileIO.saveGlobalSigns();
		}
		
		((CraftWorld)s.getWorld()).getHandle().g(s.getX(),s.getY(),s.getZ());
	}
	
	public void useSign(Sign s, Player p) {
			
		String signType = SignOperator.getSignType(s.getLine(0));
		if(signType == null)
			return;
		//Get transaction data and check it.
		int[] lOneData = SignOperator.getTransFormat(s.getLine(1));
		int[] lTwoData = SignOperator.getTransFormat(s.getLine(2));
		if (lOneData == null || lTwoData == null){
			return;
		}
		
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ() + ":" + s.getWorld().getName();
		//See if the sign has been activated
		String signOwner = "";
		if (SignTrader.signLocs.containsKey(location))
			signOwner = SignTrader.signLocs.get(location);
		else
			return;
		
		Chest chest = null;
		
		if (signType.compareToIgnoreCase("personal") == 0) {
			int chestFind = SignOperator.findChest(location, s, signOwner);
			
			if (chestFind == 1 || chestFind == 0)
				chest = SignOperator.getChest(location, s);
			else {
				p.sendMessage(plugName + SignOperator.sendChestErr(chestFind));
				return;
			}
		}
		// See if player has permission to use signs.
		if(!PermissionsHandler.canUseSign(p)){
			p.sendMessage(plugName + PermissionsHandler.permissionErr);
			return;
		}
		if (lOneData[0] < -1){
			p.sendMessage(plugName + SignOperator.sendTransFormatErr(lOneData[0]));
			return;
		}
		else if (lTwoData[0] < -1){
			p.sendMessage(plugName + SignOperator.sendTransFormatErr(lTwoData[0]));
			return;
		}
		if (!SignTrader.signLocs.containsKey(location)){
			p.sendMessage(plugName + activeErr);
			return;
		}
		
		// Check to see if the items were blacklisted
		if ((lOneData[1] > 0) && (SignOperator.isBlackListed(lOneData[1]))){
			p.sendMessage(plugName  + blacklistErr);
			return;
		}
		if ((lTwoData[1] > 0) && (SignOperator.isBlackListed(lTwoData[1]))){
			p.sendMessage(plugName + blacklistErr);
			return;
		}
		
		
		
		//if the sign is giving items away for free
		if(lOneData[0] == 0 && lTwoData[1] > 0){
			giveFree(signType, p, chest, lTwoData);
		}
		//if the sign is taking donations
		else if(lTwoData[0] == 0 && lOneData[1] > 0){
			getDontation(signType, s, p, chest, lOneData);
		}
		//if the sign is selling items.
		else if(lOneData[1] == -1){
			sellItem(signType, p, chest, lOneData[0], lTwoData, signOwner);
		}
		//if the sign is buying items.
		else if(lTwoData[1] == -1){
			buyItem(signType, p, chest, lOneData, lTwoData[0], signOwner);
		}
		//if the sign is trading for items.
		else if (lTwoData[1] > 0 && lOneData[1] > 0){
			tradeItems(signType, p, chest, lOneData, lTwoData);
		}
		//catch
		else {
			p.sendMessage(plugName + formatErr );
		}
	}

	private void tradeItems(String signType, Player p, Chest chest,
			int[] lOneData, int[] lTwoData) {
		if (signType.compareToIgnoreCase(globalStr) == 0){
			if (!PlayerOperator.playerHasEnough(lOneData[0], lOneData[1], lOneData[2], p))
				p.sendMessage(plugName + PlayerOperator.playerStockErr);
			else {
				PlayerOperator.removeFromPlayer(lOneData[0], lOneData[1], lOneData[2], p);
				PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
			}
		}
		else if (signType.compareToIgnoreCase(personalStr) == 0){
			if (!PlayerOperator.playerHasEnough(lOneData[0], lOneData[1], lOneData[2], p))
				p.sendMessage(plugName + PlayerOperator.playerStockErr);
			else if (!ChestOperator.containsEnough(lTwoData[0], lTwoData[1], lTwoData[2], chest))
				p.sendMessage(plugName + ChestOperator.notEnoughErr);
			else if (!ChestOperator.hasEnoughSpace(lOneData[0], lOneData[1], lOneData[2], chest))
				p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
			
			else {
				ChestOperator.removeFromChestStock(lTwoData[0], lTwoData[1], lTwoData[2], chest);
				ChestOperator.addToChestStock(lOneData[0], lOneData[1], lOneData[2], chest);
				PlayerOperator.removeFromPlayer(lOneData[0], lOneData[1], lOneData[2], p);
				PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
			}
		}
	}

	private void buyItem(String signType, Player p, Chest chest, int[] lOneData, int costAmount, String signOwner) {
		if (signType.compareToIgnoreCase(globalStr) == 0){
			if (!PlayerOperator.playerHasEnough(lOneData[0], lOneData[1], lOneData[2], p)){
				p.sendMessage(plugName + PlayerOperator.playerStockErr);
				return;
			}
			else {
				PlayerOperator.removeFromPlayer(lOneData[0], lOneData[1], lOneData[2], p);
				EconomyHandler.modifyMoney(p.getName(), costAmount);
			}
		}
		else if (signType.compareToIgnoreCase(personalStr) == 0){
			if (!PlayerOperator.playerHasEnough(lOneData[0], lOneData[1], lOneData[2], p)){
				p.sendMessage(plugName + PlayerOperator.playerStockErr);
				return;
			}
			if (!ChestOperator.hasEnoughSpace(lOneData[0], lOneData[1], lOneData[2], chest)){
				p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
				return;
			}
			int econ = EconomyHandler.hasEnough(signOwner, costAmount);
			if (econ != 1){
				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
				return;
			}
			EconomyHandler.modifyMoney(signOwner, -costAmount);
			ChestOperator.addToChestStock(lOneData[0], lOneData[1], lOneData[2], chest);
			PlayerOperator.removeFromPlayer(lOneData[0], lOneData[1], lOneData[2], p);
			EconomyHandler.modifyMoney(p.getName(), costAmount);
			p.sendMessage(plugName + "You now have " + EconomyHandler.playerHave(p.getName()) + ".");
		}
	}

	private void sellItem(String signType, Player p, Chest chest, int costAmount, int[] lTwoData, String signOwner) {
		if (signType.compareToIgnoreCase(globalStr) == 0){
			int econ = EconomyHandler.hasEnough(p.getName(), costAmount);
			if (econ != 1){
				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
				return;
			}
			EconomyHandler.modifyMoney(p.getName(), -costAmount);
			p.sendMessage(plugName + "You now have " + EconomyHandler.playerHave(p.getName()) + ".");
			PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
		}
		else if (signType.compareToIgnoreCase(personalStr) == 0){
			int econPlayer = EconomyHandler.hasEnough(p.getName(), costAmount);
			if (econPlayer != 1){
				p.sendMessage(plugName + EconomyHandler.getEconError(econPlayer));
				return;
			}
			else if (!ChestOperator.containsEnough(lTwoData[0], lTwoData[1], lTwoData[2], chest)){
				p.sendMessage(plugName + ChestOperator.notEnoughErr);
				return;
			}
			int econOwner = EconomyHandler.modifyMoney(signOwner, costAmount);
			if (econOwner != 1){
				p.sendMessage(plugName + EconomyHandler.getEconError(econPlayer));
				return;
			}
			ChestOperator.removeFromChestStock(lTwoData[0], lTwoData[1], lTwoData[2], chest);
			EconomyHandler.modifyMoney(p.getName(), -costAmount);
			p.sendMessage(plugName + "You now have " + EconomyHandler.playerHave(p.getName()) + ".");
			PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
		}
	}

	private void getDontation(String signType, Sign s, Player p, Chest chest, int[] lOneData) {
		if (signType.compareToIgnoreCase(globalStr) == 0)
			return; //Global signs dont take donations atm.
		
		else if (signType.compareToIgnoreCase(personalStr) == 0){
			if (!PlayerOperator.playerHasEnough(lOneData[0], lOneData[1], lOneData[2], p))
				p.sendMessage(plugName + PlayerOperator.playerStockErr);
			else if (!ChestOperator.hasEnoughSpace(lOneData[0], lOneData[1], lOneData[2], chest))
				p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
			else {
				PlayerOperator.removeFromPlayer(lOneData[0], lOneData[1], lOneData[2], p);
				ChestOperator.addToChestStock(lOneData[0], lOneData[1], lOneData[2], chest);
			}
		}
	}

	private void giveFree(String signType, Player p, Chest chest, int[] lTwoData) {
		if (signType.compareToIgnoreCase(globalStr) == 0)
			PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
		else if (signType.compareToIgnoreCase(personalStr) == 0){
			if (ChestOperator.containsEnough(lTwoData[0], lTwoData[1], lTwoData[2], chest)){
				ChestOperator.removeFromChestStock(lTwoData[0], lTwoData[1], lTwoData[2], chest);
				PlayerOperator.givePlayerItem(lTwoData[0], lTwoData[1], lTwoData[2], p);
			}
			else
				p.sendMessage(plugName + ChestOperator.notEnoughErr);
		}
	}
	
	public void setChest(Sign s, Chest c, Player p){
		String sLoc = s.getX() + ":" + s.getY() + ":" + s.getZ()  + ":" + s.getWorld().getName();
		String cLoc = c.getX() + ":" + c.getY() + ":" + c.getZ(); // chests can only be in the same world as the sign and close.
		
		int distX = s.getX() - c.getX();
		int distZ = s.getZ() - c.getZ();
		if (distX > maxDist || distZ > maxDist ) {
			p.sendMessage(plugName + "The chest and sign are too far apart.");
			return;
		}
		
		String chestOwner = ChestProtectionHandler.getChestOwner(c.getBlock());
		if (chestOwner.compareToIgnoreCase(p.getName()) == 0 ||
				chestOwner.compareToIgnoreCase("-noprotection") == 0){
			p.sendMessage(plugName + "Sign and chest linked.");
		}
		else if (chestOwner.compareToIgnoreCase("-NoOwner") == 0){
			p.sendMessage(plugName + "The chest needs to be protected first.");
			return;
		}
		else {
			p.sendMessage(plugName + "You are not the owner of that chest.");
			return;
		}
		
		if (SignTrader.SignChest.get(sLoc) == cLoc){
			p.sendMessage(plugName + "That sign is already linked to that chest.");
			return;
		}
		
		SignTrader.SignChest.put(sLoc, cLoc);
		
	}

	public void setOwner(Player p, String str, Sign s) {
		if (!SignOperator.isSign(s)){
			p.sendMessage(plugName + "Sign needs to be activated first.");
			return;
		}
		s.setLine(0, str);
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ()  + ":" + s.getWorld().getName();
		SignTrader.signLocs.put(location, str);
		plugin.fileIO.saveGlobalSigns();
		((CraftWorld)s.getWorld()).getHandle().g(s.getX(),s.getY(),s.getZ());
	}
}
