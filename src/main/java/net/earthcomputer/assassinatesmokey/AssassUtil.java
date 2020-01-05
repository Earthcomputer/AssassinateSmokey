package net.earthcomputer.assassinatesmokey;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssassUtil {

    private static final Map<UUID, PlayerType> playerTypes = new HashMap<>();

    public static boolean isSpeedrunner(PlayerEntity player) {
        return playerTypes.computeIfAbsent(player.getUuid(), k -> PlayerType.SPEEDRUNNER) == PlayerType.SPEEDRUNNER;
    }

    public static boolean isAssassin(PlayerEntity player) {
        return playerTypes.computeIfAbsent(player.getUuid(), k -> PlayerType.SPEEDRUNNER) == PlayerType.ASSASSIN;
    }

    public static void setPlayerType(PlayerEntity player, PlayerType type) {
        playerTypes.put(player.getUuid(), type);
    }

    public static enum PlayerType {
        SPEEDRUNNER, ASSASSIN
    }

}
