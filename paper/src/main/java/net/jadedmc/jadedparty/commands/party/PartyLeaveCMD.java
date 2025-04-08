/*
 * This file is part of JadedParty, licensed under the MIT License.
 *
 *  Copyright (c) JadedMC
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package net.jadedmc.jadedparty.commands.party;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.player.PartyPlayer;
import net.jadedmc.jadedparty.party.player.PartyRole;
import net.jadedmc.jadedparty.party.types.LocalParty;
import net.jadedmc.jadedparty.settings.ConfigMessage;
import net.jadedmc.jadedparty.utils.chat.ChatUtils;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyLeaveCMD {
    private final JadedPartyBukkitPlugin plugin;

    /**
     * Creates the sub command.
     * @param plugin Instance of the plugin.
     */
    public PartyLeaveCMD(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command.
     * @param player Player running the command.
     * @param args Command arguments.
     */
    public void execute(@NotNull final Player player, final String[] args) {
        // Makes sure the player is in a party.
        final LocalParty party = plugin.getPartyManager().getLocalPartyFromPlayer(player);
        if(party == null) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_ERROR_NOT_IN_PARTY));
            return;
        }

        // If the party leader leaves, disband the party instead.
        final PartyPlayer partyPlayer = party.getPlayer(player.getUniqueId());
        if(partyPlayer.getRole() == PartyRole.LEADER) {
            party.sendMessage(plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DISBAND_PARTY_DISBANDED));
            party.disband();
            return;
        }

        // Sends the leave message with placeholders processed.
        party.sendMessage(plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_LEAVE_PLAYER_LEFT));

        party.removePlayer(player.getUniqueId());
        JadedSyncAPI.updatePlayer(player);
        party.updateToRedis();
    }
}
