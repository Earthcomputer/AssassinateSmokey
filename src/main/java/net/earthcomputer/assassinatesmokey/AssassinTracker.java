package net.earthcomputer.assassinatesmokey;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Stream;

public class AssassinTracker {

    private final MinecraftServer server;
    private final UUID player;
    private UUID trackingSpeedrunner;
    private final Queue<UUID> nextPlayers = new ArrayDeque<>();
    private BlockPos trackingPos;
    private boolean frozen; // Let it go!

    private static Map<UUID, AssassinTracker> trackers = new HashMap<>();

    public static AssassinTracker forPlayer(PlayerEntity player) {
        return trackers.computeIfAbsent(player.getUuid(), k -> new AssassinTracker(player.getServer(), k));
    }

    private AssassinTracker(MinecraftServer server, UUID player) {
        this.server = server;
        this.player = player;
    }

    private Optional<PlayerEntity> getPlayer() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(player));
    }

    private Stream<? extends PlayerEntity> speedrunners() {
        return getPlayer().map(player -> player.world.getPlayers().stream().filter(AssassUtil::isSpeedrunner))
                .orElse(Stream.empty());
    }

    public void nextPlayer() {
        Optional<PlayerEntity> optionalPlayer = getPlayer();
        if (!optionalPlayer.isPresent())
            return;
        PlayerEntity player = optionalPlayer.get();
        if (trackingSpeedrunner != null)
            nextPlayers.add(trackingSpeedrunner);
        trackingSpeedrunner = speedrunners()
                .filter(p -> !nextPlayers.contains(p.getUuid()))
                .min(Comparator.comparingDouble(player::squaredDistanceTo))
                .map(Entity::getUuid)
                .orElseGet(() -> {
                    for (UUID p = nextPlayers.poll(); p != null; p = nextPlayers.poll()) {
                        PlayerEntity p1 = server.getPlayerManager().getPlayer(p);
                        if (p1 != null && p1.world == player.world && AssassUtil.isSpeedrunner(p1))
                            return p;
                    }
                    return null;
                });
    }

    public Optional<PlayerEntity> getTrackingPlayer() {
        if (trackingSpeedrunner == null)
            return Optional.empty();
        return Optional.ofNullable(server.getPlayerManager().getPlayer(trackingSpeedrunner));
    }

    public void trackPosition(BlockPos pos) {
        this.trackingPos = pos;
        getPlayer().ifPresent(player -> ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(pos, 0)));
    }

    public BlockPos getTrackingPos() {
        return trackingPos;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
}
