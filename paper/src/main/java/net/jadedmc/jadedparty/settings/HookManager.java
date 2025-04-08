package net.jadedmc.jadedparty.settings;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.settings.integration.JadedSyncIntegration;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import org.jetbrains.annotations.NotNull;

/**
 * Manages hooks into various third-party plugins.
 */
public class HookManager {
    private final JadedPartyBukkitPlugin plugin;
    private boolean hasPlaceholderAPI;
    private boolean hasJadedSync;

    /**
     * Creates the HookManager.
     * @param plugin Instance of the plugin.
     */
    public HookManager(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;

        this.hasPlaceholderAPI = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if(this.hasPlaceholderAPI) {
            // TODO: Register Placeholders if PlaceholderAPI is installed.

            // If debug mode is enable, announce PlaceholderAPI support.
            if(plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("Enabling PlaceholderAPI support.");
            }
        }

        this.hasJadedSync = plugin.getServer().getPluginManager().isPluginEnabled("JadedSync");
        if(hasJadedSync) {
            if(plugin.getConfigManager().isDebugMode()) plugin.getLogger().info("Enabling JadedSync support.");
            JadedSyncAPI.registerIntegration(new JadedSyncIntegration(plugin));
        }
    }

    /**
     * Get if the plugin should interact with PlaceholderAPI.
     * @return Whether PlaceholderAPI is enabled.
     */
    public boolean usePlaceholderAPI() {
        return this.hasPlaceholderAPI;
    }

    public boolean useJadedSync() {
        return this.hasJadedSync;
    }
}