package com.novarch.jojomod.client.entity.render;

import com.novarch.jojomod.JojoBizarreSurvival;
import com.novarch.jojomod.client.entity.model.TheHandModel;
import com.novarch.jojomod.entities.stands.TheHandEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TheHandRenderer extends MobRenderer<TheHandEntity, TheHandModel<TheHandEntity>> {
	protected static final ResourceLocation TEXTURE = new ResourceLocation(JojoBizarreSurvival.MOD_ID, "textures/stands/whitesnake.png");

	public TheHandRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new TheHandModel<>(), 0.5f);
	}

	public ResourceLocation getEntityTexture(final TheHandEntity entity) {
		return TEXTURE;
	}
}
