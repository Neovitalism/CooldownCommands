package me.neovitalism.cooldowncommands.cooldowns;

import me.neovitalism.cooldowncommands.storage.PlayerCooldownStore;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Duration;
import java.util.Map;

public class LegacyCooldownHelper { // ToDo: remove come 2.2
    private static LuckPerms luckPermsAPI;

    public static void init() {
        LegacyCooldownHelper.luckPermsAPI = LuckPermsProvider.get();
    }

    public static MetaNode getMetaNode(ServerPlayerEntity player, String permission) {
        return LegacyCooldownHelper.luckPermsAPI.getPlayerAdapter(ServerPlayerEntity.class).getMetaData(player).queryMetaValue(permission).node();
    }

    public static void transformLegacy(ServerPlayerEntity player, PlayerCooldownStore store) {
        for (Map.Entry<String, CooldownCommand> entry : CooldownManager.entrySet()) {
            MetaNode node = LegacyCooldownHelper.getMetaNode(player, "cooldowncommands." + entry.getKey() + ".on-cooldown");
            if (node == null) continue;
            Duration expiryDuration = node.getExpiryDuration();
            if (expiryDuration == null) continue;
            long expiryTime = System.currentTimeMillis() + expiryDuration.toMillis();
            store.markCooldown(entry.getValue().getKey(), expiryTime);
        }
    }
}
