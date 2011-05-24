package com.polycrypt.bukkit.tools.darknesschaos;

import com.nijikokun.register.payment.Methods;
import com.polycrypt.bukkit.plugin.darknesschaos.SignTrader.SignTrader;

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class EconServerListener extends ServerListener {
    // Change "MyPlugin" to the name of your MAIN class file.
    // Let's say my plugins MAIN class is: Register.java
    // I would change "MyPlugin" to "Register"
    private SignTrader plugin;
    static Methods Methods = null;

    public EconServerListener(SignTrader plugin) {
        this.plugin = plugin;
        EconServerListener.Methods = new Methods();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        // Check to see if the plugin thats being disabled is the one we are using
        if (EconServerListener.Methods != null && EconServerListener.Methods.hasMethod()) {
            Boolean check = EconServerListener.Methods.checkDisabled(event.getPlugin());

            if(check) {
                this.plugin.Method = null;
                System.out.println("[" + plugin.name + "] Payment method was disabled. No longer accepting payments.");
                EconomyHandler.currencyEnabled = false;
            }
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        // Check to see if we need a payment method
        if (!EconServerListener.Methods.hasMethod()) {
            if(EconServerListener.Methods.setMethod(event.getPlugin())) {
                // You might want to make this a public variable inside your MAIN class public Method Method = null;
                // then reference it through this.plugin.Method so that way you can use it in the rest of your plugin ;)
                this.plugin.Method = EconServerListener.Methods.getMethod();
                System.out.println("[" + plugin.name + "] Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
                EconomyHandler.currencyEnabled = true;
            }
        }
    }
}