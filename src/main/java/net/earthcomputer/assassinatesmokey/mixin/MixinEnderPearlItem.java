package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderPearlItem.class)
public class MixinEnderPearlItem {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/EnderPearlEntity;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        if (AssassUtil.isAssassin(user)) {
            int slot = hand == Hand.MAIN_HAND ? 36 + user.getInventory().selectedSlot : 45;
            ((ServerPlayerEntity) user).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(user.playerScreenHandler.syncId, slot, user.playerScreenHandler.getSlot(slot).getStack()));
            ci.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
        }
    }

}
