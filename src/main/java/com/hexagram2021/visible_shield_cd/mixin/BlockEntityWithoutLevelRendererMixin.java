package com.hexagram2021.visible_shield_cd.mixin;

import com.hexagram2021.visible_shield_cd.common.ILivingEntityContext;
import com.hexagram2021.visible_shield_cd.common.IShieldModelWithCooldown;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = BlockEntityWithoutLevelRenderer.class, priority = 43)
public class BlockEntityWithoutLevelRendererMixin implements ILivingEntityContext {
	@Shadow
	private ShieldModel shieldModel;

	@Unique @Nullable
	private LivingEntity visible_shield_cooldown$contextLivingEntity;

	@Inject(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ShieldModel;handle()Lnet/minecraft/client/model/geom/ModelPart;", shift = At.Shift.BEFORE))
	private void visible_shield_cooldown$renderShieldStatus(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack transform, MultiBufferSource multiBufferSource, int uv2, int y, CallbackInfo ci) {
		if(this.shieldModel instanceof IShieldModelWithCooldown shieldModelWithCooldown && this.visible_shield_cooldown$contextLivingEntity instanceof Player player) {
			shieldModelWithCooldown.visible_shield_cooldown$setCooldown(player.getCooldowns().getCooldownPercent(itemStack.getItem(), 0.0F));
		}
	}

	@Redirect(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
	private void visible_shield_cooldown$modifyRenderColor(ModelPart instance, PoseStack poseStack, VertexConsumer vertexConsumer, int uv2, int y, float r, float g, float b, float a) {
		if(this.shieldModel instanceof IShieldModelWithCooldown shieldModelWithCooldown) {
			float remain = 1.0F - shieldModelWithCooldown.visible_shield_cooldown$getCooldown();
			b = remain * remain;
			g = remain * (1.0F + remain * (1.0F - remain));
			r = 1.0F - remain * (1.0F - remain * remain);
		}
		instance.render(poseStack, vertexConsumer, uv2, y, r, g, b, a);
	}

	@Override
	public void visible_shield_cooldown$setLivingEntity(@Nullable LivingEntity living) {
		this.visible_shield_cooldown$contextLivingEntity = living;
	}

	@Override @Nullable
	public LivingEntity visible_shield_cooldown$getLivingEntity() {
		return this.visible_shield_cooldown$contextLivingEntity;
	}
}
