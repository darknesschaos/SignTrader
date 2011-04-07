package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.polycrypt.bukkit.tools.darknesschaos.PermissionsHandler;

public class CommandOperator {

	SignTrader plugin;
	
	public CommandOperator (SignTrader p) {
		plugin = p;
	}
	
	public boolean command(CommandSender sender, Command command, String commandLabel, String[] args) {
		Player p = null;
		if (sender instanceof Player)
			p = (Player)sender;
		else{
			System.out.println("[" + plugin.name + "] Currently, only players have access to commands.");
			return true;
		}
		
		if (commandLabel.compareToIgnoreCase("signtrader") == 0) {
			if (args.length < 1){
				p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " To use this command use the following structure:");
				p.sendMessage("'/signtrader argument p' the 'p' is optional.");
				p.sendMessage("The possible arguments are: ");
				p.sendMessage(ChatColor.AQUA + "-s" + ChatColor.WHITE + " to set signs.");
				p.sendMessage(ChatColor.AQUA + "-sc" + ChatColor.WHITE + " to link chests and signs.");
				p.sendMessage(ChatColor.AQUA + "-so" + ChatColor.WHITE + " to set the owner of a sign. Case Sensitive");
				return true;
			}
			else {
				
				boolean bool = true;
				if (plugin.playerListener.playerSetSign.containsKey(p)){
					plugin.playerListener.playerSetSign.remove(p);
					p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You are no longer setting signs.");
					if (args[0].compareToIgnoreCase("-s") == 0)
						return bool;
				}
				if(plugin.playerListener.playerSetChest.containsKey(p)){
					plugin.playerListener.playerSetChest.remove(p);
					plugin.playerListener.playerSign.remove(p);
					plugin.playerListener.playerChest.remove(p);
					p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You are no longer linking chests to signs.");
					if (args[0].compareToIgnoreCase("-sc") == 0)
						return bool;
				}
				if (plugin.playerListener.setOwner.containsKey(p)){
					plugin.playerListener.playerSetSign.remove(p);
					p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You are no longer setting sign owners.");
					if (args[0].compareToIgnoreCase("-so") == 0)
						return bool;
				}
				
				if (args[0].compareToIgnoreCase("-s") == 0) {
					bool = sCommand(p, args);
				}
				else if (args[0].compareToIgnoreCase("-sc") == 0) {
					bool = scCommand(p,args);
				}
				else if (args[0].compareToIgnoreCase("-so") == 0){
					bool = soCommand(p,args);
				}
				else
					bool = false;
				
				return bool;
			}
		}
		else if (commandLabel.compareToIgnoreCase("getdata") == 0) {
			return getData(p);
		}
		
		return false;
	}

	private boolean soCommand(Player p, String[] args) {
		if (!PermissionsHandler.canSetOwner(p)) {
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + PermissionsHandler.permissionErr);
			return true;	
		}
		else if (args.length != 2)
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You can only have 1 player name specified.");
		else {
			plugin.playerListener.setOwner.put(p, args[1]);
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " The next sign you set will have the owner: " + args[1] + ".");
		}
		return true;
		
	}

	private boolean scCommand(Player p, String[] args) {
		if (!PermissionsHandler.canSetPersonalSign(p)) {
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + PermissionsHandler.permissionErr);
			return true;
	}
		else if (args.length == 2) {
			if (args[1].compareToIgnoreCase("-p") == 0){
				p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " Punch a sign and the chest you want to link to it. You can do this until you type this command again.");
				plugin.playerListener.playerSetChest.put(p, true);
			}
			else
				p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " Unknown argumemnts.");
		}
		else if (args.length == 1){
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " Punch a sign and the chest you want to link to it.");
			plugin.playerListener.playerSetChest.put(p, false);
		}
		else
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " Unknown argumemnts.");
		
		return true;
	}

	private boolean sCommand(Player p, String[] args) {
		if (!PermissionsHandler.canSetPersonalSign(p)) {
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + PermissionsHandler.permissionErr);
			return true;
		}
		else if (args.length == 2) {
			if (args[1].compareToIgnoreCase("p") == 0){
				plugin.playerListener.playerSetSign.put(p, true);
					p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You are now setting signs. You can do this until you type this command again.");
			}
			else
				p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " Unknown argumemnts.");
		}
		else {
			plugin.playerListener.playerSetSign.put(p, false);
			p.sendMessage(ChatColor.DARK_BLUE + "[" + plugin.name + "]" + ChatColor.WHITE + " You are now setting signs.");
		}
		
		return true;
	}

	private boolean getData(Player p) {
		p.sendMessage("You have " + p.getItemInHand().getAmount() + " " + p.getItemInHand().getType().toString().toLowerCase(Locale.ENGLISH) + " with " + p.getItemInHand().getDurability() + " durability, and type number of " + p.getItemInHand().getTypeId() + ".");
		return true;
	}
	
	
}
