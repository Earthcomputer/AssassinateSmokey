package net.earthcomputer.assassinatesmokey.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.assassinatesmokey.AssassinateSmokeyCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class MixinCommandManager {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegisterCommands(boolean isDedicatedServer, CallbackInfo ci) {
        AssassinateSmokeyCommand.register(this.dispatcher);
    }

}
