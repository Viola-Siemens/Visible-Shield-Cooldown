package com.hexagram2021.visible_shield_cd.mixin;

import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin {
	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void visible_shield_cd$clearCD(CallbackInfo ci) {
		RemotePlayer current = (RemotePlayer)(Object)this;
		ItemStack itemStack = current.getUseItem();
		if(itemStack.isEmpty()) {
			return;
		}
		Item useItem = itemStack.getItem();
		if (current.getCooldowns().isOnCooldown(useItem)) {
			current.getCooldowns().removeCooldown(useItem);
		}
	}
}
