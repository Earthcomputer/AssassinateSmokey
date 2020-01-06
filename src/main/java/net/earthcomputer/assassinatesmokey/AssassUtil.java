package net.earthcomputer.assassinatesmokey;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssassUtil {

    private static final Map<UUID, PlayerType> playerTypes = new HashMap<>();

    public static boolean isSpeedrunner(PlayerEntity player) {
        return playerTypes.computeIfAbsent(player.getUuid(), k -> PlayerType.SPECTATOR) == PlayerType.SPEEDRUNNER;
    }

    public static boolean isAssassin(PlayerEntity player) {
        return playerTypes.computeIfAbsent(player.getUuid(), k -> PlayerType.SPECTATOR) == PlayerType.ASSASSIN;
    }

    public static void setPlayerType(PlayerEntity player, PlayerType type) {
        playerTypes.put(player.getUuid(), type);
        if (type == PlayerType.ASSASSIN) {
            // check if there is already a compass
            for (int i = 0; i < player.inventory.getInvSize(); i++) {
                if (player.inventory.getInvStack(i).getItem() == Items.COMPASS)
                    return;
            }
            player.inventory.insertStack(new ItemStack(Items.COMPASS));
        }
    }

    public enum PlayerType {
        SPEEDRUNNER, ASSASSIN, SPECTATOR
    }

}
