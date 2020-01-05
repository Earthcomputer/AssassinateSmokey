package net.earthcomputer.assassinatesmokey.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Scoreboard.class)
public interface ScoreboardAccessor {

    @Accessor("teams")
    Map<String, Team> assassinateSmokey_getTeams();

}
