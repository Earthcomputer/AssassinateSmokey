package net.earthcomputer.assassinatesmokey.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {

    public MixinServerPlayerEntity(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (AssassUtil.isAssassin(this)) {
            if (AssassinTracker.forPlayer(this).isFrozen()) {
                spawnAssassinParticle();
            }
        }
    }

    @Unique
    private void spawnAssassinParticle() {
        for (PlayerEntity player : world.getPlayers()) {
            if (player != this) {
                double y = getY() + getHeight() + 0.2;
                ((ServerWorld) world).spawnParticles((ServerPlayerEntity) player, DustParticleEffect.RED, true, getX(), y, getZ(), 1, 0, 0, 0, 1);
            }
        }
    }

}
