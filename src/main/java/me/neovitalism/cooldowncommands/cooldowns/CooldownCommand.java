package me.neovitalism.cooldowncommands.cooldowns;

import me.neovitalism.cooldowncommands.storage.CooldownStoreManager;
import me.neovitalism.cooldowncommands.storage.PlayerCooldownStore;
import me.neovitalism.neoapi.NeoAPI;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neoapi.utils.ColorUtil;
import me.neovitalism.neoapi.utils.StringUtil;
import me.neovitalism.neoapi.utils.TimeUtil;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownCommand {
    private final String key;
    private final long defaultCooldown;
    private final String cooldownMessage;
    private final boolean appliesOnUse;

    public CooldownCommand(String key, Configuration config) {
        this.key = key.replace(" ", "_");
        this.defaultCooldown = config.getLong("cooldown-seconds");
        this.cooldownMessage = config.getString("cooldown-message");
        this.appliesOnUse = config.getBoolean("applies-on-use", true);
    }

    public String getKey() {
        return this.key;
    }

    private long getCooldownTime(ServerPlayerEntity player) {
        String cooldownTime = NeoAPI.getPermissionProvider().getMetaValue(player, this.permission("cooldown"));
        if (cooldownTime == null) return this.defaultCooldown;
        try {
            return Long.parseLong(cooldownTime);
        } catch (NumberFormatException e) {
            return this.defaultCooldown;
        }
    }

    public boolean checkCooldown(ServerPlayerEntity player) {
        if (this.bypassesCooldown(player)) return false;
        PlayerCooldownStore cooldownStore = CooldownStoreManager.getStore(player.getUuid());
        long endTime = cooldownStore.getCooldownExpiry(this.key);
        if (endTime <= 0) return false;
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(endTime - System.currentTimeMillis());
        String message = StringUtil.replaceReplacements(this.cooldownMessage, Map.of("{time-formatted}", TimeUtil.getFormattedTime(secondsLeft)));
        if (message != null) player.sendMessage(ColorUtil.parseColour(message));
        return true;
    }

    public long markOnCooldown(ServerPlayerEntity player) {
        if (this.bypassesCooldown(player)) return -1;
        PlayerCooldownStore cooldownStore = CooldownStoreManager.getStore(player.getUuid());
        long cooldownTime = this.getCooldownTime(player);
        cooldownStore.markCooldown(this.key, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(cooldownTime));
        return cooldownTime;
    }

    private String permission(String suffix) {
        return "cooldowncommands." + this.key + "." + suffix;
    }

    public boolean bypassesCooldown(ServerPlayerEntity player) {
        return NeoPermission.of(this.permission("bypass")).matches(player);
    }

    public boolean appliesOnUse() {
        return this.appliesOnUse;
    }
}
