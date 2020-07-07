package com.novarch.jojomod.entities.stands;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.attacks.KingCrimsonPunchEntity;
import com.novarch.jojomod.init.EffectInit;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.init.SoundInit;
import com.novarch.jojomod.util.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * You shouldn't be surprised if you're confused by this code, even I can barely read it now.
 */
@SuppressWarnings("ConstantConditions")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class KingCrimsonEntity extends AbstractStandEntity {
    public KingCrimsonEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
        spawnSound = SoundInit.SPAWN_KING_CRIMSON.get();
    }

    public KingCrimsonEntity(World world) {
        super(EntityInit.KING_CRIMSON.get(), world);
        spawnSound = SoundInit.SPAWN_KING_CRIMSON.get();
    }

    @Override
    public void attack(boolean special) {
        if (getMaster() == null) return;
        if (!ability || !Stand.getCapabilityFromPlayer(getMaster()).getAbility()) {
            attackTick++;
            if (attackTick == 1)
                if (special)
                    attackRush = true;
                else {
                    world.playSound(null, getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 1, 0.6f / (rand.nextFloat() * 0.3f + 1) * 2);
                    KingCrimsonPunchEntity kingCrimsonPunchEntity = new KingCrimsonPunchEntity(world, this, getMaster());
                    kingCrimsonPunchEntity.shoot(getMaster(), rotationPitch, rotationYaw, 3, 0.05f);
                    world.addEntity(kingCrimsonPunchEntity);
                }
        }
    }

    /**
     * Gets all entities in the {@link net.minecraft.world.server.ServerWorld} using {@link net.minecraft.world.server.ServerWorld}#getAllEntities,
     * then applies the {@link com.novarch.jojomod.effects.CrimsonEffect} to them to make them glow.
     * Also applies the {@link com.novarch.jojomod.effects.CrimsonEffectUser} to it's user, impairing his vision.
     */
    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            PlayerEntity player = getMaster();

            followMaster();
            setRotationYawHead(player.rotationYaw);
            setRotation(player.rotationYaw, player.rotationPitch);

            player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 40, 2));
            Stand.getLazyOptional(player).ifPresent(props -> {
                ability = !(props.getCooldown() > 0);

                if (!props.getAbility()) {
                    if (!player.isCreative() && !player.isSpectator())
                        player.setGameType(GameType.SURVIVAL);
                    player.setInvulnerable(false);
                }

                if (ability && props.getAbility() && props.getStandOn()) {
                    if (props.getTimeLeft() > 800) {
                        attackRush = false;
                        getMaster().setInvulnerable(true);
                        player.addPotionEffect(new EffectInstance(EffectInit.CRIMSON_USER.get(), 10000, 255));
                        if (!player.isCreative() && !player.isSpectator())
                            player.setGameType(GameType.ADVENTURE);
                        props.subtractTimeLeft(1);

                        if (!world.isRemote)
                            world.getServer().getWorld(dimension).getEntities()
                                    .filter(entity -> entity instanceof LivingEntity)
                                    .filter(entity -> !(entity instanceof GoldExperienceEntity))
                                    .filter(entity -> entity != this)
                                    .filter(Entity::isAlive)
                                    .forEach(entity -> {
                                        if (entity instanceof MobEntity) {
                                            if (((MobEntity) entity).getAttackTarget() == getMaster() || ((MobEntity) entity).getRevengeTarget() == getMaster()) {
                                                ((MobEntity) entity).setAttackTarget(null);
                                                ((MobEntity) entity).setRevengeTarget(null);
                                            }
                                            ((MobEntity) entity).addPotionEffect(new EffectInstance(EffectInit.CRIMSON.get(), 200, 255));
                                        }

                                        if (entity instanceof PlayerEntity)
                                            Stand.getLazyOptional((PlayerEntity) entity).ifPresent(prs -> {
                                                if (entity != player && prs.getStandID() != Util.StandID.GER) {
                                                    if (prs.getStandID() == Util.StandID.KING_CRIMSON && prs.getStandOn() && prs.getAbility() && prs.getTimeLeft() > 800)
                                                        return;
                                                    ((PlayerEntity) entity).addPotionEffect(new EffectInstance(EffectInit.CRIMSON.get(), 200, 255));
                                                }
                                            });
                                    });
                    } else {
                        if (!player.isCreative() && !player.isSpectator())
                            player.setGameType(GameType.SURVIVAL);
                        player.setInvulnerable(false);
                        ability = false;
                        player.removePotionEffect(EffectInit.CRIMSON_USER.get());
                        props.setCooldown(200);
                    }
                }

                if (!ability) {
                    if (props.getCooldown() > 0 && props.getAbility())
                        props.subtractCooldown(1);
                    if (props.getCooldown() <= 0) {
                        props.setTimeLeft(1000);
                        ability = true;
                    }
                }

                if (!props.getAbility()) {
                    if (player.isPotionActive(EffectInit.CRIMSON_USER.get()))
                        player.removePotionEffect(EffectInit.CRIMSON_USER.get());
                    if (props.getTimeLeft() < 1000)
                        props.addTimeLeft(1);
                }
                if (player.swingProgressInt == 0 && !attackRush)
                    attackTick = 0;
                if (attackRush) {
                    attackTicker++;
                    if (attackTicker >= 10)
                        if (!world.isRemote) {
                            player.setSprinting(false);
                            KingCrimsonPunchEntity kingCrimson1 = new KingCrimsonPunchEntity(world, this, player);
                            kingCrimson1.setRandomPositions();
                            kingCrimson1.shoot(player, player.rotationPitch, player.rotationYaw, 2.5f, 0.2f);
                            world.addEntity(kingCrimson1);
                            KingCrimsonPunchEntity kingCrimson2 = new KingCrimsonPunchEntity(world, this, player);
                            kingCrimson2.setRandomPositions();
                            kingCrimson2.shoot(player, player.rotationPitch, player.rotationYaw, 2.5f, 0.2f);
                            world.addEntity(kingCrimson2);
                        }
                    if (attackTicker >= 80) {
                        attackRush = false;
                        attackTicker = 0;
                    }
                }
            });
        }
    }
}
