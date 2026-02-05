package net.jadedmc.jadedparty;

import net.jadedmc.jadedparty.party.Party;
import net.jadedmc.nanoid.NanoID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JadedPartyAPI {
    private static JadedPartyBukkitPlugin plugin;

    public static void initialize(@NotNull final JadedPartyBukkitPlugin pl) {
        plugin = pl;
        System.out.println("Called");
    }

    public static Party getParty(Player player) {
        return plugin.getPartyManager().getLocalPartyFromPlayer(player);
    }

    public static Party getParty(NanoID nanoID) {
        return plugin.getPartyManager().getLocalPartyFromNanoID(nanoID);
    }

    public static Party getRemoteParty(UUID uuid) {
        return plugin.getPartyManager().getRemotePartyFromPlayer(uuid);
    }
}