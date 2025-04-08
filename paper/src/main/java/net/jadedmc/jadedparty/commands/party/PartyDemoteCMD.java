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
import net.jadedmc.jadedparty.utils.Tuple;
import net.jadedmc.jadedparty.utils.chat.ChatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Powers the /party demote command, which demotes a player to a lower party role.
 * <p>
 * Usage: /party demote [player]
 *   - [player]: The player to be demoted
 */
public class PartyDemoteCMD {
    private final JadedPartyBukkitPlugin plugin;

    /**
     * Creates the sub command.
     * @param plugin Instance of the plugin.
     */
    public PartyDemoteCMD(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command.
     * @param player Player running the command.
     * @param args Command arguments.
     */
    public void execute(@NotNull final Player player, @NotNull final String[] args) {
        final LocalParty party = plugin.getPartyManager().getLocalPartyFromPlayer(player);

        // Makes sure the player is in a party.
        if(party == null) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_ERROR_NOT_IN_PARTY));
            return;
        }

        // Makes sure the player is using the command correctly.
        if(args.length == 1) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_USAGE));
            return;
        }

        // Makes sure they have permission.
        if(party.getPlayer(player.getUniqueId()).getRole() != PartyRole.LEADER) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_NOT_ALLOWED));
            return;
        }

        if(args[1].equalsIgnoreCase(player.getName())) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_CANNOT_DEMOTE_SELF));
            return;
        }

        final PartyPlayer localTargetPlayer = party.getPlayer(args[0]);
        if(localTargetPlayer == null) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_TARGET_NOT_IN_PARTY));
            return;
        }

        if(localTargetPlayer.getRole() == PartyRole.MEMBER) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_CANNOT_DEMOTE_MEMBER));
            return;
        }

        // Demotes the player
        final PartyPlayer partyTargetPlayer = party.getPlayer(localTargetPlayer.getUniqueId());
        partyTargetPlayer.setRole(PartyRole.MEMBER);
        party.updateToRedis();

        final Tuple<String, String> placeholder = new Tuple<>("%target_name%", partyTargetPlayer.getName());
        party.sendMessage(plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_DEMOTE_TARGET_DEMOTED_MEMBER, placeholder));
    }
}