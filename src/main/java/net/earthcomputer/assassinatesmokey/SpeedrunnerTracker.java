package net.earthcomputer.assassinatesmokey;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SpeedrunnerTracker {

    private final MinecraftServer server;
    private final UUID player;
    private PlayerEntity lookingAt;

    private static Map<UUID, SpeedrunnerTracker> trackers = new HashMap<>();

    public static SpeedrunnerTracker forPlayer(PlayerEntity player) {
        return trackers.computeIfAbsent(player.getUuid(), k -> new SpeedrunnerTracker(player.getServer(), k));
    }

    private SpeedrunnerTracker(MinecraftServer server, UUID player) {
        this.server = server;
        this.player = player;
    }

    private Optional<PlayerEntity> getPlayer() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(player));
    }

    public PlayerEntity getLookingAt() {
        return lookingAt;
    }

    public PlayerEntity recalculateLookingAt() {
        final int DISTANCE = 128;

        Optional<PlayerEntity> optionalPlayer = getPlayer();
        if (!optionalPlayer.isPresent()) {
            return lookingAt = null;
        }
        PlayerEntity player = optionalPlayer.get();

        HitResult blockHitResult = player.rayTrace(DISTANCE, 1, false);
        double maxDistanceSq = player.getCameraPosVec(1).squaredDistanceTo(blockHitResult.getPos());
        Vec3d cameraVec = player.getRotationVec(1);
        Box box = player.getBoundingBox().stretch(cameraVec.multiply(DISTANCE)).expand(1, 1, 1);
        EntityHitResult entityHitResult = rayTrace(player,
                player.getCameraPosVec(1),
                player.getCameraPosVec(1).add(cameraVec.multiply(DISTANCE)),
                box,
                entity -> !entity.isSpectator() && entity.collides(),
                maxDistanceSq);

        if (entityHitResult != null
                && player.getCameraPosVec(1).squaredDistanceTo(entityHitResult.getPos()) < maxDistanceSq) {
            Entity target = entityHitResult.getEntity();
            if (target instanceof PlayerEntity && AssassUtil.isAssassin((PlayerEntity) target)) {
                return lookingAt = (PlayerEntity) target;
            }
        }

        return lookingAt = null;
    }

    private static EntityHitResult rayTrace(Entity sourceEntity, Vec3d fromPos, Vec3d toPos, Box box, Predicate<Entity> predicate, double maxDistanceSq) {
        World world = sourceEntity.world;
        double distanceLeftSq = maxDistanceSq;
        Entity resultEntity = null;
        Vec3d resultPos = null;
        for (final Entity entity : world.getEntities(sourceEntity, box, predicate)) {
            Box targetBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> optionalRayTraceResult = targetBox.rayTrace(fromPos, toPos);
            if (targetBox.contains(fromPos)) {
                if (distanceLeftSq < 0.0) {
                    continue;
                }
                resultEntity = entity;
                resultPos = optionalRayTraceResult.orElse(fromPos);
                distanceLeftSq = 0.0;
            }
            else {
                if (!optionalRayTraceResult.isPresent()) {
                    continue;
                }
                Vec3d rayTraceResult = optionalRayTraceResult.get();
                double rayLength = fromPos.squaredDistanceTo(rayTraceResult);
                if (rayLength >= distanceLeftSq && distanceLeftSq != 0.0) {
                    continue;
                }
                if (entity.getRootVehicle() == sourceEntity.getRootVehicle()) {
                    if (distanceLeftSq != 0.0) {
                        continue;
                    }
                    resultEntity = entity;
                    resultPos = rayTraceResult;
                }
                else {
                    resultEntity = entity;
                    resultPos = rayTraceResult;
                    distanceLeftSq = rayLength;
                }
            }
        }
        if (resultEntity == null) {
            return null;
        }
        return new EntityHitResult(resultEntity, resultPos);
    }
}
