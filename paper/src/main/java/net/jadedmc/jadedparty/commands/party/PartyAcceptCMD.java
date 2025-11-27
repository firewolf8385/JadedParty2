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
import net.jadedmc.jadedparty.party.types.RemoteParty;
import net.jadedmc.jadedparty.settings.ConfigMessage;
import net.jadedmc.jadedparty.utils.chat.ChatUtils;
import net.jadedmc.jadedsync.api.JadedSyncAPI;
import net.jadedmc.jadedsync.api.player.JadedSyncPlayer;
import net.jadedmc.jadedsync.api.player.JadedSyncPlayerMap;
import net.jadedmc.jadedsync.libraries.bson.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Powers the /party accept command, which allows a player to accept a party invite.
 * <p>
 * Usage: /party accept [player]
 *   - [player]: The player who sent the invite.
 */
public class PartyAcceptCMD {
    private final JadedPartyBukkitPlugin plugin;

    /**
     * Creates the sub command.
     * @param plugin Instance of the plugin.
     */
    public PartyAcceptCMD(@NotNull final JadedPartyBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the command.
     * @param player Player running the command.
     * @param args Command arguments.
     */
    public void execute(@NotNull final Player player, final String[] args) {

        // Makes sure the player is using the command correctly.
        if(args.length == 1) {
            ChatUtils.chat(player, plugin.getConfigManager().getMessage(player, ConfigMessage.PARTY_ACCEPT_USAGE));
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            final JadedSyncPlayerMap jadedSyncPlayers = JadedSyncAPI.getPlayers();
            String username = args[1];

            if(!jadedSyncPlayers.contains(username)) {
                ChatUtils.chat(player, "<red><bold>Error</bold> <dark_gray>» <red>That player is not online");
                return;
            }

            final JadedSyncPlayer targetSyncPlayer = jadedSyncPlayers.get(username);
            final Document document = Document.parse(targetSyncPlayer.getIntegration("jadedparty"));

            if(document.isEmpty()) {
                ChatUtils.chat(player, "<red><bold>Error</bold> <dark_gray>» <red>That player is not in a party");
                return;
            }

            final String targetPartyID = document.getString("party");

            if(targetPartyID.isEmpty()) {
                ChatUtils.chat(player, "<red><bold>Error</bold> <dark_gray>» <red>That player is not in a party");
                return;
            }

            final RemoteParty remoteParty = plugin.getPartyManager().getRemotePartyFromNanoID(targetPartyID);


            if(!remoteParty.getInvites().contains(player.getUniqueId())) {
                ChatUtils.chat(player, "<red><bold>Error</bold> <dark_gray>» <red>You do not have an invite to that party.");
                System.out.println("Invites Found: " + remoteParty.getInvites().size());

                for(UUID inviteUUID : remoteParty.getInvites()) {
                    System.out.println(inviteUUID.toString());
                }
                return;
            }

            // Display the other players in the party.
            {
                ChatUtils.chat(player, "<green>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</green>");
                ChatUtils.chat(player, ChatUtils.centerText("<green><bold>You are partying with"));
                ChatUtils.chat(player, "");

                final StringBuilder members = new StringBuilder();
                for(final PartyPlayer partyPlayer : remoteParty.getPlayers()) {
                    if(partyPlayer.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }

                    members.append("<gray>");
                    members.append(partyPlayer.getName());
                    members.append("<green>,");
                }

                ChatUtils.chat(player, members.substring(0, members.length() - 1));
                ChatUtils.chat(player, "");
                ChatUtils.chat(player, "<green>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</green>");
            }

            remoteParty.addPlayer(player, PartyRole.MEMBER);
            remoteParty.updateToRedis();
            remoteParty.sendMessage("<green><bold>Party</bold> <dark_gray>» " + "<gray>" + player.getName() + " <green>has joined the party.");

            if(plugin.getPartyManager().getLocalPartyFromNanoID(remoteParty.getNanoID()) == null) {
                plugin.getPartyManager().cacheRemoteParty(remoteParty.getNanoID().toString());
            }

            JadedSyncAPI.updatePlayerIntegrations(player);
        });
    }
}