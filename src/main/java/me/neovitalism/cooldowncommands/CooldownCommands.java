package me.neovitalism.cooldowncommands;

import me.neovitalism.cooldowncommands.cooldowns.CooldownManager;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandRegistryInfo;
import me.neovitalism.neoapi.modloading.command.ReloadCommand;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.server.network.ServerPlayerEntity;

public class CooldownCommands extends NeoMod {
    private static LuckPerms luckPermsAPI;

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
        ServerLifecycleEvents.SERVER_STARTED.register(server -> CooldownCommands.luckPermsAPI = LuckPermsProvider.get());
    }

    @Override
    public void configManager() {
        CooldownManager.reload(this.getConfig("config.yml", true));
    }

    @Override
    public void registerCommands(CommandRegistryInfo info) {
        new ReloadCommand(this, info.getDispatcher(), "cooldowncommands");
    }

    public static User getLuckPermsUser(ServerPlayerEntity player) {
        return CooldownCommands.luckPermsAPI.getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
    }

    public static void saveUser(User user) {
        CooldownCommands.luckPermsAPI.getUserManager().saveUser(user);
    }

    public static MetaNode getMetaNode(ServerPlayerEntity player, String permission) {
        return CooldownCommands.luckPermsAPI.getPlayerAdapter(ServerPlayerEntity.class).getMetaData(player).queryMetaValue(permission).node();
    }
}
