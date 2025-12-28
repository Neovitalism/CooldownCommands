package me.neovitalism.cooldowncommands.storage;

import me.neovitalism.cooldowncommands.CooldownCommands;
import me.neovitalism.neoapi.async.NeoAPIExecutorManager;
import me.neovitalism.neoapi.async.NeoExecutor;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.storage.AbstractStorage;
import me.neovitalism.neoapi.storage.StorageType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownStoreManager extends AbstractStorage {
    private static CooldownStoreManager instance;
    private static final Map<UUID, PlayerCooldownStore> LOCAL_CACHE = new HashMap<>();
    private static final NeoExecutor ASYNC_EXEC = NeoAPIExecutorManager.createScheduler("CooldownCommands-Save-Thread", 1);

    public CooldownStoreManager() {
        super(CooldownCommands.inst(), CooldownCommands.inst().getConfig("config.yml", true).getSection("storage"));
        CooldownStoreManager.instance = this;
    }

    private static final String SAVE_COOLDOWNS = """
                INSERT INTO command_cooldowns (player, cooldowns) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE cooldowns = VALUES(cooldowns)
            """;
    private static final String GET_COOLDOWNS = "SELECT cooldowns FROM command_cooldowns WHERE player = ?";

    @Override
    public Map<String, String> getTables() {
        return Map.of("command_cooldowns", "player CHAR(36) PRIMARY KEY, cooldowns JSON NOT NULL");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void load() {
        File folder = CooldownCommands.inst().getFile("cooldowns");
        if (!folder.exists()) return;
        File[] children = folder.listFiles();
        if (children == null) return;
        for (File dataFile : children) {
            UUID uuid = UUID.fromString(dataFile.getName().replace(".yml", ""));
            Configuration config = CooldownCommands.inst().getConfig(dataFile);
            PlayerCooldownStore data = new PlayerCooldownStore(uuid, config);
            CooldownStoreManager.LOCAL_CACHE.put(uuid, data);
            if (this.storageType == StorageType.MARIADB) dataFile.delete();
        }
        if (this.storageType == StorageType.MARIADB) {
            folder.delete();
            CooldownStoreManager.instance.databaseConnection.queryBatch(SAVE_COOLDOWNS, statement -> {
                for (PlayerCooldownStore store : CooldownStoreManager.LOCAL_CACHE.values()) {
                    statement.setString(1, store.getPlayerUUID().toString());
                    statement.setString(2, store.serialize().toJson());
                    statement.addBatch();
                }
            });
        }
    }

    public static PlayerCooldownStore getStore(UUID uuid) {
        return CooldownStoreManager.LOCAL_CACHE.computeIfAbsent(uuid, playerUUID -> {
            if (CooldownStoreManager.instance.storageType == StorageType.MARIADB) {
                return CooldownStoreManager.instance.databaseConnection.query(GET_COOLDOWNS, statement -> {
                    statement.setString(1, playerUUID.toString());
                }, result -> {
                    if (!result.next()) return new PlayerCooldownStore(playerUUID);
                    Configuration data = Configuration.fromJson(result.getString("cooldowns"));
                    return new PlayerCooldownStore(playerUUID, data);
                });
            } else return new PlayerCooldownStore(playerUUID);
        });
    }

    public void logout(UUID playerUUID) {
        PlayerCooldownStore store = CooldownStoreManager.getStore(playerUUID);
        CooldownStoreManager.markToSave(store);
        if (CooldownStoreManager.instance.storageType == StorageType.MARIADB) {
            CooldownStoreManager.LOCAL_CACHE.remove(store.getPlayerUUID());
        }
    }

    public static void markToSave(PlayerCooldownStore store) {
        UUID playerUUID = store.getPlayerUUID();
        Configuration serialized = store.serialize();
        CooldownStoreManager.ASYNC_EXEC.runTaskAsync(() -> {
            if (CooldownStoreManager.instance.storageType == StorageType.YAML) {
                CooldownCommands.inst().saveConfig("cooldowns/" + playerUUID.toString() + ".yml", serialized);
            } else {
                CooldownStoreManager.instance.databaseConnection.query(SAVE_COOLDOWNS, statement -> {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, serialized.toJson());
                });
            }
        });
    }
}
