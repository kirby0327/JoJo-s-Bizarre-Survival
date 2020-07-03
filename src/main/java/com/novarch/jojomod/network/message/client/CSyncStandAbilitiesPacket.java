package com.novarch.jojomod.network.message.client;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.*;
import com.novarch.jojomod.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

import static com.novarch.jojomod.util.Util.StandID.*;

@SuppressWarnings( "ConstantConditions")
public class CSyncStandAbilitiesPacket {
	private int action;

	public CSyncStandAbilitiesPacket(int action) {
		this.action = action;
	}

	public static void encode(CSyncStandAbilitiesPacket msg, PacketBuffer buffer) {
		buffer.writeInt(msg.action);
	}

	public static CSyncStandAbilitiesPacket decode(PacketBuffer buffer) {
		return new CSyncStandAbilitiesPacket(buffer.readInt());
	}

	public static void handle(CSyncStandAbilitiesPacket message, Supplier<Context> ctx) {
		if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				if (player == null) return;
				World world = player.world;
				if (world != null) {
					if (!world.isRemote) {
						Stand.getLazyOptional(player).ifPresent(props -> {
							switch (props.getStandID()) {
								case KILLER_QUEEN: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof KillerQueenEntity)
											.filter(entity -> ((KillerQueenEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> {
												if (message.action == 1)
													((KillerQueenEntity) entity).detonate();
												else
													((KillerQueenEntity) entity).toggleSheerHeartAttack();
											});
									break;
								}
								case GER: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof GoldExperienceRequiemEntity)
											.filter(entity -> ((GoldExperienceRequiemEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> {
												if (message.action == 1)
													((GoldExperienceRequiemEntity) entity).toggleTruth();
												else
													((GoldExperienceRequiemEntity) entity).toggleFlight();
											});
									break;
								}
								case AEROSMITH: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof AerosmithEntity)
											.filter(entity -> ((AerosmithEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> ((AerosmithEntity) entity).shootBomb());
									break;
								}
								case CRAZY_DIAMOND: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof CrazyDiamondEntity)
											.filter(entity -> ((CrazyDiamondEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> ((CrazyDiamondEntity) entity).repair());
									break;
								}
								case WEATHER_REPORT: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof WeatherReportEntity)
											.filter(entity -> ((WeatherReportEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> ((WeatherReportEntity) entity).changeWeather());
									break;
								}
								case MAGICIANS_RED: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof MagiciansRedEntity)
											.filter(entity -> ((MagiciansRedEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> ((MagiciansRedEntity) entity).crossfireHurricane());
								}
								case THE_HAND: {
									world.getServer().getWorld(player.dimension).getEntities()
											.filter(entity -> entity instanceof TheHandEntity)
											.filter(entity -> ((TheHandEntity) entity).getMaster().getEntityId() == player.getEntityId())
											.forEach(entity -> {
												if (message.action == 1) {
													Entity entity1 = Minecraft.getInstance().getRenderViewEntity();
													float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
													if (entity1 != null) {
														if (Minecraft.getInstance().world != null) {
															Minecraft.getInstance().getProfiler().startSection("pick");
															Minecraft.getInstance().pointedEntity = null;
															Minecraft.getInstance().objectMouseOver = entity1.pick(Minecraft.getInstance().playerController.getBlockReachDistance(), partialTicks, false);
															Vec3d vec3d = entity1.getEyePosition(partialTicks);
															boolean flag = false;
															double range = 30.0D;
															Vec3d vec3d1 = entity1.getLook(1.0f);
															Vec3d vec3d2 = vec3d.add(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range);
															AxisAlignedBB axisalignedbb = entity1.getBoundingBox().expand(vec3d1.scale(range)).grow(1.0D, 1.0D, 1.0D);
															EntityRayTraceResult entityRayTraceResult =
																	ProjectileHelper.rayTraceEntities(
																			entity1,
																			vec3d,
																			vec3d2,
																			axisalignedbb,
																			Util.Predicates.STAND_PUNCH_TARGET.and((predicateEntity) -> predicateEntity != entity && !(predicateEntity instanceof AbstractStandPunchEntity)),
																			3000);
															if (entityRayTraceResult != null) {
																Entity entity11 = entityRayTraceResult.getEntity();
																Vec3d vec3d3 = entityRayTraceResult.getHitVec();
																if (entity11 != null)
																	((TheHandEntity) entity).teleportEntity(entity11.getEntityId());
																double d2 = vec3d.squareDistanceTo(vec3d3);
																if (flag && d2 > 9.0D) {
																	Minecraft.getInstance().objectMouseOver = BlockRayTraceResult.createMiss(vec3d3, Direction.getFacingFromVector(vec3d1.x, vec3d1.y, vec3d1.z), new BlockPos(vec3d3));
																} else if (d2 < 30 || Minecraft.getInstance().objectMouseOver == null) {
																	Minecraft.getInstance().objectMouseOver = entityRayTraceResult;
																	if (entity11 instanceof LivingEntity || entity11 instanceof ItemFrameEntity) {
																		Minecraft.getInstance().pointedEntity = entity11;
																	}
																}
															}
															Minecraft.getInstance().getProfiler().endSection();
														}
													}
												} else
													((TheHandEntity) entity).teleportMaster();
											});
									break;
								}
								default:
									break;
							}
						});
					}
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}
}