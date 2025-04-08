package net.jadedmc.jadedparty.settings.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.player.PartyPlayer;
import net.jadedmc.jadedparty.party.types.LocalParty;
import net.jadedmc.jadedparty.utils.StringUtils;
import net.jadedmc.jadedparty.utils.chat.ChatUtils;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import net.jadedmc.jadedsync.api.integration.Integration;
import net.jadedmc.nanoid.NanoID;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class JadedSyncIntegration extends Integration {
    private final JadedPartyBukkitPlugin plugin;

    public JadedSyncIntegration(@NotNull final JadedPartyBukkitPlugin plugin) {
        super("jadedparty");
        this.plugin = plugin;
    }

    @Override
    public Document getPlayerDocument(@NotNull final Player player) {
        final Document document = new Document();
        final LocalParty localParty = plugin.getPartyManager().getLocalPartyFromPlayer(player);

        if(localParty != null) {
            document.append("party", localParty.getNanoID().toString());
            document.append("role", localParty.getPlayer(player.getUniqueId()).getRole().toString());
            document.append("prefix", localParty.getPlayer(player.getUniqueId()).getPrefix());
        }
        else {
            document.append("party", "");
            document.append("role", "NONE");
            document.append("prefix", PlaceholderAPI.setPlaceholders(player, plugin.getConfigManager().getConfig().getString("Player.prefix")));
        }

        return document;
    }

    @Override
    public boolean onPlayerJoin(@NotNull final Player player, @NotNull final Document document) {
        final String partyID = document.getString("party");

        if(partyID.isEmpty()) {
           return false;
        }

        plugin.getPartyManager().cacheRemoteParty(partyID);
        return false;
    }

    @Override
    public void onRedisMessage(@NotNull final String message) {
        final String[] args = message.split(" ", 2);
        final String channel = args[0];
        final String command = args[1];

        switch (channel) {
            case "party" -> partyChannel(command);
            case "jadedparty" -> jadedPartyChannel(command);
        }
    }

    private void partyChannel(@NotNull final String message) {
        final String[] args = message.split(" ");
        final String command = args[0];

        switch (command) {
            case "disband" -> {
                final NanoID partyNanoID = NanoID.fromString(args[1]);
                final LocalParty party = plugin.getPartyManager().getLocalPartyFromNanoID(partyNanoID);

                // Make sure the party exists before deleting.
                if(party == null) {
                    return;
                }

                // Delete the local party.
                plugin.getPartyManager().deleteLocalParty(party);

                for(final PartyPlayer partyPlayer : party.getPlayers()) {
                    final Player player = plugin.getServer().getPlayer(partyPlayer.getUniqueId());
                    if(player != null) JadedSyncAPI.updatePlayer(player);
                }
            }

            case "join" -> {
                final NanoID partyNanoID = NanoID.fromString(args[1]);
                final UUID playerUUID = UUID.fromString(args[2]);
                final LocalParty party = plugin.getPartyManager().getLocalPartyFromNanoID(partyNanoID);
                final Player player = plugin.getServer().getPlayer(playerUUID);

                if(party != null) {
                    return;
                }

                if(player != null) {
                    return;
                }

                plugin.getPartyManager().cacheRemoteParty(partyNanoID.toString());
            }

            case "leave" -> {
                final NanoID partyNanoID = NanoID.fromString(args[1]);
                final UUID playerUUID = UUID.fromString(args[2]);
                final LocalParty party = plugin.getPartyManager().getLocalPartyFromNanoID(partyNanoID);

                if(party == null) {
                    return;
                }

                if(party.getOnlinePlayers().size() > 1) {
                    return;
                }

                plugin.getPartyManager().deleteLocalParty(party);
            }

            case "update" -> {
                final NanoID partyNanoID = NanoID.fromString(args[1]);
                final LocalParty party = plugin.getPartyManager().getLocalPartyFromNanoID(partyNanoID);

                if(party == null) {
                    return;
                }

                final Document document = Document.parse(JadedSyncAPI.getRedis().get("jadedparty:parties:" + partyNanoID));
                party.updateFromDocument(document);
            }
        }
    }

    private void jadedPartyChannel(@NotNull final String message) {
        final String[] args = message.split(" ");
        final String command = args[0];

        switch(command) {
            // Sends a message to a specific player or group of players no matter what server they are on.
            case "message" -> {
                final String[] playerUUIDs = args[1].split(",");
                final String toSend = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");

                // Loop through all specified players in the message.
                for(final String playerUUID : playerUUIDs) {
                    final UUID uuid = UUID.fromString(playerUUID);

                    // Skip the player if they are not online.
                    if(plugin.getServer().getPlayer(uuid) == null) {
                        continue;
                    }

                    // Sends the player the message.
                    final Player player = plugin.getServer().getPlayer(uuid);
                    ChatUtils.chat(player, toSend);
                }
            }
        }
    }
}