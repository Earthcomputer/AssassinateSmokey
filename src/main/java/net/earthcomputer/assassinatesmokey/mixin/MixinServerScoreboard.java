package net.earthcomputer.assassinatesmokey.mixin;

import net.minecraft.network.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public abstract class MixinServerScoreboard extends Scoreboard {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        readdTeam("assassins");
        readdTeam("speedrunners");
    }

    @Unique
    private void readdTeam(String name) {
        Team team = new Team(this, name);
        ((ScoreboardAccessor) this).assassinateSmokey_getTeams().put(name, team);
    }

    @Redirect(method = "updateScoreboardTeamAndPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"))
    private void redirectSendToAll(PlayerManager pm, Packet<?> packet) {
        if (pm != null)
            pm.sendToAll(packet);
    }
}
