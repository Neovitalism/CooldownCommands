package me.neovitalism.cooldowncommands;

import me.neovitalism.cooldowncommands.commands.PlaceCooldownCommand;
import me.neovitalism.cooldowncommands.commands.RemoveCooldownCommand;
import me.neovitalism.cooldowncommands.cooldowns.CooldownManager;
import me.neovitalism.cooldowncommands.cooldowns.LegacyCooldownHelper;
import me.neovitalism.cooldowncommands.storage.CooldownStoreManager;
import me.neovitalism.cooldowncommands.storage.PlayerCooldownStore;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.lang.LangManager;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandRegistryInfo;
import me.neovitalism.neoapi.modloading.command.ReloadCommand;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class CooldownCommands extends NeoMod {
    private static CooldownCommands instance;
    private static CooldownStoreManager storeManager;
    private static LangManager langManager;

    @Override
    public String getModID() {
        return "CooldownCommands";
    }

    @Override
    public String getModPrefix() {
        return "&#696969[&#7E50C7C&#8054C2o&#8159BDo&#835DB8l&#8562B3d&#8666AEo&#886BA9w&#8A6FA4n&#8B749FC&#8D789Ao&#8F7D95m&#908190m&#92868Ba&#948A86n&#958F81d&#97937Cs&#696969]&f ";
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        CooldownCommands.instance = this;
        CooldownCommands.storeManager = new CooldownStoreManager();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerCooldownStore cached = CooldownStoreManager.getStore(handler.getPlayer().getUuid());
            LegacyCooldownHelper.transformLegacy(handler.getPlayer(), cached);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            CooldownCommands.storeManager.logout(handler.getPlayer().getUuid());
        });
    }

    @Override
    public void onServerStart() {
        super.onServerStart();
        CooldownCommands.storeManager.load();
        LegacyCooldownHelper.init();
    }

    @Override
    public void configManager() {
        Configuration config = this.getConfig("config.yml", true);
        CooldownManager.reload(config);
        CooldownCommands.langManager = config.getLangManager("lang", false);
    }

    @Override
    public void registerCommands(CommandRegistryInfo info) {
        new ReloadCommand(this, info.getDispatcher(), "cooldowncommands");
        new PlaceCooldownCommand(info.getDispatcher());
        new RemoveCooldownCommand(info.getDispatcher());
    }

    public static CooldownCommands inst() {
        return CooldownCommands.instance;
    }

    public static LangManager getLangManager() {
        return CooldownCommands.langManager;
    }
}
