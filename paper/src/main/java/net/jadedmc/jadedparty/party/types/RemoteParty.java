package net.jadedmc.jadedparty.party.types;

import net.jadedmc.jadedparty.JadedPartyBukkitPlugin;
import net.jadedmc.jadedparty.party.Party;
import net.jadedmc.jadedsync.libraries.bson.Document;
import org.jetbrains.annotations.NotNull;

public class RemoteParty extends Party {
    public RemoteParty(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final Document document) {
        super(plugin, document);
    }

    public RemoteParty(@NotNull final JadedPartyBukkitPlugin plugin, @NotNull final String json) {
        super(plugin, Document.parse(json));
    }
}