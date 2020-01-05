package net.earthcomputer.assassinatesmokey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.*;

public class AssassinateSmokeyCommand {

    private static final SimpleCommandExceptionType NOT_ASSASSIN_EXCEPTION = new SimpleCommandExceptionType(() -> "This command must be run by an assassin");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("assassinatesmokey")
            .then(literal("team")
                .executes(ctx -> printTeam(ctx.getSource()))
                .then(literal("assassin")
                    .executes(ctx -> joinTeam(ctx.getSource(), AssassUtil.PlayerType.ASSASSIN)))
                .then(literal("speedrunner")
                    .executes(ctx -> joinTeam(ctx.getSource(), AssassUtil.PlayerType.SPEEDRUNNER))))
            .then(literal("target")
                .then(literal("next")
                    .executes(ctx -> targetNext(ctx.getSource())))));
    }

    private static int printTeam(ServerCommandSource source) throws CommandSyntaxException {
        source.sendFeedback(new LiteralText("You are on team " + (AssassUtil.isAssassin(source.getPlayer()) ? "assassin" : "speedrunner")), false);
        return 0;
    }

    private static int joinTeam(ServerCommandSource source, AssassUtil.PlayerType type) throws CommandSyntaxException {
        AssassUtil.setPlayerType(source.getPlayer(), type);
        source.sendFeedback(new LiteralText("Joined team " + type.name().toLowerCase()), true);
        return 0;
    }

    private static int targetNext(ServerCommandSource source) throws CommandSyntaxException {
        if (!AssassUtil.isAssassin(source.getPlayer()))
            throw NOT_ASSASSIN_EXCEPTION.create();

        AssassinTracker tracker = AssassinTracker.forPlayer(source.getPlayer());
        tracker.nextPlayer();
        Optional<PlayerEntity> target = tracker.getTrackingPlayer();
        if (target.isPresent()) {
            source.sendFeedback(new LiteralText("Now tracking ").append(target.get().getName()), true);
        } else {
            source.sendFeedback(new LiteralText("No longer tracking a player"), true);
        }

        return 0;
    }

}
