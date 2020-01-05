package net.earthcomputer.assassinatesmokey;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;

public class AssassUtil {

    public static boolean isSpeedrunner(PlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        if (team == null)
            return false;
        return "speedrunners".equals(team.getName());
    }

    public static boolean isAssassin(PlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        if (team == null)
            return false;
        return "assassins".equals(team.getName());
    }

}
