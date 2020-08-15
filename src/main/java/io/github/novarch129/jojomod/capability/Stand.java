package io.github.novarch129.jojomod.capability;

import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.network.message.server.SSyncStandCapabilityPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.novarch129.jojomod.util.Util.Null;
import static io.github.novarch129.jojomod.util.Util.StandID.CMOON;
import static io.github.novarch129.jojomod.util.Util.StandID.MADE_IN_HEAVEN;

/**
 * The {@link Capability} used for storing the player's Stand ability.
 */
public class Stand implements IStand, ICapabilitySerializable<INBT> {
    @CapabilityInject(IStand.class)
    public static final Capability<IStand> STAND = Null(); //Null method suppresses warnings
    private final PlayerEntity player;
    private int standID;
    /**
     * The {@link Entity#getEntityId()} of the player's Stand, can be used in conjunction with a {@link net.minecraft.world.World} to get the entity.
     */
    private int standEntityID;
    private int standAct;
    private boolean standOn;
    private double cooldown;
    private double timeLeft = 1000;
    private String diavolo = "";
    private boolean ability = true;
    private boolean abilityActive;
    private int transformed;
    private boolean noClip;
    private double invulnerableTicks;
    private float standDamage;
    private boolean charging;
    private int abilityUseCount;
    private LazyOptional<IStand> holder = LazyOptional.of(() -> new Stand(getPlayer()));

    public Stand(@Nonnull PlayerEntity player) {
        this.player = player;
    }

    public static IStand getCapabilityFromPlayer(PlayerEntity player) {
        return player.getCapability(STAND).orElse(new Stand(player));
    }

    public static LazyOptional<IStand> getLazyOptional(PlayerEntity player) {
        return player.getCapability(STAND);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IStand.class, new Capability.IStorage<IStand>() {
            @Nonnull
            @Override
            public INBT writeNBT(Capability<IStand> capability, IStand instance, Direction side) {
                CompoundNBT props = new CompoundNBT();
                props.putInt("standID", instance.getStandID());
                props.putInt("standAct", instance.getAct());
                props.putBoolean("standOn", instance.getStandOn());
                props.putDouble("cooldown", instance.getCooldown());
                props.putDouble("timeLeft", instance.getTimeLeft());
                props.putBoolean("ability", instance.getAbility());
                props.putInt("transformed", instance.getTransformed());
                props.putString("diavolo", instance.getDiavolo());
                props.putBoolean("noClip", instance.getNoClip());
                props.putInt("standEntityID", instance.getPlayerStand());
                props.putBoolean("abilityActive", instance.getAbilityActive());
                props.putDouble("invulnerableTicks", instance.getInvulnerableTicks());
                props.putFloat("standDamage", instance.getStandDamage());
                props.putBoolean("charging", instance.isCharging());
                props.putInt("abilityUseCount", instance.getAbilityUseCount());
                return props;
            }

            @Override
            public void readNBT(Capability<IStand> capability, IStand instance, Direction side, INBT nbt) {
                CompoundNBT compoundNBT = (CompoundNBT) nbt;
                instance.putStandID(compoundNBT.getInt("standID"));
                instance.putAct(compoundNBT.getInt("standAct"));
                instance.putStandOn(compoundNBT.getBoolean("standOn"));
                instance.putCooldown(compoundNBT.getDouble("cooldown"));
                instance.putTimeLeft(compoundNBT.getDouble("timeLeft"));
                instance.putAbility(compoundNBT.getBoolean("ability"));
                instance.putTransformed(compoundNBT.getInt("transformed"));
                instance.putDiavolo(compoundNBT.getString("diavolo"));
                instance.putNoClip(compoundNBT.getBoolean("noClip"));
                instance.putPlayerStand(compoundNBT.getInt("standEntityID"));
                instance.putAbilityActive(compoundNBT.getBoolean("abilityActive"));
                instance.putInvulnerableTicks(compoundNBT.getDouble("invulnerableTicks"));
                instance.putStandDamage(compoundNBT.getFloat("standDamage"));
                instance.putCharging(compoundNBT.getBoolean("charging"));
                instance.putAbilityUseCount(compoundNBT.getInt("abilityUseCount"));
            }
        }, () -> new Stand(Null()));
    }

    @Override
    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public int getStandID() {
        return standID;
    }

    @Override
    public void setStandID(int value) {
        this.standID = value;
        onDataUpdated();
    }

    @Override
    public int getPlayerStand() {
        return standEntityID;
    }

    @Override
    public void setPlayerStand(int standEntityID) {
        this.standEntityID = standEntityID;
        onDataUpdated();
    }

    @Override
    public void putPlayerStand(int standEntityID) {
        this.standEntityID = standEntityID;
    }

    @Override
    public int getAct() {
        return standAct;
    }

    @Override
    public void setAct(int standAct) {
        this.standAct = standAct;
        onDataUpdated();
    }

    @Override
    public void changeAct() {
        standAct++;
        if (standAct == getMaxAct())
            standAct = 0;
        onDataUpdated();
    }

    @Override
    public boolean hasAct() {
        return getStandID() == MADE_IN_HEAVEN || getStandID() == CMOON;
    }

    @Override
    public int getMaxAct() {
        switch (standID) {
            case MADE_IN_HEAVEN:
                return 3;
            case CMOON:
                return 2;
        }
        return 0;
    }

    @Override
    public boolean getStandOn() {
        return this.standOn;
    }

    @Override
    public void setStandOn(boolean value) {
        this.standOn = value;
        onDataUpdated();
    }

    @Override
    public double getCooldown() {
        return this.cooldown;
    }

    @Override
    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
        onDataUpdated();
    }

    @Override
    public double getTimeLeft() {
        return timeLeft;
    }

    @Override
    public void setTimeLeft(double timeLeft) {
        this.timeLeft = timeLeft;
        onDataUpdated();
    }

    @Override
    public String getDiavolo() {
        return diavolo;
    }

    @Override
    public void setDiavolo(String truth) {
        this.diavolo = truth;
        onDataUpdated();
    }

    @Override
    public boolean getAbility() {
        return ability;
    }

    @Override
    public void setAbility(boolean ability) {
        this.ability = ability;
        onDataUpdated();
    }

    @Override
    public int getTransformed() {
        return transformed;
    }

    @Override
    public void setTransformed(int value) {
        this.transformed = value;
        onDataUpdated();
    }

    @Override
    public void addTransformed(int addition) {
        this.transformed += addition;
        onDataUpdated();
    }

    @Override
    public boolean getNoClip() {
        return noClip;
    }

    @Override
    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
        onDataUpdated();
    }

    @Override
    public void putStandID(int standID) {
        this.standID = standID;
    }

    @Override
    public void putAct(int standAct) {
        this.standAct = standAct;
    }

    @Override
    public void putStandOn(boolean standOn) {
        this.standOn = standOn;
    }

    @Override
    public void putTimeLeft(double timeLeft) {
        this.timeLeft = timeLeft;
    }

    @Override
    public void putCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public void putAbility(boolean ability) {
        this.ability = ability;
    }

    @Override
    public void putTransformed(int transformed) {
        this.transformed = transformed;
    }

    @Override
    public void putDiavolo(String truth) {
        this.diavolo = truth;
    }

    @Override
    public void putNoClip(boolean noClip) {
        this.noClip = noClip;
    }

    @Override
    public boolean getAbilityActive() {
        return abilityActive;
    }

    @Override
    public void setAbilityActive(boolean abilityActive) {
        this.abilityActive = abilityActive;
        onDataUpdated();
    }

    @Override
    public void putAbilityActive(boolean abilityActive) {
        this.abilityActive = abilityActive;
    }

    @Override
    public double getInvulnerableTicks() {
        return invulnerableTicks;
    }

    @Override
    public void setInvulnerableTicks(double invulnerableTicks) {
        this.invulnerableTicks = invulnerableTicks;
        onDataUpdated();
    }

    @Override
    public void putInvulnerableTicks(double invulnerableTicks) {
        this.invulnerableTicks = invulnerableTicks;
    }

    @Override
    public float getStandDamage() {
        return standDamage;
    }

    @Override
    public void setStandDamage(float standDamage) {
        this.standDamage = standDamage;
        onDataUpdated();
    }

    @Override
    public void putStandDamage(float standDamage) {
        this.standDamage = standDamage;
    }

    @Override
    public boolean isCharging() {
        return charging;
    }

    @Override
    public void setCharging(boolean charging) {
        this.charging = charging;
        onDataUpdated();
    }

    @Override
    public void putCharging(boolean charging) {
        this.charging = charging;
    }

    @Override
    public int getAbilityUseCount() {
        return abilityUseCount;
    }

    @Override
    public void setAbilityUseCount(int abilityUseCount) {
        this.abilityUseCount = abilityUseCount;
        onDataUpdated();
    }

    @Override
    public void putAbilityUseCount(int abilityUseCount) {
        this.abilityUseCount = abilityUseCount;
    }

    public void clone(IStand props) {
        putStandID(props.getStandID());
        putAct(props.getAct());
        putStandOn(props.getStandOn());
        putCooldown(props.getCooldown());
        putTimeLeft(props.getTimeLeft());
        putTransformed(props.getTransformed());
        putDiavolo(props.getDiavolo());
        putAbility(props.getAbility());
        putPlayerStand(props.getPlayerStand());
        putAbilityActive(props.getAbilityActive());
        putInvulnerableTicks(props.getInvulnerableTicks());
        putStandDamage(props.getStandDamage());
        putCharging(props.isCharging());
        putAbilityUseCount(props.getAbilityUseCount());
        onDataUpdated();
    }

    @Override
    public void removeStand() {
        putStandOn(false);
        putAct(0);
        putStandID(0);
        putCooldown(0);
        putTimeLeft(1000);
        putTransformed(0);
        putDiavolo("");
        putAbility(true);
        putNoClip(false);
        putPlayerStand(0);
        putAbilityActive(false);
        putInvulnerableTicks(0);
        putStandDamage(0);
        putCharging(false);
        putAbilityUseCount(0);
        onDataUpdated();
    }

    /**
     * Called to update the {@link Capability} to the client.
     */
    @Override
    public void onDataUpdated() {
        if (!player.world.isRemote)
            JojoBizarreSurvival.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SSyncStandCapabilityPacket(this));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return capability == STAND ? holder.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return Stand.STAND.getStorage().writeNBT(STAND, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        Stand.STAND.getStorage().readNBT(STAND, holder.orElseThrow(() -> new IllegalArgumentException("LazyOptional is empty!")), null, nbt);
    }
}