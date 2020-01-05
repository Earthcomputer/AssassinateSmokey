package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.earthcomputer.assassinatesmokey.SpeedrunnerTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, CallbackInfo ci) {
        for (PlayerEntity player : players)
            if (AssassUtil.isAssassin(player))
                AssassinTracker.forPlayer(player).setFrozen(false);
        for (PlayerEntity player : players) {
            if (AssassUtil.isSpeedrunner(player)) {
                PlayerEntity lookingAt = SpeedrunnerTracker.forPlayer(player).recalculateLookingAt();
                if (lookingAt != null) {
                    AssassinTracker.forPlayer(lookingAt).setFrozen(true);
                }
            }
        }
    }

}
