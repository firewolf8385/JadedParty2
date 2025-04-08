package net.jadedmc.jadedparty.settings;

import me.clip.placeholderapi.PlaceholderAPI;
import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.utils.Tuple;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Manages everything configurable in the plugin.
 */
public final class ConfigManager {
    private final JadedPartyBukkitPlugin plugin;
    private FileConfiguration config;
    private final File configFile;
    private FileConfiguration messages;
    private final File messagesFile;

    /**
     * Sets up and loads the plugin configuration.
     * @param plugin Instance of the plugin.
     */
    public ConfigManager(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;

        // Loads config.yml
        this.config = plugin.getConfig();
        this.config.options().copyDefaults(true);
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.saveConfig();

        // Loads messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if(!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Get the main configuration file.
     * @return Main configuration file.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets a configurable message from the config.
     * @param configMessage Targeted Configurable Message.
     * @return Configured String of the message.
     */
    public String getMessage(final ConfigMessage configMessage) {
        // Loads the default config message.
        String message = configMessage.getDefaultMessage();

        // If the message is configured, use that one instead.
        if(messages.isSet(configMessage.getKey())) {
            message = messages.getString(configMessage.getKey());
        }
        else if(isDebugMode()) {
            plugin.getLogger().info(configMessage.getKey() + " called while missing from messages.yml.");
        }

        // Replace newline characters from YAML with MiniMessage newline.
        message = message.replace("\\n", "<newline>");

        return message;
    }

    /**
     * Gets a configurable message from the config with Placeholder support.
     * @param player Player to process placeholders for.
     * @param configMessage Targeted Configurable message.
     * @param placeholders Placeholders to apply to the message.
     * @return Configured String of the message, with placeholders.
     */
    @SafeVarargs
    public final String getMessage(@NotNull final Player player, final ConfigMessage configMessage, final Tuple<String, String>... placeholders) {
        String message = getMessage(configMessage);

        // Assigned placeholders.
        for(final Tuple<String, String> placeholder : placeholders) {
            message = message.replace(placeholder.getLeft(), placeholder.getRight());
        }

        // Player username placeholder.
        message = message.replace("%player_name%", player.getName());

        // Process placeholders if PlaceholderAPI is installed.
        if(plugin.getHookManager().usePlaceholderAPI()) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }

    /**
     * Check if the plugin is in Debug Mode.
     * Debug mode logs various information to the console to help diagnose issues.
     * @return true if in debug mode, false otherwise.
     */
    public boolean isDebugMode() {
        return this.config.getBoolean("debugMode");
    }

    /**
     * Check if the plugin is in standalone mode.
     * Standalone mode does not try to sync data between multiple servers.
     * @return true if in standalone mode, false otherwise.
     */
    public boolean isStandalone() {
        return this.config.getBoolean("standalone");
    }
}