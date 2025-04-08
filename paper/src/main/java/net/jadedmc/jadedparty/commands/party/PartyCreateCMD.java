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
import net.jadedmc.jadedparty.party.types.LocalParty;
import net.jadedmc.jadedparty.settings.ConfigMessage;
import net.jadedmc.jadedparty.utils.chat.ChatUtils;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Powers the /party create command, which lets a player create a new party.
 * <p>
 * Usage: /party create
 */
public class PartyCreateCMD {
    private final JadedPartyBukkitPlugin plugin;

    /**
     * Creates the sub command.
     * @param plugin Instance of the plugin.
     */
    public PartyCreateCMD(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command.
     * @param player Player running the command.
     * @param args Command arguments.
     */
    public void execute(@NotNull final Player player, final String[] args) {
        // Makes sure the player is not already in a party.
        if(plugin.getPartyManager().getLocalPartyFromPlayer(player) != null) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_ERROR_ALREADY_IN_PARTY));
            return;
        }

        // Creates the party and updates it through the messaging service.
        final LocalParty party = plugin.getPartyManager().createParty(player);
        JadedSyncAPI.updatePlayer(player);
        //party.updateToRedis();

        // Tell the player the party was created.
        ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_CREATE_PARTY_CREATED));
    }

}