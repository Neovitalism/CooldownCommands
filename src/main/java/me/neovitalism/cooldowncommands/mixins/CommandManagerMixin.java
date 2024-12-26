package me.neovitalism.cooldowncommands.mixins;

import com.mojang.brigadier.ParseResults;
import me.neovitalism.cooldowncommands.cooldowns.CooldownCommand;
import me.neovitalism.cooldowncommands.cooldowns.CooldownManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V"), cancellable = true)
    public void cooldownCommands$execute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
        ServerPlayerEntity player = parseResults.getContext().getSource().getPlayer();
        if (player == null) return;
        CooldownCommand cooldownCommand = CooldownManager.getCooldownCommand(command);
        if (cooldownCommand == null) return;
        if (cooldownCommand.checkCooldown(player)) ci.cancel();
        else if (parseResults.getExceptions().isEmpty()) cooldownCommand.markOnCooldown(player);
    }
}
