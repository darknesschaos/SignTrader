package com.polycrypt.bukkit.tools.darknesschaos;


public class EconomyHandler {
	
	public final static String noConomyErr = "There is no money system.";
	public final static String noAccountErr = "The account does not exist:";
	public final static String noFundsErr = "You or they do not have enough funds for that.";
	public final static String oddErr = "Something odd happened.";

	public static boolean currencyEnabled = false;
	
	public static int hasEnough(String pName, int amount) {
		// returns -2 if account doesnt have enough
		// returns -1 if no money system
		// returns 0 if the player account can't be found
		// returns +1 if successful
		
		if (!currencyEnabled){
			return -1;
		}
		if (!EconServerListener.Methods.getMethod().hasAccount(pName))
			return 0;
		if (EconServerListener.Methods.getMethod().getAccount(pName).hasEnough(amount))
			return 1;
		else
			return -2;
	}
	
	public static int modifyMoney(String pName, int amount) {
		// returns -2 if they don't have enough money.
		// returns -1 if no money system
		// returns 0 if the player account can't be found
		// returns +1 if successful
		if (!currencyEnabled)
			return -1;
		if (!EconServerListener.Methods.getMethod().hasAccount(pName))
			return 0;
		
		if (amount > -1){
			EconServerListener.Methods.getMethod().getAccount(pName).set(EconServerListener.Methods.getMethod().getAccount(pName).balance() + amount);
			return 1;
		}
		else {
			EconServerListener.Methods.getMethod().getAccount(pName).set(EconServerListener.Methods.getMethod().getAccount(pName).balance() - amount);
			return 1;
		}
	}

	public static String playerHave(String pName) {
		// Returns the value of the players account + currency name
		if (!currencyEnabled)
			return noConomyErr;
		if (!EconServerListener.Methods.getMethod().hasAccount(pName))
			return noAccountErr;
		
		return "" + EconServerListener.Methods.getMethod().getAccount(pName).balance();
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

	public static String getCurrencyName() {
		if (!currencyEnabled)
			return noConomyErr;
		return "coin";
	}
}
