package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.polycrypt.bukkit.tools.darknesschaos.ChestProtectionHandler;

public class SignTraderPlayerListener extends PlayerListener {

	private SignTrader plugin;
	private String plugName;
	
	//Sign Setting
	public HashMap<Player, Boolean> playerSetSign = new HashMap<Player,Boolean>();
	
	//Chest setting
	public HashMap<Player, Boolean> playerSetChest = new HashMap<Player, Boolean>();
	public HashMap<Player, Chest> playerChest = new HashMap<Player, Chest>();
	public HashMap<Player, Sign> playerSign = new HashMap<Player, Sign>();
	
	//SignOwner Setting
	public HashMap<Player, String> setOwner = new HashMap<Player,String>();
	

	public SignTraderPlayerListener(SignTrader signTrader) {
		this.plugin = signTrader;
		plugName = "" + ChatColor.BLUE + "[" + signTrader.name + "] " + ChatColor.WHITE;
	}

	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign) {
			if (SignOperator.isSign(e.getClickedBlock()))
				e.setCancelled(true);
			plugin.sm.useSign((Sign)e.getClickedBlock().getState(), e.getPlayer());
		}	
		if (e.getAction() == Action.LEFT_CLICK_BLOCK){
			if (e.getClickedBlock().getState() instanceof Sign){
				leftClickSign(e.getPlayer(), e.getClickedBlock());
			}
			
			if (e.getClickedBlock().getState() instanceof Chest)
				leftClickChest(e.getPlayer(), e.getClickedBlock());
		}
	}

	private void leftClickChest(Player p, Block b) {
		if (playerSetChest.containsKey(p)){
			if (playerChest.get(p) == (Chest)b.getState())
				p.sendMessage(plugName + "You have already marked that chest.");
			else {
				String name = ChestProtectionHandler.getChestOwner(b);
				int length = p.getName().length();
				if(length > 15) length = 15;
				if ( !name.equals(p.getName().substring(0, length))&&
						name.compareToIgnoreCase("-noprotection") != 0){
					p.sendMessage(plugName + "That is not your chest.");
					return;
				}
				playerChest.put(p, (Chest)b.getState());
				p.sendMessage(plugName + "Marked that Chest.");
				if (playerChest.containsKey(p) && playerSign.containsKey(p)){
					plugin.sm.setChest(playerSign.get(p), playerChest.get(p), p);
					if (playerSetChest.get(p) == false)
						playerSetChest.remove(p);
					playerChest.remove(p);
					playerSign.remove(p);
					plugin.fileIO.saveGlobalSigns();
				}
			}
		}
		
	}

	private void leftClickSign(Player p, Block b) {
		if (playerSetSign.containsKey(p)){
			plugin.sm.setSign((Sign) b.getState(), p);
			if (!playerSetSign.get(p))
				playerSetSign.remove(p);
		}
		else if (setOwner.containsKey(p)) {
			plugin.sm.setOwner(p, setOwner.get(p), (Sign) b.getState());
			setOwner.remove(p);
		}
		else if (playerSetChest.containsKey(p)){
			if (playerSign.get(p) == (Sign)b.getState())
				p.sendMessage(plugName + "You have already marked that sign.");
			else {
				if (!SignOperator.isSignOwner((Sign)b.getState(), p.getName())){
					p.sendMessage(plugName + "That is not your sign.");
					return;
				}
				playerSign.put(p, (Sign)b.getState());
				p.sendMessage(plugName + "Marked that sign.");
				if (playerChest.containsKey(p) && playerSign.containsKey(p)) {
					plugin.sm.setChest(playerSign.get(p), playerChest.get(p), p);
					if (playerSetChest.get(p) == false)
						playerSetChest.remove(p);
					playerChest.remove(p);
					playerSign.remove(p);
					plugin.fileIO.saveGlobalSigns();
				}
			}
		}
		else
			SignOperator.displaySignInfo((Sign)b.getState(), p);
		
	}

}
