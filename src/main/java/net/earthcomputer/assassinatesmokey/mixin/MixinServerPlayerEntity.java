package net.earthcomputer.assassinatesmokey.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.earthcomputer.assassinatesmokey.SpeedrunnerTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
            AssassinTracker tracker = AssassinTracker.forPlayer(this);
            if (tracker.isFrozen()) {
                spawnFreezeParticle(this, getX(), getY() + getHeight() + 0.2, getZ());
            }

            if (!dimension.hasSkyLight()) {
                if (getMainHandStack().getItem() == Items.COMPASS || getOffHandStack().getItem() == Items.COMPASS) {
                    if (tracker.getTrackingPlayer().isPresent()) {
                        Vec3d compassVec = new Vec3d(tracker.getTrackingPos().getX() - getX(), 0, tracker.getTrackingPos().getZ() - getZ())
                                .normalize();
                        for (int i = 1; i <= 5; i++) {
                            if (Math.random() < 0.1)
                                ((ServerWorld) world).spawnParticles((ServerPlayerEntity) (Object) this,
                                        new DustParticleEffect(1, 1, 0, 1),
                                        true,
                                        getX() + compassVec.x * i,
                                        getY() + 1,
                                        getZ() + compassVec.z * i,
                                        1, 0, 0, 0, 1);
                        }
                    }
                }
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

    @Inject(method = "copyFrom", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;enchantmentTableSeed:I"))
    private void onCopyPlayer(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (alive)
            return;

        if (!AssassUtil.isAssassin(this))
            return;

        // check if we already have a compass
        for (int i = 0; i < inventory.getInvSize(); i++) {
            if (inventory.getInvStack(i).getItem() == Items.COMPASS)
                return;
        }

        inventory.insertStack(new ItemStack(Items.COMPASS));
    }

}
