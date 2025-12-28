package me.neovitalism.cooldowncommands.storage;

import me.neovitalism.neoapi.config.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCooldownStore {
    private final UUID playerUUID;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public PlayerCooldownStore(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public PlayerCooldownStore(UUID playerUUID, Configuration data) {
        this.playerUUID = playerUUID;
        for (String key : data.getKeys()) this.cooldowns.put(key, data.getLong(key));
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public long getCooldownExpiry(String command) {
        boolean success = this.cooldowns.entrySet().removeIf(cd -> cd.getValue() < System.currentTimeMillis());
        if (success) this.markDirty();
        return this.cooldowns.getOrDefault(command, -1L);
    }

    public void markCooldown(String command, long expiryTime) {
        this.cooldowns.put(command, expiryTime);
        this.markDirty();
    }

    public void removeCooldown(String command) {
        this.cooldowns.remove(command);
        this.markDirty();
    }

    public Configuration serialize() {
        Configuration config = new Configuration();
        for (Map.Entry<String, Long> entry : this.cooldowns.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        return config;
    }

    private void markDirty() {
        CooldownStoreManager.markToSave(this);
    }
}
