package com.polycrypt.bukkit.plugin.darknesschaos.SignTrader;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.polycrypt.bukkit.tools.darknesschaos.ChestProtectionHandler;
import com.polycrypt.bukkit.tools.darknesschaos.EconomyHandler;
import com.polycrypt.bukkit.tools.darknesschaos.PermissionsHandler;

public class SignTrader extends JavaPlugin{

	public final Logger log = Logger.getLogger("Minecraft");
	
	public final String name = "SignTrader";
	public final String version = "1.0.6";
	
	//File Handler
	FileOperations fileIO = new FileOperations(this);
	CommandOperator co = new CommandOperator(this);
	
	//Mapping
	public static HashMap<Integer,Integer> itemMaxIdStack = new HashMap<Integer,Integer>(); // Contains itemId and max size of stack
	public static HashMap<String,Integer> itemNameId = new HashMap<String,Integer>(); // Contains the name and id of the item associated with it
	public static HashMap<Integer,String> itemIdName = new HashMap<Integer,String>(); // Contains the name and id of the item associated with it
	public static HashMap<String,String> signLocs = new HashMap<String,String>(); //Contains Sign location, and playerName
	public static HashMap<String,String> SignChest = new HashMap<String,String>(); // Contains Sign location and chest Location.

	
	// Listeners
	public final SignTraderBlockListener blockListener = new SignTraderBlockListener(this);
	public final SignTraderPlayerListener playerListener = new SignTraderPlayerListener(this);

	public File makeFolder = this.getDataFolder();
	public SignManager sm = new SignManager(this);
	
	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
		
		//Setup the handlers
		ChestProtectionHandler.setupChestProtection(this);
		EconomyHandler.setupEconomy(this);
		PermissionsHandler.setupPermissions(this);
		fileIO.checkDataFolder();
		fileIO.loadItemData();
		signLocs = fileIO.loadGlobalSignData();
		
		log.info("[" + name + "] version " + version + " has been enabled.");
	}
	
	public void onDisable() {
		log.info("[" + name + "] version " + version + " has been disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return co.command(sender, command, commandLabel, args);
	}
}
