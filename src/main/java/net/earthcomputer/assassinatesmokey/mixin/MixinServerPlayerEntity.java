package net.earthcomputer.assassinatesmokey.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.earthcomputer.assassinatesmokey.SpeedrunnerTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
                spawnFreezeParticle(this, getX(), getY() + getHeight() + 0.2, getZ());
            }
        } else {
            PlayerEntity assassin = SpeedrunnerTracker.forPlayer(this).getLookingAt();
            if (assassin != null) {
                Vec3d posA = getCameraPosVec(1);
                Vec3d posB = assassin.getCameraPosVec(1);
                int steps = Math.max(2, MathHelper.ceil(Math.log(distanceTo(assassin))));
                for (int step = 1; step < steps; step++) {
                    if (Math.random() < 0.5) {
                        double delta = (double) step / steps;
                        spawnFreezeParticle(null,
                                MathHelper.lerp(delta, posA.getX(), posB.getX()),
                                MathHelper.lerp(delta, posA.getY(), posB.getY()),
                                MathHelper.lerp(delta, posA.getZ(), posB.getZ()));
                    }
                }
            }
        }
    }

    @Unique
    private void spawnFreezeParticle(PlayerEntity excluding, double x, double y, double z) {
        for (PlayerEntity player : world.getPlayers()) {
            if (player != excluding) {
                ((ServerWorld) world).spawnParticles((ServerPlayerEntity) player, DustParticleEffect.RED, true, x, y, z, 1, 0, 0, 0, 1);
            }
        }
    }

}
