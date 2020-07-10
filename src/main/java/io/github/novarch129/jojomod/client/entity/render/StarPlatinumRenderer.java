package io.github.novarch129.jojomod.client.entity.render;

import io.github.novarch129.jojomod.entity.stands.StarPlatinumEntity;
import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.client.entity.model.StarPlatinumModel;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarPlatinumRenderer extends MobRenderer<StarPlatinumEntity, StarPlatinumModel<StarPlatinumEntity>> {
	protected static final ResourceLocation TEXTURE = new ResourceLocation(JojoBizarreSurvival.MOD_ID, "textures/stands/star_platinum.png");

	public StarPlatinumRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new StarPlatinumModel<>(), 0.5f);
	}

	public ResourceLocation getEntityTexture(final StarPlatinumEntity entity) {
		return TEXTURE;
	}
}
