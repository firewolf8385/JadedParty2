package net.jadedmc.jadedparty.party.types;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.Party;
import net.jadedmc.jadedparty.party.player.PartyPlayer;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import net.jadedmc.jadedsync.libraries.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class LocalParty extends Party {

    public LocalParty(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Document document) {
        super(plugin, document);
    }

    public LocalParty(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Player leader) {
        super(plugin, leader);
    }

    public Collection<Player> getOnlinePlayers() {
        final Collection<Player> onlinePlayers = new HashSet<>();

        for(final PartyPlayer partyPlayer : this.getPlayers()) {
            final Player player = Bukkit.getPlayer(partyPlayer.getUniqueId());

            if(player == null) {
                continue;
            }

            onlinePlayers.add(player);
        }

        return onlinePlayers;
    }

    public void updateFromDocument(@NotNull final Document document) {
        // Empty cached players.
        this.getPlayers().clear();

        // Loads the party players.
        final Document playersDocument = document.get("players", Document.class);
        for(@NotNull final String playerUUID : playersDocument.keySet()) {
            final PartyPlayer partyPlayer = new PartyPlayer(playersDocument.get(playerUUID, Document.class));
            this.getPlayers().add(partyPlayer);

            final Player player = Bukkit.getPlayer(partyPlayer.getUniqueId());
            if(player != null) {
                JadedSyncAPI.updatePlayerIntegrations(player);
            }
        }

        // Loads new invites.
        final List<String> inviteUUIDs = document.getList("invites", String.class);
        for(@NotNull final String uuid : inviteUUIDs) {
            this.getInvites().add(UUID.fromString(uuid));
        }
    }
}