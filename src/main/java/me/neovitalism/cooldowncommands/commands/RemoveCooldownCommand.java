package me.neovitalism.cooldowncommands.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.neovitalism.cooldowncommands.CooldownCommands;
import me.neovitalism.cooldowncommands.cooldowns.CooldownCommand;
import me.neovitalism.cooldowncommands.cooldowns.CooldownManager;
import me.neovitalism.cooldowncommands.storage.CooldownStoreManager;
import me.neovitalism.cooldowncommands.storage.PlayerCooldownStore;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.SuggestionProviders;
import me.neovitalism.neoapi.permissions.NeoPermission;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class RemoveCooldownCommand extends CommandBase {
    public RemoveCooldownCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(dispatcher, "removecooldown");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.of("cooldowncommands.removecooldown").toArray();
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument(
                "player", EntityArgumentType.player()
        ).then(argument("command", StringArgumentType.string())
                .suggests((ctx, builder) ->
                        new SuggestionProviders.List("command", CooldownManager.keys()).getSuggestions(ctx, builder)
                ).executes(ctx -> this.execute(
                        ctx.getSource(),
                        EntityArgumentType.getPlayer(ctx, "player"),
                        ctx.getArgument("command", String.class)
                ))
        ));
    }

    private int execute(ServerCommandSource source, ServerPlayerEntity player, String commandID) {
        CooldownCommand command = CooldownManager.getCooldownCommand(commandID);
        if (command == null) {
            CooldownCommands.getLangManager().sendLang(source, "invalid-command", null);
            return 0;
        }
        PlayerCooldownStore store = CooldownStoreManager.getStore(player.getUuid());
        store.removeCooldown(command.getKey());
        CooldownCommands.getLangManager().sendLang(source, "removed-cooldown",
                Map.of("{player}", player.getName().getString(), "{command}", commandID)
        );
        return Command.SINGLE_SUCCESS;
    }
}
