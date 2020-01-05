package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.minecraft.client.network.packet.VehicleMoveS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.server.network.packet.VehicleMoveC2SPacket;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow public ServerPlayerEntity player;
    @Shadow private double lastTickRiddenX;
    @Shadow private double lastTickRiddenY;
    @Shadow private double lastTickRiddenZ;
    @Shadow @Final public ClientConnection client;
    @Shadow private Entity topmostRiddenEntity;
    @Shadow @Final private MinecraftServer server;
    @Shadow private boolean floating;

    @Shadow public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;validatePlayerMove(Lnet/minecraft/server/network/packet/PlayerMoveC2SPacket;)Z"), cancellable = true)
    private void onOnPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (packet.getX(player.getX()) != player.getX() || packet.getY(player.getY()) != player.getY() || packet.getZ(player.getZ()) != player.getZ()) {
            if (AssassUtil.isAssassin(player) && AssassinTracker.forPlayer(player).isFrozen()) {
                requestTeleport(player.getX(), player.getY(), player.getZ(), packet.getYaw(player.yaw), packet.getPitch(player.pitch));
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerMove", at = @At("RETURN"))
    private void fixFlying(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (AssassUtil.isAssassin(player) && AssassinTracker.forPlayer(player).isFrozen())
            floating = false;
    }

    @Inject(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;validateVehicleMove(Lnet/minecraft/server/network/packet/VehicleMoveC2SPacket;)Z"), cancellable = true)
    private void onOnVehicleMove(VehicleMoveC2SPacket packet, CallbackInfo ci) {
        if (packet.getX() != lastTickRiddenX || packet.getY() != lastTickRiddenY || packet.getZ() != lastTickRiddenZ) {
            if (AssassUtil.isAssassin(player) && AssassinTracker.forPlayer(player).isFrozen()) {
                Entity riddenEntity = player.getRootVehicle();
                if (riddenEntity != player && riddenEntity.getPrimaryPassenger() == player && riddenEntity == topmostRiddenEntity) {
                    client.send(new VehicleMoveS2CPacket(riddenEntity));
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    private void onOnAttackEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (AssassUtil.isAssassin(player)) {
            ServerWorld world = server.getWorld(player.dimension);
            Entity victim = packet.getEntity(world);
            if (victim instanceof PlayerEntity && AssassUtil.isSpeedrunner((PlayerEntity) victim)) {
                if (!AssassinTracker.forPlayer(player).isFrozen())
                    victim.damage(DamageSource.player(player), Float.MAX_VALUE);
            }
        }
    }

}
