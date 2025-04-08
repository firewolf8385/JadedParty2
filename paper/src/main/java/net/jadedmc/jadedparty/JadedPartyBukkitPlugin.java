package net.jadedmc.jadedparty;

import net.jadedmc.jadedparty.commands.party.PartyCMD;
import net.jadedmc.jadedparty.listeners.PlayerQuitListener;
import net.jadedmc.jadedparty.party.PartyManager;
import net.jadedmc.jadedparty.settings.ConfigManager;
import net.jadedmc.jadedparty.settings.HookManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JadedPartyBukkitPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private HookManager hookManager;
    private PartyManager partyManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.hookManager = new HookManager(this);
        this.partyManager = new PartyManager(this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register commands.
        getCommand("party").setExecutor(new PartyCMD(this));

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        JadedPartyAPI.initialize(this);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public HookManager getHookManager() {
        return this.hookManager;
    }

    public PartyManager getPartyManager() {
        return this.partyManager;
    }
}