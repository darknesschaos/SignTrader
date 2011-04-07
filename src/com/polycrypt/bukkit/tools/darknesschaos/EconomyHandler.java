package com.polycrypt.bukkit.tools.darknesschaos;

import org.bukkit.plugin.Plugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.polycrypt.bukkit.plugin.darknesschaos.SignTrader.SignTrader;

public class EconomyHandler {
	
	private static iConomy iconomy = null;
	public static String currencyName = "coin";
	
	public final static String noConomyErr = "There is no money system.";
	public final static String noAccountErr = "The account does not exist:";
	public final static String noFundsErr = "You or they do not have enough funds for that.";
	public final static String oddErr = "Something odd happened.";

	public static boolean currencyEnabled = false;
	
	public static void setupEconomy(SignTrader plugin){
	    Plugin test = plugin.getServer().getPluginManager().getPlugin("iConomy");
	    try{
		    if (test != null){
		    	iconomy = (iConomy)test;
		    	currencyName = iConomy.getBank().getCurrency();
		    	currencyEnabled = true;
		    	System.out.println("[" + plugin.name + "] iConomy found, using it.");
		    	return;
		    }
	    }
	    catch (Error e) {
	    	System.out.println("[" + plugin.name + "] Something odd happened trying to load iConomy. Try updating iConomy and SignTrader.");
	    }
	}
	
	public static int hasEnough(String pName, int amount) {
		// returns -3 if something odd happened
		// returns -2 if account doesnt have enough
		// returns -1 if no money system
		// returns 0 if the player account can't be found
		// returns +1 if successful
		
		if (!currencyEnabled){
			return -1;
		}
		
		if (iconomy != null){
			if (!(iConomy.getBank().hasAccount(pName))){
				return 0;
			}
			
			if (iConomy.getBank().getAccount(pName).hasEnough(amount))
				return 1;
			else return -2;
		}
		
		return -3;
	}
	
	@SuppressWarnings("deprecation")
	public static int modifyMoney(String pName, int amount) {
		// returns -3 if something odd happened.
		// returns -2 if they don't have enough money.
		// returns -1 if no money system
		// returns 0 if the player account can't be found
		// returns +1 if successful
		if (!currencyEnabled){
			return -1;
		}
		
		if (iconomy != null){
			if (!(iConomy.getBank().hasAccount(pName))){
				return 0;
			}
	
			Account account = iConomy.getBank().getAccount(pName);
			double balance = account.getBalance();
			
			if (amount > -1)
				balance += amount;
			else{
				if (account.hasEnough(amount))
					balance += amount;
				else
					return -2;
			}
			
			account.setBalance(balance);
			account.save();
			return 1;
		}
		return -3;
	}

	public static String playerHave(String pName) {
		if (iconomy != null){
			return "" + iConomy.getBank().getAccount(pName).getBalance() + " " + currencyName;
		}
		return noConomyErr;
	}

	public static String getEconError(int econ) {
		if (econ == 0)
			return noAccountErr;
		if (econ == -1)
			return noConomyErr;
		if (econ == -2)
			return noFundsErr;
		if (econ == -3)
			return oddErr;
		return "Could not find error code.";
	}
}
