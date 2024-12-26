package me.neovitalism.cooldowncommands.cooldowns;

import me.neovitalism.cooldowncommands.CooldownCommands;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neoapi.utils.ColorUtil;
import me.neovitalism.neoapi.utils.StringUtil;
import me.neovitalism.neoapi.utils.TimeUtil;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownCommand {
    private final String key;
    private final long defaultCooldown;
    private final String cooldownMessage;

    public CooldownCommand(String key, Configuration config) {
        this.key = key;
        this.defaultCooldown = config.getLong("cooldown-seconds");
        this.cooldownMessage = config.getString("cooldown-message");
    }

    private long getCooldown(ServerPlayerEntity player) {
        MetaNode meta = CooldownCommands.getMetaNode(player, this.permission("cooldown"));
        if (meta == null) return this.defaultCooldown;
        try {
            return Long.parseLong(meta.getMetaValue());
        } catch (NumberFormatException e) {
            return this.defaultCooldown;
        }
    }

    public boolean checkCooldown(ServerPlayerEntity player) {
        if (NeoPermission.of(this.permission("bypass")).matches(player)) return false;
        MetaNode meta = CooldownCommands.getMetaNode(player, this.permission("on-cooldown"));
        if (meta == null) return false;
        Duration expiryDuration = meta.getExpiryDuration();
        long seconds = (expiryDuration != null) ? expiryDuration.getSeconds() : -1;
        String message = StringUtil.replaceReplacements(this.cooldownMessage, Map.of("{time-formatted}", TimeUtil.getFormattedTime(seconds)));
        if (message != null) player.sendMessage(ColorUtil.parseColour(message));
        return true;
    }

    public void markOnCooldown(ServerPlayerEntity player) {
        if (NeoPermission.of(this.permission("bypass")).matches(player)) return;
        User user = CooldownCommands.getLuckPermsUser(player);
        user.data().add(MetaNode.builder(this.permission("on-cooldown"), "true").expiry(this.getCooldown(player), TimeUnit.SECONDS).build());
        CooldownCommands.saveUser(user);
    }

    private String permission(String suffix) {
        return "cooldowncommands." + this.key + "." + suffix;
    }
}
