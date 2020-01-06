package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void onRespawnPlayer(ServerPlayerEntity oldPlayer, DimensionType dimension, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> ci) {
        if (!alive && AssassUtil.isSpeedrunner(oldPlayer)) {
            ci.getReturnValue().setGameMode(GameMode.SPECTATOR);
            AssassUtil.setPlayerType(ci.getReturnValue(), AssassUtil.PlayerType.SPECTATOR);
        }
    }

}
