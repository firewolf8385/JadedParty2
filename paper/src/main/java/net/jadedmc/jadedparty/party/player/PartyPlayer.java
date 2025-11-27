package net.jadedmc.jadedparty.party.player;

import me.clip.placeholderapi.PlaceholderAPI;
import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedsync.libraries.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PartyPlayer {
    private final String username;
    private final UUID uuid;
    private PartyRole role;
    private final String prefix;

    public PartyPlayer(@NotNull final Document document) {
        this.uuid = UUID.fromString(document.getString("uuid"));
        this.username = document.getString("username");
        this.role = PartyRole.valueOf(document.getString("role"));
        this.prefix = document.getString("prefix");
    }

    public PartyPlayer(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Player player, final PartyRole role) {
        this.username = player.getName();
        this.uuid = player.getUniqueId();
        this.role = role;

        if(plugin.getHookManager().usePlaceholderAPI()) {
            this.prefix = PlaceholderAPI.setPlaceholders(player, plugin.getConfigManager().getConfig().getString("Player.prefix"));
        }
        else {
            this.prefix = plugin.getConfigManager().getConfig().getString("Player.prefix");
            System.out.println("[PREFIX] No PlaceholderAPI");
        }
    }

    public String getName() {
        return this.username;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public PartyRole getRole() {
        return this.role;
    }

    public void setRole(final PartyRole role) {
        this.role = role;
    }

    public Document toDocument() {
        return new Document()
                .append("uuid", uuid.toString())
                .append("username", username)
                .append("role", role.toString())
                .append("prefix", prefix);
    }

    public String getPrefix() {
        return this.prefix;
    }
}