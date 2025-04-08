package net.jadedmc.jadedparty.party;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.player.PartyPlayer;
import net.jadedmc.jadedparty.party.player.PartyRole;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import net.jadedmc.nanoid.NanoID;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Party {
    private final JadedPartyBukkitPlugin plugin;
    private final NanoID nanoID;
    private final Collection<PartyPlayer> players = new HashSet<>();
    private final Collection<UUID> invites = new HashSet<>();

    public Party(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Document document) {
        this.plugin = plugin;
        this.nanoID = NanoID.fromString(document.getString("nanoID"));

        // Load the players from the document.
        final Document playersDocument = document.get("players", Document.class);
        for(final String playerUUID : playersDocument.keySet()) {
            players.add(new PartyPlayer(playersDocument.get(playerUUID, Document.class)));
        }

        // Load the pending invites of the party.
        final List<String> inviteUUIDs = document.getList("invites", String.class);
        for(final String uuid : inviteUUIDs) {
            this.invites.add(UUID.fromString(uuid));
        }
    }

    public Party(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Player leader) {
        this.plugin = plugin;

        // Generates the party's NanoID with configured settings in config.yml.
        final NanoID.Settings nanoIDSettings = new NanoID.Settings()
                .setAlphabet(plugin.getConfigManager().getConfig().getString("Party.ID.alphabet"))
                .setMinimumSize(plugin.getConfigManager().getConfig().getInt("Party.ID.minLength"))
                .setMaximumSize(plugin.getConfigManager().getConfig().getInt("Party.ID.maxLength"))
                .setPrefix(plugin.getConfigManager().getConfig().getString("Party.ID.prefix"));
        this.nanoID = new NanoID(nanoIDSettings);

        addPlayer(leader, PartyRole.LEADER);
    }

    public void addPlayer(@NotNull final Player player, final PartyRole role) {
        this.players.add(new PartyPlayer(plugin, player, role));
    }

    public Collection<UUID> getInvites() {
        return this.invites;
    }

    public PartyPlayer getPlayer(@NotNull final UUID uuid) {
        for(final PartyPlayer partyPlayer : this.players) {
            if(partyPlayer.getUniqueId().equals(uuid)) {
                return partyPlayer;
            }
        }

        return null;
    }

    public Collection<PartyPlayer> getPlayers() {
        return this.players;
    }

    public NanoID getNanoID() {
        return this.nanoID;
    }

    /**
     * Converts the cached party into a Bson Document.
     * @return Bson document of the party.
     */
    public Document toDocument() {
        final Document document = new Document();
        document.append("nanoID", this.nanoID.toString());

        final Document playersDocument = new Document();
        for(PartyPlayer player : this.players) {
            playersDocument.append(player.getUniqueId().toString(), player.toDocument());
        }
        document.append("players", playersDocument);

        final List<String> invites = new ArrayList<>();
        this.invites.forEach(invite -> invites.add(invite.toString()));
        document.append("invites", invites);

        return document;
    }

    public void removePlayer(@NotNull final UUID playerUUID) {
        players.remove(getPlayer(playerUUID));
    }

    public void updateToRedis() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JadedSyncAPI.getRedis().set("jadedparty:parties:" + this.nanoID.toString(), toDocument().toJson());
            JadedSyncAPI.getIntegration("jadedparty").publish("party update " + this.nanoID);
        });
    }

    /**
     * Sends a message to all members of the party.
     * @param message Message to be sent.
     */
    public void sendMessage(@NotNull final String message) {
        final StringBuilder builder = new StringBuilder();
        players.forEach(partyPlayer -> {
            builder.append(partyPlayer.getUniqueId());
            builder.append(",");
        });

        final String targets = builder.substring(0, builder.length() - 1);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JadedSyncAPI.getIntegration("jadedparty").publish("jadedparty message " + targets + " " + message);
        });
    }

    public boolean hasPlayer(@NotNull final String username) {
        for(final PartyPlayer partyPlayer : this.players) {
            if(partyPlayer.getName().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    public PartyPlayer getPlayer(@NotNull final String username) {
        for(final PartyPlayer partyPlayer : this.players) {
            if(partyPlayer.getName().equalsIgnoreCase(username)) {
                return partyPlayer;
            }
        }

        return null;
    }
    /**
     * Disbands the party.
     */
    public void disband() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getPartyManager().deleteLocalParty(plugin.getPartyManager().getLocalPartyFromNanoID(this.nanoID));

            for(final PartyPlayer partyPlayer : this.players) {
                final Player player = plugin.getServer().getPlayer(partyPlayer.getUniqueId());
                if(player != null) JadedSyncAPI.updatePlayer(player);
            }

            JadedSyncAPI.getIntegration("jadedparty").publish("party disband " + this.nanoID.toString());
            JadedSyncAPI.getRedis().del("jadedparty:parties:" + this.nanoID);
        });
    }

    public void addInvite(@NotNull final UUID uuid) {
        this.invites.add(uuid);
    }
}