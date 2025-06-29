package com.hexagram2021.visible_shield_cd.mixin;

import com.hexagram2021.visible_shield_cd.client.VisibleShieldCooldownClient;
import com.hexagram2021.visible_shield_cd.client.config.VisibleShieldCooldownConfig;
import com.hexagram2021.visible_shield_cd.common.ILivingEntityContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import java.util.Objects;

@Mixin(value = BlockEntityWithoutLevelRenderer.class, priority = 3663)
public class BlockEntityWithoutLevelRendererMixin implements ILivingEntityContext {
	@Shadow
	private ShieldModel shieldModel;

	@Unique @Nullable
	private LivingEntity visible_shield_cd$contextLivingEntity;

	@Inject(method = "renderByItem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE), cancellable = true)
	private void visible_shield_cd$renderShieldStatus(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack transform, MultiBufferSource multiBufferSource, int uv2, int y, CallbackInfo ci) {
		if(this.visible_shield_cd$contextLivingEntity instanceof Player player) {
			float cd = player.getCooldowns().getCooldownPercent(Items.SHIELD, 0.0F);
			if(cd > 0) {
				BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
				DyeColor dyeColor2 = itemStack.get(DataComponents.BASE_COLOR);
				boolean hasBlockEntityData = !bannerPatternLayers.layers().isEmpty() || dyeColor2 != null;
				transform.pushPose();
				transform.scale(1.0F, -1.0F, -1.0F);
				Material material = hasBlockEntityData ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
				VertexConsumer vertexConsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(multiBufferSource, this.shieldModel.renderType(material.atlasLocation()), true, itemStack.hasFoil()));

				float remain = 1.0F - cd;
				VisibleShieldCooldownConfig.RenderMode renderMode = VisibleShieldCooldownClient.getConfig().getRenderMode();
				float a = renderMode.a(remain);
				float b = renderMode.b(remain);
				float g = renderMode.g(remain);
				float r = renderMode.r(remain);
				this.shieldModel.handle().render(transform, vertexConsumer, uv2, y, r, g, b, a);
				if (hasBlockEntityData) {
					visible_shield_cd$renderPatterns(transform, multiBufferSource, uv2, y, this.shieldModel.plate(), material, Objects.requireNonNullElse(dyeColor2, DyeColor.WHITE), bannerPatternLayers, itemStack.hasFoil(), r, g, b, a);
				} else {
					this.shieldModel.plate().render(transform, vertexConsumer, uv2, y, r, g, b, a);
				}

				transform.popPose();
				ci.cancel();
			}
		}
	}

	@Override
	public void visible_shield_cd$setLivingEntity(@Nullable LivingEntity living) {
		this.visible_shield_cd$contextLivingEntity = living;
	}

	@Override @Nullable
	public LivingEntity visible_shield_cd$getLivingEntity() {
		return this.visible_shield_cd$contextLivingEntity;
	}

	@Unique
	private static void visible_shield_cd$renderPatterns(PoseStack poseStack, MultiBufferSource multiBufferSource, int uv2, int y, ModelPart modelPart, Material material,
														 DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, boolean hasFoil, float r, float g, float b, float a) {
		modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entitySolid, hasFoil), uv2, y);
		visible_shield_cd$renderPatternLayer(poseStack, multiBufferSource, uv2, y, modelPart, Sheets.SHIELD_BASE, dyeColor, r, g, b, a);

		for(int i = 0; i < 16 && i < bannerPatternLayers.layers().size(); ++i) {
			BannerPatternLayers.Layer layer = bannerPatternLayers.layers().get(i);
			visible_shield_cd$renderPatternLayer(poseStack, multiBufferSource, uv2, y, modelPart, Sheets.getShieldMaterial(layer.pattern()), layer.color(), r, g, b, a);
		}

	}

	@Unique
	private static void visible_shield_cd$renderPatternLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ModelPart modelPart, Material material,
															 DyeColor dyeColor, float r, float g, float b, float a) {
		float[] fs = dyeColor.getTextureDiffuseColors();
		modelPart.render(poseStack, material.buffer(multiBufferSource, RenderType::entityNoOutline), i, j, fs[0] * r, fs[1] * g, fs[2] * b, a);
	}
}
