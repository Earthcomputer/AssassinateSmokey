package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class MixinEntityTrackerEntry {

    @Shadow @Final private Entity entity;

    @Inject(method = "method_14306", at = @At("HEAD"))
    private void onUpdateAttributes(CallbackInfo ci) {
        if (entity instanceof PlayerEntity && AssassUtil.isAssassin((PlayerEntity) entity) && AssassinTracker.forPlayer((PlayerEntity) entity).isFrozen()) {
            ((PlayerEntity) entity).getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).addModifier(AssassinTracker.FREEZE_MODIFIER);
        }
    }

    @Inject(method = "method_14306", at = @At("RETURN"))
    private void postUpdateAttributes(CallbackInfo ci) {
        if (entity instanceof PlayerEntity)
            ((PlayerEntity) entity).getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).removeModifier(AssassinTracker.FREEZE_MODIFIER);
    }

}
