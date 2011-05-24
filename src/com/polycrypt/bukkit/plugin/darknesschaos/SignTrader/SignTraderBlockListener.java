package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignTraderBlockListener extends BlockListener{

	private SignTrader plugin;
	@SuppressWarnings("unused")
	private String plugName;
	
	public SignTraderBlockListener(SignTrader signTrader) {
		this.plugin = signTrader;
		plugName = "" + ChatColor.BLUE + "[" + signTrader.name + "] " + ChatColor.WHITE;
	}
	
	public void onSignChange (SignChangeEvent e) {
		Location location = e.getBlock().getLocation();
		String loc = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().toString();
		if (!e.isCancelled() && SignTrader.signLocs.containsKey(loc))
			SignTrader.signLocs.remove(loc);
		plugin.sm.setSign(e.getLines(), e.getPlayer(), e.getBlock());
	}
	
	public void onBlockPlace (BlockPlaceEvent e) {
		Location location = e.getBlock().getLocation();
		String loc = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().toString();
		if (!e.isCancelled() && SignTrader.signLocs.containsKey(loc))
			SignTrader.signLocs.remove(loc);
	}
	
	public void onBlockBreak (BlockBreakEvent e) {
		Location location = e.getBlock().getLocation();
		String loc = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().toString();
		if (!e.isCancelled() && SignTrader.signLocs.containsKey(loc))
			SignTrader.signLocs.remove(loc);
		
		Sign[] signs = SignOperator.getAttachedSigns(e.getBlock());
		for (Sign sign : signs) {
			if (sign != null) {
				location = sign.getBlock().getLocation();
				loc = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().toString();
				if (!e.isCancelled() && SignTrader.signLocs.containsKey(loc))
					SignTrader.signLocs.remove(loc);
			}
		}
	}
}
