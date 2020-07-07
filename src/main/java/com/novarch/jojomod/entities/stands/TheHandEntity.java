package com.novarch.jojomod.entities.stands;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.attacks.TheHandPunchEntity;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.init.SoundInit;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("ConstantConditions")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TheHandEntity extends AbstractStandEntity {
    public TheHandEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
        spawnSound = SoundInit.SPAWN_MAGICIANS_RED.get();
    }

    public TheHandEntity(World world) {
        super(EntityInit.THE_HAND.get(), world);
        spawnSound = SoundInit.SPAWN_MAGICIANS_RED.get();
    }

    public void teleportEntity(int id) {
        Entity entity = world.getEntityByID(id);
        if (entity == null || getMaster() == null) return;
        float yaw = getMaster().rotationYaw;
        float pitch = getMaster().rotationPitch;
        double motionX = (-MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
        double motionZ = (MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
        double motionY = (-MathHelper.sin((pitch) / 180.0F * (float) Math.PI) * 1.0f);
        if (!world.isRemote)
            entity.setMotion(-motionX * (entity.getDistance(getMaster()) / 4), -motionY * (entity.getDistance(getMaster()) / 4), -motionZ * (entity.getDistance(getMaster()) / 4));
    }

    @Deprecated  //Safe to call, @Deprecated because it's buggy
    public void teleportMaster() {
        PlayerEntity master = getMaster();
        if (master == null || world.isRemote) return;
        int distance = 20;
        float f1 = MathHelper.cos(-master.rotationYaw * 0.017453292f - (float) Math.PI);
        float f2 = MathHelper.sin(-master.rotationYaw * 0.017453292f - (float) Math.PI);
        float f3 = -MathHelper.cos(-master.rotationPitch * 0.017453292f);
        float f4 = MathHelper.sin(-master.rotationPitch * 0.017453292f);
        if (!master.world.isRemote)
            master.move(MoverType.PLAYER, new Vec3d(distance * f2 * f3, distance * f4, distance * f1 * f3));
    }

    @Override
    public void attack(boolean special) {
        if (getMaster() == null) return;
        attackTick++;
        if (attackTick == 1)
            if (special)
                attackRush = true;
            else {
                world.playSound(null, getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 1, 0.6f / (rand.nextFloat() * 0.3f + 1) * 2);
                TheHandPunchEntity theHandPunchEntity = new TheHandPunchEntity(world, this, getMaster());
                theHandPunchEntity.shoot(getMaster(), rotationPitch, rotationYaw, 1, 0.4f);
                world.addEntity(theHandPunchEntity);
            }
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            PlayerEntity player = getMaster();
            Stand.getLazyOptional(player).ifPresent(props -> ability = props.getAbility());

            followMaster();
            setRotationYawHead(player.rotationYaw);
            setRotation(player.rotationYaw, player.rotationPitch);

            if (player.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                player.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        player.setSprinting(false);
                        TheHandPunchEntity theHand1 = new TheHandPunchEntity(world, this, player);
                        theHand1.setRandomPositions();
                        theHand1.shoot(player, player.rotationPitch, player.rotationYaw, 0.8f, 0.5f);
                        world.addEntity(theHand1);
                        TheHandPunchEntity theHand2 = new TheHandPunchEntity(world, this, player);
                        theHand2.setRandomPositions();
                        theHand2.shoot(player, player.rotationPitch, player.rotationYaw, 0.8f, 0.5f);
                        world.addEntity(theHand2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
