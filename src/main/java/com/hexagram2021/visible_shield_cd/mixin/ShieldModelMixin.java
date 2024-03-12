package com.hexagram2021.visible_shield_cd.mixin;

import com.hexagram2021.visible_shield_cd.common.IShieldModelWithCooldown;
import net.minecraft.client.model.ShieldModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShieldModel.class)
public class ShieldModelMixin implements IShieldModelWithCooldown {
	@Unique
	private float visible_shield_cooldown$cooldown = 0;

	@Override
	public void visible_shield_cooldown$setCooldown(float cd) {
		this.visible_shield_cooldown$cooldown = cd;
	}

	@Override
	public float visible_shield_cooldown$getCooldown() {
		return this.visible_shield_cooldown$cooldown;
	}
}
