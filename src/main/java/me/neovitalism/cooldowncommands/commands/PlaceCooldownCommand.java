package me.neovitalism.cooldowncommands.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.neovitalism.cooldowncommands.CooldownCommands;
import me.neovitalism.cooldowncommands.cooldowns.CooldownCommand;
import me.neovitalism.cooldowncommands.cooldowns.CooldownManager;
import me.neovitalism.cooldowncommands.storage.CooldownStoreManager;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.SuggestionProviders;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neoapi.utils.TimeUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlaceCooldownCommand extends CommandBase {
    public PlaceCooldownCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(dispatcher, "placecooldown");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.of("cooldowncommands.placecooldown").toArray();
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
                        ctx.getArgument("command", String.class),
                        -1
                )).then(argument("seconds", LongArgumentType.longArg(1))
                        .executes(ctx -> this.execute(
                                ctx.getSource(),
                                EntityArgumentType.getPlayer(ctx, "player"),
                                ctx.getArgument("command", String.class),
                                ctx.getArgument("seconds", Long.class)
                        ))
                )
        ));
    }

    private int execute(ServerCommandSource source, ServerPlayerEntity player, String commandID, long seconds) {
        CooldownCommand command = CooldownManager.getCooldownCommand(commandID);
        if (command == null) {
            CooldownCommands.getLangManager().sendLang(source, "invalid-command", null);
            return 0;
        }
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{player}", player.getName().getString());
        replacements.put("{command}", commandID);
        if (command.bypassesCooldown(player)) {
            CooldownCommands.getLangManager().sendLang(source, "bypasses-cooldown", replacements);
            return 0;
        }
        long time;
        if (seconds == -1) {
            time = command.markOnCooldown(player);
        } else {
            time = seconds;
            CooldownStoreManager.getStore(player.getUuid()).markCooldown(
                    command.getKey(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds)
            );
        }
        replacements.put("{time-formatted}", TimeUtil.getFormattedTime(time, CooldownCommands.getLangManager()));
        CooldownCommands.getLangManager().sendLang(source, "placed-cooldown", replacements);
        return Command.SINGLE_SUCCESS;
    }
}
