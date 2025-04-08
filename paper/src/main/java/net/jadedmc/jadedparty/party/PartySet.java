package net.jadedmc.jadedparty.party;

import net.jadedmc.jadedparty.party.player.PartyPlayer;
import net.jadedmc.nanoid.NanoID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.UUID;

public class PartySet<V extends Party> extends HashSet<V> {
    /**
     * Retrieves a Party containing a given Player, identified via their UUID.
     * Returns null if a corresponding Party is not found.
     * @param playerUUID UUID of the player we are looking for.
     * @return Party that contains the player.
     */
    @Nullable
    public V getFromPlayer(@NotNull final UUID playerUUID) {
        // Loop through each party looking for the player.
        for(final V party : this) {
            for(final PartyPlayer partyPlayer : party.getPlayers()) {
                if(partyPlayer.getUniqueId().equals(playerUUID)) {
                    return party;
                }
            }
        }

        // If no matching parties are found, return null.
        return null;
    }

    /**
     * Retrieves a Party containing a given Player.
     * Returns null if a corresponding Party is not found.
     * @param player Player we are looking for.
     * @return Party that contains the player.
     */
    @Nullable
    public V getFromPlayer(@NotNull final Player player) {
        return getFromPlayer(player.getUniqueId());
    }

    /**
     * Retrieves a Party containing a player with a given username.
     * Returns null if a corresponding Party is not found.
     * @param playerUsername Username of the player being looked for.
     * @return Party that contains the player.
     */
    @Nullable
    public V getFromUsername(@NotNull String playerUsername) {
        // Loop through each party looking for the player.
        for(final V party : this) {
            for(final PartyPlayer partyPlayer : party.getPlayers()) {
                if(partyPlayer.getName().equalsIgnoreCase(playerUsername)) {
                    return party;
                }
            }
        }

        // If no matching parties are found, return null.
        return null;
    }

    /**
     * Retrieves a Party object from the set given its NanoID.
     * Returns null if a corresponding Party is not found.
     * @param partyNanoID NanoID of the target party.
     * @return Party that corresponds to the given NanoID. Null if none found.
     */
    @Nullable
    public V getFromNanoID(@NotNull final NanoID partyNanoID) {
        for(final V party : this) {
            if(party.getNanoID().equals(partyNanoID)) {
                return party;
            }
        }

        return null;
    }
}