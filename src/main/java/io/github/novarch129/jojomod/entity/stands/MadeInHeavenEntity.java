package io.github.novarch129.jojomod.entity.stands;

import io.github.novarch129.jojomod.entity.stands.attacks.MadeInHeavenPunchEntity;
import io.github.novarch129.jojomod.init.DimensionInit;
import io.github.novarch129.jojomod.capability.stand.Stand;
import io.github.novarch129.jojomod.config.JojoBizarreSurvivalConfig;
import io.github.novarch129.jojomod.event.EventD4CTeleportProcessor;
import io.github.novarch129.jojomod.init.EntityInit;
import io.github.novarch129.jojomod.init.SoundInit;
import io.github.novarch129.jojomod.util.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("ConstantConditions")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MadeInHeavenEntity extends AbstractStandEntity {
    public int heavenTick = 3600;

    public MadeInHeavenEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
        spawnSound = SoundInit.SPAWN_MADE_IN_HEAVEN.get();
    }

    public MadeInHeavenEntity(World world) {
        super(EntityInit.MADE_IN_HEAVEN.get(), world);
        spawnSound = SoundInit.SPAWN_MADE_IN_HEAVEN.get();
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
                MadeInHeavenPunchEntity madeInHeavenPunchEntity = new MadeInHeavenPunchEntity(world, this, getMaster());
                madeInHeavenPunchEntity.shoot(getMaster(), rotationPitch, rotationYaw, 6, Float.MIN_VALUE);
                world.addEntity(madeInHeavenPunchEntity);
            }
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            PlayerEntity player = getMaster();
            Stand.getLazyOptional(player).ifPresent(props -> {
                props.setTimeLeft(heavenTick - 1200);
                if (props.getAct() == 1) {
                    remove();
                    CMoonEntity cMoon = new CMoonEntity(world);
                    cMoon.setLocationAndAngles(getMaster().getPosX() + 0.1, getMaster().getPosY(), getMaster().getPosZ(), getMaster().rotationYaw, getMaster().rotationPitch);
                    cMoon.setMaster(getMaster());
                    world.addEntity(cMoon);
                    cMoon.playSpawnSound();
                }
            });
            player.addPotionEffect(new EffectInstance(Effects.SPEED, 40, 19));
            player.setHealth(20.0f);
            player.getFoodStats().addStats(20, 20.0f);

            if (player.isCrouching() && JojoBizarreSurvivalConfig.COMMON.madeInHeavenAbilityAccelerating.get())
                heavenTick -= 200;

            if (heavenTick > 0)
                heavenTick--;

            if (heavenTick == 1200)
                player.sendMessage(new StringTextComponent("\"Heaven\" has begun!"));

            if (heavenTick < 1200) {
                world.setDayTime(world.getDayTime() + 50);
                player.addPotionEffect(new EffectInstance(Effects.SPEED, 40, 39));
                LightningBoltEntity lightning = new LightningBoltEntity(world, getPosX() + rand.nextInt(50), getPosY(), getPosZ() + rand.nextInt(50), false);
                lightning.setSilent(true);
                if (!world.isRemote)
                    ((ServerWorld) world).addLightningBolt(lightning);
                world.addEntity(lightning);
                world.getGameRules().write().putInt(GameRules.RANDOM_TICK_SPEED.getName(), world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED) + 5);
            }

            if (heavenTick < 800) {
                world.setDayTime(world.getDayTime() + 80);
                world.setRainStrength(10.0f);
                world.getWorldInfo().setRaining(true);
                player.addPotionEffect(new EffectInstance(Effects.SPEED, 40, 99));
            }

            if (heavenTick < 400) {
                player.addPotionEffect(new EffectInstance(Effects.SPEED, 40, 255));
                player.addPotionEffect(new EffectInstance(Effects.LEVITATION, 40, 2));
                world.createExplosion(this, getPosX() + rand.nextInt(100), getPosY() - fallDistance, getPosZ() + rand.nextInt(100), 4.0f, Explosion.Mode.DESTROY);
                world.setDayTime(world.getDayTime() + 500);
            }

            if (heavenTick <= 0) {
                world.getPlayers().forEach(entity -> Stand.getLazyOptional(entity).ifPresent(prps -> {
                    if (prps.getStandID() != Util.StandID.GER) {
                        entity.inventory.clear();
                        entity.getInventoryEnderChest().clear();
                        EventD4CTeleportProcessor.madeInHeaven.add(entity);
                        entity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 40, 99));
                        entity.fallDistance = 0;
                        entity.setSpawnDimenion(DimensionType.byName(DimensionInit.MADE_IN_HEAVEN_DIMENSION_TYPE));
                        prps.removeStand();
                        entity.setInvulnerable(false);
                    }
                }));
            }

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
                        MadeInHeavenPunchEntity madeInHeaven1 = new MadeInHeavenPunchEntity(world, this, player);
                        madeInHeaven1.setRandomPositions();
                        madeInHeaven1.shoot(player, player.rotationPitch, player.rotationYaw, 4, 0.1f);
                        world.addEntity(madeInHeaven1);
                        MadeInHeavenPunchEntity madeInHeaven2 = new MadeInHeavenPunchEntity(world, this, player);
                        madeInHeaven2.setRandomPositions();
                        madeInHeaven2.shoot(player, player.rotationPitch, player.rotationYaw, 4, 0.1f);
                        world.addEntity(madeInHeaven2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}