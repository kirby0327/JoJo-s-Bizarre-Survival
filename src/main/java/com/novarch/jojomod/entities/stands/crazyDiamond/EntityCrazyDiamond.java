package com.novarch.jojomod.entities.stands.crazyDiamond;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.EntityStandBase;
import com.novarch.jojomod.entities.stands.EntityStandPunch;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.init.SoundInit;
import com.novarch.jojomod.util.JojoLibs;
import com.novarch.jojomod.util.handlers.KeyHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.WeakHashMap;

@SuppressWarnings("ConstantConditions")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EntityCrazyDiamond extends EntityStandBase {
	private int oratick = 0;

	private int oratickr = 0;

	private KeyBinding repair = KeyHandler.keys[2];

	private WeakHashMap<BlockPos, BlockState> repairBlocks = new WeakHashMap<>();

	public EntityCrazyDiamond(EntityType<? extends EntityStandBase> type, World world) {
		super(type, world);
		this.spawnSound = SoundInit.SPAWN_CRAZY_DIAMOND.get();
		this.standID = JojoLibs.StandID.crazyDiamond;
	}

	public EntityCrazyDiamond(World world) {
		super(EntityInit.CRAZY_DIAMOND.get(), world);
		this.spawnSound = SoundInit.SPAWN_CRAZY_DIAMOND.get();
		this.standID = JojoLibs.StandID.crazyDiamond;
	}

	public void putRepairBlock(BlockPos blockPos, BlockState state) {
		repairBlocks.put(blockPos, state);
	}

	@Override
	public void spawnSound() {
		world.playSound(null, new BlockPos(getMaster().getPosX(), getMaster().getPosY(), getMaster().getPosZ()), getSpawnSound(), getSoundCategory(), 2.0f, 1.0f);
	}

	@Override
	public void tick() {
		super.tick();
		this.fallDistance = 0.0f;
		if (getMaster() != null) {
			PlayerEntity player = getMaster();

			Stand.getLazyOptional(player).ifPresent(props -> {
				this.ability = props.getAbility();

				//Crazy Diamond's ability
				if (repair.isPressed() && props.getCooldown() <= 0) {
					if (repairBlocks.size() > 0) {
						world.playSound(null, new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ()), SoundInit.SPAWN_CRAZY_DIAMOND.get(), getSoundCategory(), 1.0f, 1.0f);
						props.setCooldown(100);
					}
					repairBlocks.forEach(world::setBlockState);
					repairBlocks.clear();
				}

				if (props.getCooldown() > 0 && ability)
					props.subtractCooldown(1);
			});

			if (this.standOn) {
				followMaster();
				setRotationYawHead(player.rotationYaw);
				setRotation(player.rotationYaw, player.rotationPitch);

				if (!player.isAlive())
					remove();
				if (player.isSprinting()) {
					if (attackSwing(player))
						this.oratick++;
					if (this.oratick == 1) {
						if (!world.isRemote)
							world.playSound(null, new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ()), SoundInit.DORARUSH.get(), getSoundCategory(), 1.0f, 1.0f);

						if (!this.world.isRemote)
							this.orarush = true;
					}
				} else if (attackSwing(getMaster())) {
					if (!this.world.isRemote) {
						this.oratick++;
						if (this.oratick == 1) {
							this.world.playSound(null, new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ()), SoundInit.PUNCH_MISS.get(), getSoundCategory(), 1.0f, 0.8f / (this.rand.nextFloat() * 0.4f + 1.2f) + 0.5f);
							EntityStandPunch.crazyDiamond crazyDiamond = new EntityStandPunch.crazyDiamond(this.world, this, player);
							crazyDiamond.shoot(player, player.rotationPitch, player.rotationYaw, 2.0f, 0.2f);
							this.world.addEntity(crazyDiamond);
						}
					}
				}
				if (player.swingProgressInt == 0)
					this.oratick = 0;
				if (this.orarush) {
					player.setSprinting(false);
					this.oratickr++;
					if (this.oratickr >= 10)
						if (!this.world.isRemote) {
							player.setSprinting(false);
							EntityStandPunch.crazyDiamond crazyDiamond1 = new EntityStandPunch.crazyDiamond(this.world, this, player);
							crazyDiamond1.setRandomPositions();
							crazyDiamond1.shoot(player, player.rotationPitch, player.rotationYaw, 2.0f, 0.2f);
							this.world.addEntity(crazyDiamond1);
							EntityStandPunch.crazyDiamond crazyDiamond2 = new EntityStandPunch.crazyDiamond(this.world, this, player);
							crazyDiamond2.setRandomPositions();
							crazyDiamond2.shoot(player, player.rotationPitch, player.rotationYaw, 2.0f, 0.2f);
							this.world.addEntity(crazyDiamond2);
						}
					if (this.oratickr >= 100) {
						this.orarush = false;
						this.oratickr = 0;
					}
				}
			}
		}
	}
}
