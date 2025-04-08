package net.jadedmc.jadedparty.party;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.types.LocalParty;
import net.jadedmc.jadedparty.party.types.RemoteParty;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import net.jadedmc.nanoid.NanoID;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PartyManager {
    private final JadedPartyBukkitPlugin plugin;
    private final PartySet<LocalParty> localParties = new PartySet<>();

    public PartyManager(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void cacheRemoteParty(@NotNull final String partyID) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final Document document = Document.parse(JadedSyncAPI.getRedis().get("jadedparty:parties:" + partyID));

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                final LocalParty localParty = new LocalParty(plugin, document);
                this.localParties.add(localParty);
                for(final Player player : localParty.getOnlinePlayers()) {
                    JadedSyncAPI.updatePlayer(player);
                }
            });
        });
    }

    public LocalParty createParty(@NotNull final Player leader) {
        final LocalParty localParty = new LocalParty(plugin, leader);
        localParties.add(localParty);
        localParty.updateToRedis();

        return localParty;
    }

    /**
     * Deletes a Party from the local Party cache.
     * @param party Party to be deleted.
     */
    public void deleteLocalParty(@NotNull final LocalParty party) {
        this.localParties.remove(party);
    }

    /**
     * Retrieves a locally-cached party from its NanoID.
     * Returns null if non are found.
     * @param partyNanoID NanoID of target Party.
     * @return Corresponding Party object.
     */
    @Nullable
    public LocalParty getLocalPartyFromNanoID(@NotNull final NanoID partyNanoID) {
        return this.localParties.getFromNanoID(partyNanoID);
    }

    /**
     * Retrieves a locally-cached party from its UUID.
     * Returns null if non are found.
     * @param player Player to get the Party of.
     * @return Corresponding Party object.
     */
    @Nullable
    public LocalParty getLocalPartyFromPlayer(@NotNull final Player player) {
        return this.localParties.getFromPlayer(player);
    }

    public RemoteParty getRemotePartyFromNanoID(@NotNull final NanoID nanoID) {
        return new RemoteParty(plugin, JadedSyncAPI.getRedis().get("jadedparty:parties:" + nanoID));
    }

    public RemoteParty getRemotePartyFromNanoID(@NotNull final String nanoID) {
        return new RemoteParty(plugin, JadedSyncAPI.getRedis().get("jadedparty:parties:" + nanoID));
    }
}