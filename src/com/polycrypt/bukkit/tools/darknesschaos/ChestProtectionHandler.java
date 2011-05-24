package com.polycrypt.bukkit.tools.darknesschaos;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.yi.acru.bukkit.Lockette.Lockette;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.polycrypt.bukkit.plugin.darknesschaos.SignTrader.SignTrader;

public class ChestProtectionHandler {
	
	private static boolean chestProtectionEnabled = false;
	private static LWC lwc = null;
	private static Lockette lockette = null;
	
	public static void setupChestProtection(SignTrader plugin){
		
    	Plugin test = plugin.getServer().getPluginManager().getPlugin("LWC");
    	
        if (test != null){
        	plugin.log.info("["+plugin.name+"] LWC found, using it.");
        	lwc = (LWC) ((LWCPlugin) test).getLWC();
        	chestProtectionEnabled = true;
        }
        
        test = plugin.getServer().getPluginManager().getPlugin("Lockette");
        if (test != null) {
        	plugin.log.info("["+plugin.name+"] Lockette found, using it.");
        	lockette = (Lockette)test;
        	chestProtectionEnabled = true;
        }
    }
	
	public static String getChestOwner(Block chest){
		if (!chestProtectionEnabled)
			return "-noprotection";
		
		if (lwc != null){
			if(!lwc.isProtectable(chest)) {
				return null;
			}
			
			List<Block> blocksProtected = lwc.getProtectionSet(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ());
			if(blocksProtected.size() > 0) {
				for(Block block : blocksProtected) {
					Protection protection = lwc.findProtection(block.getWorld(), block.getX(), block.getY(), block.getZ());
					if(protection != null) {
						return protection.getOwner();
					}
				}
			}
			return "-NoOwner";
		}
		
		else if (lockette != null){
			try {
				String name = Lockette.getProtectedOwner(chest);
				if (name != null)
					return name;
				else
					return "-NoOwner";
			}
			catch(Exception e) {
			}
		}
			
		return "-noprotection";
	}
}
