package com.novarch.jojomod.entities.stands.attacks;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.AbstractStandEntity;
import com.novarch.jojomod.entities.stands.AbstractStandPunchEntity;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.init.ItemInit;
import com.novarch.jojomod.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;

public class WhitesnakePunchEntity extends AbstractStandPunchEntity {
    public WhitesnakePunchEntity(EntityType<? extends AbstractStandPunchEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public WhitesnakePunchEntity(World worldIn, AbstractStandEntity shooter, PlayerEntity player) {
        super(EntityInit.WHITESNAKE_PUNCH.get(), worldIn, shooter, player);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = result.getEntity();
        entity.attackEntityFrom(DamageSource.causeMobDamage(standMaster), 0.8f);
        entity.hurtResistantTime = 0;
        if (shootingStand.ability)
            if (entity instanceof PlayerEntity)
                Stand.getLazyOptional((PlayerEntity) entity).ifPresent(props -> {
                    if (props.getStandID() != 0 && props.getStandID() != Util.StandID.GER && ((PlayerEntity) entity).getHealth() <= ((PlayerEntity) entity).getMaxHealth() / 2) {
                        ItemStack itemStack = new ItemStack(ItemInit.STAND_DISC.get());
                        CompoundNBT nbt = itemStack.getTag() == null ? new CompoundNBT() : itemStack.getTag();
                        if (standMaster.inventory.getStackInSlot(standMaster.inventory.getBestHotbarSlot()).isEmpty()) {
                            standMaster.inventory.currentItem = standMaster.inventory.getBestHotbarSlot();
                            nbt.putInt("StandID", props.getStandID());
                            props.removeStand();
                            itemStack.setTag(nbt);
                            standMaster.inventory.add(standMaster.inventory.getBestHotbarSlot(), itemStack);
                        }
                    }
                });
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {
        BlockPos pos = result.getPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlockHardness(world, pos) != -1 && state.getBlockHardness(world, pos) < 3) {
            world.removeBlock(pos, false);
            if (world.rand.nextBoolean())
                state.getBlock().harvestBlock(world, standMaster, pos, state, null, standMaster.getActiveItemStack());
        }
    }
}