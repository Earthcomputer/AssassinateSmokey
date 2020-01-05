package net.earthcomputer.assassinatesmokey.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class MixinScoreboard {

    @Shadow public abstract Team getTeam(String string);

    @Shadow public abstract boolean addPlayerToTeam(String playerName, Team team);

    @Shadow @Final private Map<String, Team> teamsByPlayer;

    @Unique
    private Team getAssassins() {
        return getTeam("assassins");
    }

    @Unique
    private Team getSpeedrunners() {
        return getTeam("speedrunners");
    }

    @Inject(method = "getPlayerTeam", at = @At("RETURN"), cancellable = true)
    private void onGetPlayerTeam(String player, CallbackInfoReturnable<Team> ci) {
        if (!isPlayer(player))
            return;
        if (ci.getReturnValue() == null) {
            Team speedrunners = getSpeedrunners();
            teamsByPlayer.put(player, speedrunners);
            speedrunners.getPlayerList().add(player);
            addPlayerToTeam(player, speedrunners);
            ci.setReturnValue(speedrunners);
        }
    }

    @Inject(method = "removePlayerFromTeam", at = @At("RETURN"))
    private void onRemovePlayerFromTeam(String player, Team team, CallbackInfo ci) {
        if (!isPlayer(player))
            return;
        if (team == getSpeedrunners())
            addPlayerToTeam(player, getAssassins());
        else
            addPlayerToTeam(player, getSpeedrunners());
    }

    @Inject(method = "clearPlayerTeam", at = @At("HEAD"), cancellable = true)
    private void onClearPlayerTeam(String player, CallbackInfoReturnable<Boolean> ci) {
        if (!isPlayer(player))
            return;
        Team speedrunners = getSpeedrunners();
        if (getTeam(player) == speedrunners) {
            ci.setReturnValue(false);
        } else {
            teamsByPlayer.put(player, speedrunners);
            speedrunners.getPlayerList().add(player);
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "addTeam", at = @At("HEAD"), cancellable = true)
    private void onAddTeam(String team, CallbackInfoReturnable<Team> ci) {
        if ("assassins".equals(team))
            ci.setReturnValue(getAssassins());
        else if ("speedrunners".equals(team))
            ci.setReturnValue(getSpeedrunners());
    }

    @Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
    private void onRemoveTeam(Team team, CallbackInfo ci) {
        if (team == getAssassins() || team == getSpeedrunners())
            ci.cancel();
    }

    private boolean isPlayer(String player) {
        //noinspection ConstantConditions
        if (!((Object) this instanceof ServerScoreboard))
            return false;
        MinecraftServer server = ((ServerScoreboardAccessor) this).getServer();
        return server.getPlayerManager().getPlayer(player) != null;
    }

}
