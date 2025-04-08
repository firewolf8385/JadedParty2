package net.jadedmc.jadedparty.listeners;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.types.LocalParty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {
    private final JadedPartyBukkitPlugin plugin;

    public PlayerQuitListener(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(@NotNull final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final LocalParty party = plugin.getPartyManager().getLocalPartyFromPlayer(player);

        // Exit if there is no party to delete.
        if(party == null) {
            return;
        }

        // Make sure there are not other players online before deleting the party.
        if(party.getOnlinePlayers().size() > 1) {
            return;
        }

        // Deletes the party.
        plugin.getPartyManager().deleteLocalParty(party);
    }
}
