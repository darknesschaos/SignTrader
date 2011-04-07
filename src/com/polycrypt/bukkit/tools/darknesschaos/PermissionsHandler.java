package com.polycrypt.bukkit.tools.darknesschaos;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.polycrypt.bukkit.plugin.darknesschaos.SignTrader.SignTrader;


public class PermissionsHandler {
	
	private static PermissionHandler permissions = null;
	public static final String permissionErr = "You don't have permission to use that.";
	public static final String permissionBreakErr = "You don't have permission to break that.";
	
	public static boolean permissionsEnabled = false;
	
	public static void setupPermissions(SignTrader plugin) {
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");
		if (test != null) {
			permissions = ((Permissions)test).getHandler();
			plugin.log.info("["+plugin.name+"] Permission found, using Permissions from TheYeti");
			permissionsEnabled = true;
			return;
		}
	}
	
	private static boolean checkNode(Player p, String node){
		if(permissions != null){
			if(Permissions.Security.permission(p, node))
				return true;
		}
		return false;
	}
	
	public static boolean canSetGlobalSign(Player p){
		//Checks to see if the player can set signs that get and give global values (aka not tied to an account)
		if (!permissionsEnabled)
			return p.isOp();
		else
			return checkNode(p, "signtrader.MakeGlobalSign");
	}
	
	public static  boolean canSetPersonalSign(Player p){
		//Checks to see if the player has permission to make trading signs
		if (permissionsEnabled)
			return checkNode(p, "signtrader.MakePersonalSign");
		return true;
	}
	
	public static boolean isSignOwner(Sign s, String name){
		String location = s.getX() + ":" + s.getY() + ":" + s.getZ() + ":" + s.getWorld().getName();
		if (SignTrader.signLocs.get(location) == null)
			return true;
		if (name.compareToIgnoreCase(SignTrader.signLocs.get(location)) == 0)
			return true;
		return false;
	}
	
	public static boolean canUseSign(Player p){
		//Checks to see if the player can use signs.
		if (permissionsEnabled)
			return checkNode(p, "signtrader.Use");
		
		return true;
	}

	public static boolean canSetOwner(Player p) {
		if (permissionsEnabled)
			return checkNode(p, "signtrader.admin.SetOwner");
		return p.isOp();
	}
	
}