package net.earthcomputer.assassinatesmokey.mixin;

import net.earthcomputer.assassinatesmokey.AssassUtil;
import net.earthcomputer.assassinatesmokey.AssassinTracker;
import net.minecraft.client.network.packet.PlayerSpawnPositionS2CPacket;
import net.minecraft.client.network.packet.TitleS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(CompassItem.class)
public abstract class MixinCompassItem extends Item {
    public MixinCompassItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient || !AssassUtil.isAssassin(user))
            return super.use(world, user, hand);

        AssassinTracker tracker = AssassinTracker.forPlayer(user);
        tracker.nextPlayer();
        Optional<PlayerEntity> target = tracker.getTrackingPlayer();
        if (target.isPresent()) {
            sendSubtitle(user, new LiteralText("Now tracking ").styled(s -> s.setColor(Formatting.GREEN)).append(target.get().getName()));
            sendSpawnPoint(user, new BlockPos(target.get()));
        } else {
            sendSubtitle(user, new LiteralText("No longer tracking a player").styled(s -> s.setColor(Formatting.GREEN)));
            sendSpawnPoint(user, world.getSpawnPos());
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Unique
    private void sendSubtitle(PlayerEntity player, Text text) {
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, text, 10, 40, 10));
    }

    @Unique
    private void sendSpawnPoint(PlayerEntity player, BlockPos spawnPoint) {
        ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(spawnPoint));
    }
}
